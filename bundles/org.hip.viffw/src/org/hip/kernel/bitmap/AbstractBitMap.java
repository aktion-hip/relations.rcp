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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.code.CodeList;
import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.VObject;

/** Base class for BitMaps. A bitmap represents a matrix of binary values.
 *
 * @author Benno Luthiger */
abstract public class AbstractBitMap extends VObject { // NOPMD by lbenno
    // constants
    private final static String TAG_ROOT = "Checkfield";
    private final static String TAG_ROW = "Row";
    private final static String TAG_COLUMN = "Column";
    private final static String TAG_ELEMENT = "Checkelement";
    private final static String ATTRIBUTE_ROW = "rowID";
    private final static String ATTRIBUTE_COLUMN = "columnID";
    private final static String ATTRIBUTE_CHECKED = "checked";

    private final String[] columnIDs;
    private final transient BitField bitmap;

    /** Constructor for AbstractBitMap.
     *
     * @param inColumns org.hip.kernel.code.CodeList
     * @param inRows org.hip.kernel.bom.QueryResult
     * @param inContent org.hip.kernel.bom.QueryResult
     * @throws org.hip.kernel.exc.VException */
    protected AbstractBitMap(final CodeList inColumns, final QueryResult inRows, final QueryResult inContent)
            throws VException {
        super();
        columnIDs = loadColumnIDs(inColumns);
        bitmap = loadBitMap(initBitMap(inRows), inContent);
    }

    /** Constructor for AbstractBitMap.
     *
     * @param inColumnIDs java.lang.String
     * @param inBitField org.hip.kernel.bitmap.BitField */
    protected AbstractBitMap(final String[] inColumnIDs, final BitField inBitField) {
        super();
        columnIDs = inColumnIDs.clone();
        bitmap = inBitField;
    }

    /** Hook for loading the array of column IDs
     *
     * @param inColumns org.hip.kernel.code.CodeList */
    protected abstract String[] loadColumnIDs(CodeList inColumns);

    /** Hoof for initializing the bit field, i.e. loading the rows. Note: Initialization has to be separated from
     * loading, because the loading of the content requires an initialized bit field.
     *
     * @param inRows org.hip.kernel.bom.QueryResult
     * @return org.hip.kernel.bitmap.BitField
     * @throws org.hip.kernel.exc.VException */
    protected abstract BitField initBitMap(QueryResult inRows) throws VException;

    /** Hook for loading the bit field
     *
     * @param inBitField org.hip.kernel.bitmap.BitField
     * @param inContent org.hip.kernel.bom.QueryResult
     * @return org.hip.kernel.bitmap.BitField
     * @throws org.hip.kernel.exc.VException */
    protected abstract BitField loadBitMap(BitField inBitField, QueryResult inContent) throws VException;

    /** Accessor to the private column IDs for the subclasses.
     *
     * @return java.lang.String[] */
    protected String[] getColumnIDs() {
        final String[] out = new String[columnIDs.length];
        System.arraycopy(columnIDs, 0, out, 0, columnIDs.length);
        return out;
    }

    /** Accessor to the private bit field for the subclasses.
     *
     * @return org.hip.kernel.bitmap.BitField */
    protected BitField getBitField() {
        return bitmap;
    }

    protected int getColumnSize() { // NOPMD by lbenno
        return columnIDs.length;
    }

    protected int getRowSize() { // NOPMD by lbenno
        return bitmap.getRowSize();
    }

    /** Checks the bit in the specified row and column.
     *
     * @param inRow int
     * @param inColumn int
     * @return boolean */
    public boolean checkElementAt(final int inRow, final int inColumn) {
        return bitmap.getBit(inRow, inColumn);
    }

    /** Returns the row position of the specified row ID.
     *
     * @param inRowID java.lang.String
     * @return int */
    protected int rowIndexOf(final String inRowID) {
        for (int i = 0; i < getRowSize(); i++) {
            if (inRowID.equals(getRowID(bitmap.getObject(i)))) {
                return i;
            }
        }
        return -1;
    }

    /** Returns the column position of the specified column ID.
     *
     * @param inColumnID java.lang.String
     * @return int */
    protected int columnIndexOf(final String inColumnID) {
        for (int i = 0; i < columnIDs.length; i++) {
            if (columnIDs[i].equals(inColumnID)) {
                return i;
            }
        }
        return 0;
    }

    /** Adds a new row to the BitMap
     *
     * @param inObject java.lang.Object */
    protected void addNewRow(final Object inObject) {
        bitmap.addRow(inObject, new BitRowImpl(getColumnSize()));
    }

    /** Returns a string representation of this bit pattern.
     *
     * @param java.lang.String */
    @Override
    public String toString() {
        StringBuilder outXML = new StringBuilder(100).append(' ').append(TAG_ROOT).append(">\n"); // NOPMD by lbenno

        for (int i = 0; i < bitmap.getRowSize(); i++) {
            final Tuple lTuple = bitmap.getTuple(i);
            outXML.append("\t<").append(TAG_ROW).append(' ').append(ATTRIBUTE_ROW).append("='")
            .append(getRowID(lTuple.getTupleObject())).append("'>\n");

            for (int j = 0; j < columnIDs.length; j++) {
                outXML.append("\t\t<").append(TAG_COLUMN).append(' ').append(ATTRIBUTE_COLUMN).append("='")
                .append(columnIDs[i]).append("'>");
                outXML.append(checkElementAt(i, i) ? "1" : "0");
                outXML.append("</").append(TAG_COLUMN).append(">\n");
            } // for

            outXML.append("</").append(TAG_ROW).append(">\n");
        } // for

        outXML = new StringBuilder("</").append(TAG_ROOT).append(">");

        return new String(outXML);
    }

    /** Returns a string representation of this bit pattern in form of an XML.
     *
     * @param java.lang.String */
    public String toXML() {
        final StringBuilder outXML = new StringBuilder(100).append('<').append(TAG_ROOT).append(">\n");

        for (int i = 0; i < bitmap.getRowSize(); i++) {
            outXML.append("\t<").append(TAG_ROW).append(">\n");
            final Tuple lTuple = bitmap.getTuple(i);
            final String lRowID = getRowID(lTuple.getTupleObject());
            outXML.append(getRowAddToXML(lTuple.getTupleObject()));

            for (int j = 0; j < columnIDs.length; j++) {
                outXML.append("\t\t<").append(TAG_ELEMENT).append(' ').append(ATTRIBUTE_ROW).append("='")
                .append(lRowID).append("'  ").append(ATTRIBUTE_COLUMN).append("='").append(columnIDs[j])
                        .append("'  ").append(ATTRIBUTE_CHECKED).append("='")
                        .append(checkElementAt(i, j) ? ATTRIBUTE_CHECKED : "").append("'>"); // NOPMD by lbenno
                outXML.append("</").append(TAG_ELEMENT).append(">\n");
            } // for

            outXML.append("</").append(TAG_ROW).append(">\n");
        } // for

        outXML.append("</").append(TAG_ROOT).append('>');

        return new String(outXML);
    }

    /** Returns an XML part which is added to each row when the bitmap is serialized to XML.
     *
     * @param inObject java.lang.Object
     * @return java.lang.String */
    protected abstract String getRowAddToXML(Object inObject);

    /** Returns the ID of the tuple's object.
     *
     * @param inObject java.lang.Object
     * @return java.lang.String */
    protected abstract String getRowID(Object inObject);

    /** Returns the ID of the specified row.
     *
     * @param inRowPosition int
     * @return java.lang.String */
    protected String getRowID(final int inRowPosition) {
        return getRowID(bitmap.getObject(inRowPosition));
    }

    /** Removes the specified row of the bit field.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.Tuple */
    protected Tuple remove(final int inRowPosition) {
        return bitmap.removeRow(inRowPosition);
    }

    /** Returns a collection of checked positions of IDs of the specified row.
     *
     * @param inRowPosition int
     * @return org.hip.kernel.bitmap.IDPositions
     * @deprecated Use <code>{@link #getCheckedOfRow2(String)}</code> instead. */
    @Deprecated
    public IDPositions getCheckedOfRow(final String inRowID) {
        final IDPositions outPositions = new IDPositions();
        final int lRowPosition = rowIndexOf(inRowID);
        for (final Iterator<?> lPositions = bitmap.getCheckedOfRow(lRowPosition).iterator(); lPositions.hasNext();) {
            outPositions.add(inRowID, columnIDs[((MatrixPosition) lPositions.next()).getColumnPosition()]);
        }

        return outPositions;
    }

    /** Returns a collection of checked positions of IDs of the row with the specified ID.
     *
     * @param inRowID int
     * @return Collection<IDPosition> */
    public Collection<IDPosition> getCheckedOfRow2(final String inRowID) {
        final Collection<IDPosition> outPositions = new ArrayList<IDPosition>();
        final int lRowPosition = rowIndexOf(inRowID);
        for (final MatrixPosition lPosition : bitmap.getCheckedOfRow2(lRowPosition)) {
            outPositions.add(new IDPosition(inRowID, columnIDs[lPosition.getColumnPosition()]));
        }
        return outPositions;
    }

    /** Converts a collection of matrix positions to id positions.
     *
     * @param org.hip.kernel.bitmap.MatrixPositions
     * @return org.hip.kernel.bitmap.IDPositions
     * @deprecated Use <code>AbstractBitMap#convertPositions(Collection)</code> instead. */
    @Deprecated
    public IDPositions convertPositions(final MatrixPositions inMatrixPositions) {
        final IDPositions outPositions = new IDPositions();
        for (final Iterator<?> lPositions = inMatrixPositions.iterator(); lPositions.hasNext();) {
            final MatrixPosition lPosition = (MatrixPosition) lPositions.next();
            outPositions.add(new IDPosition(getRowID(lPosition.getRowPosition()), columnIDs[lPosition
                    .getColumnPosition()]));
        }
        return outPositions;
    }

    /** Converts a collection of matrix positions to a collection of ID positions.
     *
     * @param inMatrixPositions Collection<MatrixPosition>
     * @return Collection<IDPosition> */
    public Collection<IDPosition> convertPositions(final Collection<MatrixPosition> inMatrixPositions) {
        final Collection<IDPosition> outPositions = new ArrayList<IDPosition>();
        for (final MatrixPosition lPosition : inMatrixPositions) {
            outPositions.add(new IDPosition(getRowID(lPosition.getRowPosition()), columnIDs[lPosition
                    .getColumnPosition()]));
        }
        return outPositions;
    }

}
