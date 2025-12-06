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

import java.util.Collection;

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

/** The NameValueList holds a list of name/value-pairs.
 *
 * @author Benno Luthiger */
public interface NameValueList extends java.io.Serializable {
    /** @param inNameValueListVisitor org.hip.kernel.util.NameValueListVisitor */
    void accept(NameValueListVisitor inNameValueListVisitor);

    /** This method adds a new item to the set.
     *
     * @param inNameValue org.hip.kernel.util.NameValue */
    void add(NameValue inNameValue);

    /** Returns the item with the specified name.
     *
     * @return org.hip.kernel.util.NameValue
     * @param inName java.lang.String */
    NameValue get(String inName);

    /** Returns a Collection view of the names contained in this <code>NameValueList</code> list.
     *
     * @return Collection<String> */
    Collection<String> getNames2();

    /** Returns a Collection view of the <code>NameValue</code>s contained in this <code>NameValueList</code> list.
     *
     * @return Collection<NameValue> */
    Collection<NameValue> getNameValues2();

    /** Returns the value for the given name.
     *
     * @return java.lang.Object
     * @param inName java.lang.String */
    Object getValue(String inName) throws VInvalidNameException;

    /** This method sets the value of the item with the specified name.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    void setValue(String inName, Object inValue) throws VInvalidNameException, VInvalidValueException;

    /** Returns count of nameValues added to this list.
     *
     * @return int */
    int size();

    /** Checks whether the list contains a value for the specified name.
     *
     * @param inName String
     * @return boolean */
    boolean hasValue(String inName);
}