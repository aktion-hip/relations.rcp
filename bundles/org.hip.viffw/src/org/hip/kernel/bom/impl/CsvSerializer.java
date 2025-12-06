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
import org.hip.kernel.util.ListJoiner;

/** This class produces CSV-Files.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class CsvSerializer extends AbstractSerializer {

    private StringBuilder header; // NOPMD by lbenno 

    /** CsvSerializer default constructor. */
    public CsvSerializer() {
        super();
    }

    /** CsvSerializer for inputed level.
     *
     * @param inLevel int */
    public CsvSerializer(final int inLevel) {
        super(inLevel);
    }

    /** Sequence ending the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void endDomainObject(final GeneralDomainObject inObject) {
        // emit_nl();
    }

    /** Sequence ending the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void endIterator(final DomainObjectIterator inIterator) {
        // intentionally left empty
    }

    /** Sequence ending the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void endProperty(final Property inProperty) {
        emitText(",");
    }

    /** Sequence ending the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void endPropertySet(final PropertySet inSet) {
        // intentionally left empty
    }

    /** Sequence ending the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void endSortedArray(final SortedArray inSortedArray) {
        // intentionally left empty
    }

    /** @return java.lang.String */
    public String getHeader() {
        return header.toString();
    }

    /** Sequence starting the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void startDomainObject(final GeneralDomainObject inObject) {
        if (header == null) {

            header = new StringBuilder(1024);
            final ListJoiner lHeader = new ListJoiner();
            for (final String lName : inObject.getPropertyNames2()) {
                lHeader.addEntry(lName);
            }
            header.append(lHeader.joinSpaced(","));
        }
    }

    /** Sequence starting the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void startIterator(final DomainObjectIterator inIterator) {
        // intentionally left empty
    }

    /** Sequence starting the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void startProperty(final Property inProperty) {
        final Object lObject = inProperty.getValue();
        String lText = "";
        if (lObject != null) {
            lText = lObject.toString();
        }

        lText = lText.trim();
        lText = lText.replace(',', ' ');
        emitText(lText);
    }

    /** Sequence starting the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void startPropertySet(final PropertySet inSet) {
        // intentionally left empty
    }

    /** Sequence starting the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void startSortedArray(final SortedArray inSortedArray) {
        // intentionally left empty
    }
}
