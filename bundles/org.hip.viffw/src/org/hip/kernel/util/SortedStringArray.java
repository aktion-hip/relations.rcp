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

import java.util.Locale;

/** The SortedStringArray is an array which can grow by adding new elements. If a new element is added, it is inserted at
 * the correct place an thus makes the array sorted.
 *
 * @author: Benno Luthiger */
public class SortedStringArray {
    private transient String[] sortedStrings = new String[0];
    private transient boolean caseSensitive;

    /** SortedStringArray default constructor. */
    public SortedStringArray() {
        super();
    }

    /** SortedStringArray constructor for a case-sensitive sort. */
    public SortedStringArray(final boolean inCaseSensitive) {
        super();
        caseSensitive = inCaseSensitive;
    }

    /** Adds a new element to the sortet array, i.e. inserts the element at the correct place.
     *
     * @param inElement java.lang.String The element to be added. */
    public void addSorted(final String inElement) {

        if (isEmpty()) {
            sortedStrings = new String[1];
            sortedStrings[0] = inElement;
        }
        else {
            final int lPosition = quickSearchPosition(getSearchString(inElement), 0, sortedStrings.length - 1);
            insertAtPosition(inElement, lPosition);
        }
    }

    /** Adds a new element to the sorted array only if that element doesn't exist yet.
     *
     * @param inElement java.lang.String The string determing the DomainObjects correct place */
    public void addSortedUnique(final String inElement) {

        if (isEmpty()) {
            sortedStrings = new String[1];
            sortedStrings[0] = inElement;
        }
        else {
            final String lSearchString = getSearchString(inElement);
            final int lPosition = quickSearchPosition(lSearchString, 0, sortedStrings.length - 1);
            if (lPosition < sortedStrings.length) {
                if (getSearchString(sortedStrings[lPosition]).equals(lSearchString)) {
                    return;
                }
            }
            if (lPosition > 0) {
                if (getSearchString(sortedStrings[lPosition - 1]).equals(lSearchString)) {
                    return;
                }
            }
            if (lPosition < sortedStrings.length - 1) {
                if (getSearchString(sortedStrings[lPosition + 1]).equals(lSearchString)) {
                    return;
                }
            }
            insertAtPosition(inElement, lPosition);
        }
    }

    /** @param inFrom String[]
     * @param inTo String[]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArr(final String[] inFrom, final String[] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        if (inUpperBoundary - inLowerBoundary > 0) {
            System.arraycopy(inFrom, inLowerBoundary, inTo, inLowerBoundary, inUpperBoundary - inLowerBoundary);
        }
    }

    /** @param inFrom String[]
     * @param inTo String[]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArrDownShifted(final String[] inFrom, final String[] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        if (inUpperBoundary - inLowerBoundary > 0) {
            System.arraycopy(inFrom, inLowerBoundary, inTo, inLowerBoundary - 1, inUpperBoundary - inLowerBoundary);
        }
    }

    /** @param inFrom String[]
     * @param inTo String[]
     * @param inLowerBoundary int
     * @param inUpperBoundary int */
    private void copyArrShifted(final String[] inFrom, final String[] inTo, final int inLowerBoundary,
            final int inUpperBoundary) {
        if (inUpperBoundary - inLowerBoundary > 0) {
            System.arraycopy(inFrom, inLowerBoundary, inTo, inLowerBoundary + 1, inUpperBoundary - inLowerBoundary);
        }
    }

    /** Returns the element at the specified position.
     *
     * @return java.lang.String
     * @param index int */
    public String elementAt(final int index) {
        return sortedStrings[index];
    }

    /** Clones the array of sorted Strings
     *
     * @return java.lang.String[] */
    public String[] getAsArray() {
        final String[] outArray = new String[sortedStrings.length];
        System.arraycopy(sortedStrings, 0, outArray, 0, sortedStrings.length);
        return outArray;
    }

    /** @return java.lang.String */
    private String getSearchString(final String inElement) {
        return (caseSensitive ? inElement : inElement.toUpperCase(Locale.getDefault()));
    }

    /** @param inElement java.lang.String
     * @param inPosition int */
    private void insertAtPosition(final String inElement, final int inPosition) {
        final String[] lSortedArr = new String[sortedStrings.length + 1];
        copyArr(sortedStrings, lSortedArr, 0, inPosition);
        lSortedArr[inPosition] = inElement;
        copyArrShifted(sortedStrings, lSortedArr, inPosition, sortedStrings.length);
        sortedStrings = lSortedArr;
    }

    private boolean isEmpty() {
        return sortedStrings.length == 0;
    }

    /** @return boolean
     * @param inSortString java.lang.String
     * @param inLowerBoundary int */
    private boolean isGreaterThenPosition(final String inSortString, final int inPosition) {
        return getSearchString(sortedStrings[inPosition]).compareTo(inSortString) < 0;
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
        return getSearchString(sortedStrings[inPosition]).compareTo(inSortString) > 0;
    }

    /** @return boolean
     * @param inSortString java.lang.String */
    private boolean isMax(final String inSortString) {
        return isGreaterThenPosition(inSortString, sortedStrings.length - 1);
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
            outPosition = sortedStrings.length;
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
    public void remove(final int inPosition) {
        if (inPosition < 0 || inPosition >= sortedStrings.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        final String[] lSortedArr = new String[sortedStrings.length - 1];
        copyArr(sortedStrings, lSortedArr, 0, inPosition);
        copyArrDownShifted(sortedStrings, lSortedArr, inPosition + 1, sortedStrings.length);
        sortedStrings = lSortedArr;
    }

    /** Returns the size of the sorted array
     *
     * @return int */
    public int size() {
        return sortedStrings.length;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lOut = new StringBuilder(100).append('{');
        boolean lFirst = true;
        for (int i = 0; i < sortedStrings.length; i++) {
            if (!lFirst) {
                lOut.append(", ");
            }
            lFirst = false;
            lOut.append(sortedStrings[i]);
        }
        lOut.append('}');
        return new String(lOut);
    }
}
