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

/** Thrown when a peer session cannot be opened. No session exists and nothing has been
 * exported when this is thrown.
 *
 * @author lbenno */
public class PeerSessionException extends Exception {
    private static final long serialVersionUID = 1L;

    /** Why the session could not be opened. */
    public enum Reason {
        /** The configured port is held by another process. */
        PORT_UNAVAILABLE,
        /** Listening for peers failed for another reason; see the cause. */
        NOT_STARTED
    }

    private final Reason reason;
    private final int port;

    /** PeerSessionException constructor.
     *
     * @param reason {@link Reason}
     * @param port int the port the session was to listen on
     * @param cause {@link Throwable} the underlying error */
    public PeerSessionException(final Reason reason, final int port, final Throwable cause) {
        super(String.format("Unable to open a peer session on port %d (%s)", port, reason), cause); //$NON-NLS-1$
        this.reason = reason;
        this.port = port;
    }

    public Reason getReason() {
        return this.reason;
    }

    public int getPort() {
        return this.port;
    }

}
