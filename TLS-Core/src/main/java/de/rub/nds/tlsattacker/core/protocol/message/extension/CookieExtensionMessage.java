/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2021 Ruhr University Bochum, Paderborn University, Hackmanit GmbH
 *
 * Licensed under Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package de.rub.nds.tlsattacker.core.protocol.message.extension;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.ModifiableVariableProperty;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.modifiablevariable.integer.ModifiableInteger;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ExtensionType;
import de.rub.nds.tlsattacker.core.protocol.handler.extension.CookieExtensionHandler;
import de.rub.nds.tlsattacker.core.protocol.parser.extension.CookieExtensionParser;
import de.rub.nds.tlsattacker.core.protocol.preparator.extension.CookieExtensionPreparator;
import de.rub.nds.tlsattacker.core.protocol.serializer.extension.CookieExtensionSerializer;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.io.InputStream;

/**
 * The cookie extension used in TLS 1.3
 */
public class CookieExtensionMessage extends ExtensionMessage<CookieExtensionMessage> {

    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.LENGTH)
    private ModifiableInteger cookieLength;

    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.COOKIE)
    private ModifiableByteArray cookie;

    public CookieExtensionMessage() {
        super(ExtensionType.COOKIE);
    }

    public CookieExtensionMessage(Config config) {
        super(ExtensionType.COOKIE);
    }

    public ModifiableInteger getCookieLength() {
        return cookieLength;
    }

    public void setCookieLength(ModifiableInteger cookieLength) {
        this.cookieLength = cookieLength;
    }

    public void setCookieLength(int length) {
        this.cookieLength = ModifiableVariableFactory.safelySetValue(cookieLength, length);
    }

    public ModifiableByteArray getCookie() {
        return cookie;
    }

    public void setCookie(ModifiableByteArray cookie) {
        this.cookie = cookie;
    }

    public void setCookie(byte[] cookieBytes) {
        this.cookie = ModifiableVariableFactory.safelySetValue(cookie, cookieBytes);
    }

    @Override
    public CookieExtensionParser getParser(TlsContext tlsContext, InputStream stream) {
        return new CookieExtensionParser(stream, tlsContext.getConfig());
    }

    @Override
    public CookieExtensionPreparator getPreparator(TlsContext tlsContext) {
        return new CookieExtensionPreparator(tlsContext.getChooser(), this, getSerializer(tlsContext));
    }

    @Override
    public CookieExtensionSerializer getSerializer(TlsContext tlsContext) {
        return new CookieExtensionSerializer(this);
    }

    @Override
    public CookieExtensionHandler getHandler(TlsContext tlsContext) {
        return new CookieExtensionHandler(tlsContext);
    }

}
