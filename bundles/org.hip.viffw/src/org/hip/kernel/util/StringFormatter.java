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

/** Utility class for String formatting to eliminate trailing spaces.
 *
 * @author Benno Luthiger */
public final class StringFormatter {

    private StringFormatter() {
        // prevent instantiation
    }

    /** @return java.lang.String
     * @param inString java.lang.String */
    public static String removeEndingSpace(final String inString) {

        if (inString.length() < 1) { // NOPMD by lbenno
            return inString;
        }

        // length >=1
        if (inString.length() == 1 && inString.charAt(0) == ' ') {
            return "";
        }

        // length > 1
        if (inString.charAt(inString.length() - 1) == ' ') { // NOPMD by lbenno
            return removeEndingSpace(inString.substring(0, inString.length() - 1));
        }

        return inString;
    }

    /** @return java.lang.String
     * @param inString java.lang.String */
    public static String removeLeadingAndEndingSpace(final String inString) {

        return removeLeadingSpace(removeEndingSpace(inString));
    }

    /** @return java.lang.String
     * @param inString java.lang.String */
    public static String removeLeadingSpace(final String inString) {

        if (inString.length() < 1) { // NOPMD by lbenno
            return inString;
        }

        // length >= 1
        if (inString.length() == 1 && inString.charAt(0) == ' ') {
            return "";
        }

        // length > 1
        if (inString.charAt(0) == ' ') { // NOPMD by lbenno
            return removeLeadingSpace(inString.substring(1, inString.length()));
        }

        return inString;
    }
}
