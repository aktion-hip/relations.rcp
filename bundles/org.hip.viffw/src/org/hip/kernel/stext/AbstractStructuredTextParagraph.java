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

import org.hip.kernel.sys.VObject;

/** Abstract class with generalized functionality of StructuredTextParagraphs.
 *
 * @see org.hip.kernel.stext.StructuredTextParagraph
 * @author: Benno Luthiger */
public abstract class AbstractStructuredTextParagraph extends VObject {
    private final transient String rawParagraph;

    /** Constructor for AbstractStructuredTextParagraph. */
    public AbstractStructuredTextParagraph(final String inRawParagraph) {
        super();
        rawParagraph = inRawParagraph;
    }

    /** Returns the raw string of this paragraph.
     *
     * @return java.lang.String */
    public String getRawString() {
        return rawParagraph;
    }

    /** Returns the type of this paragraph
     *
     * @return int */
    protected abstract int getParagraphType();

    /** Checks if this paragraph's type is equal to the specified object's paragraph type.
     *
     * @param inObject java.lang.Object
     * @return boolean */
    public boolean equalsType(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (inObject instanceof StructuredTextParagraph) {
            return ((StructuredTextParagraph) inObject).getParagraphType() == getParagraphType();
        }
        else {
            return false;
        }
    }
}
