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

/** Interface for a row of bit values.
 *
 * @author Benno Luthiger */
public interface BitRow extends Cloneable {

    /** Returns the value of the row as bit pattern.
     *
     * @return int */
    int getBitValue();

    /** Sets the bit pattern of the row.
     *
     * @param int */
    void setBitValue(int inBitPattern);

    /** Gets the bit value at the specified position
     *
     * @param inPosition int
     * @param boolean */
    boolean getBit(int inPosition);

    /** Sets the bit value at the specified position
     *
     * @param inPosition int
     * @param inValue int */
    void setBit(int inPosition, boolean inValue);

    /** Returns the inverted bit pattern of the row.
     *
     * @return org.hip.kernel.bitmap.BitRow */
    BitRow invert();

    /** Returns the bit pattern after logical AND with the specified bit pattern.
     *
     * @param inAnd org.hip.kernel.bitmap.BitRow The bit pattern to add.
     * @return org.hip.kernel.bitmap.BitRow */
    BitRow and(BitRow inAnd);

    /** Returns the bit pattern after logical OR with the specified bit pattern.
     *
     * @param inOr org.hip.kernel.bitmap.BitRow The bit pattern to or.
     * @return org.hip.kernel.bitmap.BitRow */
    BitRow or(BitRow inOr); // NOPMD by lbenno

    /** Returns the bit pattern after logical XOR with the specified bit pattern.
     *
     * @param inXOr org.hip.kernel.bitmap.BitRow The bit pattern to xor.
     * @return org.hip.kernel.bitmap.BitRow */
    BitRow xor(BitRow inXOr);

    /** Returns the size of the row.
     *
     * @return int */
    int getSize();

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

    /** @return */
    BitRow clone() throws CloneNotSupportedException; // NOPMD by lbenno

}
