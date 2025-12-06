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
package org.hip.kernel.bom.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.impl.PropertyImpl;
import org.hip.kernel.bom.impl.PropertySetImpl;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.bom.model.KeyDefDef;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.ObjectDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.util.Debug;
import org.hip.kernel.util.DefaultNameValue;
import org.hip.kernel.util.DefaultNameValueList;
import org.hip.kernel.util.NameValue;
import org.hip.kernel.util.NameValueList;
import org.hip.kernel.util.VInvalidNameException;

/** Implements the ObjectDef interface
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.ObjectDef */
@SuppressWarnings("serial")
public class ObjectDefImpl extends AbstractModelObject implements ObjectDef {

    // Instance variables
    private Map<String, HelperTableDef> helperTableMap;

    /** ObjectDefImpl default constructor. */
    public ObjectDefImpl() {
        super();
    }

    /** ObjectDefImpl constructor with initial values. The array of the objects contains the names in the first column
     * and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public ObjectDefImpl(final Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);
    }

    /** Adds a PropertyDef to the list of propertyDefs.
     *
     * @param inPropertyDef org.hip.kernel.bom.model.PropertyDef */
    @Override
    public void addPropertyDef(final PropertyDef inPropertyDef) {
        try {
            PropertySet lSet = (PropertySet) this.get(ObjectDefDef.propertyDefs);
            if (lSet == null) {
                lSet = new PropertySetImpl(this);
                this.set(ObjectDefDef.propertyDefs, lSet);
            }
            lSet.add(new PropertyImpl(lSet, (String) inPropertyDef.get(PropertyDefDef.propertyName), inPropertyDef));
        } catch (final GettingException | SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** ObjectDefs are equal if their objectName, parent and version attribute are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof ObjectDef)) {
            return false;
        }

        final ObjectDef lObjectDef = (ObjectDef) inObject;
        try {
            return ((String) get(ObjectDefDef.objectName)).equals(lObjectDef.get(ObjectDefDef.objectName)) &&
                    ((String) get(ObjectDefDef.parent)).equals(lObjectDef.get(ObjectDefDef.parent)) &&
                    ((String) get(ObjectDefDef.version)).equals(lObjectDef.get(ObjectDefDef.version));
        } catch (final GettingException exc) {
            return false;
        }
    }

    /** Returns the MappingDef associated with the specified property.
     *
     * @return org.hip.kernel.bom.model.MappingDef
     * @param inPropertyName java.lang.String */
    @Override
    public MappingDef getMappingDef(final String inPropertyName) {
        return getPropertyDef(inPropertyName).getMappingDef();
    }

    @Override
    public Collection<MappingDef> getMappingDefs2() { // NOPMD by lbenno
        final Collection<MappingDef> outMappingDefs = new ArrayList<MappingDef>();

        for (final PropertyDef lPropertyDef : getPropertyDefs2()) {
            outMappingDefs.add(lPropertyDef.getMappingDef());
        }
        return outMappingDefs;
    }

    @Override
    public Collection<MappingDef> getMappingDefsForTable2(final String inTableName) { // NOPMD by lbenno
        final HelperTableDef lHelperTableDef = helperTableDefs().get(inTableName);
        if (lHelperTableDef == null) {
            return new Hashtable<String, MappingDef>().values();
        }

        return lHelperTableDef.getMappingDefs2();
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getObjectDefDef();
    }

    /** Returns the primary key definition. The actual version of this domain object framework does no support secondary
     * primary keys.
     *
     * @return org.hip.kernel.bom.model.KeyDef */
    @Override
    public KeyDef getPrimaryKeyDef() {
        try {
            // pre
            if (this.get(ObjectDefDef.keyDefs) == null) {
                return null;
            }
            return (KeyDef) ((NameValueList) this.get(ObjectDefDef.keyDefs)).getValue(KeyDefDef.keyType_PrimaryKey);
        } catch (final GettingException | VInvalidNameException exc) {
            DefaultExceptionHandler.instance().handle(exc);
            return null;
        }
    }

    /** Returns the PropertyDef for the specified Property.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param lName java.lang.String */
    @Override
    public PropertyDef getPropertyDef(final String lName) {
        try {
            return (PropertyDef) ((PropertySet) this.get(ObjectDefDef.propertyDefs)).getValue(lName);
        } catch (final VInvalidNameException exc) {
            return null;
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
            return null;
        }
    }

    @Override
    public List<PropertyDef> getPropertyDefs2() { // NOPMD by lbenno
        final List<PropertyDef> outDefs = getPropertyDefs2(PropertyDefDef.propertyTypeSimple);
        outDefs.addAll(getPropertyDefs2(PropertyDefDef.propertyTypeComposite));
        outDefs.addAll(getPropertyDefs2(PropertyDefDef.propertyTypeObjectRef));
        return outDefs;
    }

    @Override
    public List<PropertyDef> getPropertyDefs2(final String inPropertyType) { // NOPMD by lbenno
        final List<PropertyDef> outPropertyDefs = new ArrayList<PropertyDef>();
        try {
            final PropertySet lPropertyDefs = (PropertySet) get(ObjectDefDef.propertyDefs);
            if (lPropertyDefs != null) {
                for (final NameValue lNameValue : lPropertyDefs.getNameValues2()) {
                    final PropertyDef lPropertyDef = (PropertyDef) lNameValue.getValue();
                    if (lPropertyDef.getPropertyType().equals(inPropertyType)) {
                        outPropertyDefs.add(lPropertyDef);
                    }
                }
            } // for
            return outPropertyDefs;
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
            return new ArrayList<PropertyDef>();
        } // try-catch
    }

    @Override
    public Set<String> getTableNames2() { // NOPMD by lbenno
        return helperTableDefs().keySet();
    }

    /** Returns a hash code value for the object def.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 1;
        try {
            outCode = ((String) get(ObjectDefDef.objectName)).hashCode() ^
                    ((String) get(ObjectDefDef.parent)).hashCode() ^
                    ((String) get(ObjectDefDef.version)).hashCode();
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return outCode;
    }

    /** @return java.util.Hashtable */
    private synchronized Map<String, HelperTableDef> helperTableDefs() { // NOPMD by lbenno
        if (helperTableMap == null) {
            helperTableMap = HelperTableDef.createTableDefs(this);
        }
        return helperTableMap;
    }

    /** Sets the definition of the primary key.
     *
     * @param inKeyDef org.hip.kernel.bom.model.KeyDef */
    public void setPrimaryKeyDef(final KeyDef inKeyDef) {
        try {
            NameValueList lKeys = (NameValueList) this.get(ObjectDefDef.keyDefs);
            if (lKeys == null) {
                lKeys = new DefaultNameValueList();
                this.set(ObjectDefDef.keyDefs, lKeys);
            }
            lKeys.add(new DefaultNameValue(lKeys, KeyDefDef.keyType_PrimaryKey, inKeyDef));
        } catch (final GettingException | SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    @Override
    public String toString() { // NOPMD by lbenno
        String lMessage = "";
        try {
            lMessage = "objectName=\"" + (String) get(ObjectDefDef.objectName) +
                    "\" parent=\"" + (String) get(ObjectDefDef.parent) +
                    "\" version=\"" + (String) get(ObjectDefDef.version) + "\"";
        } catch (final GettingException esc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return Debug.classMarkupString(this, lMessage);
    }
}
