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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.VError;
import org.hip.kernel.util.SortableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is the prepared select Statement.
 *
 * @author Benno Luthiger */
public class PreparedSelectStatement extends SqlPreparedStatement {
    private static final Logger LOG = LoggerFactory.getLogger(PreparedSelectStatement.class);

    /** PreparedSelectStatement constructor, initializes the home and prepares the select statement.
     *
     * @param inHome org.hip.kernel.bom.DomainObjectHome
     * @param inKey org.hip.kernel.bom.KeyObject */
    public PreparedSelectStatement(final DomainObjectHome inHome, final KeyObject inKey) {
        super();
        home = inHome;
        initConnection();
        prepareStatement(inKey);
    }

    /** Executes the select statement and returns a QueryResult.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @exception java.sql.SQLException */
    public QueryResult executeQuery(final KeyObject inKey) throws SQLException {
        LOG.debug("Execute Statement: {}", sqlString);

        setValues(inKey);
        try (ResultSet lResult = statement.executeQuery()) {
            return new DefaultQueryResult(home, lResult, null);
        } finally {
            close(statement);
            traceWarnings(connection);
            connection.close();
        }
    }

    /** This method prepares the select statemen. The specified KeyObject holds the data the query is selected for.
     *
     * @param inKey org.hip.kernel.bom.KeyObject */
    public void prepareStatement(final KeyObject inKey) {
        sqlString = home.createPreparedSelectString(inKey);
        try {
            statement = connection.prepareStatement(sqlString);
        } catch (final SQLException exc) {
            throw new VError("SQL error while preparing statement : " + exc.toString(), exc);
        }
    }

    /** This method was inspired by the DomainObjectImpl.createInsertString() method.
     *
     * @param inKey org.hip.kernel.bom.KeyObject */
    public void setValues(final KeyObject inKey) {
        int i = 0; // NOPMD by lbenno
        for (final SortableItem lItem : inKey.getItems2()) {
            i++;
            Object lValue = null;
            lValue = lItem.getValue();
            if (lValue == null) {
                throw new VError("Null can not be used for setting values of prepared select statements");
            }
            else {
                setValueToStatement(lValue, i);
            }
        }
    }

}