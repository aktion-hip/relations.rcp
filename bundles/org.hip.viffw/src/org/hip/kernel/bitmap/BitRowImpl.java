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

/** Implementation of a bit row.
 *
 * @author Benno Luthiger */
public class BitRowImpl extends VObject implements BitRow, Cloneable {
    private transient boolean[] bits;
    private int size;

    /** Constructor for BitRowImpl. */
    public BitRowImpl(final int inSize) {
        super();
        setSize(inSize);
    }

    /** @see BitRow#getBitValue() */
    @Override
    public int getBitValue() {
        int outValue = 0;
        for (int i = 0; i < size; i++) {
            if (bits[i]) {
                outValue += (int) Math.pow(2, i);
            }
        }
        return outValue;
    }

    /** @see BitRow#setBitValue(int) */
    @Override
    public void setBitValue(final int inBitPattern) {
        for (int i = 0; i < size; i++) {
            final int lPattern = (int) Math.pow(2, i);
            setBit(i, (lPattern & inBitPattern) > 0);
        }
    }

    /** @see BitRow#getBit(int) */
    @Override
    public boolean getBit(final int inPosition) {
        return bits[inPosition];
    }

    /** @see BitRow#setBit(int, boolean) */
    @Override
    public void setBit(final int inPosition, final boolean inValue) {
        bits[inPosition] = inValue;
    }

    /** @see BitRow#invert() */
    @Override
    public BitRow invert() {
        final BitRow outBitRow = new BitRowImpl(size);
        for (int i = 0; i < size; i++) {
            outBitRow.setBit(i, !getBit(i));
        }

        return outBitRow;
    }

    /** @see BitRow#and(BitRow) */
    @Override
    public BitRow and(final BitRow inAnd) {
        final BitRow outBitRow = new BitRowImpl(size);
        outBitRow.setBitValue(getBitValue() & inAnd.getBitValue());
        return outBitRow;
    }

    /** @see BitRow#or(BitRow) */
    @Override
    public BitRow or(final BitRow inOr) { // NOPMD by lbenno
        final BitRow outBitRow = new BitRowImpl(size);
        outBitRow.setBitValue(getBitValue() | inOr.getBitValue());
        return outBitRow;
    }

    /** @see BitRow#xor(BitRow) */
    @Override
    public BitRow xor(final BitRow inXOr) {
        final BitRow outBitRow = new BitRowImpl(size);
        outBitRow.setBitValue(getBitValue() ^ inXOr.getBitValue());
        return outBitRow;
    }

    /** Returns the size of the row.
     *
     * @return int */
    @Override
    public int getSize() {
        return size;
    }

    private void setSize(final int inSize) {
        size = inSize;
        bits = new boolean[size];
    }

    /** Returns a hash code value for this bit pattern.
     *
     * @return int */
    @Override
    public int hashCode() {
        return getBitValue();
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

        return getBitValue() == ((BitRow) inObject).getBitValue();
    }

    /** Returns a string representation of this bit pattern.
     *
     * @param java.lang.String */
    @Override
    public String toString() {
        final StringBuilder outPattern = new StringBuilder(50).append("<BitRow>");
        for (int i = size - 1; i >= 0; i--) {
            outPattern.append(getBit(i) ? '1' : '0');
        }

        return outPattern.append("</BitRow>").toString();
    }

    /** Creates and returns a copy of this object.
     *
     * @return java.lang.Object
     * @throws CloneNotSupportedException */
    @Override
    public BitRow clone() throws CloneNotSupportedException {
        final BitRowImpl outClone = (BitRowImpl) super.clone();
        outClone.setSize(getSize());
        outClone.setBitValue(getBitValue());
        return outClone;
    }
}
