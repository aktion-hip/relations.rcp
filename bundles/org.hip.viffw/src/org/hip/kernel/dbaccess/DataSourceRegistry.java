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

package org.hip.kernel.dbaccess;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.hip.kernel.bom.impl.DBAdapterType;
import org.hip.kernel.exc.VException;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The registry of <code>DataSourceFactory</code> components.
 *
 * @author Luthiger Created: 26.01.2012 */
public enum DataSourceRegistry {
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(DataSourceRegistry.class);

    private final Map<String, FactoryWrapper> factories = new ConcurrentHashMap<>();
    private DBAccessConfiguration activeConfiguration;
    private DataSourceFactory activeFactory = new NOOpFactory();

    /** Registers the specified factory to the registry.
     *
     * @param factory {@link DataSourceFactory} */
    public void register(final DataSourceFactory factory) {
        try {
            final FactoryWrapper wrapper = new FactoryWrapper(factory);
            this.factories.put(wrapper.getDriverName(), wrapper);
            LOG.debug("Registered DB access \"{}\".", wrapper.getDriverName());
        } catch (final SQLException exc) {
            LOG.error("Error encountered while registering DB access!", exc);
        }
    }

    /** Unregisters the specified factory from the registry.
     *
     * @param factory {@link DataSourceFactory} */
    public void unregister(final DataSourceFactory factory) {
        for (final FactoryWrapper wrapper : this.factories.values()) {
            if (wrapper.getFactory().equals(factory)) {
                this.factories.remove(wrapper.getDriverName());
                return;
            }
        }
    }

    /** Returns the <code>DataSourceFactory</code> suitable to the specified properties.
     *
     * @param properties {@link DBAccessConfiguration}
     * @return {@link DataSourceFactory}
     * @throws VException */
    public DataSourceFactory getFactory(final DBAccessConfiguration properties) throws VException {
        final FactoryWrapper factory = this.factories.get(properties.getDBSourceID());
        if (factory != null) {
            return factory.getFactory();
        }
        LOG.error("Configuration problem: no data access bundle provided for \"{}\".", properties.getDBSourceID());
        throw new VException(String.format("Configuration problem: no data access bundle provided for \"%s\".", // NOPMD
                properties.getDBSourceID()));
    }

    /** Returns the <code>DataSource</code> suitable to the specified properties.
     *
     * @param properties {@link DBAccessConfiguration}
     * @return {@link DataSource}
     * @throws SQLException
     * @throws VException */
    public DataSource getDataSource(final DBAccessConfiguration properties) throws SQLException, VException {
        final FactoryWrapper factory = this.factories.get(properties.getDBSourceID());
        if (factory != null) {
            return factory.getFactory().createDataSource(properties.getProperties());
        }
        LOG.error("Configuration problem: no data access bundle provided for \"{}\".", properties.getDBSourceID());
        throw new VException(String.format("Configuration problem: no data access bundle provided for \"%s\".",
                properties.getDBSourceID()));
    }

    /** Returns the <code>ConnectionPoolDataSource</code> suitable to the specified properties.
     *
     * @param inProperties {@link DBAccessConfiguration}
     * @return {@link ConnectionPoolDataSource}
     * @throws SQLException
     * @throws VException */
    public ConnectionPoolDataSource getConnectionPoolDataSource(final DBAccessConfiguration inProperties)
            throws SQLException, VException {
        final FactoryWrapper factory = this.factories.get(inProperties.getDBSourceID());
        if (factory != null) {
            return factory.getFactory().createConnectionPoolDataSource(inProperties.getProperties());
        }
        LOG.error("Configuration problem: no data access bundle provided for \"{}\".", inProperties.getDBSourceID());
        throw new VException(String.format("Configuration problem: no data access bundle provided for \"%s\".",
                inProperties.getDBSourceID()));
    }

    /** Setter for the active <code>DBAccessConfiguration</code>. This setter should be called by the application's
     * preferences handler.
     *
     * @param activeConfiguration {@link DBAccessConfiguration}
     * @return <code>true</code> if the registry configured to return <code>DataSource</code> instances */
    public boolean setActiveConfiguration(final DBAccessConfiguration activeConfiguration) {
        this.activeConfiguration = activeConfiguration;
        synchronized (this) {
            final FactoryWrapper factory = this.factories.get(activeConfiguration.getDBSourceID());
            if (factory == null) {
                return false;
            } else {
                this.activeFactory = factory.getFactory();
                return true;
            }
        }
    }

    /** Checks the initialization, i.e. the method <code>DataSourceRegistry.setActiveConfiguration()</code> has been
     * called..
     *
     * @return boolean <code>true</code> if this instance is initialized (with <code>DBAccessConfiguration</code>) */
    public boolean isInitialized() {
        return this.activeConfiguration != null;
    }

    /** Shortcut method to set the active <code>DataSourceFactory</code> directly.
     *
     * @param factory {@link DataSourceFactory} */
    public void setFactory(final DataSourceFactory factory) {
        this.activeFactory = factory;
    }

    private DataSourceFactory getFactory() throws VException {
        if (this.activeConfiguration == null) {
            throw new VException("Configuration problem: no DB access configuration provided");
        }

        if (NOOpFactory.class.equals(this.activeFactory.getClass())) {
            if (this.activeConfiguration.getDBSourceID() == null) {
                throw new VException(String.format("Configuration problem: no data access bundle provided for \"%s\".",
                        this.activeConfiguration.getDBSourceID()));
            }
            final String[] parts = this.activeConfiguration.getDBSourceID().split("/");
            final FactoryWrapper factory = this.factories.get(parts[0]);
            if (factory == null) {
                throw new VException(String.format("Configuration problem: no data access bundle provided for \"%s\".",
                        this.activeConfiguration.getDBSourceID()));
            }
            this.activeFactory = factory.getFactory();
        }
        return this.activeFactory;
    }

    /** Returns a pooled <code>Connection</code> based on the active <code>DBAccessConfiguration</code>.
     *
     * @return {@link Connection}
     * @throws SQLException
     * @throws VException */
    public Connection getConnection() throws SQLException, VException {
        return getFactory().createConnectionPoolDataSource(this.activeConfiguration.getProperties()).getPooledConnection()
                .getConnection();
    }

    /** Returns a pooled <code>Connection</code> based on the specified <code>DBAccessConfiguration</code>.
     *
     * @param inConfiguration {@link DBAccessConfiguration} the DB access configuration
     * @return {@link Connection}
     * @throws SQLException
     * @throws VException */
    public Connection getConnection(final DBAccessConfiguration inConfiguration) throws SQLException, VException {
        return getConnectionPoolDataSource(inConfiguration).getPooledConnection().getConnection();
    }

    /** Returns the DB adapter type matching the actual DB access configuration.
     *
     * @return {@link DBAdapterType} */
    public DBAdapterType getAdapterType() {
        if (this.activeConfiguration == null) {
            return DBAdapterType.DB_TYPE_MYSQL;
        }
        return getAdapterType(this.activeConfiguration);
    }

    /** Returns the DB adapter type matching the specified DB access configuration.
     *
     * @param inDBConfiguration {@link DBAccessConfiguration}
     * @return {@link DBAdapterType} */
    public DBAdapterType getAdapterType(final DBAccessConfiguration inDBConfiguration) {
        for (final DBAdapterType lType : DBAdapterType.values()) {
            if (lType.isOfType(inDBConfiguration.getDBSourceID())) {
                return lType;
            }
        }
        return DBAdapterType.DB_TYPE_MYSQL;
    }

    // ---

    private static class FactoryWrapper {
        private final String driverName;
        private final DataSourceFactory factory;

        protected FactoryWrapper(final DataSourceFactory factory) throws SQLException {
            this.factory = factory;
            this.driverName = factory.createDriver(new Properties()).getClass().getName();
        }

        protected String getDriverName() {
            return this.driverName;
        }

        protected DataSourceFactory getFactory() {
            return this.factory;
        }
    }

    /** No-operation instance of <code>DataSourceFactory</code> */
    private static class NOOpFactory implements DataSourceFactory {
        @Override
        public DataSource createDataSource(final Properties props) throws SQLException { // NOPMD by lbenno
            return null;
        }

        @Override
        public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException { // NOPMD
            return null;
        }

        @Override
        public XADataSource createXADataSource(final Properties props) throws SQLException { // NOPMD by lbenno
            return null;
        }

        @Override
        public Driver createDriver(final Properties props) throws SQLException { // NOPMD by lbenno
            return null;
        }
    }

}
