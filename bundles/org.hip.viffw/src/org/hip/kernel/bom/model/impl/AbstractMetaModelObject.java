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

import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.impl.AbstractSemanticObject;
import org.hip.kernel.bom.impl.PropertyImpl;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.util.Debug;

/** This is the base abstract implementation of the MetaModelObject interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.MetaModelObject */
@SuppressWarnings("serial")
abstract public class AbstractMetaModelObject extends AbstractSemanticObject implements MetaModelObject { // NOPMD

    /** Returns the meta information.
     *
     * @return java.lang.Object[][] */
    abstract protected Object[][] getConstantDef();

    /** Initializes a new PropertySet based on the meta information.
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    public synchronized void initializePropertySet(final PropertySet inSet) { // NOPMD by lbenno

        final Object[][] lDef = getConstantDef();

        for (int i = 0; i < lDef.length; i++) {
            inSet.add(new PropertyImpl(inSet, (String) lDef[i][0], lDef[i][1]));
        }
    }

    /** @return boolean */
    @Override
    public boolean isDynamicAddAllowed() {
        return false;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return Debug.classMetaModelMarkupString(this, getConstantDef());
    }
}
