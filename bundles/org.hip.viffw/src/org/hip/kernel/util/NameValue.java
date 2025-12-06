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
package org.hip.kernel.util;

/** This is a generic interface for name/value pairs. Within most system name/value pairs and lists of name/value pairs
 * are used. A key-element may be defined as name-value pair etc.
 *
 * @author Benno Luthiger */
public interface NameValue extends java.io.Serializable, Cloneable {

    /** @param inNameValueListVisitor org.hip.kernel.util.NameValueListVisitor */
    void accept(NameValueListVisitor inNameValueListVisitor);

    /** Returns the name of the name value pair.
     *
     * @return java.lang.String */
    String getName();

    /** Returns the list the instance is member from.
     *
     * @return org.hip.kernel.util.NameValueList */
    NameValueList getOwingList();

    /** Returns the value of the name value pair.
     *
     * @return java.lang.Object */
    Object getValue();

    /** Sets the name and value.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    void set(String inName, Object inValue) throws VInvalidValueException, VInvalidNameException;

    /** Sets the name of the name/value pair.
     *
     * @param inName java.lang.String */
    void setName(String inName) throws VInvalidNameException;

    /** @param inOwingList org.hip.kernel.util.NameValueList */
    void setOwingList(NameValueList inOwingList);

    /** Sets the value of the name/value pair.
     *
     * @param inValue java.lang.Object */
    void setValue(Object inValue) throws VInvalidValueException;

    /** Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @throws CloneNotSupportedException */
    Object clone() throws CloneNotSupportedException; // NOPMD

}
