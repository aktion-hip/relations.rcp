/**
	This package is part of the domain object framework used for the application VIF.
	Copyright (C) 2003 // NOPMD

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.ColumnDefDef;
import org.hip.kernel.bom.model.HiddenDef;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MappingDefDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.util.ListJoiner;
import org.hip.kernel.util.NameValueList;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VNameValueException;

/** Abstract class providing functionality for joined domain objects.
 *
 * @author Benno Luthiger Created on Dec 20, 2003 */
@SuppressWarnings("serial")
public abstract class AbstractDefWithColumns extends AbstractModelObject { // NOPMD by lbenno
    protected Collection<PropertyDef> propertyDefs = new ArrayList<PropertyDef>();
    private final Map<String, ObjectDef> objectDefs = new ConcurrentHashMap<String, ObjectDef>(5); // NOPMD by lbenno
    private final Map<String, String> hidden = new ConcurrentHashMap<String, String>(3); // NOPMD by lbenno

    /** AbstractDefWithColumns default constructor. */
    public AbstractDefWithColumns() {
        super();
    }

    /** AbstractDefWithColumns constructor with initial values.
     *
     * @param inInitialValues */
    public AbstractDefWithColumns(final Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);
    }

    /** Adds a column def to the object definition. The purpose of this method is to retrieve an existing PropertyDef
     * (with its table mapping) to reuse it for the JoinedDomainObject, i.e. the SQL commands to retrieve the persistent
     * data. The result of this method (i.e. the SQL command that will be created) is contingent on the existence of the
     * different attributes: columnName, alias, domainObject, nestedObject, valueType, modifier. 'columnName' is
     * required. if 'domainObject' exists => SQL: mappedTable.mappedColumnName if 'nestedObject' existes => SQL:
     * nestedObject.columnName if 'domainObject' and 'nestedObject' => SQL: nestedObject.mappedColumnName if 'alias' =>
     * sets the propertyDef's tableName attribute to the specified alias if 'valueType' => sets the propertyDef's
     * valueType to the specified value if 'as' => SQL: (tab.col) AS alias if 'modifier' => SQL modifier(tab.col) if
     * 'template' => SQL format(template, new Object[] {tab.col})
     *
     * @param inColumnDefAttributes org.hip.kernel.util.NameValueList
     * @throws BOMException */
    public void addColumnDef(final NameValueList inColumnDefAttributes) throws BOMException { // NOPMD by lbenno
        try {
            final String lVisibleColumnName = (String) inColumnDefAttributes.getValue(ColumnDefDef.columnName);
            String lMappedColumnName = lVisibleColumnName;
            String lTableName = "";
            PropertyDef lPropertyDef = null;

            final boolean lHasValueAttr = inColumnDefAttributes.hasValue(ColumnDefDef.valueType);
            boolean lHasDomainObjDef = false;
            if (inColumnDefAttributes.hasValue(ColumnDefDef.domainObject)) {
                lHasDomainObjDef = true;
                final String lDomainObjectName = (String) inColumnDefAttributes.getValue(ColumnDefDef.domainObject);
                final ObjectDef lObjectDef = getDomainObjectDef(lDomainObjectName);
                lMappedColumnName = lObjectDef.getPropertyDef(lVisibleColumnName).getMappingDef().getColumnName();
                lTableName = getTableName(lObjectDef);

                // We need a clone of the original PropertyDef because we want to change some attributes.
                lPropertyDef = (PropertyDef) lObjectDef.getPropertyDef(lVisibleColumnName).clone();
                if (lHasValueAttr) {
                    lPropertyDef.set(PropertyDefDef.valueType, inColumnDefAttributes.getValue(ColumnDefDef.valueType));
                }
            }

            if (inColumnDefAttributes.hasValue(ColumnDefDef.nestedObject)) {
                lTableName = (String) inColumnDefAttributes.getValue(ColumnDefDef.nestedObject);

                // We need either a valueType or domainObject attribute here.
                if (!(lHasValueAttr || lHasDomainObjDef)) {
                    throw new BOMException(
                            "Either attribute valueType or domainObject must be defined for a nested object.");
                }
                if (lHasValueAttr) {
                    final String lValueType = (String) inColumnDefAttributes.getValue(ColumnDefDef.valueType);
                    lPropertyDef = createPropertyDef(lTableName, lVisibleColumnName, lValueType);
                }
            }

            StringBuilder lSQLCommand = new StringBuilder(lTableName);
            lSQLCommand.append('.').append(lMappedColumnName);
            if (inColumnDefAttributes.hasValue(ColumnDefDef.modifier)) {
                final StringBuilder lModified = new StringBuilder(
                        (String) inColumnDefAttributes.getValue(ColumnDefDef.modifier));
                lModified.append('(').append(lSQLCommand).append(')');
                lSQLCommand = lModified;
            }
            if (inColumnDefAttributes.hasValue(ColumnDefDef.template)) {
                final StringBuilder lModified = new StringBuilder(100).append('(');
                lModified.append(
                        MessageFormat.format((String) inColumnDefAttributes.getValue(ColumnDefDef.template),
                                new Object[] { lSQLCommand })).append(')');
                lSQLCommand = lModified;
            }
            if (inColumnDefAttributes.hasValue(ColumnDefDef.alias)) {
                final String lAlias = (String) inColumnDefAttributes.getValue(ColumnDefDef.alias);
                lPropertyDef.set(PropertyDefDef.propertyName, lAlias);
            }
            if (inColumnDefAttributes.hasValue(ColumnDefDef.as)) {
                final String lAlias = (String) inColumnDefAttributes.getValue(ColumnDefDef.as);
                lPropertyDef.set(PropertyDefDef.propertyName, lAlias);
                final MappingDef lMappingDef = lPropertyDef.getMappingDef();
                lMappingDef.set(MappingDefDef.columnName, lAlias);
                lMappingDef.set(MappingDefDef.tableName, "");
                lSQLCommand.append(" AS ").append(lAlias);
            }

            propertyDefs.add(lPropertyDef);
            getColumns().add(new String(lSQLCommand));
        } catch (final VNameValueException | CloneNotSupportedException exc) {
            throw new BOMException(exc.getMessage(), exc);
        }
    }

    /** Adds a hidden column def to the object definition.
     *
     * @param inHiddenAttributes NameValueList */
    public void addHidden(final NameValueList inHiddenAttributes) {
        try {
            final String lPropertyName = (String) inHiddenAttributes.getValue(HiddenDef.columnName);
            final String lDomainObjectName = (String) inHiddenAttributes.getValue(HiddenDef.domainObject);

            final ObjectDef lObjectDef = getDomainObjectDef(lDomainObjectName);
            final String lColumnName = lObjectDef.getPropertyDef(lPropertyName).getMappingDef().getColumnName();
            final String lExpression = getTableName(lObjectDef) + "." + lColumnName;
            hidden.put(lPropertyName, lExpression);
        } catch (final VInvalidNameException | BOMException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** Returns the mapping of the specified hidden field.
     *
     * @param inPropertyName String
     * @return String */
    public String getHidden(final String inPropertyName) {
        return hidden.get(inPropertyName);
    }

    private PropertyDef createPropertyDef(final String inTableName, final String inPropertyName,
            final String inValueType) throws SettingException {
        final PropertyDef outDef = new PropertyDefImpl();
        outDef.set(PropertyDefDef.propertyName, inPropertyName);
        outDef.set(PropertyDefDef.valueType, inValueType);
        outDef.set(PropertyDefDef.propertyType, PropertyDefDef.propertyTypeSimple);
        final MappingDef lMappingDef = new MappingDefImpl();
        lMappingDef.set(MappingDefDef.tableName, inTableName);
        lMappingDef.set(MappingDefDef.columnName, inPropertyName);
        outDef.setMappingDef(lMappingDef);
        return outDef;
    }

    /** Returns the key to set and retrieve the columnDefs in the hashtable.
     *
     * @return String */
    protected abstract String getColumnsKey();

    /** @return java.util.Vector */
    @SuppressWarnings("unchecked")
    private List<String> getColumns() {
        try {
            List<String> outColumns = (List<String>) get(getColumnsKey());
            if (outColumns == null) {
                outColumns = new ArrayList<String>();
                set(getColumnsKey(), outColumns);
            }
            return outColumns;
        } catch (final VNameValueException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return new ArrayList<String>();
        }
    }

    /** Returns the <code>ObjectDef</code> for the specified object.
     *
     * @param inObjectName String
     * @return {@link ObjectDef}
     * @throws BOMException */
    protected ObjectDef getDomainObjectDef(final String inObjectName) throws BOMException {
        ObjectDef outObjectDef = objectDefs.get(inObjectName);
        if (outObjectDef == null) {
            try {
                final Class<?> lClass = Class.forName(inObjectName);
                final DomainObject lDomainObject = (DomainObject) lClass.newInstance();
                outObjectDef = (ObjectDef) lDomainObject.getHome().getObjectDef().clone();
                objectDefs.put(inObjectName, outObjectDef);
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                    | CloneNotSupportedException exc) {
                throw new BOMException("getDomainObjectDef", exc);
            }
        }
        return outObjectDef;
    }

    /** Returns the SQL name of the table where this column set resides.
     *
     * @return String SQL name of table */
    protected String getTableName() {
        return getTableName(objectDefs.values().toArray(new ObjectDef[1])[0]);
    }

    /** Returns the SQL name of the table from the specified <code>ObjectDef</code>.
     *
     * @param inObjectDef ObjectDef
     * @return String table name */
    protected String getTableName(final ObjectDef inObjectDef) {
        return inObjectDef.getTableNames2().toArray(new String[1])[0];
    }

    /** @return String Comma separated list of field names. */
    protected String getFields() {
        final ListJoiner lColumns = new ListJoiner();
        for (final String lColumn : getColumns()) {
            lColumns.addEntry(lColumn);
        }
        return lColumns.joinSpaced(",");
    }
}