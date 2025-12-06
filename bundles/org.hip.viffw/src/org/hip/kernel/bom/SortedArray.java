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
package org.hip.kernel.bom;

/** The SortedArray is an array which can grow by adding new elements. If a new element is added, it is inserted at the
 * correct place an thus makes the array sorted.
 *
 * @author: Benno Luthiger */
public interface SortedArray {
    /** Sets the specified visitor. This method implements the visitor pattern.
     *
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    void accept(DomainObjectVisitor inVisitor);

    /** Returns the DomainObject at the specified position.
     *
     * @return DomainObject
     * @param index int */
    DomainObject elementAt(int index);

    /** Removes the element at the specified position from the sorted array.
     *
     * @param int */
    void remove(int i); // NOPMD

    /** Returns the actual size of the sorted array.
     *
     * @return int */
    int size();

    /** @return String */
    @Override
    String toString();
}
