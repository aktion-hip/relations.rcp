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
package org.hip.kernel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;

/** This is the default implementation for the name value support. */
@SuppressWarnings("serial")
public class DefaultNameValue extends AbstractNameValue {
    /** DefaultNameValue default constructor.
     *
     * @param inOwingList org.hip.kernel.util.NameValueList
     * @param inName java.lang.String
     * @param inValue java.lang.Object */
    public DefaultNameValue(final NameValueList inOwingList, final String inName, final Object inValue) {
        super(inOwingList, inName, inValue);
    }

    /** @param inName java.lang.String
     * @exception org.hip.kernel.util.VInvalidNameException */
    @Override
    protected void checkName(final String inName) throws VInvalidNameException {
        // intentionally left empty
    }

    /** @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    protected void checkValue(final Object inValue) throws VInvalidValueException {
        // intentionally left empty
    }

    /** The incoming string must have the form "&lt;name>=&lt;value>,".
     *
     * @return List&lt;NameValue> of NameValues extracted from the specified inSource
     * @param inSource java.lang.String */
    static public List<NameValue> extract(final String inSource) {

        final List<NameValue> outNameValues = new ArrayList<NameValue>();

        // Pre: inSource not null
        if (VSys.assertNotNull(DefaultNameValue.class, "extract", inSource) == Assert.FAILURE) {
            return outNameValues;
        }

        // We let the string tokenize
        final StringTokenizer lTokenizer = new StringTokenizer(inSource, ",");
        while (lTokenizer.hasMoreElements()) {
            final String lNext = lTokenizer.nextToken();
            final int lPosition = lNext.indexOf('=');
            final String lName = lNext.substring(0, Math.min(lPosition, lNext.length())).trim();
            final String lValue = lNext.substring(lPosition + 1).trim();
            final NameValue lNameValue = new DefaultNameValue(null, lName, lValue);
            outNameValues.add(lNameValue);
        }
        return outNameValues;
    }
}
