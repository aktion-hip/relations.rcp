/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2001-2025, Benno Luthiger

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

import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryStatement;

/** This is the default implementation of the query result bahaviour.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.QueryResult */
@SuppressWarnings("serial")
public class DefaultQueryResult extends AbstractQueryResult {
    /** DefaultQueryResult default constructor.
     *
     * @param home org.hip.kernel.bom.GeneralDomainObjectHome
     * @param result java.sql.ResultSet
     * @param statement org.hip.kernel.bom.QueryStatement */
    public DefaultQueryResult(final GeneralDomainObjectHome home, final ResultSet result,
            final QueryStatement statement) {
        super(home, result, statement);
    }
}
