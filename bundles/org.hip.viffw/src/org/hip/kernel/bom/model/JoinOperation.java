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

/** Interface for a join operation.<br/>
 * Helper to create the part of the SQL statement:<br/>
 * 
 * <pre>
 * tblTextVersion INNER JOIN tblTextAuthorReviewer ON tblTextVersion.TEXTID = tblTextAuthorReviewer.TEXTID
 * </pre>
 * 
 * <pre>
 * leftTable JoinType rightTable ON leftOperand = rightOperand
 * </pre>
 *
 * Created on 08.09.2002
 * 
 * @author Benno Luthiger */
public interface JoinOperation {
    /** @param inTableName java.lang.String */
    void setTableName(String inTableName);

    /** @return java.lang.String */
    String getLeftTableName();

    /** @return java.lang.String */
    String getRightTableName();

    /** @param inJoinOperand java.lang.String */
    void setJoinOperand(String inJoinOperand);

    /** @param inJoinType java.lang.String */
    void setJoinType(String inJoinType);

    /** @return java.lang.String */
    String getJoinType();

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

    /** Adds a new join condition part with the specified operator type.
     * 
     * @param inOperatorType String <code>AND | OR</code>, may be <code>null</code> */
    void addJoinCondition(String inOperatorType);

    /** Renders this join operation for SQL.
     * 
     * @return String */
    String renderSQL();
}
