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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hip.kernel.bom.AlternativeModel;
import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.exc.DefaultExceptionHandler;

/** An alternative QueryResult.<br>
 * The result of the query is loaded into a Collection. This is done in the instance's constructor.
 *
 * @author Benno Luthiger */
public class AlternativeQueryResult extends AbstractQueryResult {
    private List<AlternativeModel> models = new ArrayList<>();

    /** @param home GeneralDomainObjectHome
     * @param result ResultSet
     * @param statement QueryStatement
     * @param factory {@link AlternativeModelFactory} */
    public AlternativeQueryResult(final GeneralDomainObjectHome home, final ResultSet result,
            final QueryStatement statement, final AlternativeModelFactory factory) {
        super(home); // we pass only the home for that the ResultSet can be evaluated here
        this.models = loadData(factory, result);
    }

    private List<AlternativeModel> loadData(final AlternativeModelFactory factory, final ResultSet result) {
        try {
            final List<AlternativeModel> objects = new ArrayList<>(result.getFetchSize());
            while (result.next()) {
                objects.add(factory.createModel(result));
            }
            return objects;
        } catch (final SQLException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return Collections.emptyList();
    }

    /** @param home GeneralDomainObjectHome */
    public AlternativeQueryResult(final GeneralDomainObjectHome home) {
        super(home);
    }

    /** @return List&lt;AlternativeModel> */
    public List<AlternativeModel> getAlternativeModels() {
        return new ArrayList<>(this.models);
    }

    /** @param max int the size of the list returned
     * @return List&lt;AlternativeModel> */
    public List<AlternativeModel> getAlternativeModels(final int max) {
        return new ArrayList<>(this.models.subList(0, Math.min(max, this.models.size())));
    }

}
