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

import java.sql.SQLException;
import java.util.List;

import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.KeyCriterion;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.exc.VError;
import org.hip.kernel.util.SortableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the prepared update statement.
 *
 * @author Benno Luthiger */
public class PreparedUpdateStatement extends SqlPreparedStatement { // NOPMD by lbenno
    private static final Logger LOG = LoggerFactory.getLogger(PreparedUpdateStatement.class);

    /** PreparedUpdateStatement constructor, initializes the home and prepares the update statement. Use this constructor
     * to update a single entry (the selection is extracted from the home's key).
     *
     * @param inHome org.hip.kernel.bom.DomainObjectHome */
    public PreparedUpdateStatement(final DomainObjectHome inHome) {
        super();
        home = inHome;
        initConnection();
        prepareStatement();
    }

    /** PreparedUpdateStatement constructor to update a subset of values of a range of entries selected by the key
     * <code>inWhere</code>.
     *
     * @param inHome DomainObjectHome
     * @param inChange KeyObject containing the fields to change and their new values.
     * @param inWhere KeyObject for the selection. */
    public PreparedUpdateStatement(final DomainObjectHome inHome, final KeyObject inChange, final KeyObject inWhere) {
        super();
        home = inHome;
        initConnection();
        prepareStatement(inChange, inWhere);
        int lPosition = 0;
        lPosition = setValues(inChange, lPosition);
        lPosition = setValues(inWhere, lPosition);
    }

    private int setValues(final KeyObject inKey, int inPosition) {
        for (final SortableItem lItem : inKey.getItems2()) {
            if (isKey((KeyCriterion) lItem)) {
                // recursive
                inPosition = setValues((KeyObject) lItem.getValue(), inPosition);
            }
            else {
                setValueToStatement(lItem.getValue(), ++inPosition);
            }
        }
        return inPosition;
    }

    private boolean isKey(final KeyCriterion inCriterion) {
        return KeyCriterion.NAME_FOR_KEY.equals(inCriterion.getName());
    }

    /** This method executes the update. NOTE: If auto commit is on, the connection is closed. Else the connection has to
     * be committed (or rollbacked) and is closed then. Be CAREFUL to close the Connection in case of failure. (By
     * default, new connections are in auto-commit mode.)
     *
     * @return int the row count
     * @exception java.sql.SQLException
     * @see java.sql.Connection#setAutoCommit(boolean) */
    public int executeUpdate() throws SQLException {
        try {
            int outValue;
            outValue = statement.executeUpdate();
            return outValue;
        } catch (final SQLException exc) {
            LOG.error("Error encountered while processing '{}'!", sqlString, exc);
            throw exc;
        } finally {
            close(statement);
            traceWarnings(connection);
            if (connection.getAutoCommit()) {
                connection.close();
            }
        }
    }

    /** This method was inspired by the DomainObjectImpl.createUpdateString() method. Except that every property will be
     * updated. This has the advantage of using the one preparedUpdateStatement for every update. */
    public final void prepareStatement() {
        final List<String> lSQLs = home.createPreparedUpdates2();
        if (lSQLs.isEmpty()) {
            throw new VError("Table not defined for : " + home.toString());
        }
        if (lSQLs.size() > 1) { // NOPMD by lbenno
            throw new VError("More than one table defined for : " + home.toString());
        }

        sqlString = lSQLs.get(0);

        try {
            statement = connection.prepareStatement(sqlString);
        } catch (final SQLException exc) {
            throw new VError("SQL error while preparing statement : " + exc.toString(), exc);
        }
    }

    private void prepareStatement(final KeyObject inChange, final KeyObject inWhere) {
        sqlString = home.createPreparedUpdate(inChange, inWhere);
        try {
            statement = connection.prepareStatement(sqlString);
        } catch (final SQLException exc) {
            throw new VError("SQL error while preparing statement : " + exc.toString(), exc);
        }
    }

    /** This method was inspired by the DomainObjectImpl.createUpdateString() method.
     *
     * @param inObject org.hip.kernel.bom.DomainObject */
    public void setValues(final DomainObject inObject) { // NOPMD by lbenno

        if (inObject.getHome() != home) {
            throw new Error("The object " + inObject.toString()
                    + " can not be used with this statement which has been prepared with " + home.toString());
        }

        final ObjectDef lDef = home.getObjectDef();
        final String lTable = getTablename(lDef);
        Object lValue = null;

        int i = 0; // NOPMD by lbenno
        try {
            for (final MappingDef lMappingDef : lDef.getMappingDefsForTable2(lTable)) {
                final String lPropertyName = lMappingDef.getPropertyDef().getName();
                if (isKeyPropertyName(lDef, lPropertyName)) {
                    continue;
                }
                i++;

                // Now we get the value
                lValue = inObject.get(lPropertyName);

                if (lValue == null) {
                    final String lValueType = lMappingDef.getPropertyDef().getValueType();
                    statement.setNull(i, convertToSqlType(lValueType));
                }
                else {
                    setValueToStatement(lValue, i);
                }
            }

            // now the primary key
            for (final MappingDef lMappingDef : lDef.getMappingDefsForTable2(lTable)) {
                final String lPropertyName = lMappingDef.getPropertyDef().getName();
                if (isKeyPropertyName(lDef, lPropertyName)) {
                    i++;
                    try {
                        lValue = inObject.get(lPropertyName);
                    } catch (final GettingException exc) {
                        DefaultExceptionWriter.printOut(this, exc, true);
                        lValue = null; // NOPMD by lbenno
                    }
                    setValueToStatement(lValue, i);
                }
            }
        } // try
        catch (final GettingException exc) {
            throw new VError("PreparedUpdateStatement.setValues: " + exc.toString(), exc);
        } catch (final SQLException exc) {
            throw new VError("SQL Error while settings values in a prepared update statement : " + exc.toString(), exc);
        }
    }

    /** Returns true if passed columnName is part of the primary key.
     *
     * @return boolean
     * @param inObjectDef org.hip.kernel.bom.model.ObjectDef
     * @param inPropertyName java.lang.String the name of the property, i.e. the column. */
    private boolean isKeyPropertyName(final ObjectDef inObjectDef, final String inPropertyName) {
        for (final String lKeyName : inObjectDef.getPrimaryKeyDef().getKeyNames2()) {
            if (inPropertyName.equals(lKeyName)) {
                return true;
            }
        }
        return false;
    }

}