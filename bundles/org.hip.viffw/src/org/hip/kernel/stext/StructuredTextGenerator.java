/**
	This package is part of the structured text framework used for the application VIF.
	Copyright (C) 2003-2015, Benno Luthiger

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
package org.hip.kernel.stext;

import java.util.regex.Pattern;

/** This class generates StructuredText classes out of strings which are formatted according to structured text rules.
 *
 * @author: Benno Luthiger */
public final class StructuredTextGenerator {
    private static StructuredTextGenerator singleton;

    /** Constructor for StructuredTextGenerator. */
    private StructuredTextGenerator() {
        // prevent instantiation
    }

    /** Returns the singleton instance of StructuredTextGenerator
     *
     * @return StructuredTextGenerator */
    public synchronized static StructuredTextGenerator getSingleton() { // NOPMD by lbenno
        if (singleton == null) {
            singleton = new StructuredTextGenerator();
        }
        return singleton;
    }

    /** Creates a HTML formatted text of the specified string which is formatted according to structured text rules.
     *
     * @return StructuredText
     * @param inRawString The text formatted according to structured text rules. */
    public StructuredText createStructuredText(final String inRawString) {
        final String[] lParagraphs = createParagraphs(inRawString);
        return new StructuredText(lParagraphs);
    }

    private String[] createParagraphs(final String inRawString) {
        final Pattern lPattern = Pattern.compile("\\n\\s*\\n|\\r\\n\\s*\\r\\n");
        return lPattern.split(inRawString);
    }

}
