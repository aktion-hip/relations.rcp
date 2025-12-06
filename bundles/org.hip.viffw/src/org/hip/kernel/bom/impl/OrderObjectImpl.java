/**
 This package is part of the framework used for the application VIF.
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

import java.util.Iterator;

import org.hip.kernel.bom.OrderItem;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.util.AbstractSortedList;
import org.hip.kernel.util.SortableItem;
import org.hip.kernel.util.VInvalidSortCriteriaException;
import org.hip.kernel.util.VInvalidValueException;

/** Implementation of the OrderObject interface.
 *
 * Created on 13.09.2002
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.OrderObject */
@SuppressWarnings("serial")
public class OrderObjectImpl extends AbstractSortedList implements OrderObject { // NOPMD by lbenno

    /** Class for a parameter object. */
    private static class ParameterObject {
        private final String columnName;
        private final boolean descending;

        private ParameterObject(final String inColumnName, final boolean inDescending) {
            super();
            columnName = inColumnName;
            descending = inDescending;
        }

        public String getColumnName() { // NOPMD by lbenno
            return columnName;
        }

        public boolean isDescending() { // NOPMD by lbenno
            return descending;
        }
    }

    /** @see org.hip.kernel.util.AbstractSortedList#create(Object, int) */
    @Override
    protected SortableItem create(final Object inValue, final int inPosition) throws VInvalidSortCriteriaException,
    VInvalidValueException {
        try {
            final ParameterObject lParameter = (ParameterObject) inValue;
            return new OrderItemImpl(lParameter.getColumnName(), lParameter.isDescending(), inPosition);
        } catch (final ClassCastException exc) {
            throw new VInvalidValueException(exc);
        }
    }

    /** @see org.hip.kernel.util.AbstractSortedList#create(Object, Object) */
    @Override
    protected SortableItem create(final Object inValue, final Object inSortCriteria)
            throws VInvalidSortCriteriaException, VInvalidValueException {
        throw new VInvalidSortCriteriaException("SortCriteria not supported for OrderObject.");
    }

    /** This method sets an order item (i.e. column) with the specified name and default sort order (= ascending) at the
     * specified position.
     *
     * @param inColumnName java.lang.String
     * @param inPosition int
     * @throws org.hip.kernel.util.VInvalidSortCriteriaException
     * @throws org.hip.kernel.util.VInvalidValueException */
    @Override
    public void setValue(final String inColumnName, final int inPosition) throws VInvalidSortCriteriaException,
    VInvalidValueException {
        setValue(inColumnName, false, inPosition);
    }

    /** This method sets an order item (i.e. column) with the specified name and sort order at the specified position.
     *
     * @param inColumnName java.lang.String
     * @param inDescending boolean
     * @param inPosition int
     * @throws org.hip.kernel.util.VInvalidSortCriteriaException
     * @throws org.hip.kernel.util.VInvalidValueException */
    @Override
    public void setValue(final String inColumnName, final boolean inDescending, final int inPosition)
            throws VInvalidSortCriteriaException, VInvalidValueException {
        add(create(new ParameterObject(inColumnName, inDescending), inPosition)); // NOPMD by lbenno 
    }

    /** Compares all items of OrderObjects.
     *
     * @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {
        // pre
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof OrderObject)) {
            return false;
        }

        final OrderObject lOrderObject = ((OrderObject) inObject);
        if (lOrderObject.size() != size()) {
            return false;
        }

        final Iterator<?> lThis = getItems();
        final Iterator<?> lItems = lOrderObject.getItems2().iterator();
        while (lItems.hasNext()) {
            if (!((OrderItem) lItems.next()).equals(lThis.next())) {
                return false;
            }
        }

        return true;
    }

    /** Returns a hash code value for the order object.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outHashCode = 0;

        for (final Iterator<?> lItems = getItems(); lItems.hasNext();) {
            outHashCode ^= ((OrderItem) lItems.next()).hashCode();
        }
        return outHashCode;
    }
}
