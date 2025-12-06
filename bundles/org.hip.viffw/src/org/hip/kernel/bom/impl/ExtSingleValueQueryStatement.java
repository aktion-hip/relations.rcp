/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006-2014, Benno Luthiger

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

import java.sql.Connection;
import java.sql.SQLException;

import org.hip.kernel.dbaccess.DBAccessConfiguration;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;

/** Statement to query an additional ('external') database for single values.
 *
 * @author Luthiger Created on 29.05.2007 */
public class ExtSingleValueQueryStatement extends SingleValueQueryStatementImpl {
    private transient final DBAccessConfiguration dbConfiguration;

    /** ExtSingleValueQueryStatement constructor.
     *
     * @param inConfiguration {@link DBAccessConfiguration} */
    public ExtSingleValueQueryStatement(final DBAccessConfiguration inConfiguration) {
        super();
        dbConfiguration = inConfiguration;
    }

    @Override
    protected Connection getConnection() throws SQLException, VException { // NOPMD by lbenno
        return DataSourceRegistry.INSTANCE.getConnection(dbConfiguration);
    }

}
