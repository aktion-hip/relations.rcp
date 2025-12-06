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

/** This class implements a Structured Text Bulleted List, i.e.
 *
 * <pre>
 * <ul>
 *   <li><p>Some text</p></li>
 *   <li><p>Further text</p></li>
 * </ul>
 * </pre>
 *
 * @author: Benno Luthiger */
public class StructuredTextBullet extends AbstractStructuredTextParagraph implements StructuredTextParagraph {
    private final transient Collection<StructuredTextParagraph> bullets;
    private transient StructuredTextParagraph currentBullet;

    /** Constructor for StructuredTextBullet.
     *
     * @param inRawParagraph java.lang.String */
    public StructuredTextBullet(final String inRawParagraph) {
        super(inRawParagraph);
        bullets = new ArrayList<StructuredTextParagraph>();
        add(inRawParagraph);
    }

    /** Implementation of Visitor Pattern, e.g. to create an HTML string out of this text formatted with structured text
     * rules.
     *
     * @param inSerializer org.hip.kernel.stext.StructuredTextSerializer */
    @Override
    public void accept(final StructuredTextSerializer inSerializer) {
        inSerializer.visitStructuredTextBullet(this);
    };

    /** Adds a new list element to this bulleted list.
     *
     * @param inRawParagraph java.lang.String */
    @Override
    public void add(final String inRawParagraph) {
        currentBullet = new StructuredTextPlain(inRawParagraph);
        bullets.add(currentBullet);
    };

    /** Adds a new paragraph to the current list element.
     *
     * @param inRawParagraph java.lang.String */
    @Override
    public void addIndented(final String inRawParagraph) {
        currentBullet.add(inRawParagraph);
    };

    /** Returns the type of this paragraph.
     *
     * @return int
     * @see org.hip.kernel.stext.StructuredTextParagraph */
    @Override
    public int getParagraphType() {
        return PARAGRAPH_BULLET;
    }

    /** Checks whether this list has list elements.
     *
     * @return boolean */
    @Override
    public boolean hasSubElements() {
        return !bullets.isEmpty();
    }

    /** Returns an iterator over all list elements of this bulleted list.
     *
     * @return java.util.Iterator */
    @Override
    public Iterator<StructuredTextParagraph> getSubElements() {
        return bullets.iterator();
    }

    /** Returns a string representation of this object.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return Debug.classMultilineMarkupString(this, getRawString());
    }

    @Override
    public int hashCode() { // NOPMD by lbenno 
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
        return getRawString().equals(((StructuredTextBullet) inObj).getRawString());
    }

}
