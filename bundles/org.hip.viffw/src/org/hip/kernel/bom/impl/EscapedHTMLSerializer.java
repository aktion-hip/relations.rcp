/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2003-2014, Benno Luthiger

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

import org.hip.kernel.stext.StructuredTextSerializer;

/** This class can be used to serialize DomainObjects with String type properties whose values are formated according to
 * StructuredText rules. This serializer converts these formatings to escaped HTML text.
 *
 * @author: Benno Luthiger
 * @see org.hip.kernel.bom.impl.HTMLSerializer */
@SuppressWarnings("serial")
public class EscapedHTMLSerializer extends AbstractStructuredTextSerializer {
    /** Constructor for EscapedHTMLSerializer. */
    public EscapedHTMLSerializer() {
        super();
    }

    /** Constructor for EscapedHTMLSerializer.
     *
     * @param inLevel */
    public EscapedHTMLSerializer(final int inLevel) {
        super(inLevel);
    }

    /** Constructor for EscapedHTMLSerializer.
     *
     * @param inLevel
     * @param inFilter */
    public EscapedHTMLSerializer(final int inLevel, final XMLCharacterFilter inFilter) {
        super(inLevel, inFilter);
    }

    /** Constructor for EscapedHTMLSerializer.
     *
     * @param inFilter */
    public EscapedHTMLSerializer(final XMLCharacterFilter inFilter) {
        super(inFilter);
    }

    /** Returns the correct StructuredTextSerializer to serialize DomainObjects with text formated according to
     * StructuredText rules. In this case returns an instance of org.hip.kernel.stext.EscapedHTMLSerializer.
     *
     * @see org.hip.kernel.stext.EscapedHTMLSerializer
     * @return org.hip.kernel.stext.StructuredTextSerializer */
    @Override
    protected StructuredTextSerializer getStructuredTextSerializer() {
        return new org.hip.kernel.stext.EscapedHTMLSerializer();
    }
}
