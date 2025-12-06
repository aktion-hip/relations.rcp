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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.util.Debug;

/** This class implements a plain StructuredText paragraph, i.e.
 *
 * <pre>
 *   <p>Some text</p>
 * </pre>
 *
 * @author: Benno Luthiger */
public class StructuredTextPlain extends AbstractStructuredTextParagraph implements StructuredTextParagraph {
    private transient Collection<StructuredTextParagraph> plains;

    /** Constructor for StructuredTextPlain out of a raw paragraph.
     *
     * @param inRawParagraph java.lang.String */
    public StructuredTextPlain(final String inRawParagraph) {
        super(inRawParagraph);
    }

    /** Implementation of Visitor Pattern, e.g. to create an HTML string out of this text formatted with structured text
     * rules.
     *
     * @param inSerializer org.hip.kernel.stext.StructuredTextSerializer */
    @Override
    public void accept(final StructuredTextSerializer inSerializer) {
        inSerializer.visitStructuredTextPlain(this);
    };

    /** Adds a new sub paragraph.
     *
     * @param inRawParagraph java.lang.String */
    @Override
    public void add(final String inRawParagraph) {
        getPlains().add(new StructuredTextPlain(inRawParagraph));
    };

    /** Adds a new sub paragraph.
     *
     * @param inRawParagraph java.lang.String */
    @Override
    public void addIndented(final String inRawParagraph) {
        add(inRawParagraph);
    };

    /** Returns the type of this paragraph.
     *
     * @return int
     * @see org.hip.kernel.stext.StructuredTextParagraph */
    @Override
    public int getParagraphType() {
        return PARAGRAPH_PLAIN;
    }

    /** Checks whether this paragraph has subparagraphs.
     *
     * @return boolean */
    @Override
    public boolean hasSubElements() {
        return plains != null;
    }

    /** Returns an iterator over this paragraph's subparagraphs.
     *
     * @return java.util.Iterator */
    @Override
    public Iterator<StructuredTextParagraph> getSubElements() {
        return getPlains().iterator();
    }

    /** Lazy initialisation */
    private Collection<StructuredTextParagraph> getPlains() {
        if (plains == null) {
            plains = new ArrayList<StructuredTextParagraph>();
        }
        return plains;
    }

    /** Returns a string representation of this object.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return Debug.classMultilineMarkupString(this, getRawString());
    }

    /** Returns a hash code value for this object.
     *
     * @return int */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(final Object inObj) { // NOPMD by lbenno
        if (this == inObj) {
            return true;
        }
        if (inObj == null) {
            return false;
        }
        if (getClass() != inObj.getClass()) {
            return false;
        }
        return getRawString().equals(((StructuredTextPlain) inObj).getRawString());
    }

}
