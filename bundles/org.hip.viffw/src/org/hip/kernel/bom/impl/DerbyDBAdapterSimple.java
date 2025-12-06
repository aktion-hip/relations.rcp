/**
This package is part of Relations project.
Copyright (C) 2007-2014, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.bom.impl;

import org.hip.kernel.bom.IValueForSQL;
import org.hip.kernel.bom.model.ObjectDef;

/** Implementation of <code>DBAdapterSimple</code> for Derby database systems. Derby needs double quotes to escape single
 * quotes in Strings.
 *
 * @author Luthiger Created on 22.07.2007 */
public class DerbyDBAdapterSimple extends DefaultDBAdapterSimple {

    /** DerbyDBAdapterSimple constructor.
     *
     * @param inObjectDef {@link ObjectDef} */
    public DerbyDBAdapterSimple(final ObjectDef inObjectDef) {
        super(inObjectDef);
    }

    /** Returns an instance of <code>ValueForSQLDoubleQuote</code>.
     *
     * @see org.hip.kernel.bom.impl.DefaultDBAdapterSimple#createValueForSQL(java.lang.Object) */
    @Override
    protected IValueForSQL createValueForSQL(final Object inValue) {
        return new ValueForSQLDoubleQuote(inValue);
    }

}
