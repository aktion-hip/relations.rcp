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

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.RelationshipDef;
import org.hip.kernel.bom.model.RelationshipDefDef;
import org.hip.kernel.util.Debug;

/** This is the implementation of the RelationshipDef interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.RelationshipDef */
@SuppressWarnings("serial")
public class RelationshipDefImpl extends AbstractModelObject implements RelationshipDef { // NOPMD by lbenno

    /** @return java.lang.String */
    @Override
    public String getHomeName() {
        try {
            return (String) get(RelationshipDefDef.homeName);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** @return java.lang.String */
    @Override
    public String getKeyDefName() {
        try {
            return (String) get(RelationshipDefDef.keyDefName);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getRelationshipDefDef();
    }

    @Override
    public String toString() { // NOPMD by lbenno 
        return Debug.classMarkupString(this, RelationshipDefDef.keyDefName + "=\"" + getKeyDefName());
    }
}
