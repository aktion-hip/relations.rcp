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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Set;

import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.TypeDef;
import org.hip.kernel.exc.VError;

/** This is the abstract implementation of prepared Statements.
 *
 * @author Benno Luthiger */
public abstract class SqlPreparedStatement extends CommittableStatement { // NOPMD by lbenno
    protected transient DomainObjectHome home;
    protected transient PreparedStatement statement;

    // this variable should be used to store the sql statement
    // definition for diagnostics purposes
    protected String sqlString;

    /** SqlPreparedStatement default constructor. */
    protected SqlPreparedStatement() {
        super();
    }

    /** This method converts the specified ValueType to an SQL type code defined by java.sql.Types.
     *
     * @return int SQL type code defined by java.sql.Types
     * @param inValueType java.lang.String */
    protected int convertToSqlType(final String inValueType) {
        int outValue;

        if (TypeDef.Date.equals(inValueType)) {
            outValue = Types.DATE;
        } else if (TypeDef.Timestamp.equals(inValueType)) {
            outValue = Types.DATE;
        } else if (TypeDef.String.equals(inValueType)) {
            outValue = Types.CHAR;
        } else if (TypeDef.Number.equals(inValueType)) {
            outValue = Types.INTEGER;
        } else if (TypeDef.Binary.equals(inValueType)) {
            outValue = Types.BINARY;
        } else {
            throw new Error("Value type : " + inValueType + " defined in " + home.toString() + " not supported");
        }

        return outValue;
    }

    /** Returns the name of the table accessed through the specified ObjectDef
     *
     * @return java.lang.String
     * @param inDef org.hip.kernel.bom.model.ObjectDef */
    protected String getTablename(final ObjectDef inDef) {
        final Set<String> lTables = inDef.getTableNames2();
        final String outTable = lTables.toArray(new String[1])[0];
        if (outTable == null) {
            throw new Error("Table not defined for : " + home.toString());
        }
        // TODO: at the moment this class does not support more than one table per ObjectDef
        if (lTables.size() > 1) { // NOPMD by lbenno
            throw new Error("More than one table defined for : " + home.toString());
        }
        return outTable;
    }

    /** Sets a value to the statement after doing type check
     *
     * @param inValue java.lang.Object */
    protected void setValueToStatement(final Object inValue, final int inPosition) { // NOPMD by lbenno
        String lValueString;
        try {
            if (inValue instanceof Timestamp) {
                statement.setTimestamp(inPosition, (Timestamp) inValue);
            } else if (inValue instanceof Date) {
                statement.setDate(inPosition, (Date) inValue);
            } else if (inValue instanceof Number) {
                lValueString = ((Number) inValue).toString();
                if (inValue instanceof BigDecimal) {
                    statement.setBigDecimal(inPosition, (BigDecimal) inValue);
                } else {
                    statement.setInt(inPosition, ((Number) inValue).intValue());
                }
            } else if (inValue instanceof String) {
                lValueString = (String) inValue;
                statement.setString(inPosition, lValueString);
            } else if (inValue instanceof File) {
                final File lFile = (File) inValue;
                try {
                    statement.setBinaryStream(inPosition, new FileInputStream(lFile), (int) lFile.length());
                } catch (final FileNotFoundException exc) {
                    throw new VError(exc.getMessage(), exc);
                }
            } else if (inValue instanceof Blob) {
                statement.setBlob(inPosition, (Blob) inValue);
            } else if (inValue instanceof byte[]) {
                statement.setBytes(inPosition, (byte[]) inValue);
            } else {
                throw new Error("Value : " + inValue.toString() + " (class " + inValue.getClass().getName()
                        + ") defined as key in " + home.toString() + " not supported");
            }
        } catch (final SQLException exc) {
            throw new VError("SQL Error while settings values in a prepared insert statement : " + exc.toString(), exc);
        }
    }
}