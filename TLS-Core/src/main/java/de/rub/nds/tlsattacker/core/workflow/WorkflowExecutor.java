/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.workflow;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.exceptions.BouncyCastleNotLoadedException;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import de.rub.nds.tlsattacker.core.exceptions.WorkflowExecutionException;
import de.rub.nds.tlsattacker.core.record.layer.RecordLayerFactory;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.executor.WorkflowExecutorType;
import de.rub.nds.tlsattacker.transport.TransportHandlerFactory;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import java.io.IOException;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WorkflowExecutor {

    private static final Logger LOGGER = LogManager.getLogger();

    private Function<State, Integer> beforeTransportPreInitCallback = (State state) -> {
        LOGGER.trace("BeforePreInitCallback");
        return 0;
    };

    private Function<State, Integer> beforeTransportInitCallback = (State state) -> {
        LOGGER.trace("BeforeInitCallback");
        return 0;
    };

    private Function<State, Integer> afterTransportInitCallback = (State state) -> {
        LOGGER.trace("AfterTransportInitCallback");
        return 0;
    };

    private Function<State, Integer> afterExecutionCallback = (State state) -> {
        LOGGER.trace("AfterExecutionCallback");
        return 0;
    };

    static {
        if (!BouncyCastleProviderChecker.isLoaded()) {
            throw new BouncyCastleNotLoadedException("BouncyCastleProvider not loaded");
        }
    }

    protected final WorkflowExecutorType type;

    protected final State state;
    protected final Config config;

    /**
     * Prepare a workflow trace for execution according to the given state and executor type. Try various ways to
     * initialize a workflow trace and add it to the state. For workflow creation, use the first method which does not
     * return null, in the following order: state.getWorkflowTrace(), state.config.getWorkflowInput(),
     * config.getWorkflowTraceType().
     *
     * @param type
     *              of the workflow executor (currently only DEFAULT)
     * @param state
     *              to work on
     */
    public WorkflowExecutor(WorkflowExecutorType type, State state) {
        this.type = type;
        this.state = state;
        this.config = state.getConfig();
    }

    public abstract void executeWorkflow() throws WorkflowExecutionException;

    /**
     * Initialize the context's transport handler.Start listening or connect to a server, depending on our connection
     * end type.
     *
     * @param context
     */
    public void initTransportHandler(TlsContext context) {

        if (context.getTransportHandler() == null) {
            if (context.getConnection() == null) {
                throw new ConfigurationException("Connection end not set");
            }
            context.setTransportHandler(TransportHandlerFactory.createTransportHandler(context.getConnection()));
            if (context.getTransportHandler() instanceof ClientTcpTransportHandler) {
                ((ClientTcpTransportHandler) context.getTransportHandler())
                    .setRetryFailedSocketInitialization(config.isRetryFailedClientTcpSocketInitialization());
            }
        }

        try {
            getBeforeTransportPreInitCallback().apply(state);
            context.getTransportHandler().preInitialize();
            getBeforeTransportInitCallback().apply(state);
            context.getTransportHandler().initialize();
            getAfterTransportInitCallback().apply(state);
        } catch (NullPointerException | NumberFormatException ex) {
            throw new ConfigurationException("Invalid values in " + context.getConnection().toString(), ex);
        } catch (IOException ex) {
            throw new TransportHandlerConnectException(
                "Unable to initialize the transport handler with: " + context.getConnection().toString(), ex);
        } catch (Exception ex) {
            throw new TransportHandlerConnectException(
                "Unable to initialize the transport handler with: " + context.getConnection().toString(), ex);
        }
    }

    /**
     * Initialize the context's record layer.
     *
     * @param context
     */
    public void initRecordLayer(TlsContext context) {
        if (context.getRecordLayerType() == null) {
            throw new ConfigurationException("No record layer type defined");
        }
        context.setRecordLayer(RecordLayerFactory.getRecordLayer(context.getRecordLayerType(), context));
    }

    public Function<State, Integer> getBeforeTransportPreInitCallback() {
        return beforeTransportPreInitCallback;
    }

    public void setBeforeTransportPreInitCallback(Function<State, Integer> beforeTransportPreInitCallback) {
        this.beforeTransportPreInitCallback = beforeTransportPreInitCallback;
    }

    public Function<State, Integer> getBeforeTransportInitCallback() {
        return beforeTransportInitCallback;
    }

    public void setBeforeTransportInitCallback(Function<State, Integer> beforeTransportInitCallback) {
        this.beforeTransportInitCallback = beforeTransportInitCallback;
    }

    public Function<State, Integer> getAfterTransportInitCallback() {
        return afterTransportInitCallback;
    }

    public void setAfterTransportInitCallback(Function<State, Integer> afterTransportInitCallback) {
        this.afterTransportInitCallback = afterTransportInitCallback;
    }

    public Function<State, Integer> getAfterExecutionCallback() {
        return afterExecutionCallback;
    }

    public void setAfterExecutionCallback(Function<State, Integer> afterExecutionCallback) {
        this.afterExecutionCallback = afterExecutionCallback;
    }
}
