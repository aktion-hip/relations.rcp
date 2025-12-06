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

import java.util.Locale;

import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.SortedArray;

/** The SortedArray is an array which can grow by adding new elements. If a new element is added, it is inserted at the
 * correct place an thus makes the array sorted.
 *
 * @author: Benno Luthiger */
public abstract class AbstractSortedArray implements SortedArray { // NOPMD by lbenno
    private transient Object[][] sortedArr = new Object[0][0];

    /** Sets the specified visitor. This method implements the visitor pattern.
     *
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    @Override
    public void accept(final DomainObjectVisitor inVisitor) {
        inVisitor.visitSortedArray(this);
    }

    /** Adds a new element to the sorted array, i.e. inserts the element at the correct place.
     *
     * @param inObject org.hip.kernel.bom.DomainObject
     * @param inSortString java.lang.String The string that determines the DomainObject's correct place */
    protected void addSorted(final DomainObject inObject, final String inSortString) {

        if (isEmpty()) {
            sortedArr = new Object[1][2];
            sortedArr[0][0] = inObject;
            sortedArr[0][1] = inSortString;
        }
        else {
            final int lPosition = quickSearchPosition(inSortString.toUpperCase(Locale.getDefault()), 0,
                    sortedArr.length - 1);
            insertAtPosition(inObject, inSortString, lPosition);
        }
    }

    /** Adds a new element to the sorted array only if no other element with the same key (inSortString) exists.
     *
     * @param inObject org.hip.kernel.bom.DomainObject
     * @param inSortString java.lang.String The string determing the DomainObjects correct place */
    protected void addSortedUnique(final DomainObject inObject, final String inSortString) {

        if (isEmpty()) {
            sortedArr = new Object[1][2];
            sortedArr[0][0] = inObject;
            sortedArr[0][1] = inSortString;
        }
        else {
            final int lPosition = quickSearchPosition(inSortString.toUpperCase(Locale.getDefault()), 0,
                    sortedArr.length - 1);
            if (lPosition < sortedArr.length) {
                if (sortedArr[lPosition][1].equals(inSortString)) {
                    return;
                }
            }
            if (lPosition > 0) {
                if (sortedArr[lPosition - 1][1].equals(inSortString)) {
                    return;
                }
            }
            if (lPosition < sortedArr.length - 1) {
                if (sortedArr[lPosition + 1][1].equals(inSortString)) {
                    return;
                }
            }
            insertAtPosition(inObject, inSortString, lPosition);
        }
    }

    /** @param inFrom Object[][]
     * @param inTo Object[][]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArr(final Object[][] inFrom, final Object[][] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        for (int i = inLowerBoundary; i <= inUpperBoundary; i++) {
            inTo[i][0] = inFrom[i][0];
            inTo[i][1] = inFrom[i][1];
        }
    }

    /** @param inFrom Object[][]
     * @param inTo Object[][]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArrDownShifted(final Object[][] inFrom, final Object[][] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        for (int i = inLowerBoundary; i < inUpperBoundary; i++) {
            inTo[i - 1][0] = inFrom[i][0];
            inTo[i - 1][1] = inFrom[i][1];
        }
    }

    /** @param inFrom Object[][]
     * @param inTo Object[][]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArrShifted(final Object[][] inFrom, final Object[][] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        for (int i = inLowerBoundary + 1; i <= inUpperBoundary; i++) {
            inTo[i][0] = inFrom[i - 1][0];
            inTo[i][1] = inFrom[i - 1][1];
        }
    }

    /** Returns the DomainObject at the specified position.
     *
     * @return DomainObject
     * @param index int */
    @Override
    public DomainObject elementAt(final int index) {
        return (DomainObject) elementAtPosition(index);
    }

    /** Returns the element at the specified position.
     *
     * @return Object
     * @param index int */
    protected Object elementAtPosition(final int index) {
        return sortedArr[index][0];
    }

    /** @param inObject org.hip.kernel.bom.DomainObject
     * @param inSortString java.lang.String
     * @param inPosition int */
    private void insertAtPosition(final DomainObject inObject, final String inSortString, final int inPosition) {
        final Object[][] lSortedArr = new Object[sortedArr.length + 1][2];
        copyArr(sortedArr, lSortedArr, 0, inPosition - 1);
        lSortedArr[inPosition][0] = inObject;
        lSortedArr[inPosition][1] = inSortString;
        copyArrShifted(sortedArr, lSortedArr, inPosition, sortedArr.length);
        sortedArr = lSortedArr;
    }

    private boolean isEmpty() {
        return sortedArr.length == 0;
    }

    /** @return boolean
     * @param inSortString java.lang.String
     * @param inLowerBoundary int */
    private boolean isGreaterThenPosition(final String inSortString, final int inPosition) {
        return ((String) sortedArr[inPosition][1]).toUpperCase().compareTo(inSortString) < 0;
    }

    /** @return boolean
     * @param inSortString java.lang.String
     * @param inUpperBoundary int */
    private boolean isGreaterThenUpper(final String inSortString, final int inUpperBoundary) {
        return isGreaterThenPosition(inSortString, inUpperBoundary);
    }

    /** @return boolean
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private boolean isInMinimalIntervall(final int inLowerBoundary, final int inUpperBoundary) {
        return inUpperBoundary - inLowerBoundary <= 1;
    }

    /** @return boolean
     * @param inSortString java.lang.String
     * @param inLowerBoundary int */
    private boolean isLessThenLower(final String inSortString, final int inLowerBoundary) {
        return isLessThenPosition(inSortString, inLowerBoundary);
    }

    /** @return boolean
     * @param inElement java.lang.String
     * @param inLowerBoundary int */
    private boolean isLessThenPosition(final String inSortString, final int inPosition) {
        return ((String) sortedArr[inPosition][1]).toUpperCase().compareTo(inSortString) > 0;
    }

    /** @return boolean
     * @param inSortString java.lang.String */
    private boolean isMax(final String inSortString) {
        return isGreaterThenPosition(inSortString, sortedArr.length - 1);
    }

    /** @return boolean */
    private boolean isMin(final String inSortString) {
        return isLessThenPosition(inSortString, 0);
    }

    /** QuickSearch for the position in a sorted array using binary search. This method is invoked recursively.
     *
     * @return int
     * @param inElement java.lang.String */
    private int quickSearchPosition(final String inSortString, final int inLowerBoundary, final int inUpperBoundary) {
        int outPosition = 0;

        if (isEmpty()) {
            outPosition = 0;
        }
        else if (isMin(inSortString)) {
            outPosition = 0;
        }
        else if (isMax(inSortString)) {
            outPosition = sortedArr.length;
        }
        else if (isInMinimalIntervall(inLowerBoundary, inUpperBoundary)) {
            if (isLessThenLower(inSortString, inLowerBoundary)) {
                outPosition = inLowerBoundary;
            }
            else if (isGreaterThenUpper(inSortString, inUpperBoundary)) {
                outPosition = inUpperBoundary;
            }
            else {
                outPosition = inUpperBoundary;
            }
        }
        else {
            final int lNewBoundary = (inLowerBoundary + inUpperBoundary) / 2;
            if (isLessThenPosition(inSortString, lNewBoundary)) {
                outPosition = quickSearchPosition(inSortString, inLowerBoundary, lNewBoundary);
            }
            else {
                outPosition = quickSearchPosition(inSortString, lNewBoundary, inUpperBoundary);
            }
        }

        return outPosition;
    }

    /** Removes the element at the specified position from the sorted array.
     *
     * @param int */
    @Override
    public void remove(final int inPosition) {
        if (inPosition < 0 || inPosition >= sortedArr.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        final Object[][] lSortedArr = new Object[sortedArr.length - 1][2];
        copyArr(sortedArr, lSortedArr, 0, inPosition - 1);
        copyArrDownShifted(sortedArr, lSortedArr, inPosition + 1, sortedArr.length);
        sortedArr = lSortedArr;
    }

    /** Returns the size of the sorted array
     *
     * @return int */
    @Override
    public int size() {
        return sortedArr.length;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lXML = new StringBuilder(50);

        lXML.append("<DomainObjects>");

        final XMLSerializer lXMLSerializer = new XMLSerializer();
        DomainObject lObject;
        for (int i = 0; i < sortedArr.length; i++) {
            lObject = (DomainObject) sortedArr[i][0];
            lObject.accept(lXMLSerializer);
            lXML.append(lXMLSerializer.toString());
        }

        lXML.append("\n</DomainObjects>");

        return lXML.toString();
    }
}
