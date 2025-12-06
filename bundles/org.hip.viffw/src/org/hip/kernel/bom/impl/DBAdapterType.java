/**
	This package is part of the application VIF.
	Copyright (C) 2011-2014, Benno Luthiger

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.bom.impl;

import org.hip.kernel.bom.ColumnModifier;
import org.hip.kernel.bom.DBAdapterJoin;
import org.hip.kernel.bom.DBAdapterSimple;
import org.hip.kernel.bom.model.JoinedObjectDef;
import org.hip.kernel.bom.model.ObjectDef;

/** The DB adapter type enumeration. Instances of this enum adapt the SQL statements of different DB providers.
 *
 * @author Luthiger Created: 01.02.2012 */
public enum DBAdapterType {
    DB_TYPE_MYSQL("com.mysql.jdbc.Driver", new DefaultDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
            new MySQLColumnModifierUCase()),
    DB_TYPE_POSTGRESQL("org.postgresql.Driver", new DefaultDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
            new MySQLColumnModifierUCase()),
    DB_TYPE_ORACLE("oracle.jdbc.OracleDriver", new DefaultDBAdapterSimpleFactory(), new OracleAdapterJoinFactory(),
            new OracleColumnModifierUCase()),
    DB_TYPE_DERBY("org.apache.derby.jdbc.", new DerbyDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
            new MySQLColumnModifierUCase());

    private String type;
    private IAdapterSimpleFactory simpleAdapterFactory;
    private IAdapterJoinFactory joinAdapterFactory;
    private ColumnModifier columnModifier;

    DBAdapterType(final String inType, final IAdapterSimpleFactory inSimpleAdapterFactory,
            final IAdapterJoinFactory inJoinAdapterFactory, final ColumnModifier inColumnModifier) {
        type = inType;
        simpleAdapterFactory = inSimpleAdapterFactory;
        joinAdapterFactory = inJoinAdapterFactory;
        columnModifier = inColumnModifier;
    }

    /** This adapter's type string.
     *
     * @return String */
    public String getType() {
        return type;
    }

    /** Helper method to select the adapter type.
     *
     * @param inDBSourceID String e.g. <code>com.mysql.jdbc.Driver/MySQL/5.8.1</code>
     * @return boolean <code>true</code> if the specified DB source id matches this adapter type */
    public boolean isOfType(final String inDBSourceID) {
        return inDBSourceID.startsWith(type);
    }

    /** Returns the adapter for simple queries.
     *
     * @param inObjectDef {@link ObjectDef}
     * @return {@link DBAdapterSimple} */
    public DBAdapterSimple getSimpleDBAdapter(final ObjectDef inObjectDef) {
        return simpleAdapterFactory.createAdapterSimple(inObjectDef);
    }

    /** Returns the adapter for joined queries.
     *
     * @param inObjectDef {@link JoinedObjectDef}
     * @return {@link DBAdapterJoin} */
    public DBAdapterJoin getJoinDBAdapter(final JoinedObjectDef inObjectDef) {
        return joinAdapterFactory.createAdapterJoin(inObjectDef);
    }

    /** @return {@link ColumnModifier} */
    public ColumnModifier getColumnModifierUCase() {
        return columnModifier;
    }

    // --- private classes ---

    /** Interface for adapter factory. */
    private interface IAdapterSimpleFactory {
        /** Returns a DB adapter instance.
         *
         * @param inObjectDef {@link ObjectDef}
         * @return {@link DBAdapterSimple} */
        DBAdapterSimple createAdapterSimple(ObjectDef inObjectDef);
    }

    /** Factory for default DB */
    static private class DefaultDBAdapterSimpleFactory implements IAdapterSimpleFactory {
        @Override
        public DBAdapterSimple createAdapterSimple(final ObjectDef inObjectDef) { // NOPMD by lbenno 
            return new DefaultDBAdapterSimple(inObjectDef);
        }
    }

    /** Factory for Derby DB */
    static private class DerbyDBAdapterSimpleFactory implements IAdapterSimpleFactory {
        @Override
        public DBAdapterSimple createAdapterSimple(final ObjectDef inObjectDef) { // NOPMD by lbenno 
            return new DerbyDBAdapterSimple(inObjectDef);
        }
    }

    /** Interface for join factory. */
    private interface IAdapterJoinFactory {
        /** Returns a DB adapter instance for joins.
         *
         * @param inObjectDef {@link JoinedObjectDef}
         * @return {@link DBAdapterJoin} */
        DBAdapterJoin createAdapterJoin(JoinedObjectDef inObjectDef);
    }

    /** Factory for MySQL join */
    static private class MySQLAdapterJoinFactory implements IAdapterJoinFactory {
        @Override
        public DBAdapterJoin createAdapterJoin(final JoinedObjectDef inObjectDef) { // NOPMD by lbenno 
            return new MySQLAdapterJoin(inObjectDef);
        }
    }

    /** Factory for Oracle join */
    static private class OracleAdapterJoinFactory implements IAdapterJoinFactory {
        /** After switching to ANSI join, we can reuse <code>MySQLAdapterJoin</code> for oracle too. */
        @Override
        public DBAdapterJoin createAdapterJoin(final JoinedObjectDef inObjectDef) {
            return new MySQLAdapterJoin(inObjectDef);
        }
    }

}
