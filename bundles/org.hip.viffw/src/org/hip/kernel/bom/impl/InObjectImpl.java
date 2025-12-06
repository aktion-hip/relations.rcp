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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/** Key object to specify value ranges, e.g. <code>value IN (val1, val2, val3)</code>
 *
 * @author Luthiger Created: 14.10.2006 */
public class InObjectImpl<T> implements SQLRange {
    private transient final Collection<T> values;
    private transient ValueConverter converter;

    /** InObjectImpl constructor
     *
     * @param inValues T[] the values that make up the range. */
    public InObjectImpl(final T[] inValues) { // NOPMD by lbenno
        values = new ArrayList<T>();
        Collections.addAll(values, inValues);
    }

    /** InObjectImpl constructor
     *
     * @param inValues {@link Collection} of the values that make up the range. */
    public InObjectImpl(final Collection<T> inValues) {
        values = inValues;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder out = new StringBuilder("IN (");
        if (values.isEmpty()) {
            return new String(out.append("0)"));
        }

        boolean lFirst = true;
        for (final T lValue : values) {
            if (!lFirst) {
                out.append(", ");
            }
            lFirst = false;
            out.append(getConverter(lValue).convert(lValue));
        }
        out.append(')');
        return new String(out);
    }

    /** @return String the representation in a prepared SQL command. */
    @Override
    @SuppressWarnings("unused")
    public String toPrepared() {
        final StringBuilder out = new StringBuilder("IN (");

        boolean lFirst = true;
        for (final T lValue : values) {
            if (!lFirst) {
                out.append(", ");
            }
            lFirst = false;
            out.append('?');
        }
        out.append(')');
        return new String(out);
    }

    /** @param inConverter {@link ValueConverter} */
    public void setConverter(final ValueConverter inConverter) {
        converter = inConverter;
    }

    private ValueConverter getConverter(final T inValue) {
        if (converter != null) {
            return converter;
        }

        if (inValue instanceof String) {
            return new StringValueConverter();
        } else if (inValue instanceof Number) {
            return new NumberValueConverter();
        }
        return new StringValueConverter();
    }

    // ---

    /** Interface for value converters.
     *
     * @author lbenno */
    private interface ValueConverter {
        /** @param inValue Object to convert
         * @return String the converted value */
        String convert(Object inValue);
    }

    private static class StringValueConverter implements ValueConverter { // NOPMD by lbenno
        @Override
        public String convert(final Object inValue) { // NOPMD by lbenno
            return String.format("\'%s\'", inValue.toString());
        }
    }

    private static class NumberValueConverter implements ValueConverter { // NOPMD by lbenno
        @Override
        public String convert(final Object inValue) { // NOPMD by lbenno
            return inValue.toString();
        }
    }

}
