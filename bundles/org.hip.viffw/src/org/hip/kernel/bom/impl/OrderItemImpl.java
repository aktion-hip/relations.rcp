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

import org.hip.kernel.bom.OrderItem;
import org.hip.kernel.util.AbstractSortableItem;
import org.hip.kernel.util.Debug;
import org.hip.kernel.util.SortableItem;
import org.hip.kernel.util.VInvalidValueException;

/** Implementation of the OrderItem interface.
 *
 * Created on 13.09.2002
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.OrderItem */
@SuppressWarnings("serial")
public class OrderItemImpl extends AbstractSortableItem implements OrderItem {
    private final int position;
    private boolean descending = true; // NOPMD by lbenno 

    /** Constructor for OrderItemImpl. */
    public OrderItemImpl(final Object inColumnName, final boolean inDescending, final int inPosition)
            throws VInvalidValueException {
        super();

        // protected setter of value
        checkValue(inColumnName);
        value(inColumnName);
        descending = inDescending;
        position = inPosition;
    }

    /** @see org.hip.kernel.util.AbstractSortableItem#checkValue(Object) */
    @Override
    protected void checkValue(final Object inValue) throws VInvalidValueException {
        if (inValue == null) {
            throw new VInvalidValueException("No value set.");
        }
    }

    /** @see org.hip.kernel.bom.OrderItem#getColumnName() */
    @Override
    public String getColumnName() {
        return (String) getValue();
    }

    /** @see org.hip.kernel.bom.OrderItem#isDescending() */
    @Override
    public boolean isDescending() {
        return descending;
    }

    @Override
    public int compareTo(final SortableItem inItem) { // NOPMD by lbenno
        return position - ((OrderItem) inItem).getPosition();
    }

    /** @see org.hip.kernel.bom.OrderItem#getPosition() */
    @Override
    public int getPosition() {
        return position;
    }

    /** Returns true if inObject is a <code>SortableItem</code> and its value is equal to this SortableItem's value.
     *
     * @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {

        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof OrderItem)) {
            return false;
        }

        final OrderItem lOrderItem = (OrderItem) inObject;
        return getValue().equals(lOrderItem.getValue()) &&
                getPosition() == lOrderItem.getPosition() &&
                (isDescending() && lOrderItem.isDescending()) || !(isDescending() || lOrderItem.isDescending());
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        final String lMessage = "value=\"" + ((getValue() == null) ? "null" : getValue().toString()) + "\"" +
                " position=\"" + getPosition() + "\"" +
                " direction=\"" + (descending ? "DESC" : "ASC") + "\"";
        return Debug.classMarkupString(this, lMessage);
    }

    /** Returns the HashCode calculated from the value.
     *
     * @return int */
    @Override
    public int hashCode() {
        return super.hashCode() ^ getPosition() ^ (descending ? 0 : 1);
    }

}
