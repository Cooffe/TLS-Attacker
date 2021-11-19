/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.tlsattacker.core.protocol.message.CertificateStatusMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import org.junit.Before;
import org.junit.Test;

public class CertificateStatusHandlerTest {

    private CertificateStatusHandler handler;
    private TlsContext context;

    @Before
    public void setUp() {
        context = new TlsContext();
        handler = new CertificateStatusHandler(context);
    }

    @Test
    public void testAdjustTLSContext() {
        CertificateStatusMessage message = new CertificateStatusMessage();
        handler.adjustTLSContext(message);
        // TODO: make sure that nothing changed
    }
}
