/**
    This package is part of the application VIF.
    Copyright (C) 2014, Benno Luthiger

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hip.kernel.util;

import org.hip.kernel.bom.impl.AbstractSemanticObject;
import org.hip.kernel.bom.impl.PropertySetImpl;
import org.hip.kernel.bom.model.impl.PropertyDefImpl;

/** @author lbenno */
public final class CloneHelper {

    private CloneHelper() {
        // prevent instantiation
    }

    /** Returns a cloned instance (i.e. a deep copy) of the specified input.
     *
     * @param inValue {@link Object} the instance to clone
     * @return {@link Object} the cloned instance if the class supports cloning
     * @throws CloneNotSupportedException */
    public static Object getCloned(final Object inValue) throws CloneNotSupportedException {
        Object out = inValue;
        if (out instanceof PropertyDefImpl) {
            out = ((PropertyDefImpl) out).clone();
        }
        else if (out instanceof PropertySetImpl) {
            out = ((PropertySetImpl) out).clone();
        }
        else if (out instanceof AbstractNameValueList) {
            out = ((AbstractNameValueList) out).clone();
        }
        else if (out instanceof AbstractSemanticObject) {
            out = ((AbstractSemanticObject) out).clone();
        }
        return out;
    }

}
