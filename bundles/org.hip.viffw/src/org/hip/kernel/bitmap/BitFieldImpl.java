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
package org.hip.kernel.bitmap; // NOPMD by lbenno

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.sys.VObject;

/** Implemantation of a bit field.
 *
 * @author Benno Luthiger */
public class BitFieldImpl extends VObject implements BitField, Cloneable { // NOPMD by lbenno
    private transient List<Tuple> fieldTuples;

    // private class Tuples extends VectorAdapter {
    // public Tuples() {
    // super();
    // }
    //
    // public boolean add(Tuple inTuple) {
    // return addElement(inTuple);
    // }
    // }

    /** Constructor for BitFieldImpl. */
    public BitFieldImpl() {
        super();
        fieldTuples = new ArrayList<Tuple>();
    }

    /** Adds the specified tuple at the end of the rows.
     *
     * @param inTuple org.hip.kernel.bitmap.Tuple */
    @Override
    public void addRow(final Tuple inTuple) {
        if (equalRowSize(inTuple.getTupleBitRow())) {
            fieldTuples.add(inTuple);
        }
        else {
            fieldTuples.add(new Tuple(inTuple.getTupleObject(), resizeRow(inTuple.getTupleBitRow())));
        }
    }

    /** Adds the specified object and bit row at the end of the rows.
     *
     * @param inObject java.lang.Object
     * @param inRow org.hip.kernel.bitmap.BitRow */
    @Override
    public void addRow(final Object inObject, final BitRow inRow) {
        if (equalRowSize(inRow)) {
            fieldTuples.add(new Tuple(inObject, inRow));
        }
        else {
            fieldTuples.add(new Tuple(inObject, resizeRow(inRow)));
        }
    }

    private boolean equalRowSize(final BitRow inRow) {
        if (fieldTuples.isEmpty()) {
            return true;
        }
        return fieldTuples.get(0).getTupleBitRow().getSize() == inRow.getSize();
    }

    private BitRow resizeRow(final BitRow inRow) {
        final BitRow outRow = new BitRowImpl(getColumnSize());
        outRow.setBitValue(inRow.getBitValue());
        return outRow;
    }

    /** @see BitField#removeRow(int) */
    @Override
    public Tuple removeRow(final int inRowPosition) {
        return fieldTuples.remove(inRowPosition);
    }

    /** Returns the tuple at the specified position.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.Tuple */
    @Override
    public Tuple getTuple(final int inRowPosition) {
        return fieldTuples.get(inRowPosition);
    }

    /** Returns the object at the specified row position.
     *
     * @param inRowPosition int
     * @return java.lang.Object */
    @Override
    public Object getObject(final int inRowPosition) {
        return fieldTuples.get(inRowPosition).getTupleObject();
    }

    /** Returns the bit pattern at the specified row position.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.BitRow */
    @Override
    public BitRow getBitRow(final int inRowPosition) {
        return fieldTuples.get(inRowPosition).getTupleBitRow();
    }

    /** @see BitField#getRowSize() */
    @Override
    public int getRowSize() {
        return fieldTuples.size();
    }

    /** @see BitField#getColumnSize() */
    @Override
    public int getColumnSize() {
        if (fieldTuples == null) {
            return 0;
        }
        if (fieldTuples.isEmpty()) {
            return 0;
        }
        return fieldTuples.get(0).getTupleBitRow().getSize();
    }

    /** @see BitField#getBit(int, int) */
    @Override
    public boolean getBit(final int inRowPosition, final int inColumnPosition) {
        return fieldTuples.get(inRowPosition).getTupleBitRow().getBit(inColumnPosition);
    }

    /** @see BitField#getBit(MatrixPosition) */
    @Override
    public boolean getBit(final MatrixPosition inPosition) {
        return fieldTuples.get(inPosition.getRowPosition()).getTupleBitRow().getBit(inPosition.getColumnPosition());
    }

    /** @see BitField#setBit(int, int, boolean) */
    @Override
    public void setBit(final int inRowPosition, final int inColumnPosition, final boolean inValue) {
        fieldTuples.get(inRowPosition).getTupleBitRow().setBit(inColumnPosition, inValue);
    }

    /** @see BitField#setBit(MatrixPosition, boolean) */
    @Override
    public void setBit(final MatrixPosition inPosition, final boolean inValue) {
        fieldTuples.get(inPosition.getRowPosition()).getTupleBitRow().setBit(inPosition.getColumnPosition(), inValue);
    }

    /** @see BitField#invert() */
    @Override
    public BitField invert() {
        final BitField outField = new BitFieldImpl();
        for (final Iterator<?> lTuples = fieldTuples.iterator(); lTuples.hasNext();) {
            outField.addRow(((Tuple) lTuples.next()).invert());
        }

        return outField;
    }

    /** @see BitField#and(BitField) */
    @Override
    public BitField and(final BitField inAnd) {
        final int lSize = Math.min(getRowSize(), inAnd.getRowSize());
        final BitField outField = new BitFieldImpl();
        for (int i = 0; i < lSize; i++) {
            outField.addRow(getTuple(i).and(inAnd.getTuple(i)));
        }

        return outField;
    }

    /** @see BitField#or(BitField) */
    @Override
    public BitField or(final BitField inOr) { // NOPMD by lbenno
        final int lSize = Math.min(getRowSize(), inOr.getRowSize());
        final BitField outField = new BitFieldImpl();
        for (int i = 0; i < lSize; i++) {
            outField.addRow(getTuple(i).or(inOr.getTuple(i)));
        }

        return outField;
    }

    /** @see BitField#xor(BitField) */
    @Override
    public BitField xor(final BitField inXOr) {
        final int lSize = Math.min(getRowSize(), inXOr.getRowSize());
        final BitField outField = new BitFieldImpl();
        for (int i = 0; i < lSize; i++) {
            outField.addRow(getTuple(i).xor(inXOr.getTuple(i)));
        }

        return outField;
    }

    /** Compares this bit field with the specified bit field and returns a collection of positions the two bit fields
     * differ.
     *
     * @param inCompare org.hip.kernel.bitmap.BitField
     * @return org.hip.kernel.bitmap.MatrixPositions
     * @deprecated Use <code>{@link #getDifferences2(BitField)}</code> instead. */
    @Deprecated
    @Override
    public MatrixPositions getDifferences(final BitField inCompare) {
        final MatrixPositions outPositions = new MatrixPositions();

        final int lColumnSize = getColumnSize();
        final int lRowSize = Math.min(getRowSize(), inCompare.getRowSize());

        for (int i = 0; i < lRowSize; i++) {
            for (int j = 0; j < lColumnSize; j++) {
                if (getBit(i, j) ^ inCompare.getBit(i, j)) {
                    outPositions.add(i, j);
                }
            } // columns
        } // rows

        return outPositions;
    }

    @Override
    public Collection<MatrixPosition> getDifferences2(final BitField inCompare) { // NOPMD by lbenno
        final Collection<MatrixPosition> outPositions = new ArrayList<MatrixPosition>();

        final int lColumnSize = getColumnSize();
        final int lRowSize = Math.min(getRowSize(), inCompare.getRowSize());

        for (int i = 0; i < lRowSize; i++) {
            for (int j = 0; j < lColumnSize; j++) {
                if (getBit(i, j) ^ inCompare.getBit(i, j)) {
                    outPositions.add(new MatrixPosition(i, j));
                }
            } // columns
        } // rows

        return outPositions;
    }

    /** Returns a hash code value for this bit pattern.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 0;
        for (final Iterator<?> lTuples = fieldTuples.iterator(); lTuples.hasNext();) {
            outCode ^= ((Tuple) lTuples.next()).hashCode();
        }

        return outCode;
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

        final BitField lToCompare = (BitField) inObject;
        if (getRowSize() != lToCompare.getRowSize()) {
            return false;
        }
        if (getColumnSize() != lToCompare.getColumnSize()) {
            return false;
        }

        for (int i = 0; i < getRowSize(); i++) {
            if (!getTuple(i).equals(lToCompare.getTuple(i))) {
                return false;
            }
        }
        return true;
    }

    /** Returns a string representation of this bit pattern.
     *
     * @param java.lang.String */
    @Override
    public String toString() {
        return "";
    }

    /** Returns a collection of checked positions of the specified row. Note: The positions contain indexes in the
     * matrix.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.MatrixPositions
     * @deprecated Use <code>BitFieldImpl#getCheckedOfRow2(int inRowPosition)</code> instead. */
    @Deprecated
    @Override
    public MatrixPositions getCheckedOfRow(final int inRowPosition) {
        final BitRow lBitRow = getBitRow(inRowPosition);
        final MatrixPositions outPositions = new MatrixPositions();
        for (int i = 0; i < lBitRow.getSize(); i++) {
            if (lBitRow.getBit(i)) {
                outPositions.add(inRowPosition, i);
            }
        }
        return outPositions;
    }

    /** Returns a collection of checked positions of the specified row. Note: The positions contain indexes in the
     * matrix.
     *
     * @param inRowPosition int The element's position in the row.
     * @return Collection<MatrixPosition> */
    @Override
    public Collection<MatrixPosition> getCheckedOfRow2(final int inRowPosition) {
        final BitRow lBitRow = getBitRow(inRowPosition);
        final Collection<MatrixPosition> outPositions = new ArrayList<MatrixPosition>();
        for (int i = 0; i < lBitRow.getSize(); i++) {
            if (lBitRow.getBit(i)) {
                outPositions.add(new MatrixPosition(inRowPosition, i));
            }
        }
        return outPositions;
    }

    /** Creates and returns a copy of this object. Note: The bit pattern of the copy is empty.
     *
     * @return java.lang.Object
     * @throws CloneNotSupportedException */
    @Override
    public BitField clone() throws CloneNotSupportedException {
        final BitField outField = (BitFieldImpl) super.clone();
        ((BitFieldImpl) outField).fieldTuples = new ArrayList<Tuple>();
        final int size = getRowSize();
        for (int i = 0; i < size; i++) {
            final Tuple lTuple = getTuple(i).clone();
            lTuple.setTupleBitRowEmpty();
            outField.addRow(lTuple);
        }
        return outField;
    }

}
