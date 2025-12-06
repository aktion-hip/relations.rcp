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

package org.hip.kernel.bom.impl;

import java.io.Serializable;

import org.hip.kernel.bom.ColumnModifier;
import org.hip.kernel.sys.VObject;

/** Default column modifier, doesn't anything.
 *
 * Created on 11.09.2002
 * 
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class DefaultColumnModifier extends VObject implements ColumnModifier, Serializable { // NOPMD by lbenno 

    /** This method returns the specified column name unmodified.
     * 
     * @param inColumnName java.lang.String
     * @return java.lang.String */
    @Override
    public String modifyColumn(final String inColumnName) {
        return inColumnName;
    }

}
