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

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.impl.CompositePropertyImpl;
import org.hip.kernel.bom.impl.ObjectRefPropertyImpl;
import org.hip.kernel.bom.impl.PropertyImpl;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MappingDefDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.bom.model.RelationshipDef;
import org.hip.kernel.bom.model.TypeDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.util.Debug;
import org.hip.kernel.util.VInvalidValueException;

/** This is the implementation of the PropertyDef interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.PropertyDef */
@SuppressWarnings("serial")
public class PropertyDefImpl extends AbstractModelObject implements PropertyDef, Cloneable {

    /** PropertyDefImpl default constructor. */
    public PropertyDefImpl() {
        super();
    }

    /** Constructs a PropertyDefImpl with initial values. The array of the objects contains the names in the first column
     * and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public PropertyDefImpl(final Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);
    }

    /** Constructs a PropertyDefImpl with initial values.
     *
     * @param inObjectDef org.hip.kernel.bom.ObjectDef
     * @param inInitialValues java.lang.Object[][] */
    public PropertyDefImpl(final ObjectDef inObjectDef, final Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);
    }

    /** Returns either an instance of ObjectRefPropertyImpl, CompositePropertyImpl or PropertyImpl depending on this
     * instances type. The returned Property is member of the specified PropertySet.
     *
     * @return org.hip.kernel.bom.Property
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    public Property create(final PropertySet inSet) {
        Property outProperty = null;
        final String lType = getPropertyType();

        if (lType == PropertyDefDef.propertyTypeObjectRef) {
            outProperty = new ObjectRefPropertyImpl(inSet, getName(), null);
        }
        else if (lType == PropertyDefDef.propertyTypeComposite) {
            outProperty = new CompositePropertyImpl(inSet, getName(), null);
        }
        else if (lType == PropertyDefDef.propertyTypeSimple) {
            try {
                outProperty = new PropertyImpl(inSet, getName(), null, getValueClassType());
            } catch (final VInvalidValueException exc) { // NOPMD by lbenno
                // left blank intentionally
            }
        }
        return outProperty;
    }

    /** PropertyDefs are equal if their propertyName, valueType and propertyType attribute plus their mappingDef are
     * equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof PropertyDef)) {
            return false;
        }

        final PropertyDef lPropertyDef = (PropertyDef) inObject;
        try {
            if (getMappingDef() == null) {
                return ((String) get(PropertyDefDef.propertyName))
                        .equals(lPropertyDef.get(PropertyDefDef.propertyName))
                        &&
                        ((String) get(PropertyDefDef.valueType)).equals(lPropertyDef.get(PropertyDefDef.valueType))
                        &&
                        ((String) get(PropertyDefDef.propertyType)).equals(lPropertyDef
                                .get(PropertyDefDef.propertyType)) &&
                        lPropertyDef.getMappingDef() == null;
            }
            else {
                return ((String) get(PropertyDefDef.propertyName))
                        .equals(lPropertyDef.get(PropertyDefDef.propertyName))
                        &&
                        ((String) get(PropertyDefDef.valueType)).equals(lPropertyDef.get(PropertyDefDef.valueType))
                        &&
                        ((String) get(PropertyDefDef.propertyType)).equals(lPropertyDef
                                .get(PropertyDefDef.propertyType)) &&
                        getMappingDef().equals(lPropertyDef.getMappingDef());
            }
        } catch (final GettingException exc) {
            return false;
        }
    }

    /** @return java.lang.String */
    public String getFormatPattern() {
        try {
            return (String) get(PropertyDefDef.formatPattern);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** @return org.hip.kernel.bom.model.MappingDef */
    @Override
    public MappingDef getMappingDef() {
        try {
            return (MappingDef) get(PropertyDefDef.mappingDef);
        } catch (final GettingException exc) {
            return null;
        }
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getPropertyDefDef();
    }

    /** @return java.lang.String */
    @Override
    public String getName() {
        try {
            return (String) get(PropertyDefDef.propertyName);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** Is PropertyDef simple or ObjectRef?
     *
     * @return java.lang.String */
    @Override
    public String getPropertyType() {
        try {
            return (String) get(PropertyDefDef.propertyType);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** @return org.hip.kernel.bom.model.RelationshipDef */
    @Override
    public RelationshipDef getRelationshipDef() {
        try {
            return (RelationshipDef) get(PropertyDefDef.relationshipDef);
        } catch (final GettingException exc) {
            return null;
        }
    }

    /** Returns the corresponding java class name to the values type according to the type information in the defining
     * XML.
     *
     * @return java.lang.String */
    @Override
    public String getValueClassType() {
        // set default value
        String outValueTypeClass = PropertyDef.DFT_VALUE_TYPE;

        if (getValueType() == null) {
            return outValueTypeClass;
        }

        final String lValueTypeKey = getValueType().intern();
        for (int i = TypeDef.valueTypes.length - 1; i >= 0; i--) {
            if (lValueTypeKey == TypeDef.valueTypes[i][0]) {
                outValueTypeClass = TypeDef.valueTypes[i][1];
                break;
            }
        }
        return outValueTypeClass;
    }

    /** Returns the type of the value. This type information is read from the defining XML.
     *
     * @return java.lang.String */
    @Override
    public String getValueType() {
        try {
            return (String) get(PropertyDefDef.valueType);
        } catch (final GettingException exc) {
            return "";
        }
    }

    /** Returns a hash code value for the object def.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 1;
        try {
            outCode = getMappingDef() == null ? 0 : getMappingDef().hashCode();
            outCode = ((String) get(PropertyDefDef.propertyName)).hashCode() ^
                    ((String) get(PropertyDefDef.valueType)).hashCode() ^
                    ((String) get(PropertyDefDef.propertyType)).hashCode() ^
                    outCode;
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return outCode;
    }

    /** @param inMappingDef org.hip.kernel.bom.model.MappingDef */
    @Override
    public void setMappingDef(final MappingDef inMappingDef) {

        try {
            set(PropertyDefDef.mappingDef, inMappingDef);
            inMappingDef.setPropertyDef(this);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @param inRelationshipDef org.hip.kernel.bom.model.RelationshipDef */
    @Override
    public void setRelationshipDef(final RelationshipDef inRelationshipDef) {

        try {
            set(PropertyDefDef.relationshipDef, inRelationshipDef);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** Returns a string representation of the object-
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        String lMessage = "";
        try {
            lMessage = PropertyDefDef.propertyName + "=\"" + (String) get(PropertyDefDef.propertyName) +
                    "\" " + PropertyDefDef.valueType + "=\"" + (String) get(PropertyDefDef.valueType) +
                    "\" " + PropertyDefDef.propertyType + "=\"" + (String) get(PropertyDefDef.propertyType) + "\"";
        } catch (final GettingException esc) { // NOPMD by lbenno
            // intentionally left empty
        }
        return Debug.classMarkupString(this, lMessage);
    }

    /** Creates and returns a copy of this object.
     *
     * @return java.lang.Object */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PropertyDef outPropertyDef = (PropertyDef) super.clone();
        outPropertyDef.setVirgin();
        try {
            outPropertyDef.set(PropertyDefDef.propertyName, get(PropertyDefDef.propertyName));
            outPropertyDef.set(PropertyDefDef.valueType, get(PropertyDefDef.valueType));
            outPropertyDef.set(PropertyDefDef.propertyType, get(PropertyDefDef.propertyType));

            final MappingDef lOldMapping = getMappingDef();
            if (lOldMapping == null) {
                return outPropertyDef;
            }

            final MappingDef lNewMapping = new MappingDefImpl();
            lNewMapping.set(MappingDefDef.tableName, lOldMapping.getTableName());
            lNewMapping.set(MappingDefDef.columnName, lOldMapping.getColumnName());
            outPropertyDef.set(PropertyDefDef.mappingDef, lNewMapping);
            return outPropertyDef;
        } catch (final GettingException | SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return outPropertyDef;
    }
}