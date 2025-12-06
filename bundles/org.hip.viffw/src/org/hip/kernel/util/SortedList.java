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
package org.hip.kernel.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/** The SortedList holds a sorted collection of SortableItems. This list has no dupblicate elements.
 *
 * Created on 13.09.2002
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.util.SortableItem */
public interface SortedList extends Serializable {

    /** @param inNameValueListVisitor org.hip.kernel.util.NameValueListVisitor */
    void accept(NameValueListVisitor inNameValueListVisitor);

    /** Returns an iterator over the sorted items.
     *
     * @return java.util.Iterator
     * @deprecated Use {@link SortedList#getItems2()} instead. */
    @Deprecated
    Iterator<?> getItems();

    /** Returns a Collection view of the sorted items.
     *
     * @return Set<SortableItem> */
    Set<SortableItem> getItems2();

    /** This method sets the value of the item with the specified sort position.
     *
     * @param inValue java.lang.Object
     * @param inPosition int
     * @throws org.hip.kernel.util.VInvalidSortCriteriaException
     * @throws org.hip.kernel.util.VInvalidValueException */
    void setValue(Object inValue, int inPosition) throws VInvalidSortCriteriaException, VInvalidValueException;

    /** This method sets the value of the item with the specified sort criteria.
     *
     * @param inValue java.lang.Object
     * @param inSortCriteria java.lang.Object
     * @throws org.hip.kernel.util.VInvalidSortCriteriaException
     * @throws org.hip.kernel.util.VInvalidValueException */
    void setValue(Object inValue, Object inSortCriteria) throws VInvalidSortCriteriaException, VInvalidValueException;

    /** Returns count of nameValues added to this list.
     *
     * @return int */
    int size();
}
