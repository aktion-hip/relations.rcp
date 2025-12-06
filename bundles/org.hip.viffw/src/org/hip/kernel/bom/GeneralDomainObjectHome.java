/**
	This package is part of the servlet framework used for the application VIF.
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

import java.sql.SQLException;
import java.util.Iterator;

import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;

/** This interface defines the general responsibilities of "Homes" for domain objects. General in this context means the
 * whole functionality for read only domain objects (i.e. no insert and delete).
 *
 * @author Benno Luthiger */
public interface GeneralDomainObjectHome extends Home { // NOPMD
    // Class variables
    String dftSerializer = "dft"; // NOPMD // Key name for default Serializer
    String xmlSerializer = "xml"; // NOPMD // Key name for XML Serializer

    /** This method creates a QueryStatement as part of the frameworks QueryService.
     *
     * @return org.hip.kernel.bom.QueryStatement */
    QueryStatement createQueryStatement();

    /** Returns the name of the column which is mapped to the specified property.
     *
     * @return java.lang.String
     * @param inPropertyName java.lang.String */
    String getColumnNameFor(String inPropertyName);

    /** Returns number of entries in database corresponding to this home.
     *
     * @return numer of database entries.
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    int getCount() throws SQLException, BOMException;

    /** Returns number of entries in database corresponding to this home and the given key.
     *
     * @param org.hip.kernel.bom.KeyObject
     * @return numer of database entries.
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    int getCount(KeyObject inKeyObject) throws SQLException, BOMException;

    /** Returns number of entries in database corresponding to this home and the given filter. <b>Note:</b> You can pass
     * empty filtering objects e.g. if you only need GROUP BY.
     *
     * @param inKeyObject KeyObject
     * @param inHaving HavingObject
     * @param inGroupBy GroupByObject
     * @return int
     * @throws SQLException
     * @throws BOMException */
    int getCount(KeyObject inKeyObject, HavingObject inHaving, GroupByObject inGroupBy) throws SQLException,
    BOMException;

    /** Returns the name of the objects which the concrete home can create. This method is abstract and must be
     * implemented by concrete subclasses.
     *
     * @return java.lang.String */
    String getObjectClassName();

    /** Returns the Object definition.
     *
     * @return org.hip.kernel.bom.model.ObjectDef */
    ObjectDef getObjectDef();

    /** Returns the mappings of hidden fields or an empty String.
     *
     * @param inPropertyName String
     * @return String */
    String getHidden(String inPropertyName);

    /** Returns the object definition string of the class managed by this home.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inPropertyName java.lang.String */
    PropertyDef getPropertyDef(String inPropertyName);

    /** This method returns the PropertyDef object which describes the behaviour of the Property object mapped to the
     * tablecolumne named inColumnName.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inColumnName java.lang.String */
    PropertyDef getPropertyDefFor(String inColumnName);

    /** Returns the testObjects created by the Homes.
     *
     * @return java.util.Iterator */
    Iterator<Object> getTestObjects();

    /** Returns true if this domain object home caches the domain objects fetched from database over findByKey.
     *
     * @return boolean */
    boolean getUseCache(); // NOPMD

    /** Returns an instance of the visitor named key. This visitor can be used to serialize the DomainObject managed by
     * the Home class.
     *
     * @return org.hip.kernel.bom.DomainObjectVisitor
     * @param key java.lang.String */
    DomainObjectVisitor getVisitor(String visitorName);

    /** This method can be used to initialize a new instance of the class. */
    void initialize();

    /** Use this method to release a DomainObject. Released objects can act as cache and, therefore, instead of creating
     * a new instance of a DomainObject from scratch, can improve performance.
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    void release(GeneralDomainObject inObject);

    /** This method allows to invoke a query. It selects all entries in the table. The returned domain objects are
     * ordered by the table's natural order.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select() throws SQLException, BOMException;

    /** This method allows to invoke a query. It selects all entries in the table. The returned domain objects are
     * ordered by the table's natural order.<br>
     * The passed model factory is used to create alternative models retrieved from the result set.
     *
     * @param factory {@link AlternativeModelFactory} the factory to create alternative models
     * @return {@link QueryResult} the query result containing the collection of alternative models retrieved from the
     *         result set
     * @throws SQLException
     * @throws BOMException */
    QueryResult select(AlternativeModelFactory factory) throws SQLException, BOMException;

    /** This method allows to invoke a query. It's a normal version of a select. It takes as argument a SQL-string.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inStatement java.lang.String
     * @throws java.sql.SQLException */
    QueryResult select(String inStatement) throws SQLException;

    /** This method selects all domain objects of the corresponding table matching the specified key. The returned domain
     * objects are ordered by the table's natural order.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select(KeyObject inKey) throws SQLException, BOMException;

    /** This method selects all domain objects of the corresponding table matching the specified key. The returned domain
     * objects are ordered according the specified order object.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select(KeyObject inKey, OrderObject inOrder) throws SQLException, BOMException;

    /** This method selects all domain objects of the corresponding table matching the specified key meeting the
     * specified HAVING clause. The returned domain objects are ordered according the specified order object.
     * <b>Note:</b> You can provide empty key and order objects.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select(KeyObject inKey, OrderObject inOrder, HavingObject inHaving) throws SQLException, BOMException;

    /** This method selects all domain objects of the corresponding table matching the specified key meeting the
     * specified HAVING clause. The returned domain objects are grouped and ordered according the specified group and
     * order objects respectively. <b>Note:</b> You can provide empty key and order objects etc.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @param inGroupBy org.hip.kernel.bom.GroupByObject
     * @return org.hip.kernel.bom.QueryResult
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select(KeyObject inKey, OrderObject inOrder, HavingObject inHaving, GroupByObject inGroupBy)
            throws SQLException, BOMException;

    /** This method selects all domain objects of the corresponding table or tables (if join). The returned domain
     * objects are ordered according the specified order object.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    QueryResult select(OrderObject inOrder) throws SQLException, BOMException;

    /** This method selects all domain objects of the corresponding table or tables (if join) and returns a QueryResult
     * limited by the specified LimitObject.
     *
     * @param inKey KeyObject
     * @param inLimit LimitObject
     * @return QueryResult
     * @throws SQLException
     * @throws BOMException */
    QueryResult select(KeyObject inKey, LimitObject inLimit) throws SQLException, BOMException;

    /** This method allows to invoke a query. It's a normal version of a select. It takes as argument a QueryStatement.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inStatement org.hip.kernel.bom.QueryStatement
     * @throws java.sql.SQLException */
    QueryResult select(QueryStatement inStatement) throws SQLException;

    /** If set true, the domain object home will cache domain objects fetched from the database with the findeByKey.
     *
     * @param inUseCache boolean */
    void setUseCache(boolean inUseCache);

    /** Creates a select string for a SQL UNION query.
     *
     * @param inSetHome SetOperatorHome
     * @param inKey KeyObject
     * @throws BOMException */
    void createSelectString(SetOperatorHome inSetHome, KeyObject inKey) throws BOMException;

    /** @param inSetHome SetOperatorHome
     * @param inKey KeyObject
     * @param inOrder OrderObject
     * @throws BOMException */
    void createSelectString(SetOperatorHome inSetHome, KeyObject inKey, OrderObject inOrder) throws BOMException;

    /** @param inSetHome SetOperatorHome
     * @param inOrder OrderObject
     * @throws BOMException */
    void createSelectString(SetOperatorHome inSetHome, OrderObject inOrder) throws BOMException;
}
