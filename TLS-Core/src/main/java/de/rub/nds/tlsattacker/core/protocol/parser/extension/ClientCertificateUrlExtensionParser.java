/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.parser.extension;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ClientCertificateUrlExtensionMessage;

public class ClientCertificateUrlExtensionParser extends ExtensionParser<ClientCertificateUrlExtensionMessage> {

    public ClientCertificateUrlExtensionParser(int startposition, byte[] array, Config config) {
        super(startposition, array, config);
    }

    @Override
    public void parseExtensionMessageContent(ClientCertificateUrlExtensionMessage msg) {
        // nothing to parse here, it's an opt-in extension.
    }

    @Override
    protected ClientCertificateUrlExtensionMessage createExtensionMessage() {
        return new ClientCertificateUrlExtensionMessage();
    }

}
