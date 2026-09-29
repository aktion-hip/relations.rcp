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

/** How a peer transfer ended.
 *
 * <p>
 * Only {@link #COMPLETED} permits the caller to clear the change log.
 * </p>
 *
 * @author lbenno */
public enum TransferOutcome {

    /** The receiver acknowledged a complete, digest-verified delivery. */
    COMPLETED,
    /** The user declined the connecting device. */
    DECLINED_BY_USER,
    /** The receiver reported that the digest did not match. */
    DIGEST_MISMATCH,
    /** The receiver could not store the payload. */
    STORAGE_ERROR,
    /** The receiver declined the transfer. */
    REJECTED_BY_RECEIVER,
    /** The stream ended before the transfer was acknowledged. */
    INTERRUPTED,
    /** The peer did not speak this protocol correctly. */
    PROTOCOL_ERROR;

    /** @return boolean <code>true</code> only when delivery was acknowledged in full */
    public boolean isComplete() {
        return this == COMPLETED;
    }
}
