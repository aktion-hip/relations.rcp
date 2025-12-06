/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001inValue.replaceAll("\'", new String(lRepl));

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
package org.hip.kernel.bom.impl;

/** SimpleFilter which replaces some characters with entities. Needed to avoid complication in an xml-parser.
 *
 * @author Benno Luthiger */
public class XMLCharacterFilter {
    /** Default character filter to process &lt; and &amp;. */
    public final static XMLCharacterFilter DEFAULT_FILTER = getFilter();

    // member
    private static final String CDATA_BEGIN = "<![CDATA[";
    private static final String CDATA_END = "]]>";
    private String[][] charsToReplace;
    private transient boolean useCDATAFlag;

    /** Constructor
     *
     * @param inCharsToReplace String[][] a field (dimension: numberOfCharsToReplace x 2) containing the characters to
     *            replace and the entities which replace them. */
    public XMLCharacterFilter(final String[][] inCharsToReplace) { // NOPMD by lbenno
        super();
        charsToReplace = inCharsToReplace;
    }

    /** Constructor
     *
     * @param inCharsToReplace String[][] a field (dimension: numberOfCharsToReplace x 2) containing the characters to
     *            replace and the entities which replace them.
     * @param inUseCDATA boolean <code>true</code> if <code>&lt;![CDATA[...]]></code> has to be used to process the
     *            strings. */
    public XMLCharacterFilter(final String[][] inCharsToReplace, final boolean inUseCDATA) { // NOPMD by lbenno 
        this(inCharsToReplace);
        charsToReplace = inCharsToReplace;
        useCDATAFlag = inUseCDATA;
    }

    /** Do the filtering.
     *
     * @param inString java.lang.String the string to clean
     * @return java.lang.String the processed string */
    public String filter(final String inString) {
        char c; // NOPMD by lbenno

        if (inString == null || charsToReplace == null) {
            return "";
        }

        final StringBuilder outValue = new StringBuilder();
        if (useCDATAFlag) {
            outValue.append(CDATA_BEGIN);
        }

        String temp = "";
        for (int i = 0; i < inString.length(); i++) {
            c = inString.charAt(i);
            temp = String.valueOf(c);
            for (int k = 0; k < charsToReplace.length; k++) {
                if (c == charsToReplace[k][0].charAt(0)) {
                    temp = charsToReplace[k][1];
                    break;
                }
            }
            outValue.append(temp);
        }

        if (useCDATAFlag) {
            outValue.append(CDATA_END);
        }

        return outValue.toString();
    }

    /** Returns Field with chars to replace.
     *
     * @return String[][] a field (dimension: numberOfCharstoReplace x 2) containing the characters to replace and the
     *         entities which replace them. */
    public String[][] getCharsToReplace() {
        return charsToReplace; // NOPMD by lbenno 
    }

    /** Set Field with chars to replace.
     *
     * @param inCharsToReplace String[][] a field (dimension: numberOfCharstoReplace x 2) containing the characters to
     *            replace and the entities which replace them. */
    public void setCharsToReplace(final String[][] inCharsToReplace) { // NOPMD by lbenno
        charsToReplace = inCharsToReplace;
    }

    /** Setter for <code>useCDATA</code> field.
     *
     * @param inUseCD boolean <code>true</code> if <code>&lt;![CDATA[...]]></code> has to be used to process the
     *            strings. */
    public void useCDATA(final boolean inUseCD) {
        useCDATAFlag = inUseCD;
    }

    private static XMLCharacterFilter getFilter() {
        final String[][] lChars = new String[2][2];

        // initialize the field with the characters to replace
        lChars[0][0] = "<";
        lChars[0][1] = "&lt;";
        lChars[1][0] = "&";
        lChars[1][1] = "&amp;";
        return new XMLCharacterFilter(lChars, false);
    }
}
