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
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MappingDefDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.util.Debug;

/** Implements the MappingDef interface
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.MappingDef */
@SuppressWarnings("serial")
public class MappingDefImpl extends AbstractModelObject implements MappingDef {
    // Instance variables
    private PropertyDef propertyDef;
    private Property optimTableName;
    private Property optimColumnName;

    /** MappingDefImpl default constructor. */
    public MappingDefImpl() {
        super();
    }

    /** MappingDefImpl constructor with initial values. The array of the objects contains the names in the first column
     * and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public MappingDefImpl(final java.lang.Object[][] inInitialValues) { // NOPMD by lbenno 
        super(inInitialValues);
    }

    /** MappingDefs are equal if their tableName and columnName attribute are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof MappingDef)) {
            return false;
        }

        final MappingDef lMappingDef = (MappingDef) inObject;
        try {
            return ((String) get(MappingDefDef.tableName)).equals(lMappingDef.get(MappingDefDef.tableName)) &&
                    ((String) get(MappingDefDef.columnName)).equals(lMappingDef.get(MappingDefDef.columnName));
        } catch (final GettingException exc) {
            return false;
        }
    }

    /** @return java.lang.String */
    @Override
    public String getColumnName() {
        if (optimColumnName == null) {
            optimColumnName = (Property) this.propertySet().get(MappingDefDef.columnName);
        }
        return (String) optimColumnName.getValue();
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getMappingDefDef();
    }

    /** @return org.hip.kernel.bom.model.PropertyDef */
    @Override
    public PropertyDef getPropertyDef() {
        return propertyDef;
    }

    /** @return java.lang.String */
    @Override
    public String getTableName() {
        if (optimTableName == null) {
            optimTableName = (Property) this.propertySet().get(MappingDefDef.tableName);
        }
        return (String) optimTableName.getValue();
    }

    /** Returns a hash code value for the object def.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 1;
        try {
            outCode = ((String) get(MappingDefDef.tableName)).hashCode() ^
                    ((String) get(MappingDefDef.columnName)).hashCode();
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return outCode;
    }

    /** @param inPropertyDef org.hip.kernel.bom.model.PropertyDef */
    @Override
    public void setPropertyDef(final PropertyDef inPropertyDef) {

        propertyDef = inPropertyDef;
    }

    @Override
    public void set(final String inName, Object inValue) throws SettingException { // NOPMD by lbenno
        if (!MappingDefDef.tableName.equals(inName)) {
            // transform value of column name to upper case to fix problem with case insensitive databases (like Derby)
            inValue = ((String) inValue).intern().toUpperCase();
        }
        super.set(inName, inValue);
    }

    @Override
    public String toString() { // NOPMD by lbenno
        String lMessage = "";
        try {
            lMessage = MappingDefDef.tableName + "=\"" + (String) get(MappingDefDef.tableName) +
                    "\" " + MappingDefDef.columnName + "=\"" + (String) get(MappingDefDef.columnName) + "\"";
        } catch (final GettingException esc) { // NOPMD by lbenno 
            // intentionally left empty
        }
        return Debug.classMarkupString(this, lMessage);
    }
}
