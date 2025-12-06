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
import java.sql.Statement;
import java.util.Collection;

import org.hip.kernel.util.Debug;

/** This is the implementation of an sql update statement.
 *
 * @author Benno Luthiger */
public class UpdateStatement extends CommittableStatement {

    // Instance variables
    private transient Collection<String> updates;

    /** UpdateStatement constructor, initializes the home. */
    public UpdateStatement() {
        super();
        initConnection();
    }

    /** Execute the update. NOTE: If auto commit is on, the connection is closed. Else the connection has to be committed
     * (or rollbacked) and is closed then. Be CAREFUL to close the Connection in case of failure.
     *
     * @exception java.sql.SQLException */
    public void executeUpdate() throws SQLException {

        if (updates != null) {
            Statement lStatement = null;
            try {
                lStatement = connection.createStatement();
                for (final String lSQL : updates) {
                    lStatement.executeUpdate(lSQL);
                }
            } finally {
                close(lStatement);
                traceWarnings(connection);
                if (connection.getAutoCommit()) {
                    connection.close();
                }
            }
        }
    }

    /** This method prepares the update by setting a Vector of SQL update statements (SQL strings).
     *
     * @param inUpdates java.util.Collection */
    public void setUpdates(final Collection<String> inUpdates) {
        updates = inUpdates;
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        String lAttributes = "null";
        final StringBuffer lSQLs = new StringBuffer();
        if (updates != null) {
            lAttributes = String.valueOf(updates.size());
            for (final String lUpdate : updates) {
                lSQLs.append("<SQL>" + lUpdate + "</SQL>\n");
            }
        }
        return Debug.classMarkupString(this, "numberOfUpdates=" + lAttributes, new String(lSQLs));
    }
}
