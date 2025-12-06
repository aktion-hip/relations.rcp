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
package org.hip.kernel.bitmap;

import org.hip.kernel.sys.VObject;

/** Simple object representing a position in a two dimensional field.
 *
 * @author Benno Luthiger */
public class MatrixPosition extends VObject {
    private final transient int rowPosition;
    private final transient int columnPosition;

    /** Constructor for MatrixPosition.
     *
     * @param inRowPosition int
     * @param inColumnPosition int */
    public MatrixPosition(final int inRowPosition, final int inColumnPosition) {
        super();
        rowPosition = inRowPosition;
        columnPosition = inColumnPosition;
    }

    /** @return int */
    public int getRowPosition() {
        return rowPosition;
    }

    /** @return int */
    public int getColumnPosition() {
        return columnPosition;
    }

    /** Returns a hash code value for this bit pattern.
     *
     * @return int */
    @Override
    public int hashCode() {
        return rowPosition * 100 + columnPosition;
    }

    /** Compares this object against the specified object.
     *
     * @param inObject java.lang.Object
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (getClass() != inObject.getClass()) {
            return false;
        }

        final MatrixPosition lToCompare = (MatrixPosition) inObject;
        return rowPosition == lToCompare.getRowPosition() &&
                columnPosition == lToCompare.getColumnPosition();
    }

    /** Returns a string representation of this bit pattern.
     *
     * @param java.lang.String */
    @Override
    public String toString() {
        return "<Position rowPosition='" + rowPosition +
                "' columnPosition='" + columnPosition + "'/>";
    }
}
