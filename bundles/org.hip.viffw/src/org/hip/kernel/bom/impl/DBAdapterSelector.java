/**
	This package is part of the framework used for the application VIF.
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

import org.hip.kernel.bom.ColumnModifier;
import org.hip.kernel.bom.DBAdapterJoin;
import org.hip.kernel.bom.DBAdapterSimple;
import org.hip.kernel.bom.model.JoinedObjectDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.sys.VObject;

/** This class selects the appropriate adapter for the application either by evaluating the connection's metadata (JNDI
 * case) or according to the information provided by the application's properties file (i.e.
 * <code>org.hip.vif.db.url</code> in <i>vif.properties</i>).
 *
 * Created on 30.08.2002
 *
 * @author Benno Luthiger
 * @deprecated */
@Deprecated
public class DBAdapterSelector extends VObject {
    // The singleton.
    private static DBAdapterSelector singleton = new DBAdapterSelector();

    public enum AdapterType { // NOPMD by lbenno
        DB_TYPE_MYSQL("jdbc:mysql:", new DefaultDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
                new MySQLColumnModifierUCase()),
                DB_TYPE_POSTGRESQL("jdbc:postgresql:", new DefaultDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
                        new MySQLColumnModifierUCase()),
                        DB_TYPE_ORACLE("jdbc:oracle:thin:", new DefaultDBAdapterSimpleFactory(), new OracleAdapterJoinFactory(),
                                new OracleColumnModifierUCase()),
                                DB_TYPE_DERBY("jdbc:derby:", new DerbyDBAdapterSimpleFactory(), new MySQLAdapterJoinFactory(),
                                        new MySQLColumnModifierUCase());

        private String type;
        private IAdapterSimpleFactory simpleAdapterFactory;
        private IAdapterJoinFactory joinAdapterFactory;
        private ColumnModifier columnModifier;

        AdapterType(final String inType, final IAdapterSimpleFactory inSimpleAdapterFactory,
                final IAdapterJoinFactory inJoinAdapterFactory, final ColumnModifier inColumnModifier) {
            type = inType;
            simpleAdapterFactory = inSimpleAdapterFactory;
            joinAdapterFactory = inJoinAdapterFactory;
            columnModifier = inColumnModifier;
        }

        public String getType() { // NOPMD by lbenno
            return type;
        }

        public boolean isOfType(final String inUrl) { // NOPMD by lbenno
            return inUrl.indexOf(type) == 0;
        }

        public DBAdapterSimple getSimpleDBAdapter(final ObjectDef inObjectDef) { // NOPMD by lbenno
            return simpleAdapterFactory.createAdapterSimple(inObjectDef);
        }

        public DBAdapterJoin getJoinDBAdapter(final JoinedObjectDef inObjectDef) { // NOPMD by lbenno
            return joinAdapterFactory.createAdapterJoin(inObjectDef);
        }

        public ColumnModifier getColumnModifierUCase() { // NOPMD by lbenno
            return columnModifier;
        }
    }

    private transient AdapterType adapterType;

    /** Constructor for DBAdapterSelector. */
    private DBAdapterSelector() {
        super();
    }

    /** <p>
     * Returns the singleton instance of DBAdapterSelector.
     * </p>
     * <p>
     * <b>Note:</b> this method is synchronized to ensure that every thread gets an initialized singleton.
     * </p>
     *
     * @return DBAdapterSelector */
    public static DBAdapterSelector getInstance() {
        synchronized (singleton) {
            if (singleton.isUnitialized()) {
                singleton.initialize();
            }
        }
        return singleton;
    }

    private boolean isUnitialized() {
        return adapterType == null;
    }

    private void initialize() {
        adapterType = AdapterType.DB_TYPE_MYSQL;
    }

    /** Returns the database adapter for simple domain objects appropriate to the running system.
     *
     * @param inObjectDef org.hip.kernel.bom.model.ObjectDef
     * @return org.hip.kernel.bom.DBAdapterSimple */
    public DBAdapterSimple getSimpleDBAdapter(final ObjectDef inObjectDef) {
        return adapterType.getSimpleDBAdapter(inObjectDef);
    }

    /** Returns the database adapter for joined domain objects appropriate to the running system.
     *
     * @param inObjectDef org.hip.kernel.bom.model.JoinedObjectDef
     * @return org.hip.kernel.bom.DBAdapterJoin */
    public DBAdapterJoin getJoinDBAdapter(final JoinedObjectDef inObjectDef) {
        return adapterType.getJoinDBAdapter(inObjectDef);
    }

    /** Returns the column modifier to modify a column value to upper case.
     *
     * @return org.hip.kernel.bom.ColumnModifier */
    public ColumnModifier getColumnModifierUCase() {
        return adapterType.getColumnModifierUCase();
    }

    /** <p>
     * Resets the database selector driver. This method has to be called to change the database.
     * </p>
     * <p>
     * <b>Note:</b> we synchronize this method for that the changed value gets flushed immediately to the main memory
     * (according to the rule that we have to synchronize whenever we are writing a variable that may be read next by
     * another thread).
     * </p> */
    public synchronized void reset() { // NOPMD by lbenno
        adapterType = null; // NOPMD by lbenno
    }

    // --- private classes ---

    private interface IAdapterSimpleFactory { // NOPMD by lbenno
        public DBAdapterSimple createAdapterSimple(ObjectDef inObjectDef); // NOPMD by lbenno
    }

    static private class DefaultDBAdapterSimpleFactory implements IAdapterSimpleFactory { // NOPMD by lbenno
        @Override
        public DBAdapterSimple createAdapterSimple(final ObjectDef inObjectDef) { // NOPMD by lbenno
            return new DefaultDBAdapterSimple(inObjectDef);
        }
    }

    static private class DerbyDBAdapterSimpleFactory implements IAdapterSimpleFactory { // NOPMD by lbenno
        @Override
        public DBAdapterSimple createAdapterSimple(final ObjectDef inObjectDef) { // NOPMD by lbenno
            return new DerbyDBAdapterSimple(inObjectDef);
        }
    }

    private interface IAdapterJoinFactory { // NOPMD by lbenno
        public DBAdapterJoin createAdapterJoin(JoinedObjectDef inObjectDef); // NOPMD by lbenno
    }

    static private class MySQLAdapterJoinFactory implements IAdapterJoinFactory { // NOPMD by lbenno
        @Override
        public DBAdapterJoin createAdapterJoin(final JoinedObjectDef inObjectDef) { // NOPMD by lbenno
            return new MySQLAdapterJoin(inObjectDef);
        }
    }

    static private class OracleAdapterJoinFactory implements IAdapterJoinFactory { // NOPMD by lbenno
        /** After switching to ANSI join, we can reuse <code>MySQLAdapterJoin</code> for oracle too. */
        @Override
        public DBAdapterJoin createAdapterJoin(final JoinedObjectDef inObjectDef) {
            return new MySQLAdapterJoin(inObjectDef);
        }
    }

}