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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.elbe.relations.services.IPeerConnectionApproval;
import org.elbe.relations.services.IPeerSession;
import org.elbe.relations.services.IPeerTransferListener;
import org.elbe.relations.services.IPeerTransferProvider;
import org.elbe.relations.services.PeerExport;
import org.elbe.relations.services.PeerSessionException;
import org.elbe.relations.services.PeerTransferOutcome;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import io.libp2p.core.crypto.PrivKey;

/** The libp2p implementation of the <code>IPeerTransferProvider</code> service: TCP with the
 * Noise handshake, speaking <code>/relations/export/1.0.0</code> as specified in
 * <code>PROTOCOL.md</code>.
 *
 * @author lbenno */
@Component(service = IPeerTransferProvider.class)
public class Libp2pPeerTransferProvider implements IPeerTransferProvider {
    private static final String NAME = "libp2p"; //$NON-NLS-1$
    private static final String KEY_FILE = "peer-identity.key"; //$NON-NLS-1$
    private static final String INSTANCE_AREA = "osgi.instance.area"; //$NON-NLS-1$

    private PeerIdentityStore identityStore;

    @Activate
    void activate(final BundleContext context) {
        this.identityStore = new PeerIdentityStore(identityFile(context));
    }

    /** The key is kept beside the application preferences, i.e. in this bundle's state
     * location within the instance area, so that it survives restarts but not a new
     * workspace. Without an instance area, the bundle's data area is used instead. */
    private static Path identityFile(final BundleContext context) {
        final String instanceArea = context.getProperty(INSTANCE_AREA);
        if (instanceArea != null) {
            try {
                return Path.of(URI.create(instanceArea.replace(" ", "%20"))) //$NON-NLS-1$ //$NON-NLS-2$
                        .resolve(".metadata").resolve(".plugins") //$NON-NLS-1$ //$NON-NLS-2$
                        .resolve(context.getBundle().getSymbolicName()).resolve(KEY_FILE);
            }
            catch (final IllegalArgumentException exc) {
                // not a file URL; fall through to the bundle's data area
            }
        }
        return context.getDataFile(KEY_FILE).toPath();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return Messages.getString("Libp2pPeerTransferProvider.description"); //$NON-NLS-1$
    }

    @Override
    public String getPeerId() throws IOException {
        return this.identityStore.getPeerId().toBase58();
    }

    @Override
    public IPeerSession openSession(final int port, final PeerExport export,
            final IPeerConnectionApproval approval, final IPeerTransferListener listener)
            throws PeerSessionException {
        final PrivKey identity;
        try {
            identity = this.identityStore.loadOrCreate();
        }
        catch (final IOException exc) {
            throw new PeerSessionException(PeerSessionException.Reason.NOT_STARTED, port, exc);
        }

        final ExportOffer offer = new ExportOffer(toScope(export.getScope()), export.getFile(),
                export.getName());
        final ExportSender sender = new ExportSender(() -> offer, approval::approve, session -> {
            final String peerId = session.getRemotePeerId();
            listener.transferStarted(peerId);
            session.getOutcome().thenAccept(outcome -> listener.transferEnded(peerId, toOutcome(outcome)));
        });

        final Libp2pTransferHost host = new Libp2pTransferHost();
        try {
            host.start(port, identity, sender);
        }
        catch (final Libp2pTransferHost.PortUnavailableException exc) {
            throw new PeerSessionException(PeerSessionException.Reason.PORT_UNAVAILABLE, port, exc);
        }
        catch (final IllegalStateException exc) {
            throw new PeerSessionException(PeerSessionException.Reason.NOT_STARTED, port, exc);
        }
        return new Libp2pPeerSession(host);
    }

    private static TransferScope toScope(final PeerExport.Scope scope) {
        return scope == PeerExport.Scope.INCREMENTAL ? TransferScope.INCREMENTAL : TransferScope.FULL;
    }

    private static PeerTransferOutcome toOutcome(final TransferOutcome outcome) {
        return switch (outcome) {
            case COMPLETED -> PeerTransferOutcome.COMPLETED;
            case DECLINED_BY_USER -> PeerTransferOutcome.DECLINED_BY_USER;
            case DIGEST_MISMATCH -> PeerTransferOutcome.DIGEST_MISMATCH;
            case STORAGE_ERROR -> PeerTransferOutcome.STORAGE_ERROR;
            case REJECTED_BY_RECEIVER -> PeerTransferOutcome.REJECTED_BY_RECEIVER;
            case INTERRUPTED -> PeerTransferOutcome.INTERRUPTED;
            case PROTOCOL_ERROR -> PeerTransferOutcome.PROTOCOL_ERROR;
        };
    }

}
