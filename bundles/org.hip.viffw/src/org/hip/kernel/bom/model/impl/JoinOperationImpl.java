/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2003-2014, Benno Luthiger

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

package org.hip.kernel.bom.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hip.kernel.bom.model.JoinOperation;
import org.hip.kernel.bom.model.PlaceholderDef;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.Debug;

/** JoinOperationImpl.java
 *
 * Created on 08.09.2002
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class JoinOperationImpl extends VObject implements JoinOperation, Serializable {
    // member variables
    private String joinType = "";
    private final Collection<JoinDefOperand> operands = new ArrayList<JoinDefOperand>();
    private JoinDefOperand currentOperand;
    private final String[] tables = { "", "" };
    private int currentTable;
    private Map<String, PlaceholderDef> placeholderDefs;

    /** Constructor for JoinOperationImpl. */
    public JoinOperationImpl() {
        super();
        currentTable = 0;
    }

    /** Sets the name of the tables this JoinDef is joining.
     *
     * @param inTableName java.lang.String */
    @Override
    public void setTableName(final String inTableName) {
        tables[currentTable] = inTableName;
        currentTable++;
    }

    /** @see org.hip.kernel.bom.model.JoinOperation#getLeftTableName() */
    @Override
    public String getLeftTableName() {
        return tables[0];
    }

    /** @see org.hip.kernel.bom.model.JoinOperation#getRightTableName() */
    @Override
    public String getRightTableName() {
        return tables[1];
    }

    /** @param inJoinOperand java.lang.String */
    @Override
    public void setJoinOperand(final String inJoinOperand) {
        currentOperand.setJoinOperand(inJoinOperand);
    }

    /** @param inJoinType java.lang.String */
    @Override
    public void setJoinType(final String inJoinType) {
        joinType = inJoinType;
    }

    /** @see org.hip.kernel.bom.model.JoinOperation#getJoinType() */
    @Override
    public String getJoinType() {
        return joinType;
    }

    /** Adds the specified PlaceholderDef to the collection of PlaceholderDefs.
     *
     * @param inPlaceholderDef PlaceholderDef */
    @Override
    public void addPlaceholderDef(final PlaceholderDef inPlaceholderDef) {
        inPlaceholderDef.setPosition(currentTable);
        setTableName(inPlaceholderDef.getName());
        getPlaceholderDefs().put(inPlaceholderDef.getName(), inPlaceholderDef);
    }

    private Map<String, PlaceholderDef> getPlaceholderDefs() {
        if (placeholderDefs == null) {
            placeholderDefs = new HashMap<String, PlaceholderDef>(3);
        }
        return placeholderDefs;
    }

    /** Fills the specified placeholder with the specified value.
     *
     * @param inAlias String The placeholder's id.
     * @param inSQL String SQL command string. */
    @Override
    public void fillPlaceholder(final String inAlias, final String inSQL) {
        final Object lPlaceholder = placeholderDefs.get(inAlias);
        if (lPlaceholder == null) {
            return;
        }

        tables[((PlaceholderDef) lPlaceholder).getPosition()] = inSQL;
    }

    /** Checks whether this join includes the specified placeholder.
     *
     * @param inAlias String
     * @return boolean */
    @Override
    public boolean hasPlaceholder(final String inAlias) {
        if (placeholderDefs == null) {
            return false;
        }

        final Object lPlaceholder = placeholderDefs.get(inAlias);
        if (lPlaceholder == null) {
            return false;
        }

        return inAlias.equals(((PlaceholderDef) lPlaceholder).getName());
    }

    @Override
    public void addJoinCondition(final String inOperatorType) { // NOPMD by lbenno 
        currentOperand = inOperatorType == null ? new JoinDefOperand() : new JoinDefOperand(inOperatorType);
        operands.add(currentOperand);
    }

    @Override
    public String renderSQL() { // NOPMD by lbenno 
        final StringBuilder out = new StringBuilder();
        for (final JoinDefOperand lOperand : operands) {
            out.append(lOperand.renderSQL());
        }
        return new String(out);
    }

    /** JoinOperations are equal if their attributes are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof JoinOperation)) {
            return false;
        }

        final JoinOperation lJoinOperation = (JoinOperation) inObject;
        return getLeftTableName().equals(lJoinOperation.getLeftTableName()) &&
                getRightTableName().equals(lJoinOperation.getRightTableName()) &&
                renderSQL().equals(lJoinOperation.renderSQL()) &&
                getJoinType().equals(lJoinOperation.getJoinType());
    }

    /** Returns a hash code value for the JoinOperation.
     *
     * @return int */
    @Override
    public int hashCode() {
        return getLeftTableName().hashCode() ^ getRightTableName().hashCode() ^
                operands.hashCode() ^ getJoinType().hashCode();
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final String lMessage = "left_tablename=\"" + getLeftTableName() + "\", " +
                "right_tablename=\"" + getRightTableName() + "\", " +
                "join_type=\"" + getJoinType() + "\", " +
                "SQL [" + renderSQL() + "]";
        return Debug.classMarkupString(this, lMessage);
    }

}