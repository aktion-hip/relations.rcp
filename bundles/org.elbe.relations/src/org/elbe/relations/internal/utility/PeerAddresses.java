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
package org.elbe.relations.internal.utility;

import java.util.List;
import java.util.Optional;

/** Chooses the one session address that is presented to the user (as QR code and text).
 *
 * @author lbenno */
public final class PeerAddresses {
    private static final String IP4_PREFIX = "/ip4/"; //$NON-NLS-1$

    private PeerAddresses() {
        // prevent instantiation
    }

    /** Returns the first IPv4 address on the local network (site-local), else the first IPv4
     * address, else nothing. The session lists the addresses in the order of the network
     * interfaces, which is the order local-network discovery uses too.
     *
     * @param addresses List&lt;String> the multiaddrs reported by the session
     * @return Optional&lt;String> the address to present */
    public static Optional<String> choose(final List<String> addresses) {
        String firstIp4 = null;
        for (final String address : addresses) {
            if (!address.startsWith(IP4_PREFIX)) {
                continue;
            }
            if (isSiteLocal(getHost(address))) {
                return Optional.of(address);
            }
            if (firstIp4 == null) {
                firstIp4 = address;
            }
        }
        return Optional.ofNullable(firstIp4);
    }

    private static String getHost(final String address) {
        final int end = address.indexOf('/', IP4_PREFIX.length());
        return end < 0 ? address.substring(IP4_PREFIX.length()) : address.substring(IP4_PREFIX.length(), end);
    }

    /** Same ranges as <code>InetAddress.isSiteLocalAddress()</code>, but parsed from the literal
     * so that a malformed entry can never cause a name lookup. */
    private static boolean isSiteLocal(final String host) {
        final String[] parts = host.split("\\."); //$NON-NLS-1$
        if (parts.length != 4) {
            return false;
        }
        final int[] octets = new int[4];
        try {
            for (int i = 0; i < octets.length; i++) {
                octets[i] = Integer.parseInt(parts[i]);
                if (octets[i] < 0 || octets[i] > 255) {
                    return false;
                }
            }
        }
        catch (final NumberFormatException exc) {
            return false;
        }
        return octets[0] == 10 || octets[0] == 172 && octets[1] >= 16 && octets[1] <= 31
                || octets[0] == 192 && octets[1] == 168;
    }

}
