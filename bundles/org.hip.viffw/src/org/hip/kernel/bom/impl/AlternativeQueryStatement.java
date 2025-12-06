/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2004-2025, Benno Luthiger

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

import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;

/** An alternative QueryStatement. The only purpose of this QueryStatement is to create and return an
 * AlternativeQueryResult in the method createQueryResult().
 *
 * @see AlternativeQueryResult
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class AlternativeQueryStatement extends AbstractQueryStatement {

    private final transient AlternativeModelFactory factory;

    /** @param home GeneralDomainObjectHome */
    public AlternativeQueryStatement(final GeneralDomainObjectHome home, final AlternativeModelFactory factory) {
        super(home);
        this.factory = factory;
    }

    /** Creates an AlternativeQueryResult.
     *
     * @param home GeneralDomainObjectHome
     * @param result ResultSet
     * @param statement QueryStatement
     * @return QueryResult */
    @Override
    protected QueryResult createQueryResult(final GeneralDomainObjectHome home, final ResultSet result,
            final QueryStatement statement) {
        return new AlternativeQueryResult(home, result, statement, this.factory);
    }
}
