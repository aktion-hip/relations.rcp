/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2026, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.peer.libp2p;

import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.libp2p.core.Host;
import io.libp2p.core.crypto.PrivKey;
import io.libp2p.core.dsl.HostBuilder;
import io.libp2p.core.multistream.ProtocolBinding;
import io.libp2p.core.mux.StreamMuxerProtocol;
import io.libp2p.discovery.MDnsDiscovery;
import io.libp2p.security.noise.NoiseXXSecureChannel;
import io.libp2p.transport.tcp.TcpTransport;

/** The libp2p host backing a peer transfer session.
 *
 * <p>
 * Transport is TCP with the Noise security handshake and the mplex multiplexer; QUIC is
 * deliberately not used, so that the receiving application is free to use any libp2p
 * binding. The host listens on all interfaces so devices on the local network can reach it.
 * </p>
 *
 * @author lbenno */
public class Libp2pTransferHost {

    private static final int OPERATION_TIMEOUT_SECONDS = 30;

    /** The mDNS service tag an open session is advertised under. It is deliberately not
     * libp2p's <code>_ipfs-discovery._udp</code> default, so only Relations sessions are
     * discovered and Relations does not answer unrelated libp2p browsers. The receiving
     * application must browse for this exact tag; it is part of the contract in PROTOCOL.md.
     *
     * <p>
     * The trailing <code>.local.</code> is required. jvm-libp2p's name parser throws
     * <code>StringIndexOutOfBoundsException</code> for a tag without it — including for the
     * library's own <code>MDnsDiscovery.ServiceTag</code> constant, which is unusable with
     * its own constructor. Only the <code>ServiceTagLocal</code> form works.
     * </p> */
    public static final String SERVICE_TAG = "_relations-sync._udp.local."; //$NON-NLS-1$

    private static final int QUERY_INTERVAL_SECONDS = 120;

    /** Thrown when the configured port cannot be bound, so the caller can tell the user
     * instead of surfacing a networking stack trace. */
    public static class PortUnavailableException extends Exception {
        private static final long serialVersionUID = 1L;
        private final int port;

        public PortUnavailableException(final int port, final Throwable cause) {
            super("Port " + port + " is not available", cause); // $NON-NLS-1$
            this.port = port;
        }

        public int getPort() {
            return this.port;
        }
    }

    private Host host;
    private int listeningPort;
    private MDnsDiscovery discovery;
    private boolean discoveryActive;

    /** Starts the host on the passed port, using this installation's persistent identity.
     *
     * @param port int the TCP port to listen on
     * @param identity {@link PrivKey} the installation's private key, so that the peer
     *            identity is stable across restarts
     * @param protocols {@link ProtocolBinding} the protocols this host answers, e.g. the
     *            {@link ExportSender}
     * @throws PortUnavailableException if the port is already in use
     * @throws IllegalStateException if this host is already running */
    public synchronized void start(final int port, final PrivKey identity, final ProtocolBinding<?>... protocols)
            throws PortUnavailableException {
        if (this.host != null) {
            throw new IllegalStateException("Host is already running"); //$NON-NLS-1$
        }
        final Host started = new HostBuilder()
                .transport(TcpTransport::new)
                .secureChannel(NoiseXXSecureChannel::new)
                .muxer(StreamMuxerProtocol::getMplex)
                .protocol(protocols)
                .builderModifier(builder -> builder.getIdentity().setFactory(() -> identity))
                .listen(String.format("/ip4/0.0.0.0/tcp/%d", port)) //$NON-NLS-1$
                .build();
        try {
            started.start().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            this.host = started;
            this.listeningPort = port;
            this.discovery = new MDnsDiscovery(started, SERVICE_TAG, QUERY_INTERVAL_SECONDS, null);
            startDiscovery();
        }
        catch (final ExecutionException exc) {
            quietlyStop(started);
            if (isPortInUse(exc)) {
                throw new PortUnavailableException(port, exc.getCause());
            }
            throw new IllegalStateException("Unable to start the peer host", exc.getCause()); //$NON-NLS-1$
        }
        catch (final TimeoutException exc) {
            quietlyStop(started);
            throw new IllegalStateException("Timed out starting the peer host", exc); //$NON-NLS-1$
        }
        catch (final InterruptedException exc) {
            quietlyStop(started);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted starting the peer host", exc); //$NON-NLS-1$
        }
    }

    /** @return boolean <code>true</code> while this host is listening */
    public synchronized boolean isRunning() {
        return this.host != null;
    }

    /** Discovery is a convenience. When it fails the session is still usable through the
     * addresses from {@link #getReachableAddresses()}, so a failure here never prevents a
     * transfer.
     *
     * @return boolean <code>true</code> if the session is being advertised on the local
     *         network */
    public synchronized boolean isDiscoveryActive() {
        return this.discoveryActive;
    }

    /** Returns the addresses a device on the local network can actually dial.
     *
     * <p>
     * The host binds a wildcard address, which libp2p reports verbatim as something like
     * <code>/ip4/0.0.0.0/tcp/9042</code>. That is useless to a user, so the wildcard is
     * expanded to this machine's real interface addresses here. Each returned address
     * carries the peer identity.
     * </p>
     *
     * @return List&lt;String> concrete, dialable addresses */
    public synchronized List<String> getReachableAddresses() {
        final Host running = requireRunning();
        final String peerId = running.getPeerId().toBase58();
        final Set<String> addresses = new LinkedHashSet<>();
        for (final InetAddress local : localAddresses()) {
            final String protocol = local instanceof Inet4Address ? "ip4" : "ip6"; //$NON-NLS-1$ //$NON-NLS-2$
            // getHostAddress() appends a zone id such as "%eth0" for IPv6, which is not
            // valid in a multiaddr.
            final String host = local.getHostAddress().split("%")[0]; //$NON-NLS-1$
            addresses.add(String.format("/%s/%s/tcp/%d/p2p/%s", //$NON-NLS-1$
                    protocol, host, this.listeningPort, peerId));
        }
        return new ArrayList<>(addresses);
    }

    /** Enumerates this machine's own addresses.
     *
     * <p>
     * libp2p's own wildcard expansion cannot be used: it expands the IPv6 wildcard but
     * returns an IPv4 wildcard unchanged, so which of the two works depends on the JVM's
     * IP stack. Enumerating here is deterministic under either.
     * </p> */
    private static List<InetAddress> localAddresses() {
        final List<InetAddress> found = new ArrayList<>();
        try {
            for (final NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                for (final InetAddress address : Collections.list(nic.getInetAddresses())) {
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                        found.add(address);
                    }
                }
            }
        }
        catch (final SocketException exc) {
            // No interfaces could be enumerated; the caller still has the listen addresses.
        }
        return found;
    }

    private void startDiscovery() {
        try {
            this.discovery.start().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            this.discoveryActive = true;
        }
        catch (final InterruptedException exc) {
            Thread.currentThread().interrupt();
        }
        catch (final ExecutionException | TimeoutException | RuntimeException exc) {
            // Multicast is unavailable on some networks. The session stays usable through
            // the address the user can copy, so this is not a failure of the transfer.
            this.discoveryActive = false;
        }
    }

    /** @return String the peer identity of this host
     * @throws IllegalStateException if the host is not running */
    public synchronized String getPeerId() {
        return requireRunning().getPeerId().toBase58();
    }

    /** Stops the host and releases the port. Calling this on a host that is not running has
     * no effect. */
    public synchronized void stop() {
        if (this.host == null) {
            return;
        }
        stopDiscovery();
        quietlyStop(this.host);
        this.host = null;
    }

    private void stopDiscovery() {
        if (this.discovery == null) {
            return;
        }
        try {
            this.discovery.stop().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (final InterruptedException exc) {
            Thread.currentThread().interrupt();
        }
        catch (final ExecutionException | TimeoutException | RuntimeException exc) {
            // stopping is best effort
        }
        finally {
            this.discovery = null;
            this.discoveryActive = false;
        }
    }

    private Host requireRunning() {
        if (this.host == null) {
            throw new IllegalStateException("Host is not running"); //$NON-NLS-1$
        }
        return this.host;
    }

    private static void quietlyStop(final Host toStop) {
        try {
            toStop.stop().get(OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (final InterruptedException exc) {
            Thread.currentThread().interrupt();
        }
        catch (final ExecutionException | TimeoutException exc) {
            // stopping is best effort; nothing useful remains to be done here
        }
    }

    private static boolean isPortInUse(final Throwable exc) {
        for (Throwable cause = exc; cause != null; cause = cause.getCause()) {
            if (cause instanceof BindException) {
                return true;
            }
        }
        return false;
    }
}
