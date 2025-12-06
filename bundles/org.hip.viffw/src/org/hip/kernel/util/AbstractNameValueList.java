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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** This is the base implementation for a name value list. */
@SuppressWarnings("serial")
abstract public class AbstractNameValueList extends VObject implements NameValueList, Serializable, Cloneable { // NOPMD

    // Instance variables
    private Map<String, NameValue> nameVals;

    /** @param inVisitor org.hip.kernel.util.NameValueListVisitor */
    @Override
    public void accept(final NameValueListVisitor inVisitor) {
        inVisitor.visitNameValueList(this);
    }

    /** This method adds a new item to the set.
     *
     * @param inNameValue org.hip.kernel.util.inNameValue */
    @Override
    public void add(final NameValue inNameValue) {

        // Pre: inNameValue not null
        if (VSys.assertNotNull(this, "add", inNameValue) == Assert.FAILURE) {
            return;
        }
        // Post: Value set
        inNameValue.setOwingList(this);
        nameValues().put(inNameValue.getName(), inNameValue);
    }

    /** This method must be implemented by concrete subclasses. They are responsible to create instances of the right
     * type.
     *
     * @return org.hip.kernel.util.NameValue
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    protected abstract NameValue create(String inName, Object inValue) throws VInvalidNameException,
            VInvalidValueException;

    /** @return boolean */
    protected abstract boolean dynamicAddAllowed();

    /** Returns the item with the specified name.
     *
     * @return org.hip.kernel.util.NameValue
     * @param inName java.lang.String */
    @Override
    public NameValue get(final String inName) {

        // Pre1: name not null
        if (VSys.assertNotNull(this, "get", inName) == Assert.FAILURE) {
            return null;
        }

        // Post:
        return nameValues().get(inName);
    }

    /** Returns a Collection view of the names contained in this <code>NameValueList</code> list.
     *
     * @return Collection<String> */
    @Override
    public Collection<String> getNames2() {
        final Collection<String> outNames = new ArrayList<String>();
        for (final NameValue lNameValue : nameValues().values()) {
            outNames.add(lNameValue.getName());
        }
        return outNames;
    }

    /** Returns a Collection view of the <code>NameValue</code>s contained in this <code>NameValueList</code> list.
     *
     * @return Collection<NameValue> */
    @Override
    public Collection<NameValue> getNameValues2() {
        return nameValues().values();
    }

    /** Checks whether the list contains a value for the specified name.
     *
     * @param inName String
     * @return boolean */
    @Override
    public boolean hasValue(final String inName) {
        return nameValues().containsKey(inName);
    }

    /** Setter vor nameValues hashtable.
     *
     * @param inNameValues java.util.Hashtable */
    protected void setNameValues(final Map<String, NameValue> inNameValues) {
        nameVals = inNameValues;
    }

    /** Returns the value for the given inName.<br />
     * Shortcut for <code>propertySet.get(inName).getValue()</code>
     *
     * @return java.lang.Object
     * @param inName java.lang.String
     * @exception org.hip.kernel.util.VInvalidNameException */
    @Override
    public Object getValue(final String inName) throws VInvalidNameException {

        // Pre: inName not null
        if (VSys.assertNotNull(this, "getValue", inName) == Assert.FAILURE) {
            return null;
        }

        // Pre: list must contain the element
        if (!nameValues().containsKey(inName)) {
            throw new VInvalidNameException(inName);

        }
        // Post: return value
        return nameValues().get(inName).getValue();
    }

    /** @return java.util.Hashtable */
    private Map<String, NameValue> nameValues() {
        if (nameVals == null) {
            synchronized (this) {
                nameVals = new HashMap<String, NameValue>(11);
            }
        }
        return nameVals;
    }

    /** This method sets the value of the item with the specified name.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    public void setValue(final String inName, final Object inValue) throws VInvalidNameException,
            VInvalidValueException {

        // Pre: inName not null
        if (VSys.assertNotNull(this, "setValue", inName) == Assert.FAILURE) {
            return;
        }

        // Post: set
        if (nameValues().containsKey(inName)) {
            nameValues().get(inName).setValue(inValue);
        }
        else {
            if (!dynamicAddAllowed()) {
                throw new VInvalidNameException(inName);
            }
            final NameValue lNameValue = create(inName, inValue);
            nameValues().put(lNameValue.getName(), lNameValue);
        }
    }

    /** Returns count of nameValues added to this list.
     *
     * @return int */
    @Override
    public int size() {
        return nameValues().size();
    }

    /** @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof NameValueList)) {
            return false;
        }

        if (size() != ((NameValueList) inObject).size()) {
            return false;
        }

        // check each NameValue in the list whether they are equal
        boolean isEqual = true;
        for (final NameValue lNameValue : getNameValues2()) {
            isEqual = lNameValue.equals(((NameValueList) inObject).get(lNameValue.getName()));
            if (!isEqual) {
                break;
            }
        }
        return isEqual;
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        final StringBuffer lMarkups = new StringBuffer();
        for (final NameValue lNameValue : getNameValues2()) {
            lMarkups.append("\t" + lNameValue.toString() + "\n");
        }

        return Debug.classMarkupString(this, "", new String(lMarkups));
    }

    /** Returns the HashCode calculated from the name.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outHashCode = 1;
        for (final NameValue lNameValue : getNameValues2()) {
            outHashCode ^= lNameValue.hashCode();
        }
        return outHashCode;
    }

    @Override
    public Object clone() throws CloneNotSupportedException { // NOPMD by lbenno
        final AbstractNameValueList outList = (AbstractNameValueList) super.clone();
        final Map<String, NameValue> clonedVals = new ConcurrentHashMap<String, NameValue>(); // NOPMD
        for (final Map.Entry<String, NameValue> entry : nameVals.entrySet()) {
            clonedVals.put(entry.getKey(), (NameValue) entry.getValue().clone());
        }
        outList.setNameValues(clonedVals);
        return outList;
    }
}