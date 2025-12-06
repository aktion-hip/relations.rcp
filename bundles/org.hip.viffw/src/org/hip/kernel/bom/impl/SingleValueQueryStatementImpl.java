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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

import org.hip.kernel.bom.SingleValueQueryStatement;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** See the interface for detailed description.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.SingleValueQueryStatement */
public class SingleValueQueryStatementImpl extends VObject implements SingleValueQueryStatement { // NOPMD by lbenno

    /** Returns the single value queried, e.g. the number of rows or a maximum value.
     *
     * @param sql java.lang.String
     * @return java.lang.BigDecimal
     * @throws java.sql.SQLException
     * @see org.hip.kernel.bom.SingleValueQueryStatement */
    @Override
    public BigDecimal executeQuery(final String sql) throws SQLException {
        // pre
        if (VSys.assertNotNull(this, "executeQuery", sql)) {
            return BigDecimal.valueOf(0);
        }

        try {
            return (BigDecimal) prepareDataRetrieval(sql).iterator().next();
        } catch (final ClassCastException exc) { // NOPMD by lbenno
            // left empty intentionally
        }
        return BigDecimal.valueOf(0);
    }

    /** Returns a collection of values of the row queried, e.g. the number of rows or a maximum value. Use this method
     * if the statement returns multiple columns.
     *
     * @param sql String the SQL statement
     * @return Collection of BigDecimal or Timestamp
     * @throws SQLException */
    @Override
    public Collection<Object> executeQuery2(final String sql) throws SQLException {
        // pre
        if (VSys.assertNotNull(this, "executeQuery", sql)) {
            return new ArrayList<>();
        }
        return prepareDataRetrieval(sql);
    }

    private Collection<Object> prepareDataRetrieval(final String sql) throws SQLException {
        Collection<Object> retrieved = new ArrayList<>();
        retrieved.add(BigDecimal.valueOf(0));

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            final ResultSet result = statement.executeQuery(sql);
            if (result.next()) {
                retrieved = loadFromResultSet(result);
            }
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        }
        return retrieved;
    }

    /** Subclasses may override.
     *
     * @return Connection
     * @throws SQLException
     * @throws VException */
    protected Connection getConnection() throws SQLException, VException {
        return DataSourceRegistry.INSTANCE.getConnection();
    }

    private Collection<Object> loadFromResultSet(final ResultSet result) throws SQLException {
        final Collection<Object> retrieved = new ArrayList<>();
        final ResultSetMetaData metaData = result.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            switch (metaData.getColumnType(i)) {
                case Types.BIGINT:
                case Types.DECIMAL:
                case Types.DOUBLE:
                case Types.FLOAT:
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    BigDecimal lNumeric = result.getBigDecimal(i);
                    if (result.wasNull()) {
                        lNumeric = BigDecimal.valueOf(0);
                    }
                    retrieved.add(lNumeric);
                    break;
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    Timestamp lTime = result.getTimestamp(i);
                    if (result.wasNull()) {
                        lTime = new Timestamp(0);
                    }
                    retrieved.add(lTime);
                    break;
                default:
                    retrieved.add(result.getBigDecimal(i));
                    break;
            }
        }
        return retrieved;
    }
}