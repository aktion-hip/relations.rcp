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

import java.util.List;

/** An open peer transfer session. The export is obtainable only while the session is open.
 *
 * @author lbenno */
public interface IPeerSession {

    /** @return String this installation's peer identity */
    String getPeerId();

    /** @return List&lt;String> the concrete addresses a device on the local network can dial,
     *         each including the peer identity */
    List<String> getAddresses();

    /** @return boolean <code>true</code> if the session is advertised for discovery on the
     *         local network; when <code>false</code>, only the addresses can be used */
    boolean isDiscoverable();

    /** Closes the session and releases the port. A transfer still running is interrupted.
     * Closing a session that is already closed has no effect. */
    void close();

}
