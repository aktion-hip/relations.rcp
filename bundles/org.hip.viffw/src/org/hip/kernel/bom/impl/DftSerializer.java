/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2001-2014, Benno Luthiger

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

import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SortedArray;

/** Default Serializer, a dummy implementation.
 *
 * @author Benno Luthiger
 * @see java.io.Serializable */
@SuppressWarnings("serial")
public class DftSerializer extends AbstractSerializer implements java.io.Serializable {
    /** XMLSerializer default constructor. */
    public DftSerializer() {
        super();
    }

    /** DftSerializer constructor of inputed level.
     *
     * @param inLevel int */
    public DftSerializer(final int inLevel) {
        super(inLevel);
    }

    /** Emits a comment line.
     *
     * @param inComment java.lang.String */
    @Override
    protected void emitComment(final String inComment) {
        synchronized (this) {
            emit_nl();
            emit("<!--" + inComment + "-->");
        }
    }

    /** @param inContent java.lang.String */
    @Override
    protected void emitEndTag(final String inContent) {
        // intentionally left empty
    }

    /** Emits a start tag.
     *
     * @param inContent java.lang.String */
    @Override
    protected void emitStartTag(final String inContent) {
        synchronized (this) {
            emit("<" + inContent + ">");
        }
    }

    /** Sequence ending the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void endDomainObject(final GeneralDomainObject inObject) {
        emit_nl();
        emit_indent();
        emitEndTag(inObject.getObjectName());
    }

    /** Emits the sequence ending the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void endIterator(final DomainObjectIterator inIterator) {
        emit_nl();
        emit_indent();
        emitEndTag("ObjectList");
    }

    /** Sequence ending the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void endProperty(final Property inProperty) {
        emitEndTag(inProperty.getName());
    }

    /** Sequence ending the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void endPropertySet(final PropertySet inSet) {
        emit_nl();
        emit_indent();
        emitEndTag("propertySet");
    }

    /** Emits the sequence ending the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void endSortedArray(final SortedArray inSortedArray) {
        emit_nl();
        emit_indent();
        emitEndTag("SortedArray");
    }

    @Override
    public void start() { // NOPMD by lbenno
    }

    /** Sequence starting the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void startDomainObject(final GeneralDomainObject inObject) {
        emit_nl();
        emitStartTag(inObject.getObjectName());
    }

    /** Sequence starting the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void startIterator(final DomainObjectIterator inIterator) {
        emit_nl();
        emitStartTag("ObjectList");
    }

    /** Sequence starting the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void startProperty(final Property inProperty) {
        emit_nl();
        emitStartTag(inProperty.getName());
        emitText(inProperty.getValue());
    }

    /** Sequence starting the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void startPropertySet(final PropertySet inSet) {
        emit_nl();
        emitStartTag("propertySet");
    }

    /** Sequence starting the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void startSortedArray(final SortedArray inSortedArray) {
        emit_nl();
        emitStartTag("SortedArray");
    }
}