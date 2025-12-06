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

import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.ObjectRefProperty;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SemanticObject;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.TypeDef;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.util.AbstractNameValue;
import org.hip.kernel.util.CloneHelper;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** This is the implementation of the Property interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.Property */
@SuppressWarnings("serial")
public class PropertyImpl extends AbstractNameValue implements Property {
    // Instance variables
    private PropertyDef propertyDef;
    private Object initial;
    private Object current;
    private boolean changed;
    private boolean initialized;
    private boolean doNotifyInit;
    private Class<?> typeInformation;

    /** PropertyImpl constructor without value.
     *
     * @param inSet {@link PropertySet} the owning set
     * @param inName java.lang.String */
    public PropertyImpl(final PropertySet inSet, final String inName) {
        this(inSet, inName, null);
        initialized = false;
    }

    /** PropertyImpl constructor which initializes with name and value.
     *
     * @param inSet {@link PropertySet} the owning set
     * @param inName java.lang.String
     * @param inValue java.lang.Object */
    public PropertyImpl(final PropertySet inSet, final String inName, final Object inValue) {
        super(inSet, inName, inValue);
        this.value(inValue);
    }

    /** PropertyImpl constructor which initializes with name and value. The Property also has typeInformation to check
     * the value.
     *
     * @param inSet {@link PropertySet} the owning set
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @param inTypeInformation java.lang.String
     * @throws org.hip.kernel.util.VInvalidValueException */
    public PropertyImpl(final PropertySet inSet, final String inName, final Object inValue,
            final String inTypeInformation) throws VInvalidValueException {
        super(inSet, inName, inValue);
        if (inTypeInformation.length() > 0) {
            try {
                typeInformation = Class.forName(inTypeInformation);
            } catch (final ClassNotFoundException exc) {
                if (!TypeDef.Binary.equals(inTypeInformation)) {
                    // We trace nothing in the case of Binary.
                    DefaultExceptionWriter.printOut(this, exc, true);
                }
            }
        }
        this.setValue(inValue);
    }

    /** Implements visitor pattern for this class.
     *
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    @Override
    public void accept(final DomainObjectVisitor inVisitor) {
        inVisitor.visitProperty(this);
    }

    /** @param inName java.lang.String
     * @exception org.hip.kernel.util.VInvalidNameException */
    @Override
    protected void checkName(final String inName) throws VInvalidNameException {
        if (inName == null) {
            throw new VInvalidNameException("Name of Property must not be null");
        }
        if (!inName.equals(name())) {
            throw new VInvalidNameException("Invalid Name " + inName);
        }
    }

    /** @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    protected void checkValue(final Object inValue) throws VInvalidValueException {
        if (typeInformation == null) {
            return;
        }
        if (inValue == null) {
            return;
        }
        if (!typeInformation.isInstance(inValue)) {
            throw new VInvalidValueException("Invalid Type " + inValue.getClass().getName());
        }
    }

    /** @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof Property)) {
            return false;
        }
        return this.getName().equals(((Property) inObject).getName());
    }

    /** @return java.lang.String */
    @Override
    public String getFormatPattern() {
        if (propertyDef == null) {
            return null;
        }
        try {
            return (String) getPropertyDef().get("formatPattern");
        } catch (final GettingException exc) {
            return null;
        }
    }

    /** Returns the SemanticObject in the OwingList the instance belongs to.
     *
     * @return org.hip.kernel.bom.SemanticObject */
    @Override
    public SemanticObject getOwingObject() {
        return ((PropertySet) getOwingList()).getParent();
    }

    /** @return org.hip.kernel.bom.PropertyDef */
    @Override
    public PropertyDef getPropertyDef() {
        if (propertyDef == null) {
            propertyDef = ((GeneralDomainObject) getOwingObject()).getHome().getPropertyDef(this.getName());
        }
        return propertyDef;
    }

    /** Gets the current value.
     *
     * @param value java.lang.Object */
    @Override
    public Object getValue() {
        return current;
    }

    /** Returns the HashCode calculated from the name.
     *
     * @return int */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /** Returns true if the value has been changed after the initial setting.
     *
     * @return boolean */
    @Override
    public boolean isChanged() {
        return changed;
    }

    /** @return boolean */
    @Override
    public boolean isObjectRef() {
        return this instanceof ObjectRefProperty;
    }

    /** @return boolean */
    @Override
    public boolean isSimple() {
        return true;
    }

    /** Sets the notification status of this Property. Notification set to true means that the Property is marked as
     * changed when the property is initialized. Default: false.
     *
     * @param inNotification boolean */
    @Override
    public void notifyInit(final boolean inNotification) {
        doNotifyInit = inNotification;
    }

    /** Used to set an existing instance to an initial state for that it can be reused. */
    @Override
    public void setVirgin() {
        initial = null; // NOPMD by lbenno
        current = initial;
        initialized = false;
        changed = false;
        doNotifyInit = false;
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        final String retVal = "<Property "
                + "name=\"" + getName() + "\" "
                + "value=\"" + ((getValue() == null) ? "null" : getValue().toString()) + "\""
                + " /> ";
        return retVal;
    }

    /** Gets the current value.
     *
     * @return java.lang.Object */
    @Override
    protected Object value() {
        return current;
    }

    /** Sets the current value of the Property and handles initial state.
     *
     * @param inValue java.lang.Object */
    @Override
    protected void value(final Object inValue) {
        if (initialized) {
            // initial value exists
            if (inValue == null) {
                // new value is null and, therefore, can't be compared
                if (current != null) {
                    // set current to new value, mark instance as changed
                    current = inValue;
                    changed = true;
                } // if
            } // if (value not null)
            else {
                // value can be compared
                if (inValue.equals(initial)) {
                    // value is equal to initial, therefore, set current to initial
                    current = initial;
                    changed = false;
                } // if (value==initial)
                else if (inValue.equals(current)) {
                    // value is equal to current, therefore, return without any change
                    return;
                } // else (value==current)
                else {
                    // value is different, therefore, set current to new value, mark instance as changed
                    current = inValue;
                    changed = true;
                } // else (value different)
            } // else (value null)
        } // if (not initialized)
        else {
            if (inValue != null) {
                // we only initialize when value not null
                initial = inValue;
                current = initial;
                initialized = true;
                if (doNotifyInit) {
                    changed = true;
                }
            }
        } // else (initialized)
    }

    @Override
    public Object clone() throws CloneNotSupportedException { // NOPMD by lbenno
        final PropertyImpl out = (PropertyImpl) super.clone();
        out.setVirgin();
        out.value(CloneHelper.getCloned(getValue()));
        return out;
    }
}