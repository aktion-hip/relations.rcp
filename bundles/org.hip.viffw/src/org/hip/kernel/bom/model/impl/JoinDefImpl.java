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

package org.hip.kernel.bom.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.model.JoinDef;
import org.hip.kernel.bom.model.JoinDefDef;
import org.hip.kernel.bom.model.JoinOperation;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.PlaceholderDef;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.util.Debug;

/** Implementation of the JoinDef interface
 *
 * @author: Benno Luthiger */
@SuppressWarnings("serial")
public class JoinDefImpl extends AbstractModelObject implements JoinDef {
    // member variables
    private final JoinOperation joinOperation;
    private JoinDef parentJoinDef;
    private JoinDef childJoinDef;

    /** JoinDefImpl default constructor. */
    public JoinDefImpl() {
        super();
        joinOperation = new JoinOperationImpl();
    }

    /** JoinDefImpl constructor with initial values. The array of the objects contains the names in the first column and
     * the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public JoinDefImpl(final Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);
        joinOperation = new JoinOperationImpl();
    }

    /** Sets the name of the tables this JoinDef is joining.
     *
     * @param inTableName java.lang.String */
    @Override
    public void setTableName(final String inTableName) {
        joinOperation.setTableName(inTableName);
    }

    /** Sets the columns the join is operating upon.
     *
     * @param inColumnName java.lang.String */
    @Override
    public void addColumnDef(final String inColumnName) {
        joinOperation.setJoinOperand(inColumnName);
    }

    /** Sets the nested query to the JoinOperation.
     *
     * @param inSQLQuery String */
    @Override
    public void setNestedQuery(final String inSQLQuery) {
        joinOperation.setTableName(inSQLQuery);
    }

    /** Fills the specified placeholder with the specified value.
     *
     * @param inAlias String The placeholder's id.
     * @param inSQL String SQL command string. */
    @Override
    public void fillPlaceholder(final String inAlias, final String inSQL) {
        joinOperation.fillPlaceholder(inAlias, inSQL);
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getJoinDefDef();
    }

    /** JoinDefs can be involved. This method sets a reference to the parent JoinDef.
     *
     * @param inJoinDef org.hip.kernel.bom.model.JoinDef */
    @Override
    public void setParentJoinDef(final JoinDef inJoinDef) {
        parentJoinDef = inJoinDef;
    }

    /** Returns the parent JoinDef if this JoinDef is involved.
     *
     * @return org.hip.kernel.bom.model.JoinDef */
    @Override
    public JoinDef getParentJoinDef() {
        return parentJoinDef;
    }

    /** JoinDefs can be involved. This method sets a reference to the child JoinDef.
     *
     * @param inJoinDef org.hip.kernel.bom.model.JoinDef */
    @Override
    public void setChildJoinDef(final JoinDef inJoinDef) {
        childJoinDef = inJoinDef;
    }

    /** Returns the child JoinDef if this JoinDef is involved.
     *
     * @return org.hip.kernel.bom.model.JoinDef */
    @Override
    public JoinDef getChildJoinDef() {
        return childJoinDef;
    }

    private boolean isInvolved() {
        return childJoinDef != null;
    }

    /** JoinDefs are equal if their join operations and operatorTypes are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof JoinDef)) {
            return false;
        }

        final JoinDef lJoinDef = (JoinDef) inObject;
        return getJoinOperations().equals(lJoinDef.getJoinOperations());
    }

    /** Returns a hash code value for the object def.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = joinOperation.hashCode();
        if (isInvolved()) {
            outCode ^= getChildJoinDef().hashCode();
        }
        return outCode;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        String lMessage = "";
        try {
            lMessage = "joinType=\"" + (String) get(JoinDefDef.joinType) + "\"";
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return Debug.classMarkupString(this, lMessage);
    }

    /** Returns a Collection containing all table names of this joined object def.
     *
     * @return java.util.Collection<String> */
    @Override
    public Collection<String> getTableNames() {
        final Set<String> outTables = new HashSet<String>();
        outTables.add(joinOperation.getLeftTableName());
        outTables.add(joinOperation.getRightTableName());
        if (isInvolved()) {
            outTables.addAll(childJoinDef.getTableNames());
        }
        return outTables;
    }

    /** Returns a Collection containing all join operations of this joined object def.
     *
     * @return java.util.Collection */
    @Override
    public Collection<JoinOperation> getJoinOperations() {
        final List<JoinOperation> outOperations = new ArrayList<JoinOperation>();
        checkJoinType();
        outOperations.add(joinOperation);
        if (isInvolved()) {
            outOperations.addAll(childJoinDef.getJoinOperations());
        }
        return outOperations;
    }

    private void checkJoinType() {
        if ("".equals(joinOperation.getJoinType())) {
            try {
                joinOperation.setJoinType((String) get(JoinDefDef.joinType));
            } catch (final GettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
                joinOperation.setJoinType(JoinDef.DFT_JOIN_TYPE);
            }
        }
    }

    /** Adds the specified PlaceholderDef to the collection of PlaceholderDefs.
     *
     * @param inPlaceholderDef PlaceholderDef */
    @Override
    public void addPlaceholderDef(final PlaceholderDef inPlaceholderDef) {
        joinOperation.addPlaceholderDef(inPlaceholderDef);
    }

    /** Checks whether this join includes the specified placeholder.
     *
     * @param inAlias String
     * @return boolean */
    @Override
    public boolean hasPlaceholder(final String inAlias) {
        return joinOperation.hasPlaceholder(inAlias);
    }

    @Override
    public void addJoinCondition(final String inOperatorType) { // NOPMD by lbenno
        joinOperation.addJoinCondition(inOperatorType);
    }

}