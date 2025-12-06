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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

/** Wrapper for a <code>DirContext</code>.
 *
 * @author Luthiger Created on 03.07.2007
 * @see javax.naming.directory.DirContext */
public class DirContextWrapper {
    private transient final LdapContext context;
    private transient String base;
    private transient int count = -1;

    /** DirContextWrapper constructor with context to be wrapped.
     *
     * @param inContext LdapContext */
    public DirContextWrapper(final LdapContext inContext) {
        super();
        context = inContext;
        count = -1;
    }

    /** Sets the contex't base.
     *
     * @param inBase String */
    public void setBase(final String inBase) {
        base = inBase;
    }

    /** Searches in the named context for entries that satisfy the given search filter. This method call implicitly
     * retrieves the number of entries. This count can be retrieved by calling the <code>{@link #getCount()}</code>
     * method.
     *
     * @param inFilter String the filter expression to use for the search; may not be null
     * @param inControls SearchControls the search controls that control the search. If null, the default search
     *            controls are used (equivalent to (new SearchControls())).
     * @return Iterator<SearchResult>
     * @throws NamingException */
    public Iterator<SearchResult> search(final String inFilter, final SearchControls inControls) throws NamingException {
        count = -1;
        final NamingEnumeration<SearchResult> lResult = context.search(base, inFilter, inControls);
        if (lResult == null) {
            return null;
        }

        // retrieve the number of entries returned
        count = 0;
        final Collection<SearchResult> outResult = new ArrayList<SearchResult>();
        while (lResult.hasMore()) {
            outResult.add(lResult.next());
            count++;
        }
        // traceResponse(context);
        return outResult.iterator();
    }

    /** Closes the context.
     *
     * @throws NamingException */
    public void close() throws NamingException {
        context.close();
    }

    /** Number of entries retrieved in the last search.
     *
     * @return int */
    public int getCount() {
        return count;
    }

    /** Sets the sort control to the context's request.
     *
     * @param inSort SortControl, may be <code>null</code>.
     * @throws NamingException */
    public void setRequestControls(final SortControl inSort) throws NamingException {
        if (inSort == null) {
            return;
        }
        context.setRequestControls(new Control[] { inSort });
    }

}
