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
package org.hip.kernel.bom.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SemanticObject;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.AbstractNameValueList;
import org.hip.kernel.util.Debug;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** This class implements the interface SemanticObject.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.SemanticObject */
@SuppressWarnings("serial")
abstract public class AbstractSemanticObject extends VObject implements SemanticObject, Serializable, Cloneable { // NOPMD
    // Instance variables
    private PropertySet properties;

    /** SemanticObjects are equal if all of their name-values are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof SemanticObject)) {
            return false;
        }

        final SemanticObject lObjectToCompare = (SemanticObject) inObject;
        try {
            for (final String lName : getPropertyNames2()) {
                final Object lValue = get(lName);
                if (lValue == null) {
                    if (lObjectToCompare.get(lName) != null) {
                        return false;
                    }
                } else {
                    if (!lValue.equals(lObjectToCompare.get(lName))) {
                        return false;
                    }
                }
            }
        } catch (final GettingException exc) {
            return false;
        }
        return true;
    }

    /** Returns the Value to a given Name.
     *
     * @return java.lang.Object
     * @param inName java.lang.String
     * @exception org.hip.kernel.bom.GettingException */
    @Override
    public Object get(final String inName) throws GettingException {
        try {
            return propertySet().getValue(inName);
        } catch (final VInvalidNameException exc) {
            throw new GettingException("InvalidName: " + inName, exc);
        }
    }

    /** Returns an Iterator of all property names.
     *
     * @return java.util.Iterator
     * @deprecated Use {@link SemanticObject#getPropertyNames2()} instead. */
    @Deprecated
    @Override
    public Iterator<String> getPropertyNames() {
        return propertySet().getNames2().iterator();
    }

    /** Returns a Collection view of all property names.
     *
     * @return Collection<String> */
    @Override
    public Collection<String> getPropertyNames2() { // NOPMD by lbenno
        return propertySet().getNames2();
    }

    /** Returns a hash code value for the semantic object.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 1;
        try {
            for (final String lName : getPropertyNames2()) {
                outCode ^= lName.hashCode() ^ (get(lName) == null ? 0 : get(lName).hashCode());
            }
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return outCode;
    }

    /** A hook-method for subclasses to initialize the property set.
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    protected abstract void initializePropertySet(PropertySet inSet);

    /** @return boolean */
    protected abstract boolean isDynamicAddAllowed();

    /** Accessor to the property set.
     *
     * @return org.hip.kernel.bom.PropertySet */
    @Override
    public PropertySet propertySet() {
        synchronized (this) {
            if (properties == null) {
                properties = new PropertySetImpl(this);
                initializePropertySet(properties);
            }
        }
        return properties;
    }

    /** This method sets the value of the specified Property
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exceptions org.hip.kernel.bom.SettingException */
    @Override
    public void set(final String inName, final Object inValue) throws SettingException {
        final Property lProperty = (Property) propertySet().get(inName);
        if (lProperty == null) {
            if (isDynamicAddAllowed()) {
                propertySet().add(new PropertyImpl(propertySet(), inName, inValue));
            } else {
                throw new SettingException("Invalid name: " + inName);
            }
        } else {
            try {
                lProperty.setValue(inValue);
            } catch (final VInvalidValueException exc) {
                throw new SettingException(exc.getMessage(), exc);
            } // catch
        } // if-else
    }

    /** Used to set an existing instance to an initial state for that it can be reused. */
    @Override
    public void setVirgin() {
        propertySet().setVirgin();
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lMessage = new StringBuilder(50);
        try {
            for (final String lName : getPropertyNames2()) {
                lMessage.append("\t<name=\"").append(lName).append("\" value=\"")
                        .append(get(lName) == null ? "null" : get(lName).toString())
                        .append("\"/>\n");
            }
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return Debug.classMultilineMarkupString(this, new String(lMessage));
    }

    @Override
    public Object clone() throws CloneNotSupportedException { // NOPMD
        final AbstractSemanticObject outObj = (AbstractSemanticObject) super.clone();
        outObj.properties = (PropertySet) ((AbstractNameValueList) properties).clone();
        return outObj;
    }
}