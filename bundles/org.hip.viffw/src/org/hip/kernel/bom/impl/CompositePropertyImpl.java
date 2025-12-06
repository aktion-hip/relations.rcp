/**
  	This package is part of the framework used for the application VIF.
	Copyright (C) 2006, Benno Luthiger

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

import org.hip.kernel.bom.CompositeProperty;
import org.hip.kernel.bom.PropertySet;

/** Implementation of composite property to hold composite objects (i.e. list of objects).
 *
 * @author Luthiger Created on 02.08.2007
 * @see CompositeProperty */
@SuppressWarnings("serial")
public class CompositePropertyImpl extends PropertyImpl implements CompositeProperty {

    /** @param inSet
     * @param inName */
    public CompositePropertyImpl(final PropertySet inSet, final String inName) {
        super(inSet, inName);
    }

    /** @param inSet
     * @param inName
     * @param inValue */
    public CompositePropertyImpl(final PropertySet inSet, final String inName, final Object inValue) {
        super(inSet, inName, inValue);
    }

}
