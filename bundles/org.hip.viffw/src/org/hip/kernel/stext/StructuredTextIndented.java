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

/** This class implements a indented StructuredText paragraph, i.e. a paragraph in a bulleted or numbered list element.
 * 
 * <pre>
 * <li>
 *    <p>Some text</p>
 *    <p>Further text</p>
 * </li>
 * <li><p>more list elements</p></li>
 * </pre>
 *
 * @author: Benno Luthiger */
public class StructuredTextIndented extends StructuredTextPlain {

    /** Constructor for StructuredTextIndented.
     * 
     * @param inRawParagraph */
    public StructuredTextIndented(final String inRawParagraph) {
        super(inRawParagraph);
    }

    /** Returns the type of this paragraph.
     * 
     * @return int
     * @see org.hip.kernel.stext.StructuredTextParagraph */
    @Override
    public int getParagraphType() {
        return PARAGRAPH_INDENTED;
    }
}
