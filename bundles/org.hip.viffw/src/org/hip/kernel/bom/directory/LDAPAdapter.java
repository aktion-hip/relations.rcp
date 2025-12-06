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

import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.AbstractCriteriumRenderer;
import org.hip.kernel.bom.impl.LDAPValueStrategy;

/** Adapter to create the filters used to retrieve items from a LDAP store. This class is used by
 * <code>LDAPObjectHome</code>.
 *
 * @author Luthiger Created on 07.07.2007
 * @see LDAPObjectHome */
public class LDAPAdapter { // NOPMD

    /** Creates the LDAP filter expression by interpreting the specified <code>KeyObject</code>.
     *
     * @param inKey KeyObject
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return String the LDAP filter expression */
    public String createSelectString(final KeyObject inKey, final GeneralDomainObjectHome inDomainObjectHome) {
        return createFilter(inKey, inDomainObjectHome);
    }

    private String createFilter(final KeyObject inKey, final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inKey not null
        if (inKey == null) {
            return "";
        }
        if (inKey.getItems2().size() == 0) {
            return "";
        }

        inKey.setCriteriumRenderer(new LDAPCriteriumRenderer());
        inKey.setGetValueStrategy(new LDAPValueStrategy());
        final StringBuilder outFilters = new StringBuilder("(");
        outFilters.append(inKey.render2(inDomainObjectHome)).append(')');
        return new String(outFilters);
    }

    private class LDAPCriteriumRenderer extends AbstractCriteriumRenderer { // NOPMD
        @Override
        public StringBuffer render() { // NOPMD
            return new StringBuffer(render2());
        }

        @Override
        public StringBuilder render2() { // NOPMD
            final StringBuilder outSQL = new StringBuilder(operand1);
            outSQL.append(comparison).append(operand2);
            return outSQL;
        }
    }

    /** To select all items on a LDAP server, an empty filter string is returned.
     *
     * @return String */
    public String createSelectAllString() {
        return "";
    }

}
