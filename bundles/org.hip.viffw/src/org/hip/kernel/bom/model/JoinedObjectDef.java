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
package org.hip.kernel.bom.model;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.util.NameValueList;

/** The JoinedObjectDef holds meta information for a joined domain object.
 *
 * @author Benno Luthiger */
public interface JoinedObjectDef extends ModelObject {

    /** Adds a column def to the object definition.<br />
     * The purpose of this method is to retrieve an existing PropertyDef (with its table mapping) to reuse it for the
     * JoinedDomainObject, i.e. the SQL commands to retrieve the persistent data.<br />
     * The result of this method (i.e. the SQL command that will be created) is contingent on the existence of the
     * different attributes: columnName, alias, domainObject, nestedObject, valueType, modifier.<br />
     * 'columnName' is required.
     * <ul>
     * <li>if 'domainObject' exists => SQL: mappedTable.mappedColumnName</li>
     * <li>if 'nestedObject' existes => SQL: nestedObject.columnName</li>
     * <li>if 'domainObject' and 'nestedObject' => SQL: nestedObject.mappedColumnName</li>
     * <li>if 'alias' => SQL: (tab.col) AS alias</li>
     * <li>if 'modifier' => SQL modifier(tab.col)</li>
     * </ul>
     *
     * @param inColumnDefAttributes org.hip.kernel.util.NameValueList
     * @throws BOMException */
    void addColumnDef(NameValueList inColumnDefAttributes) throws BOMException;

    /** Adds a hidden column def to the object definition.
     *
     * @param inHiddenAttributes NameValueList */
    void addHidden(NameValueList inHiddenAttributes);

    /** Retrieves the column name out of the attributes of the colums definition.
     *
     * @return java.lang.String
     * @param inColumnDefAttributes org.hip.kernel.util.NameValueList */
    String getColumnName(NameValueList inColumnDefAttributes);

    /** Returns an ObjectDef holding the meta information for a domain object which is returned by the JOIN.
     *
     * @return org.hip.kernel.bom.model.ObjectDef */
    ObjectDef getDomainObjectDef();

    /** Retrieves a table name involved in the join.
     *
     * @return java.lang.String
     * @param inObjectClassName java.lang.String */
    String getTableName(String inObjectClassName);

    /** Sets the JoinDef of this JoinedObjectDef
     *
     * @param inJoinDef org.hip.kernel.bom.model.JoinDef */
    void setJoinDef(JoinDef inJoinDef);

    /** Returns the JoinDef of this JoinedObjectDef
     *
     * @return org.hip.kernel.bom.model.JoinDef
     * @throws org.hip.kernel.bom.BOMException */
    JoinDef getJoinDef() throws BOMException;

    /** Adds the specified NestedDef to the collection of NestedDefs.
     *
     * @param inNestedDef NestedDef */
    void addNestedDef(NestedDef inNestedDef);

    /** Returns the mapped column name ot the specified hidden column.
     *
     * @param inPropertyName String The column name according to the object def.
     * @return String The name of the mapped table column. */
    String getHidden(String inPropertyName);
}
