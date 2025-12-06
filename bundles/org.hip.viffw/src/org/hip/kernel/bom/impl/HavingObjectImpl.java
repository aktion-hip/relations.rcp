/**
 This package is part of the framework used for the application VIF.
 Copyright (C) 2005-2014, Benno Luthiger

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
package org.hip.kernel.bom.impl;

import org.hip.kernel.bom.HavingObject;

/** Implements the HavingObject interface. HavingObjects are used to specify SQL select_where items in SQL HAVING clause.
 *
 * @author Benno Luthiger Created on Apr 13, 2005
 * @see org.hip.kernel.bom.HavingObject */
@SuppressWarnings("serial")
public class HavingObjectImpl extends KeyObjectImpl implements HavingObject { // NOPMD by lbenno

    /** Compares all items of HavingObjects.
     *
     * @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) { // NOPMD by lbenno 
        // pre
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof HavingObject)) {
            return false;
        }

        return super.equals(inObject);
    }

}
