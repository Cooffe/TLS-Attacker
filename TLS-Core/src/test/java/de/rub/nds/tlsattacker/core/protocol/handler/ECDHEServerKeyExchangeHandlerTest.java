/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.handler;

import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.constants.EllipticCurveType;
import de.rub.nds.tlsattacker.core.constants.NamedGroup;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.math.BigInteger;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ECDHEServerKeyExchangeHandlerTest {

    private ECDHEServerKeyExchangeHandler handler;
    private TlsContext context;

    @Before
    public void setUp() {
        context = new TlsContext();
        handler = new ECDHEServerKeyExchangeHandler(context);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of adjustTLSContext method, of class ECDHEServerKeyExchangeHandler.
     */
    @Test
    public void testAdjustTLSContext() {
        ECDHEServerKeyExchangeMessage message = new ECDHEServerKeyExchangeMessage();
        message.setCurveType(EllipticCurveType.NAMED_CURVE.getValue());
        message.setNamedGroup(NamedGroup.SECP256R1.getValue());
        message.setPublicKey(ArrayConverter.hexStringToByteArray(
            "04f660a88e9dae015684be56c25610f9c62cf120cb075eea60c560e5e6dd5d10ef6e391d7213a298985470dc2268949317ce24940d474a0c8386ab13b312ffc104"));
        message.setPublicKeyLength(65);
        message.prepareComputations();
        message.getComputations().setPremasterSecret(new byte[] { 0, 1, 2, 3 });
        message.getComputations().setPrivateKey(new BigInteger("12345"));
        handler.adjustTLSContext(message);
        assertNull(context.getPreMasterSecret());
        // assertNull(context.getMasterSecret());//TODO assert master secret was
        // computed correctly
    }

    @Test
    public void testAdjustTLSContextWithoutComputations() {
        ECDHEServerKeyExchangeMessage message = new ECDHEServerKeyExchangeMessage();
        message.setCurveType(EllipticCurveType.NAMED_CURVE.getValue());
        message.setNamedGroup(NamedGroup.SECP256R1.getValue());
        message.setPublicKey(ArrayConverter.hexStringToByteArray(
            "04f660a88e9dae015684be56c25610f9c62cf120cb075eea60c560e5e6dd5d10ef6e391d7213a298985470dc2268949317ce24940d474a0c8386ab13b312ffc104"));
        message.setPublicKeyLength(65);
        handler.adjustTLSContext(message);
        assertNull(context.getPreMasterSecret());
        assertNull(context.getMasterSecret());
    }

}
