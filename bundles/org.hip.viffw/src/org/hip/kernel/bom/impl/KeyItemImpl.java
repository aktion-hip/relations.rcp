/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2001, Benno Luthiger

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

import org.hip.kernel.bom.KeyItem;
import org.hip.kernel.util.AbstractNameValue;
import org.hip.kernel.util.NameValueList;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** This is the default implementation of the KeyItem interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.KeyItem */
@SuppressWarnings("serial")
public class KeyItemImpl extends AbstractNameValue implements KeyItem {

    /** KeyItemImpl constructor with NameValueList as owing list.
     *
     * @param inKey org.hip.kernel.util.NameValueList
     * @param inName java.lang.String */
    public KeyItemImpl(final NameValueList inKey, final String inName) {
        super(inKey, inName, null);
    }

    /** @param name java.lang.String
     * @exception org.hip.kernel.util.VInvalidNameException */
    @Override
    protected void checkName(final String name) throws VInvalidNameException {
        // Actually no constraints defined
    }

    /** @param value java.lang.Object
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    protected void checkValue(final Object value) throws VInvalidValueException {
        // Actually no constraints defined
    }
}
