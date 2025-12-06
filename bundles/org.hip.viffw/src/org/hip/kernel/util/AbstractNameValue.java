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

import java.io.Serializable;

import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** This is the base implementation for the name value support.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
abstract public class AbstractNameValue extends VObject implements NameValue, Serializable, Cloneable {
    // Instance variables
    private NameValueList owingList;
    private String nameObj;
    private Object valueObj;

    /** AbstractNameValue constructor. Initializes name, value and the list the instance belongs to.
     *
     * @param inOwingList org.hip.kernel.util.NameValueList
     * @param inName java.lang.String
     * @param inValue java.lang.Object */
    public AbstractNameValue(final NameValueList inOwingList, final String inName, final Object inValue) {
        super();
        owingList = inOwingList;
        nameObj = inName;
        valueObj = inValue;
    }

    /** @param inVisitor org.hip.kernel.util.NameValueListVisitor */
    @Override
    public void accept(final NameValueListVisitor inVisitor) {
        inVisitor.visitNameValue(this);
    }

    /** @param inName java.lang.String
     * @exception org.hip.kernel.util.VInvalidNameException */
    protected abstract void checkName(String inName) throws VInvalidNameException;

    /** @param inNalue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidValueException */
    protected abstract void checkValue(Object inValue) throws VInvalidValueException;

    /** Returns the name.
     *
     * @return java.lang.String */
    @Override
    public String getName() {
        return nameObj;
    }

    /** Returns the list the instance is member from.
     *
     * @return org.hip.kernel.util.NameValueList */
    @Override
    public NameValueList getOwingList() {
        return owingList;
    }

    /** @return java.lang.Object */
    @Override
    public Object getValue() {
        return valueObj;
    }

    /** @return java.lang.String */
    protected String name() {
        return nameObj;
    }

    /** @param name java.lang.String */
    protected synchronized void name(final String inName) { // NOPMD by lbenno
        nameObj = inName;
    }

    /** Sets the value of the instance with the specified name.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    public void set(final String inName, final Object inValue) throws VInvalidNameException, VInvalidValueException {
        checkName(inName);
        checkValue(inValue);
    }

    /** @param name java.lang.String */
    @Override
    public void setName(final String inName) { // NOPMD by lbenno
        // intentionally left empty
    }

    /** @param inOwingList org.hip.kernel.util.NameValueList */
    @Override
    public void setOwingList(final NameValueList inOwingList) {

        // Pre: owingList not null
        if (VSys.assertNotNull(this, "setOwingList", inOwingList) == Assert.FAILURE) {
            return;
        }

        // Post: owing list set
        owingList = inOwingList;
    }

    /** Sets the value of the instance.
     *
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    public void setValue(final Object inValue) throws VInvalidValueException {
        // protected setter
        checkValue(inValue);
        value(inValue);
    }

    /** Gets value.
     *
     * @param value java.lang.Object */
    protected Object value() {
        return valueObj;
    }

    /** Protected setter of value.
     *
     * @param inValue java.lang.Object */
    protected synchronized void value(final Object inValue) { // NOPMD
        valueObj = inValue;
    }

    /** Returns the HashCode calculated from the name and value.
     *
     * @return int */
    @Override
    public int hashCode() {
        final int outHashCode = getValue() == null ? 0 : getValue().hashCode();
        return getName().hashCode() ^ outHashCode;
    }

    /** Returns true if inObject is a <code>NameValue</code> and its name and value are equal to this NameValue.
     *
     * @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {

        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof NameValue)) {
            return false;
        }

        final NameValue lInNameValue = (NameValue) inObject;
        if (lInNameValue.getName().equals(getName())) {
            // either both values have to be null
            if (lInNameValue.getValue() == null) {
                if (getValue() == null) {
                    return true;
                }
                return false;
            }
            // or both values have to be equal
            else {
                if (lInNameValue.getValue().equals(getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        final String lMessage = "name=\"" + getName() + "\" "
                + "value=\"" + ((getValue() == null) ? "null" : getValue().toString()) + "\"";
        return Debug.classMarkupString(this, lMessage);
    }

    @Override
    public Object clone() throws CloneNotSupportedException { // NOPMD
        final AbstractNameValue out = (AbstractNameValue) super.clone();
        out.value(CloneHelper.getCloned(getValue()));
        return out;
    }

}
