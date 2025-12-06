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

/** This class provides generic functionality to create html code by serializing a StructuredText.
 *
 * @see org.hip.kernel.stext.StructuredTextSerializer
 * @see org.hip.kernel.stext.StructuredText
 * @author: Benno Luthiger */
public abstract class AbstractHTMLSerializer extends AbstractStructuredTextSerializer implements // NOPMD by lbenno
StructuredTextSerializer {
    // Instance variables
    private transient boolean inList;

    // Hooks for subclasses

    /** Writes the end tag to the buffer, e.g. "</inContent>" or "&lt;/inContent&gt;"
     *
     * @param inContent java.lang.String */
    abstract protected void emitHTMLEndTag(String inContent);

    /** Writes the start tag to the buffer, e.g. "<inContent>" or "&lt;inContent&gt;"
     *
     * @param inContent java.lang.String */
    abstract protected void emitHTMLStartTag(String inContent);

    /** Returns the specified string formatted for the output.
     *
     * @param inRawString java.lang.String
     * @return java.lang.StringBuffer */
    abstract protected StringBuffer getConverted(String inRawString);

    /** @see org.hip.kernel.stext.StructuredTextSerializer#visitStructuredText(StructuredText) */
    @Override
    public void visitStructuredText(final StructuredText inStructuredText) {
        for (final Iterator<StructuredTextParagraph> lParagraphs = inStructuredText.getStructuredTextParagraphs(); lParagraphs
                .hasNext();) {
            lParagraphs.next().accept(this);
        }
    }

    /** Visit StructuredTextBullet
     *
     * @param StructuredTextBullet */
    @Override
    public void visitStructuredTextBullet(final StructuredTextBullet inStructuredTextBullet) {
        inList = true;
        emitHTMLStartTag("ul");
        iterateOverListElements(inStructuredTextBullet.getSubElements());
        emitHTMLEndTag("ul");
        inList = false;
        emit_nl();
    }

    /** Visit StructuredTextNumbered
     *
     * @param StructuredTextNumbered */
    @Override
    public void visitStructuredTextNumbered(final StructuredTextNumbered inStructuredTextNumbered) {
        inList = true;
        emitHTMLStartTag("ol");
        iterateOverListElements(inStructuredTextNumbered.getSubElements());
        emitHTMLEndTag("ol");
        inList = false;
        emit_nl();
    }

    private void iterateOverListElements(final Iterator<StructuredTextParagraph> inListElements) {
        for (final Iterator<StructuredTextParagraph> lElements = inListElements; lElements.hasNext();) {
            emitHTMLStartTag("li");
            lElements.next().accept(this);
            emitHTMLEndTag("li");
            emit_nl();
        }
    }

    /** Visit a plain StructuredText paragraph
     *
     * @param StructuredTextNumbered */
    @Override
    public void visitStructuredTextPlain(final StructuredTextPlain inStructuredTextPlain) {
        emitHTMLStartTag("p");
        emit(getConverted(inStructuredTextPlain.getRawString()));
        emitHTMLEndTag("p");
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
}
