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

import java.util.Collection;

/** Interface of an object which represents a matrix of binary values.
 *
 * @author Benno Luthiger */
public interface BitField extends Cloneable { // NOPMD by lbenno

    /** Adds the specified tuple at the end of the rows.
     *
     * @param inTuple org.hip.kernel.bitmap.Tuple */
    void addRow(Tuple inTuple);

    /** Adds the specified object and bit row at the end of the rows.
     *
     * @param inObject java.lang.Object
     * @param inRow org.hip.kernel.bitmap.BitRow */
    void addRow(Object inObject, BitRow inRow);

    /** Removes the row at the specified position.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.Tuple The removed row */
    Tuple removeRow(int inRowPosition);

    /** Returns the tuple at the specified position.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.Tuple */
    Tuple getTuple(int inRowPosition);

    /** Returns the object at the specified row position.
     *
     * @param inRowPosition int
     * @return java.lang.Object */
    Object getObject(int inRowPosition);

    /** Returns the bit pattern at the specified row position.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.BitRow */
    BitRow getBitRow(int inRowPosition);

    /** Number of rows contained in the bit field.
     *
     * @return int */
    int getRowSize();

    /** Number of columns contained in the bit field.
     *
     * @return int */
    int getColumnSize();

    /** Gets the bit value at the specified position
     *
     * @param inRowPosition int
     * @param inColumnPosition int
     * @param boolean */
    boolean getBit(int inRowPosition, int inColumnPosition);

    /** Gets the bit value at the specified position
     *
     * @param inPosition org.hip.kernel.bitmap.MatrixPosition
     * @param boolean */
    boolean getBit(MatrixPosition inPosition);

    /** Sets the bit value at the specified position
     *
     * @param inPosition int
     * @param inValue int */
    void setBit(int inRowPosition, int inColumnPosition, boolean inValue);

    /** Sets the bit value at the specified position
     *
     * @param inPosition org.hip.kernel.bitmap.MatrixPosition
     * @param inValue int */
    void setBit(MatrixPosition inPosition, boolean inValue);

    /** Returns the inverted bit pattern of the row.
     *
     * @return org.hip.kernel.bitmap.BitField */
    BitField invert();

    /** Returns the bit pattern after logical AND with the specified bit pattern.
     *
     * @param inAnd org.hip.kernel.bitmap.BitField The bit pattern to add.
     * @return org.hip.kernel.bitmap.BitField */
    BitField and(BitField inAnd);

    /** Returns the bit pattern after logical OR with the specified bit pattern.
     *
     * @param inAdd org.hip.kernel.bitmap.BitField The bit pattern to or.
     * @return org.hip.kernel.bitmap.BitField */
    BitField or(BitField inOr); // NOPMD by lbenno

    /** Returns the bit pattern after logical XOR with the specified bit pattern.
     *
     * @param inAdd org.hip.kernel.bitmap.BitField The bit pattern to xor.
     * @return org.hip.kernel.bitmap.BitField */
    BitField xor(BitField inXOr);

    /** Compares this bit field with the specified bit field and returns a collection of positions the two bit fields
     * differ.
     *
     * @param inCompare org.hip.kernel.bitmap.BitField
     * @return org.hip.kernel.bitmap.MatrixPositions
     * @deprecated Use <code>{@link #getDifferences2(BitField)}</code> instead. */
    @Deprecated
    MatrixPositions getDifferences(BitField inCompare);

    /** Compares this bit field with the specified bit field and returns a collection of positions the two bit fields
     * differ.
     *
     * @param inCompare BitField
     * @return Collection<MatrixPosition> */
    Collection<MatrixPosition> getDifferences2(BitField inCompare);

    /** Returns a hash code value for this bit pattern.
     *
     * @return int */
    @Override
    int hashCode();

    /** Compares this object against the specified object.
     *
     * @param inObject java.lang.Object
     * @return boolean */
    @Override
    boolean equals(Object inObject);

    /** Returns a string representation of this bit pattern.
     *
     * @param java.lang.String */
    @Override
    String toString();

    /** Returns a collection of checked positions of the specified row.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.MatrixPositions
     * @deprecated Use <code>{@link #getCheckedOfRow2(int)}</code> instead. */
    @Deprecated
    MatrixPositions getCheckedOfRow(int inRowPosition);

    /** @param inRowPosition int
     * @return Collection<MatrixPosition> */
    Collection<MatrixPosition> getCheckedOfRow2(int inRowPosition);

}
