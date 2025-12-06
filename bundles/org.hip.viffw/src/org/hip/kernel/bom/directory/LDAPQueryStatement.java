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

package org.hip.kernel.bom.directory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.SortControl;

import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.impl.AbstractQueryResult;
import org.hip.kernel.exc.VException;

/** Querying the LDAP means searching the directory.
 *
 * @author Luthiger Created on 03.07.2007 */
@SuppressWarnings("serial")
public class LDAPQueryStatement implements QueryStatement, Serializable {
    private LDAPObjectHome home;
    private String filter;
    private String baseDir = ""; // NOPMD by lbenno
    private final SearchControls controls;
    private SortControl sort;

    /** LDAPQueryStatement constructor with home an base directory of LDAP context.
     *
     * @param inHome LDAPObjectHome
     * @param inBaseDir String */
    public LDAPQueryStatement(final LDAPObjectHome inHome, final String inBaseDir) {
        this.home = inHome;
        this.baseDir = inBaseDir;
        this.controls = new SearchControls();
    }

    @Override
    public QueryResult executeQuery() throws SQLException { // NOPMD by lbenno
        DirContextWrapper lContext = null;
        try {
            if (getSQLString() != null) {
                lContext = createContext();
                lContext.setRequestControls(this.sort);
                final Iterator<SearchResult> lResult = lContext.search(getSQLString(), this.controls);
                return createSearchResult(this.home, lResult, lContext.getCount(), this);
            }
        } catch (final NamingException | VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        } finally {
            if (lContext != null) {
                try {
                    lContext.close();
                } catch (final NamingException exc) { // NOPMD by lbenno
                    // intentionally left empty
                }
            }
        }
        return createSearchResult(this.home, null, -1, this);
    }

    /** Limits the number of results
     *
     * @param inLimit LimitObject */
    public void setLimit(final LimitObject inLimit) {
        final int lLimit = (Integer) inLimit.getArguments()[0];
        this.controls.setCountLimit(lLimit);
    }

    /** Makes the results returned sorted
     *
     * @param inSortBy String[] list of attributes in ascending order.
     * @throws IOException */
    public void setOrder(final String... inSortBy) throws IOException {
        this.sort = new SortControl(inSortBy, Control.NONCRITICAL);
    }

    private QueryResult createSearchResult(final LDAPObjectHome inHome, final Iterator<SearchResult> inResult,
            final int inCount, final LDAPQueryStatement inStatement) {
        return new LDAPQueryResult(inHome, inResult, inCount, inStatement);
    }

    protected DirContextWrapper createContext() throws NamingException, VException { // NOPMD by lbenno
        return LDAPContextManager.getInstance().getContext(this.baseDir, createControls());
    }

    /** @return {@link Control} Array */
    protected Control[] createControls() {
        if (this.sort == null) {
            return null;
        }
        return new Control[] { this.sort };
    }

    @Override
    public QueryResult executeQuery(final String inFilter) throws SQLException { // NOPMD by lbenno
        setSQLString(inFilter);
        return executeQuery();
    }

    /** Not used.
     *
     * @see org.hip.kernel.bom.QueryStatement#executeUpdate(boolean) */
    @Override
    public int executeUpdate(final boolean inCommit) throws SQLException {
        return 0;
    }

    /** In the LDAP case, this method returns the search filter.
     *
     * @see org.hip.kernel.bom.QueryStatement#getSQLString() */
    @Override
    public String getSQLString() {
        return this.filter;
    }

    /** In the LDAP case, this method is used to set the search filter.
     *
     * @see org.hip.kernel.bom.QueryStatement#setSQLString(java.lang.String) */
    @Override
    public QueryStatement setSQLString(final String inFilter) {
        this.filter = inFilter;
        return this;
    }

    /** Friendly method used for deserialization of <code>QueryResult</code>.
     *
     * @return Iterator<SearchResult>
     * @throws SQLException
     * @see AbstractQueryResult */
    protected Iterator<SearchResult> retrieveStatement() throws SQLException {
        if (getSQLString() == null) {
            return null;
        }

        DirContextWrapper lContext = null;
        try {
            lContext = createContext();
            return lContext.search(getSQLString(), null);
        } catch (final NamingException exc) {
            throw new SQLException(exc.getExplanation(), exc);
        } catch (final VException exc) {
            throw new SQLException(exc.getMessage(), exc);
        } finally {
            if (lContext != null) {
                try {
                    lContext.close();
                } catch (final NamingException exc) { // NOPMD by lbenno
                    // intentionally left empty
                }
            }
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.home);
        out.writeObject(this.filter);
    }

    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.home = (LDAPObjectHome) inStream.readObject();
        this.filter = (String) inStream.readObject();
    }

    @Override
    public Collection<String> showTables() throws SQLException { // NOPMD by lbenno
        return new ArrayList<String>();
    }

}
