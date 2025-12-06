/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006-2014, Benno Luthiger

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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.hip.kernel.bom.IValueForSQL;

/** This helper class is used to convert a value to a form which can easily be used in a SQL statement.
 *
 * @author Luthiger Created: 14.10.2006 */
public class ValueForSQL implements IValueForSQL {
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // NOPMD by lbenno
    private final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOPMD by lbenno

    private transient String delimiter = "";
    private transient String valueAsString = "";
    private transient String valueForPrepared = "?";

    /** @param inValue The object to format. */
    public ValueForSQL(final Object inValue) {
        super();
        initialize(inValue);
    }

    /** @param inValue java.lang.Object */
    private void initialize(final Object inValue) {
        delimiter = "'";

        if (inValue == null) {
            valueAsString = "NULL";
            delimiter = "";
        }
        else if (inValue instanceof Date) {
            valueAsString = "DATE(\'" + dateFormat.format((java.util.Date) inValue) + "\')";
            delimiter = "";
        }
        else if (inValue instanceof Timestamp) {
            valueAsString = "TIMESTAMP(\'" + timeFormat.format((java.util.Date) inValue) + "\')";
            delimiter = "";
        }
        else if (inValue instanceof Number) {
            valueAsString = inValue.toString();
            delimiter = "";
        }
        else if (inValue instanceof SQLRange) {
            valueAsString = inValue.toString();
            valueForPrepared = ((SQLRange) inValue).toPrepared();
            delimiter = "";
        }
        else if (inValue instanceof Collection<?>) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final InObjectImpl<?> lValue = new InObjectImpl((Collection<?>) inValue);
            valueAsString = lValue.toString();
            valueForPrepared = lValue.toPrepared();
            delimiter = "";
        }
        else if (inValue instanceof SQLNull) {
            valueAsString = "NULL";
            delimiter = "";
        }
        else {
            valueAsString = checkString((String) inValue);
        }
    }

    /** Helper method to escape input.
     *
     * @param inValue String
     * @return String the escaped input */
    protected String checkString(final String inValue) {
        final char[] lRepl = { '\\', '\\', '\'' };
        return inValue.replaceAll("\'", new String(lRepl));
    }

    @Override
    public String getDelimiter() { // NOPMD by lbenno
        return delimiter;
    }

    @Override
    public String getValueAsString() { // NOPMD by lbenno
        return valueAsString;
    }

    @Override
    public String getValueForPrepared() { // NOPMD by lbenno
        return valueForPrepared;
    }

}
