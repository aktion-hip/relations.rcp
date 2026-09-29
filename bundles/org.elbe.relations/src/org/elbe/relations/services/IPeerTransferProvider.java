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
package org.elbe.relations.services;

import java.io.IOException;

/** Interface defining the OSGi service to send an export directly to a companion
 * application on a device in the local network.
 *
 * <p>
 * Unlike {@link ICloudProvider}, a peer transfer is not fire-and-forget: opening a session
 * makes the export available until the session is closed, and every connecting device must
 * be approved through the passed {@link IPeerConnectionApproval} before anything is sent.
 * </p>
 *
 * @author lbenno */
public interface IPeerTransferProvider {

    /** @return String the name of the peer transfer provider */
    String getName();

    /** @return String a short, localized description of how this provider connects, suitable
     *         for display in the preferences */
    String getDescription();

    /** Returns this installation's peer identity, creating it on first use. The identity is
     * stable across restarts and is available without opening a session.
     *
     * @return String the peer identity
     * @throws IOException if the identity cannot be read or created */
    String getPeerId() throws IOException;

    /** Opens a transfer session offering the passed export.
     *
     * @param port int the TCP port to listen on
     * @param export {@link PeerExport} the prepared export to offer
     * @param approval {@link IPeerConnectionApproval} asked for every connecting device before
     *            anything is sent
     * @param listener {@link IPeerTransferListener} notified as transfers start and end
     * @return {@link IPeerSession} the open session, to be closed by the caller
     * @throws PeerSessionException if the session cannot be opened */
    IPeerSession openSession(int port, PeerExport export, IPeerConnectionApproval approval,
            IPeerTransferListener listener) throws PeerSessionException;

}
