/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2003-2014, Benno Luthiger

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

import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.sys.VObject;

/** Parameter object to transfer both a QueryResult and the QueryResult's number of rows.
 *
 * Created on 11.08.2003
 * 
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.QueryResult */
public class OutlinedQueryResult extends VObject {
    private final QueryResult queryResult;
    private final int count;

    /** OutlinedQueryResult default constructor.
     * 
     * @deprecated use OutlinedQueryResult(int inCount, QueryResult inQueryResult) instead. */
    @Deprecated
    public OutlinedQueryResult(final QueryResult inQueryResult, final int inCount) {
        this(inCount, inQueryResult);
    }

    /** OutlinedQueryResult constructor.
     * 
     * <p>
     * Note: Count is the first parameter for that the query returning the count can be processed and finished before
     * the query returning the result set is started and passed (with open connection) to this instance. Some database
     * drivers (e.g. for the Derby database) disapprove the closing of a (pooled) connection when a different connection
     * is still open.
     * </p>
     * 
     * @param inCount int The number of entries in the result set.
     * @param inQueryResult QueryResult the result set returned by the query. */
    public OutlinedQueryResult(final int inCount, final QueryResult inQueryResult) {
        super();
        queryResult = inQueryResult;
        count = inCount;
    }

    /** @return QueryResult */
    public QueryResult getQueryResult() {
        return queryResult;
    }

    /** @return int number of rows of the query */
    public int getCount() {
        return count;
    }
}
