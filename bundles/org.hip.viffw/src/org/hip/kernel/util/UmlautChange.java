/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.util;

import java.util.HashMap;
import java.util.Map;

/** This class is used to convert Umlauts in a String.
 *
 * @author Benno Luthiger */
public final class UmlautChange {
    private static Object[][] umlauts = { { new Character("ä".charAt(0)), "ae" }
            , { new Character("ö".charAt(0)), "oe" }
            , { new Character("ü".charAt(0)), "ue" }
            , { new Character("Ä".charAt(0)), "Ae" }
            , { new Character("Ö".charAt(0)), "Oe" }
            , { new Character("Ü".charAt(0)), "Ue" } };

    private static Map<Object, Object> umlautsTable;

    private UmlautChange() {
        // prevent instantiation
    }

    /** Converts the umlauts in a String.
     *
     * @return java.lang.String
     * @param inName java.lang.String */
    public static synchronized String convert(final String inName) { // NOPMD by lbenno
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < inName.length(); i++) {
            final Character lChar = new Character(inName.charAt(i));
            if (getUmlauts().containsKey(lChar)) {
                out.append(getUmlauts().get(lChar));
            }
            else {
                out.append(lChar.toString());
            }
        }
        return out.toString();
    }

    /** @return java.util.Hashtable */
    private synchronized static Map<Object, Object> getUmlauts() { // NOPMD by lbenno
        if (umlautsTable == null) {
            umlautsTable = new HashMap<Object, Object>(11);
            for (int i = 0; i < umlauts.length; i++) {
                umlautsTable.put(umlauts[i][0], umlauts[i][1]);
            }
        }
        return umlautsTable;
    }
}
