/**
	This package is part of the framework used for the application VIF.
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
package org.hip.kernel.bom.model;

import java.util.Collection;

/** The interface JoinDef defines the behaviour of join definition objects in joined domain objects.
 *
 * @author Benno Luthiger */
public interface JoinDef extends ModelObject {
    String DFT_JOIN_TYPE = "EQUI_JOIN";
    String NO_JOIN_TYPE = "NO_JOIN";

    /** Sets the columns the join is operating upon.
     *
     * @param inColumnName java.lang.String */
    void addColumnDef(String inColumnName);

    /** Returns the child JoinDef if this JoinDef is involved.
     *
     * @return org.hip.kernel.bom.model.JoinDef */
    JoinDef getChildJoinDef();

    /** Returns the parent JoinDef if this JoinDef is involved.
     *
     * @return org.hip.kernel.bom.model.JoinDef */
    JoinDef getParentJoinDef();

    /** JoinDefs can be involved. This method sets a reference to the child JoinDef.
     *
     * @param inJoinDef org.hip.kernel.bom.model.JoinDef */
    void setChildJoinDef(JoinDef inJoinDef);

    /** JoinDefs can be involved. This method sets a reference to the parent JoinDef.
     *
     * @param inJoinDef org.hip.kernel.bom.model.JoinDef */
    void setParentJoinDef(JoinDef inJoinDef);

    /** Sets the name of the tables this JoinDef is joining.
     *
     * @param inTableName java.lang.String */
    void setTableName(String inTableName);

    /** Returns a Collection containing all table names of this joined object def.
     *
     * @return java.util.Collection */
    Collection<String> getTableNames();

    /** Returns a Collection containing all join operations of this joined object def.
     *
     * @return java.util.Collection */
    Collection<JoinOperation> getJoinOperations();

    /** Sets the nested query to the JoinOperation.
     *
     * @param inSQLQuery String */
    void setNestedQuery(String inSQLQuery);

    /** Adds the specified PlaceholderDef to the collection of PlaceholderDefs.
     *
     * @param inPlaceholderDef PlaceholderDef */
    void addPlaceholderDef(PlaceholderDef inPlaceholderDef);

    /** Checks whether this join includes the specified placeholder.
     *
     * @param inAlias String
     * @return boolean */
    boolean hasPlaceholder(String inAlias);

    /** Fills the specified placeholder with the specified value.
     *
     * @param inAlias String The placeholder's id.
     * @param inSQL String SQL command string. */
    void fillPlaceholder(String inAlias, String inSQL);

    /** Adds join condition to this JoinDef
     *
     * @param inOperatorType String "AND" or "OR", may be <code>null</code> */
    void addJoinCondition(String inOperatorType);
}
