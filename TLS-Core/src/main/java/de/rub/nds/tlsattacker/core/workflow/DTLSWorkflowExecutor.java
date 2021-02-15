/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.workflow;

import de.rub.nds.tlsattacker.core.config.ConfigIO;
import de.rub.nds.tlsattacker.core.connection.AliasedConnection;
import de.rub.nds.tlsattacker.core.exceptions.PreparationException;
import de.rub.nds.tlsattacker.core.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.ReceivingAction;
import de.rub.nds.tlsattacker.core.workflow.action.SendingAction;
import de.rub.nds.tlsattacker.core.workflow.action.TlsAction;
import de.rub.nds.tlsattacker.core.workflow.action.executor.WorkflowExecutorType;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Nurullah Erinola - nurullah.erinola@rub.de
 */
public class DTLSWorkflowExecutor extends WorkflowExecutor {

    private static final Logger LOGGER = LogManager.getLogger();

    public DTLSWorkflowExecutor(State state) {
        super(WorkflowExecutorType.DTLS, state);
    }

    @Override
    public void executeWorkflow() throws WorkflowExecutionException {
        List<TlsContext> allTlsContexts = state.getAllTlsContexts();

        if (config.isWorkflowExecutorShouldOpen()) {
            for (TlsContext ctx : allTlsContexts) {
                AliasedConnection con = ctx.getConnection();
                if (con.getLocalConnectionEndType() == ConnectionEndType.SERVER) {
                    LOGGER.info("Waiting for incoming connection on " + con.getHostname() + ":" + con.getPort());
                } else {
                    LOGGER.info("Connecting to " + con.getHostname() + ":" + con.getPort());
                }
                ctx.initTransportHandler();
                LOGGER.debug("Connection for " + ctx + " initiliazed");
            }
        }

        for (TlsContext ctx : state.getAllTlsContexts()) {
            ctx.initRecordLayer();
        }

        // Warum reset, wenn nichts bisher ausgeführt?
        state.getWorkflowTrace().reset();
        int numTlsContexts = allTlsContexts.size();
        List<TlsAction> tlsActions = state.getWorkflowTrace().getTlsActions();

        // ------------------------------------------

        int retransmissions = 0;
        boolean exec_err = false;
        int errorAction = -1;
        for (int i = 0; i < tlsActions.size(); i++) {

            // TODO: in multi ctx scenarios, how to handle earlyCleanShutdown ?
            if (numTlsContexts == 1 && state.getTlsContext().isEarlyCleanShutdown()) {
                LOGGER.debug("Clean shutdown of execution flow");
                break;
            }

            // Führe Action aus
            TlsAction action = tlsActions.get(i);
            try {
                action.execute(state);
            } catch (PreparationException | WorkflowExecutionException ex) {
                throw new WorkflowExecutionException("Problem while executing Action:" + action.toString(), ex);
            }

            if ((state.getConfig().isStopActionsAfterFatal() && isReceivedFatalAlert())) {
                LOGGER.debug("Skipping all Actions, received FatalAlert, StopActionsAfterFatal active");
                break;
            }
            if ((state.getConfig().getStopActionsAfterIOException() && isIoException())) {
                LOGGER.debug("Skipping all Actions, received IO Exception, StopActionsAfterIOException active");
                break;
            }
            if (!action.executedAsPlanned()) {
                // Nutze diesen Flag für Retransmissions an/aus
                if (config.isStopTraceAfterUnexpected()) {
                    LOGGER.debug("Skipping all Actions, action did not execute as planned.");
                    break;
                } else if (retransmissions == config.getMaxRetransmissions()) {
                    break;
                } else {
                    // Aktuelle Action
                    action.reset();
                    // Davorherige Action
                    int j = i - 1;
                    for (; j >= 0; j--) {
                        if (!(tlsActions.get(j) instanceof SendingAction)) {
                            tlsActions.get(j).reset();
                        } else {
                            break;
                        }
                    }
                    for (; j >= 0; j--) {
                        if (!(tlsActions.get(j) instanceof ReceivingAction)) {
                            tlsActions.get(j).reset();
                        } else {
                            i = j;
                            break;
                        }
                    }
                    retransmissions++;
                    // Merke Action mit Fehler
                    errorAction = i;
                    exec_err = true;
                }
            } else if (errorAction == i && exec_err) {
                retransmissions = 0;
                errorAction = -1;
                exec_err = false;
            }
        }

        // Close with Notify, if execution error
        if (exec_err && config.isFinishWithCloseNotify()) {
            TlsAction action = tlsActions.get(tlsActions.size() - 1);
            try {
                action.execute(state);
            } catch (PreparationException | WorkflowExecutionException ex) {
                throw new WorkflowExecutionException("Problem while executing Action:" + action.toString(), ex);
            }
        }

        // ------------------------------------------

        if (state.getConfig().isWorkflowExecutorShouldClose()) {
            for (TlsContext ctx : state.getAllTlsContexts()) {
                try {
                    ctx.getTransportHandler().closeConnection();
                } catch (IOException ex) {
                    LOGGER.warn("Could not close connection for context " + ctx);
                    LOGGER.debug(ex);
                }
            }
        }

        if (state.getConfig().isResetWorkflowtracesBeforeSaving()) {
            state.getWorkflowTrace().reset();
        }

        state.storeTrace();

        if (config.getConfigOutput() != null) {
            ConfigIO.write(config, new File(config.getConfigOutput()));
        }
    }

    /**
     * Check if a at least one TLS context received a fatal alert.
     */
    private boolean isReceivedFatalAlert() {
        for (TlsContext ctx : state.getAllTlsContexts()) {
            if (ctx.isReceivedFatalAlert()) {
                return true;
            }
        }
        return false;
    }

    private boolean isIoException() {
        for (TlsContext ctx : state.getAllTlsContexts()) {
            if (ctx.isReceivedTransportHandlerException()) {
                return true;
            }
        }
        return false;
    }
}
