package org.hip.kernel.bom;

/*
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001, Benno Luthiger

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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;

/**
 * The SingleValueQueryStatement is special QueryStatement.
 * It just supports a SQL Query which returns a selection with a single value,
 * e.g. the count of rows or the maximum value.
 * 
 * @author Benno Luthiger
 */
public interface SingleValueQueryStatement {
	/**
	 * Accepts only a SQL-Statement which returns the count or max
	 * of a selection.
	 *
	 * For example: <code>SELECT count(*) FROM [Tablename] [WHERE statement1....] </code>
	 * 
	 * @param inSQL java.lang.String 
	 * @return java.lang.BigDecimal
	 * @throws java.sql.SQLException
	 */
	BigDecimal executeQuery(String inQuery) throws SQLException;
	
	/**
	 * Returns a collection of values of the row queried, e.g. the number of rows or a maximum value.
	 * Use this method if the statement returns multiple columns.
	 * 
	 * @param inSQL String the SQL statement
	 * @return Collection<Comparable> of BigDecimal or Timestamp
	 * @throws SQLException
	 */
	Collection<Object> executeQuery2(String inSQL) throws SQLException;
}
