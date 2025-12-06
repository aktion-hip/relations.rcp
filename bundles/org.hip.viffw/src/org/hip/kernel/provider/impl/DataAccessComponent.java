/**
	This package is part of the application VIF.
	Copyright (C) 2011-2025, Benno Luthiger

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

package org.hip.kernel.provider.impl;

import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.osgi.service.jdbc.DataSourceFactory;

/** Service component for the <code>DataSourceFactory</code> services.<br />
 * This component registers the available data sources (i.e. JDBC drivers).
 *
 * @author Luthiger */
public class DataAccessComponent { // NOPMD by lbenno

    /** Binding method.
     *
     * @param factory {@link DataSourceFactory} */
    public void registerDataSource(final DataSourceFactory factory) {
        DataSourceRegistry.INSTANCE.register(factory);
    }

    /** Unbinding method.
     *
     * @param factory {@link DataSourceFactory} */
    public void unregisterDataSource(final DataSourceFactory factory) {
        DataSourceRegistry.INSTANCE.unregister(factory);
    }

}
