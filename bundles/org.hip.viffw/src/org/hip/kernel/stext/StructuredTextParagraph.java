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

import java.util.Iterator;

/** Interface for a paragraph formatted with structured text rules.
 *
 * @author: Benno Luthiger */
public interface StructuredTextParagraph {
    int PARAGRAPH_PLAIN = 0;
    int PARAGRAPH_BULLET = 1;
    int PARAGRAPH_NUMBERED = 2;
    int PARAGRAPH_INDENTED = 3;

    /** Accepting a visitor.
     *
     * @param inSerializer {@link StructuredTextSerializer} */
    void accept(StructuredTextSerializer inSerializer);

    /** Add a sub element.
     *
     * @param inRawParagraph String */
    void add(String inRawParagraph);

    /** Add a sub element with indent.
     *
     * @param inRawParagraph String */
    void addIndented(String inRawParagraph);

    /** Returns the actual paragraph type.
     *
     * @return int */
    int getParagraphType();

    /** Returns the raw (i.e. the unstructured) string.
     *
     * @return String */
    String getRawString();

    /** Checks type equality.
     *
     * @param inObject {@link Object}
     * @return boolean <code>true</code> if both type are equal */
    boolean equalsType(Object inObject);

    /** Check for elements contained in this element.
     *
     * return boolean <code>true</code> if this element contains sub elements */
    boolean hasSubElements();

    /** Returns the sub elements.
     *
     * @return Iterator&lt;StructuredTextParagraph> */
    Iterator<StructuredTextParagraph> getSubElements();
}
