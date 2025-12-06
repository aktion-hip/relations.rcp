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

import java.util.List;
import java.util.Vector;

import org.hip.kernel.bom.impl.ModifierStrategy;

/** Interface for Adapter classes for simple domain objects. For that the framework can connect to a database of a
 * specific type (e.g. MySQL, PostgreSQL, Oracle etc.), an adapter class must be written implementing this interface.
 * The aim of these adapter classes then is to produce SQL strings in the dialects specific for the database running on
 * the target system.
 *
 * Created on 30.08.2002
 *
 * @author Benno Luthiger */
public interface DBAdapterSimple { // NOPMD

    /** Returns the SQL string to insert a new entry.
     *
     * @param inTableName java.lang.String
     * @param inDomainObject org.hip.kernel.bom.DomainObject */
    String createInsertString(String inTableName, DomainObject inDomainObject);

    /** Returns a Vector of prepared SQL strings to insert a new entry.
     *
     * @param java.util.Vector<String>
     * @deprecated Use createPreparedInserts2() instead */
    @Deprecated
    Vector<String> createPreparedInserts(); // NOPMD

    /** Returns a Vector of prepared SQL strings to insert a new entry.
     *
     * @param List&lt;String> */
    List<String> createPreparedInserts2();

    /** Returns the SQL string to delete an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createDeleteString(String inTableName, DomainObject inDomainObject);

    /** Returns the SQL string to update an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createUpdateString(String inTableName, DomainObject inDomainObject);

    /** Returns the SQL update string to update entries having the specified characteristics to the specified values.
     *
     * @param inHome DomainObjectHome
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String UPDATE table SET field=value WHERE field=value; */
    String createUpdateString(DomainObjectHome inHome, KeyObject inChange, KeyObject inWhere);

    /** Returns the SQL string to prepare an update of an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createPreparedUpdateString(String inTableName, DomainObject inDomainObject);

    /** Returns a Vector of prepared SQL strings to update entries.
     *
     * @param java.util.Vector<String>
     * @deprecated Use createPreparedUpdates2() instead */
    @Deprecated
    Vector<String> createPreparedUpdates(); // NOPMD

    /** Returns a List of prepared SQL strings to update entries.
     *
     * @param List&lt;String> */
    List<String> createPreparedUpdates2();

    /** Returns a SQL command usable for a prepared update.
     *
     * @param inHome DomainObjectHome
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String */
    String createPreparedUpdate(DomainObjectHome inHome, KeyObject inChange, KeyObject inWhere);

    /** Creates select SQL string counting all table entries corresponding to the specified home.
     *
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome;
     * @return java.lang.String */
    String createCountAllString(DomainObjectHome inDomainObjectHome);

    /** Creates the select SQL string counting all table entries with the specified key corresponding to the specified
     * home.
     *
     * @param inKey {@link KeyObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return java.lang.String */
    String createCountString(KeyObject inKey, DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL count */
    String createCountString(KeyObject inKey, HavingObject inHaving, GroupByObject inGroupBy,
            DomainObjectHome inDomainObjectHome);

    /** Creates the select SQL string returning the calculated value(s) according to the modify strategy.
     *
     * @param inStrategy ModifierStrategy
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome
     * @return String */
    String createModifiedString(ModifierStrategy inStrategy, DomainObjectHome inDomainObjectHome);

    /** Creates the select SQL string returning the calculated value(s) according to the modify strategy.
     *
     * @param inStrategy ModifierStrategy
     * @param inKey org.hip.kernel.bom.KeyObject for the sub selection.
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome
     * @return String */
    String createModifiedString(ModifierStrategy inStrategy, KeyObject inKey, DomainObjectHome inDomainObjectHome);

    /** This method looks for all key columns of the table mapped to the DomainObject managed by the specified home and
     * creates a list COUNT(keyField). Instead of sending SELECT COUNT(*) FROM ... this SQL-sequence can be used to
     * count all entries in a table with better performance.
     *
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome;
     * @return java.lang.String */
    String createKeyCountColumnList(DomainObjectHome inDomainObjectHome);

    /** Creates select SQL string to fetch all domainobjects.
     *
     * @return java.lang.String */
    String createSelectAllString();

    /** Creates select SQL string to fetch all domain objects with the specified key managed by the specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    String createSelectString(KeyObject inKey, DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inDomainObjectHome {@link StatisticsHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, StatisticsHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, OrderObject inOrder, DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, OrderObject inOrder, StatisticsHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving,
            DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving,
            StatisticsHome inDomainObjectHome);

/**
     *
     * @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject
     * @param inGroupBy {@link GroupByObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select
     */
    String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving, GroupByObject inGroupBy,
            DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving, GroupByObject inGroupBy,
            StatisticsHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inHaving {@link HavingObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, HavingObject inHaving, DomainObjectHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inHaving {@link HavingObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, HavingObject inHaving, StatisticsHome inDomainObjectHome);

    /** @param inOrder {@link OrderObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(OrderObject inOrder, DomainObjectHome inDomainObjectHome);

    /** @param inOrder {@link OrderObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(OrderObject inOrder, StatisticsHome inDomainObjectHome);

    /** @param inKey {@link KeyObject}
     * @param inLimit {@link LimitObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String SQL select */
    String createSelectString(KeyObject inKey, LimitObject inLimit, DomainObjectHome inDomainObjectHome);

    /** Creates select SQL string to delete all domain objects with the specified key managed by the specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    String createDeleteString(KeyObject inKey, DomainObjectHome inDomainObjectHome);

    /** Creates the prepared select SQL string to fetch all domain objects with the specified key managed by the
     * specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome
     * @return String SQL select */
    String createPreparedSelectString(KeyObject inKey, DomainObjectHome inDomainObjectHome);

    /** Creates the prepared select SQL string to fetch all domain objects with the specified key managed by the
     * specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome {@link StatisticsHome}
     * @return String SQL select */
    String createPreparedSelectString(KeyObject inKey, StatisticsHome inDomainObjectHome);
}