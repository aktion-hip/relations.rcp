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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.Debug;

/** This is the abstract base implementation of the QueryStatment interface. The QueryStatement interface is a kind of
 * wrapper around the JDBC statement. The intention of this interface is the integration with the domain object
 * framework. Use the QueryStatement for SELECTs. Use a CommittableStatement for INSERTs, UPDATEs and DELETEs instead.
 * Note: The Connection is closed and released to the pool after execution.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.QueryStatement
 * @see org.hip.kernel.bom.impl.CommittableStatement */
@SuppressWarnings("serial")
public abstract class AbstractQueryStatement extends VObject implements QueryStatement, Serializable {

    // Instance variables
    private GeneralDomainObjectHome home;
    private String sqlString;
    private AlternativeModelFactory factory;

    /** AbstractQueryStatement constructor, initializing instance variables.
     *
     * @param home org.hip.kernel.bom.GeneralDomainObjectHome */
    protected AbstractQueryStatement(final GeneralDomainObjectHome home) {
        super();
        this.home = home;
    }

    /** Constructor for statements without the need of a home. */
    protected AbstractQueryStatement() {
        super();
    }

    /** QueryStatements are equal if their SQL statements are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof QueryStatement)) {
            return false;
        }

        final String lSQLofCompared = ((AbstractQueryStatement) inObject).getSQLString();
        if (getSQLString() == null) {
            return lSQLofCompared == null;
        }
        else {
            return getSQLString().equals(lSQLofCompared);
        }
    }

    /** Retrieves the <code>Connection</code>. Subclasses may override.
     *
     * @return Connection
     * @throws SQLException
     * @throws VException */
    protected Connection getConnection() throws SQLException, VException {
        return DataSourceRegistry.INSTANCE.getConnection();
    }

    /** Execute the query with this statement.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @exception java.sql.SQLException */
    @Override
    public QueryResult executeQuery() throws SQLException {
        if (getSQLString() == null) {
            return createQueryResult(this.home, null, this);
        }
        else {
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                return createQueryResult(this.home, statement.executeQuery(getSQLString()), this);
            } catch (final VException exc) {
                throw new SQLException(exc.getMessage(), exc);
            }
        }
    }

    /** Executes the given SQL statement, which may return multiple results.
     *
     * @param sql String any SQL statement
     * @return boolean <code>true</code> if the first result is a <code>ResultSet</code> object; <code>false</code> if
     *         it is an update count or there are no results
     * @throws SQLException
     * @see {@link Statement#execute(String)} */
    protected boolean executeSQL(final String sql) throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            return statement.execute(sql);
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        }
    }

    /** Friendly method used for deserialization of <code>QueryResult</code>.
     *
     * @return ResultSet
     * @throws SQLException
     * @see AbstractQueryResult */
    protected ResultSet retrieveStatement() throws SQLException {
        if (getSQLString() == null) {
            return null;
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            return statement.executeQuery(getSQLString());
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        }
    }

    /** Creates the appropriate QueryResult. Subclasses may override this method.
     *
     * @param home GeneralDomainObjectHome
     * @param result ResultSet
     * @param statement QueryStatement
     * @return QueryResult */
    protected QueryResult createQueryResult(final GeneralDomainObjectHome home, final ResultSet result,
            final QueryStatement statement) {
        return this.factory == null ? new DefaultQueryResult(home, result, statement)
                : new AlternativeQueryResult(home, result, statement, this.factory);
    }

    /** Executes an SQL INSERT, UPDATE or DELETE statement.
     *
     * @param inCommit If true, the statement is commited.
     * @return int The row count for INSERT, UPDATE or DELETE statements.
     * @exception java.sql.SQLException */
    @Override
    public int executeUpdate(final boolean inCommit) throws SQLException { // NOPMD by lbenno
        // pre
        if (getSQLString() == null) {
            return 0;
        }

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            final int executed = statement.executeUpdate(getSQLString());
            if (inCommit && !connection.getAutoCommit()) {
                connection.commit();
            }
            return executed;
        } catch (final SQLException exc) { // NOPMD by lbenno
            throw exc;
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        }
    }

    /** This form of the query accepts as an input a valid SQL statement string. This is straightforward.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param sql java.lang.String
     * @exception java.sql.SQLException */
    @Override
    public QueryResult executeQuery(final String sql) throws SQLException {
        setSQLString(sql);
        return executeQuery();

    }

    /** @return java.lang.String */
    @Override
    public String getSQLString() {
        return this.sqlString;
    }

    /** Returns a hash code value for the QueryStatement.
     *
     * @return int */
    @Override
    public int hashCode() {
        if (getSQLString() == null) {
            return 1;
        }
        else {
            return getSQLString().hashCode();
        }
    }

    /** Emits a <code>SHOW TABLES</code> call and returns a collection containing the table names of the database
     * catalog.
     *
     * @return Collection<String> tables of the database catalog.
     * @throws SQLException */
    @Override
    public Collection<String> showTables() throws SQLException {
        final Collection<String> outTables = new ArrayList<>();

        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            if (statement.execute("SHOW TABLES;")) {
                try (ResultSet lResult = statement.getResultSet()) {
                    while (lResult.next()) {
                        outTables.add(lResult.getString(1));
                    }
                }
            }
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        }
        return outTables;
    }

    /** Set a valid SQL statement string to this statement.
     *
     * @param sql java.lang.String */
    @Override
    public QueryStatement setSQLString(final String sql) {
        this.sqlString = sql;
        return this;
    }

    @Override
    public QueryStatement setFactory(final AlternativeModelFactory factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final String lMessage = "SQL=\"" + (getSQLString() == null ? "null" : getSQLString()) + "\"";
        return Debug.classMarkupString(this, lMessage);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.home);
        out.writeObject(this.sqlString);
    }

    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.home = (GeneralDomainObjectHome) inStream.readObject();
        this.sqlString = (String) inStream.readObject();
        // we don't initialize connection and statement here because we expect
        // that retrieveStatement() is called in the process of deserialization.
    }
}