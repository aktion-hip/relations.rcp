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

import java.util.List;

import org.elbe.relations.services.IPeerSession;

/** An open session backed by a running {@link Libp2pTransferHost}.
 *
 * @author lbenno */
class Libp2pPeerSession implements IPeerSession {
    private final Libp2pTransferHost host;
    private final String peerId;

    Libp2pPeerSession(final Libp2pTransferHost host) {
        this.host = host;
        this.peerId = host.getPeerId();
    }

    @Override
    public String getPeerId() {
        return this.peerId;
    }

    @Override
    public List<String> getAddresses() {
        return this.host.isRunning() ? this.host.getReachableAddresses() : List.of();
    }

    @Override
    public boolean isDiscoverable() {
        return this.host.isDiscoveryActive();
    }

    @Override
    public void close() {
        this.host.stop();
    }

}
