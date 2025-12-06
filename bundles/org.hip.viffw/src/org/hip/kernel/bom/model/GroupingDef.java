/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2003-2014, Benno Luthiger

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public
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

/** This interface defines the behaviour of grouping object in nested domain objects.
 *
 * @author Benno Luthiger Created on Nov 29, 2003 */
public interface GroupingDef extends ModelObject {
    String[][] groupingTypes = { // NOPMD by lbenno
            { "group", "GROUP BY" },
            { "order", "ORDER BY" }
    };

    /** Sets the columns the grouping is operating upon.
     *
     * @param inColumnDefAttributes org.hip.kernel.util.NameValueList
     * @throws BOMException */
    void addColumnDef(NameValueList inColumnDefAttributes) throws BOMException;

    /** Returns the modifier type of this grouping, e.g. GROUP BY or ORDER BY.
     *
     * @return String */
    String getModifier();

    /** Creates the SQL grouping expression, i.e. "GROUP BY table.Column"
     *
     * @return String The SQL grouping expression */
    String getGroupingExpression();
}
