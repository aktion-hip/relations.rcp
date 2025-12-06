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

import org.hip.kernel.bom.model.GroupingDefDef;

/** Implements the GroupingDefDef interface.
 *
 * @author Benno Luthiger Created on Nov 29, 2003 */
@SuppressWarnings("serial")
public class GroupingDefDefImpl extends AbstractMetaModelObject implements GroupingDefDef { // NOPMD by lbenno
    private final static String[][] DEF = {
            { GroupingDefDef.modifier, "java.lang.String" }
            , { GroupingDefDef.columnDefs, "" } };

    /** Returns the meta information.
     *
     * @param Object[][] */
    @Override
    protected Object[][] getConstantDef() {
        return DEF; // NOPMD by lbenno
    }
}
