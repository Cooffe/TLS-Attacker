/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2020 Ruhr University Bochum, Paderborn University,
 * and Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.transport;

import de.rub.nds.tlsattacker.transport.nonblocking.ServerTCPNonBlockingTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpNoDelayTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.ServerTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.fragmentation.ClientTcpFragmentationTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.fragmentation.ServerTcpFragmentationTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.proxy.TimingProxyClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.timing.TimingClientTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.tcp.timing.TimingServerTcpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ClientUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.ServerUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.timing.TimingClientUdpTransportHandler;
import de.rub.nds.tlsattacker.transport.udp.timing.TimingServerUdpTransportHandler;

public class TransportHandlerFactory {

    public static TransportHandler createTransportHandler(Connection con) {
        ConnectionEndType localConEndType = con.getLocalConnectionEndType();
        Long timeout = new Long(con.getTimeout());
        if (con.getFirstTimeout() == null) {
            con.setFirstTimeout(con.getTimeout());
        }
        Long firstTimeout = new Long(con.getFirstTimeout());
        switch (con.getTransportHandlerType()) {
            case TCP:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new ClientTcpTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    return new ServerTcpTransportHandler(firstTimeout, timeout, con.getPort());
                }
            case EAP_TLS:
                throw new UnsupportedOperationException("EAP_TLS is currently not supported");
            case UDP:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new ClientUdpTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    return new ServerUdpTransportHandler(firstTimeout, timeout, con.getPort());
                }
            case NON_BLOCKING_TCP:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    throw new UnsupportedOperationException("NON_BLOCKING_TCP-Transporthandler is not supported");
                } else {
                    return new ServerTCPNonBlockingTransportHandler(firstTimeout, timeout, con.getPort());
                }
            case STREAM:
                throw new UnsupportedOperationException("STREAM TransportHandler can only be created manually");
            case TCP_TIMING:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new TimingClientTcpTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    return new TimingServerTcpTransportHandler(firstTimeout, timeout, con.getPort());
                }
            case UDP_TIMING:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new TimingClientUdpTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    return new TimingServerUdpTransportHandler(firstTimeout, timeout, con.getPort());
                }
            case TCP_PROXY_TIMING:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new TimingProxyClientTcpTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    throw new UnsupportedOperationException(
                            "TCP_PROXY_TIMING for server sockets is currently not supported");
                }
            case TCP_NO_DELAY:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new ClientTcpNoDelayTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    throw new UnsupportedOperationException(
                            "This transport handler type is only supported in client mode");
                }
            case TCP_FRAGMENTATION:
                if (localConEndType == ConnectionEndType.CLIENT) {
                    return new ClientTcpFragmentationTransportHandler(firstTimeout, timeout, con.getIp(), con.getPort());
                } else {
                    return new ServerTcpFragmentationTransportHandler(firstTimeout, timeout, con.getPort());
                }
            default:
                throw new UnsupportedOperationException("This transport handler " + "type is not supported");
        }
    }

    private TransportHandlerFactory() {

    }
}
