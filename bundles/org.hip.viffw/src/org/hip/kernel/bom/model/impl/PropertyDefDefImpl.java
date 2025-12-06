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

import org.hip.kernel.bom.model.PropertyDefDef;

/** Implements the PropertyDefDef interface
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.PropertyDefDef */
@SuppressWarnings("serial")
public class PropertyDefDefImpl extends AbstractMetaModelObject implements PropertyDefDef { // NOPMD by lbenno 

    private final static Object[][] DEF = {
            { PropertyDefDef.propertyName, "java.lang.String" } // NOPMD by lbenno 
            , { PropertyDefDef.propertyType, "java.lang.String" }
            , { PropertyDefDef.valueType, "java.lang.String" }
            , { PropertyDefDef.formatPattern, "java.lang.String" }
            , { PropertyDefDef.mappingDef, "org.hip.kernel.bom.model.MappingDef" }
            , { PropertyDefDef.relationshipDef, "org.hip.kernel.bom.model.RelationshipDef" }
    };

    /** Returns the meta information.
     *
     * @return java.lang.Object[][] */
    @Override
    protected Object[][] getConstantDef() {
        return DEF; // NOPMD by lbenno 
    }
}
