/**
	This package is part of the framework used for the application VIF.
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
package org.hip.kernel.bom.model;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.util.NameValueList;

/** This interface defines the behavior of nested objects in joined domain objects.
 *
 * @author Benno Luthiger Created on Nov 29, 2003 */
public interface NestedDef extends ModelObject {

    /** Sets the columns the nested object is made of.
     *
     * @param inColumnDefAttributes NameValueList
     * @throws BOMException */
    void addColumnDef(NameValueList inColumnDefAttributes) throws BOMException;

    /** Sets the grouping applied to the result set.
     *
     * @param inGroupingDef GroupingDef */
    void addGroupingDef(GroupingDef inGroupingDef);

    /** Creates and returns the nested query.
     *
     * @return String The nested query. */
    String getNestedQuery();

    /** Returns the nestings name, i.e. the alias of the nested query.
     *
     * @return String */
    String getName();

    /** Returns the SQL name of the column mapped to the <columnDef> entry with the specified columnName attribute.
     *
     * @param inColumnName String
     * @return String */
    String getSQLColumnName(String inColumnName);
}
