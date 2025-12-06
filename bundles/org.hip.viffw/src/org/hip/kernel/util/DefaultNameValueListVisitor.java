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

/** This is the default implementation for a visitor pattern to a name value list. */
public class DefaultNameValueListVisitor extends AbstractNameValueListVisitor { // NOPMD by lbenno

    /** @param inNameValue org.hip.kernel.util.NameValue */
    @Override
    public void visitNameValue(final NameValue inNameValue) {
        final String lName = inNameValue.getName() == null ? "?" : inNameValue.getName();
        String lValue;
        final Object lValueObject = inNameValue.getValue();
        if (lValueObject == null) {
            lValue = "<value=?/>";
        }
        else {
            lValue = lValueObject.toString();
            lValue = Debug.classMarkupString(lValueObject, "'" + lValue + "'");
        }
        externalized += Debug.classMarkupString(inNameValue, "name='" + lName + "'", lValue);
    }

    /** @param inNameValueList org.hip.kernel.util.NameValueList */
    @Override
    public void visitNameValueList(final NameValueList inNameValueList) {
        final String lStore = externalized;
        externalized = "";
        for (final NameValue lNameValue : inNameValueList.getNameValues2()) {
            lNameValue.accept(this);
            externalized += "\n";
        }
        externalized = lStore
                + Debug.classMarkupString(inNameValueList, "", "".equals(externalized) ? "\t<values=?/>" : externalized);
    }

    /** @param inSortableItem org.hip.kernel.util.SortableItem */
    @Override
    public void visitSortableItem(final SortableItem inSortableItem) {
        String lValueString;
        final Object lValue = inSortableItem.getValue();
        if (lValue == null) {
            lValueString = "<value=?/>";
        }
        else {
            lValueString = "<value='" + lValue.toString() + "' />";
        }
        externalized += Debug.classMarkupString(inSortableItem, lValueString);
    }

    /** @param inSortedList org.hip.kernel.util.SortedList */
    @Override
    public void visitSortedList(final SortedList inSortedList) {
        final String lStore = externalized;
        externalized = "";
        for (final SortableItem lItem : inSortedList.getItems2()) {
            lItem.accept(this);
            externalized += "\n";
        }
        externalized = lStore
                + Debug.classMarkupString(inSortedList, "", "".equals(externalized) ? "\t<values=?/>" : externalized);
    }

    /** Clears the Visitor. If an instance of this Visitor is reused, it has to be cleared before reusing. */
    @Override
    public void clear() {
        externalized = "";
    }
}
