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
import org.hip.kernel.bom.model.GroupingDefDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.util.Debug;

/** This class implements the behaviour of grouping object in nested domain objects.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.GroupingDef Created on Nov 29, 2003 */
@SuppressWarnings("serial")
public class GroupingDefImpl extends AbstractDefWithColumns implements GroupingDef {

    /** GroupingDefImpl default constructor. */
    public GroupingDefImpl(final String inModifier) {
        super();
        try {
            set(GroupingDefDef.modifier, inModifier);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @return MetaModelObject
     * @see org.hip.kernel.bom.model.ModelObject#getMetaModelObject() */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getGroupingDefDef();
    }

    /** Returns the modifier type of this grouping, e.g. GROUP BY or ORDER BY.
     *
     * @return String */
    @Override
    public String getModifier() {
        try {
            return (String) get(GroupingDefDef.modifier);
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return "";
    }

    /** Creates the SQL grouping expression, i.e. "GROUP BY table.Column"
     *
     * @return String The SQL grouping expression */
    @Override
    public String getGroupingExpression() {
        return getModifier() + " BY " + getFields();
    }

    /** Returns the key to set and retrieve the columnDefs in the hashtable.
     *
     * @return String */
    @Override
    protected String getColumnsKey() {
        return GroupingDefDef.columnDefs;
    }

    /** GroupingDefs are equal if their grouping expression is equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof GroupingDef)) {
            return false;
        }

        final GroupingDef lGroupingDef = (GroupingDef) inObject;
        return getGroupingExpression().equals(lGroupingDef.getGroupingExpression());
    }

    /** Returns a hash code value for the grouping def.
     *
     * @return int */
    @Override
    public int hashCode() {
        return getGroupingExpression().hashCode();
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return Debug.classMarkupString(this, "grouping=\"" + getGroupingExpression() + "\"");
    }
}
