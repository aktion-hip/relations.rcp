/**
 This package is part of persistency framework used for the application VIF.
 Copyright (C) 2004-2014, Benno Luthiger

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
package org.hip.kernel.bom;

import java.sql.SQLException;

/** Interface for Homes to create SELECT ... UNION ... and other SQL set operations.
 *
 * @author Benno Luthiger Created on Oct 31, 2004 */
public interface SetOperatorHome extends Home {
    // Constants
    int UNION = 0;
    int UNION_ALL = 1;
    int INTERSECT = 2;
    int MINUS = 3;

    /** Add the set for the operation.
     *
     * @param inHome GeneralDomainObjectHome
     * @param inKey KeyObject
     * @throws BOMException */
    void addSet(GeneralDomainObjectHome inHome, KeyObject inKey) throws BOMException;

    /** @param inHome GeneralDomainObjectHome
     * @param inKey KeyObject
     * @param inOrder OrderObject
     * @throws BOMException */
    void addSet(GeneralDomainObjectHome inHome, KeyObject inKey, OrderObject inOrder) throws BOMException;

    /** @param inHome GeneralDomainObjectHome
     * @param inOrder OrderObject
     * @throws BOMException */
    void addSet(GeneralDomainObjectHome inHome, OrderObject inOrder) throws BOMException;

    /** Sets the set's SQL select string.
     *
     * @param inSQL String */
    void setSelectString(String inSQL);

    /** Sets the set's SQL select count string.
     *
     * @param inSQL String */
    void setCountString(String inSQL);

    /** Executes the set operation and returns the QueryResult.
     *
     * @param inOrder OrderObject the Order of the overall query.
     * @return QueryResult
     * @throws SQLException
     * @throws BOMException */
    QueryResult select(OrderObject inOrder) throws SQLException, BOMException;

    /** Executes the set operation and returns the QueryResult.
     *
     * @return QueryResult
     * @throws SQLException
     * @throws BOMException */
    QueryResult select() throws SQLException, BOMException;

    /** Returns the number of rows in the resulting set.
     *
     * @return int number of rows.
     * @throws SQLException */
    int getCount() throws SQLException;
}
