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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.util.Debug;

/** This is the implementation of an sql insert statement.
 *
 * @author Benno Luthiger */
public class InsertStatement extends CommittableStatement {

    // Instance variables
    private transient List<String> inserts;

    /** InsertStatement constructor, initializes the home.
     *
     * @param inHome org.hip.kernel.bom.GeneralDomainObjectHome */
    public InsertStatement(final GeneralDomainObjectHome inHome) { // NOPMD by lbenno
        super();
        initConnection();
    }

    /** Execute the insert.<br>
     * NOTE: If auto commit is on, the connection is closed. Else the connection has to be committed (or rollbacked) and
     * is closed then. Be CAREFUL to close the Connection in case of failure.
     *
     * @return Collection of auto-generated keys (Long) of the new entries created by this InsertStatement.
     * @exception java.sql.SQLException */
    public Collection<Long> executeInsert() throws SQLException {

        final Collection<Long> outAutoKeys = new ArrayList<Long>();
        if (inserts != null) {
            Statement lStatement = null;
            try {
                lStatement = connection.createStatement();
                for (final String lSQLInsert : inserts) {
                    lStatement.execute(lSQLInsert, Statement.RETURN_GENERATED_KEYS);
                    try (ResultSet lResult = lStatement.getGeneratedKeys()) {
                        while (lResult.next()) {
                            outAutoKeys.add(Long.valueOf(lResult.getLong(1)));
                        }
                    }
                }
            } finally {
                close(lStatement);
                traceWarnings(connection);
                if (connection.getAutoCommit()) {
                    connection.close();
                }
            }
        }
        return outAutoKeys;
    }

    /** This method prepares the insert by setting a Vector of SQL insert statements (SQL strings).
     *
     * @param inInserts java.util.Vector<String>
     * @deprecated Use <code>setInserts(List)</code> instead */
    @Deprecated
    public void setInserts(final Vector<String> inInserts) { // NOPMD by lbenno
        inserts = inInserts;
    }

    /** This method prepares the insert by setting a Vector of SQL insert statements (SQL strings).
     *
     * @param inInserts java.util.Vector<String> */
    public void setInserts(final List<String> inInserts) {
        inserts = inInserts;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        String lAttributes = "null";
        final StringBuilder lSQLs = new StringBuilder();
        if (inserts != null) {
            lAttributes = String.valueOf(inserts.size());
            for (final String lSQLInsert : inserts) {
                lSQLs.append("<SQL>").append(lSQLInsert).append("</SQL>\n");
            }
        }
        return Debug.classMarkupString(this, "numberOfInserts=" + lAttributes, new String(lSQLs));
    }
}