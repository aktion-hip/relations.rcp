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

/** A tuple stores both any object in the first position and a BitRow in the second position.
 *
 * @author Benno Luthiger */
public class Tuple extends VObject implements Cloneable {
    private transient Object tupleObject;
    private transient BitRow tupleBitRow;

    /** Constructor for Tuple.
     *
     * @param inObject java.lang.Object
     * @param inRow org.hip.kernel.bitmap.BitRow */
    public Tuple(final Object inObject, final BitRow inRow) {
        super();
        initalize(inObject, inRow);
    }

    private void initalize(final Object inObject, final BitRow inRow) {
        tupleObject = inObject;
        tupleBitRow = inRow;
    }

    /** Returns the object of the tuple.
     *
     * @return java.lang.Object */
    public Object getTupleObject() {
        return tupleObject;
    }

    /** Returns the bit pattern of the tuple.
     *
     * @return org.hip.kernel.bitmap.BitRow */
    public BitRow getTupleBitRow() {
        return tupleBitRow;
    }

    /** Empties the bit pattern of the tuple. */
    public void setTupleBitRowEmpty() {
        tupleBitRow.setBitValue(0);
    }

    /** Returns a tuple with inverted bit pattern.
     *
     * @return org.hip.kernel.bitmap.Tuple */
    public Tuple invert() {
        return new Tuple(tupleObject, tupleBitRow.invert());
    }

    /** Returns the tuple after logical AND of its bit pattern with the specified tuple.
     *
     * @param inTuple org.hip.kernel.bitmap.Tuple
     * @return org.hip.kernel.bitmap.Tuple */
    public Tuple and(final Tuple inTuple) {
        return new Tuple(tupleObject, tupleBitRow.and(inTuple.getTupleBitRow()));
    }

    /** Returns the tuple after logical OR of its bit pattern with the specified tuple.
     *
     * @param inTuple org.hip.kernel.bitmap.Tuple
     * @return org.hip.kernel.bitmap.Tuple */
    public Tuple or(final Tuple inTuple) { // NOPMD
        return new Tuple(tupleObject, tupleBitRow.or(inTuple.getTupleBitRow()));
    }

    /** Returns the tuple after logical XOR of its bit pattern with the specified tuple.
     *
     * @param inTuple org.hip.kernel.bitmap.Tuple
     * @return org.hip.kernel.bitmap.Tuple */
    public Tuple xor(final Tuple inTuple) {
        return new Tuple(tupleObject, tupleBitRow.xor(inTuple.getTupleBitRow()));
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

        final Tuple lCompare = (Tuple) inObject;
        return tupleObject.equals(lCompare.getTupleObject()) && tupleBitRow.equals(lCompare.getTupleBitRow());
    }

    /** Returns a hash code value for this tuple object.
     *
     * @return int */
    @Override
    public int hashCode() {
        return tupleObject.hashCode() ^ tupleBitRow.hashCode();
    }

    /** Returns a string representation of this tuple object.
     *
     * @param java.lang.String */
    @Override
    public String toString() {
        return "<Tuple><Object>" + tupleObject.toString() + "</Object>" + tupleBitRow.toString() + "</Tuple>";
    }

    /** Creates and returns a copy of this object.
     *
     * @return java.lang.Object
     * @throws CloneNotSupportedException */
    @Override
    public Tuple clone() throws CloneNotSupportedException {
        final Tuple out = (Tuple) super.clone();
        out.initalize(tupleObject, tupleBitRow.clone());
        return out;
    }
}
