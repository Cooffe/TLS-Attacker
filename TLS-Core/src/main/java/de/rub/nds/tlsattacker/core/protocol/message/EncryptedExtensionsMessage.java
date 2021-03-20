/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.protocol.handler.EncryptedExtensionsHandler;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EncryptedExtensionsMessage extends HandshakeMessage {

    public EncryptedExtensionsMessage() {
        super(HandshakeMessageType.ENCRYPTED_EXTENSIONS);
    }

    public EncryptedExtensionsMessage(Config config) {
        super(config, HandshakeMessageType.ENCRYPTED_EXTENSIONS);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EncryptedExtensionMessage:");
        sb.append("\n  Extensions: ");
        if (getExtensions() == null) {
            sb.append("null");
        } else {
            for (ExtensionMessage e : getExtensions()) {
                sb.append(e.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public EncryptedExtensionsHandler getHandler(TlsContext context) {
        return new EncryptedExtensionsHandler(context);
    }

}
