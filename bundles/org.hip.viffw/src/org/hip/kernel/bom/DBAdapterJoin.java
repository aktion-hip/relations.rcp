/**
	This package is part of the framework used for the application VIF.
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
package org.hip.kernel.bom;

/** Interface for Adapter classes for joined domain objects. For that the framework can connect to a database of a
 * specific type (e.g. MySQL, PostgreSQL, Oracle etc.), an adapter class must be written implementing this interface.
 * The aim of these adapter classes then is to produce SQL strings in the dialects specific for the database running on
 * the target system.
 *
 * Created on 30.08.2002
 *
 * @author Benno Luthiger */
public interface DBAdapterJoin { // NOPMD

    /** Returns the SQL SELECT statement doing the JOIN.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    String createSelectAllSQL() throws BOMException;

    /** Returns the SQL SELECT statement matching the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    String createSelectSQL(KeyObject inKey, GeneralDomainObjectHome inHome) throws BOMException;

    /** Returns the SQL SELECT statement matching the specified key ordered by the specified object.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    String createSelectSQL(KeyObject inKey, OrderObject inOrder, GeneralDomainObjectHome inHome) throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified key and meeting the specified having
     * criterion sorted by the specified order. <b>Note</b>: To specify a clause only using e.g. the HAVING part, you
     * can provide empty objects for key and order.
     *
     * @param inKey KeyObject
     * @param inOrder OrderObject
     * @param inHaving HavingObject
     * @return String
     * @throws BOMException */
    String createSelectSQL(KeyObject inKey, OrderObject inOrder, HavingObject inHaving, GeneralDomainObjectHome inHome)
            throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified constraints.
     *
     * @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @param inHome {@link GeneralDomainObjectHome}
     * @return String the SQL statement
     * @throws BOMException */
    String createSelectSQL(KeyObject inKey, OrderObject inOrder, HavingObject inHaving, GroupByObject inGroupBy,
            GeneralDomainObjectHome inHome) throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified constraints.
     *
     * @param inKey {@link KeyObject}
     * @param inLimit {@link LimitObject}
     * @param inHome {@link GeneralDomainObjectHome}
     * @return String the SQL statement
     * @throws BOMException */
    String createSelectSQL(KeyObject inKey, LimitObject inLimit, GeneralDomainObjectHome inHome) throws BOMException;

    /** Returns the SQL SELECT statement ordered by the specified object.
     *
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    String createSelectSQL(OrderObject inOrder, GeneralDomainObjectHome inHome) throws BOMException;

    /** Returns the SQL statement counting the entries in the join.
     *
     * @return String the SQL statement
     * @throws org.hip.kernel.bom.BOMException */
    String createCountAllSQL() throws BOMException;

    /** Returns the SQL statement counting the entries in the join matching the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return String the SQL statement
     * @throws org.hip.kernel.bom.BOMException */
    String createCountSQL(KeyObject inKey, GeneralDomainObjectHome inHome) throws BOMException;

    /** Returns the SQL statement counting the entries in the join matching the specified key.
     *
     * @param inKey {@link KeyObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @param inHome {@link GeneralDomainObjectHome}
     * @return String the SQL statement
     * @throws BOMException */
    String createCountSQL(KeyObject inKey, HavingObject inHaving, GroupByObject inGroupBy,
            GeneralDomainObjectHome inHome) throws BOMException;

    /** Resets the SQL cache for that it can be recreated again. */
    void reset();
}
