/**
This package is part of Relations project.
Copyright (C) 2006-2014, Benno Luthiger

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.bom.impl;

import java.sql.SQLException;
import java.sql.Statement;

/** This object can be used to execute SQL statements like <code>CREATE TABLE ...</code> or <code>DROP TABLE ...</code>.
 *
 * @author Luthiger Created on 28.10.2006 */
@SuppressWarnings("serial")
public class DefaultStatement extends AbstractQueryStatement { // NOPMD by lbenno 

    /** Executes the given SQL statement, which may return multiple results.
     *
     * @param inSQL String any SQL statement
     * @return boolean <code>true</code> if the first result is a <code>ResultSet</code> object; <code>false</code> if
     *         it is an update count or there are no results
     * @throws SQLException
     * @see {@link Statement#execute(String)} */
    public boolean execute(final String inSQL) throws SQLException {
        if (inSQL != null) {
            return executeSQL(inSQL);
        }
        return false;
    }

}
