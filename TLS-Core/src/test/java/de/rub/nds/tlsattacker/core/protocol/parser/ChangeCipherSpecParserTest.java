/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.parser;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.ChangeCipherSpecMessage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ChangeCipherSpecParserTest {

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() {
        return Arrays.asList(new Object[][] { { new byte[] { 0x01 }, (byte) 1, ProtocolVersion.TLS12 },
            { new byte[] { 0x05 }, (byte) 5, ProtocolVersion.TLS12 },
            { new byte[] { 0x01 }, (byte) 1, ProtocolVersion.TLS10 },
            { new byte[] { 0x01 }, (byte) 1, ProtocolVersion.TLS11 } });
    }

    private final byte[] message;
    private final ProtocolVersion version;
    private final byte ccsType;
    private final Config config = Config.createConfig();

    public ChangeCipherSpecParserTest(byte[] message, byte ccsType, ProtocolVersion version) {
        this.message = message;
        this.ccsType = ccsType;
        this.version = version;
    }

    /**
     * Test of parse method, of class ChangeCipherSpecParser.
     */
    @Test
    public void testParse() {
        ChangeCipherSpecParser parser = new ChangeCipherSpecParser(new ByteArrayInputStream(message), version, config);
        ChangeCipherSpecMessage ccsMessage = new ChangeCipherSpecMessage();
        parser.parse(ccsMessage);
        assertArrayEquals(message, ccsMessage.getCompleteResultingMessage().getValue());
        assertTrue(ccsType == ccsMessage.getCcsProtocolType().getValue()[0]);
    }

}
