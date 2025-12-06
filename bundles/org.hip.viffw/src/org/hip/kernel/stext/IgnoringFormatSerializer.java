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

/** This serializer ignores all StructuredText format and returns cleaned text.
 *
 * @author: Benno Luthiger */
public class IgnoringFormatSerializer extends AbstractStructuredTextSerializer implements StructuredTextSerializer { // NOPMD by lbenno 
    // Instance variables
    private transient boolean inList;

    /** @see org.hip.kernel.stext.StructuredTextSerializer#visitStructuredText(StructuredText) */
    @Override
    public void visitStructuredText(final StructuredText inStructuredText) {
        for (final Iterator<StructuredTextParagraph> lParagraphs = inStructuredText.getStructuredTextParagraphs(); lParagraphs
                .hasNext();) {
            lParagraphs.next().accept(this);
        }
    }

    /** @see org.hip.kernel.stext.StructuredTextSerializer#visitStructuredTextBullet(StructuredTextBullet) */
    @Override
    public void visitStructuredTextBullet(final StructuredTextBullet inStructuredTextBullet) {
        iterateOverListElements(inStructuredTextBullet.getSubElements());
    }

    /** @see org.hip.kernel.stext.StructuredTextSerializer#visitStructuredTextNumbered(StructuredTextNumbered) */
    @Override
    public void visitStructuredTextNumbered(final StructuredTextNumbered inStructuredTextNumbered) {
        iterateOverListElements(inStructuredTextNumbered.getSubElements());
    }

    private void iterateOverListElements(final Iterator<StructuredTextParagraph> inListElements) {
        inList = true;
        for (final Iterator<StructuredTextParagraph> lElements = inListElements; lElements.hasNext();) {
            lElements.next().accept(this);
            emit_nl();
        }
        inList = false;
    }

    /** @see org.hip.kernel.stext.StructuredTextSerializer#visitStructuredTextPlain(StructuredTextPlain) */
    @Override
    public void visitStructuredTextPlain(final StructuredTextPlain inStructuredTextPlain) {
        emit(getConverted(inStructuredTextPlain.getRawString()));
        if (!inList) {
            emit_nl();
        }
        if (inStructuredTextPlain.hasSubElements()) {
            for (final Iterator<StructuredTextParagraph> lParagraphs = inStructuredTextPlain.getSubElements(); lParagraphs
                    .hasNext();) {
                emit_nl();
                lParagraphs.next().accept(this);
            }
        }
    }

    private StringBuffer getConverted(final String inRawString) {
        return InlineStructuredText2HTML.getSingleton().convertIgnoring(inRawString);
    }
}
