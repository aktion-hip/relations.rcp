/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2003-2014, Benno Luthiger

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
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.GroupingDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.NestedDef;
import org.hip.kernel.bom.model.NestedDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.util.Debug;

/** Implementation of the NestedDef interface.
 *
 * @author Benno Luthiger Created on Nov 29, 2003 */
@SuppressWarnings("serial")
public class NestedDefImpl extends AbstractDefWithColumns implements NestedDef {

    /** NestedDefImpl constructor.
     *
     * @param String The name of the nested query (aka virtual table). */
    public NestedDefImpl(final String inName) {
        super();
        try {
            set(NestedDefDef.name, inName);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @return MetaModelObject
     * @see org.hip.kernel.bom.model.ModelObject#getMetaModelObject() */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getNestedDefDef();
    }

    /** Sets the grouping applied to the result set.
     *
     * @param inGroupingDef GroupingDef */
    @Override
    public void addGroupingDef(final GroupingDef inGroupingDef) {
        try {
            set(NestedDefDef.resultGrouping, inGroupingDef);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** Returns the nestings name, i.e. the alias of the nested query.
     *
     * @return String */
    @Override
    public String getName() {
        try {
            return (String) get(NestedDefDef.name);
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return "";
    }

    /** Returns the key to set and retrieve the columnDefs in the hashtable.
     *
     * @return String */
    @Override
    protected String getColumnsKey() {
        return NestedDefDef.columnDefs;
    }

    /** Creates and returns the nested query.
     *
     * @return String The nested query. */
    @Override
    public String getNestedQuery() {
        final String outSQL = "(SELECT " + getFields() + " ";
        return outSQL + getFromSQL() + ") " + getName();
    }

    private String getFromSQL() {
        final StringBuilder outSQL = new StringBuilder("FROM ");
        outSQL.append(getTableName());
        final String lGrouping = getResultGrouping();
        if (lGrouping.length() != 0) {
            outSQL.append(' ').append(lGrouping);
        }
        return new String(outSQL);
    }

    private String getResultGrouping() {
        String outGrouping = "";
        try {
            final GroupingDef lGroupingDef = (GroupingDef) get(NestedDefDef.resultGrouping);
            outGrouping = lGroupingDef.getGroupingExpression();
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return outGrouping;
    }

    /** Returns the SQL name of the column mapped to the <columnDef> entry with the specified columnName attribute.
     *
     * @param inColumnName String
     * @return String */
    @Override
    public String getSQLColumnName(final String inColumnName) {
        for (final PropertyDef lPropertyDef : propertyDefs) {
            if (lPropertyDef.getName().equals(inColumnName)) {
                return lPropertyDef.getMappingDef().getColumnName();
            }
        }
        return "";
    }

    /** NestedDefs are equal if their nested query is equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof NestedDef)) {
            return false;
        }

        final NestedDef lNestedDef = (NestedDef) inObject;
        return getNestedQuery().equals(lNestedDef.getNestedQuery());
    }

    /** Returns a hash code value for the object def.
     *
     * @return int */
    @Override
    public int hashCode() {
        return getNestedQuery().hashCode();
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return Debug.classMarkupString(this, "query=\"" + getNestedQuery() + "\"");
    }
}
