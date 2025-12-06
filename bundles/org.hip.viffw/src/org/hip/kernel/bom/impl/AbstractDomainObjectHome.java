/**
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

package org.hip.kernel.bom.impl; // NOPMD

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hip.kernel.bom.AlternativeModelFactory;
import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObjectCache;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.SetOperatorHome;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.SingleValueQueryStatement;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MappingDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.bom.model.TypeDef;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** This class implements the DomainObjectHome interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObjectHome */
@SuppressWarnings("serial")
abstract public class AbstractDomainObjectHome extends VObject implements GeneralDomainObjectHome, Serializable { // NOPMD
    // Instance variables
    private volatile List<Object> testObjList;
    private List<GeneralDomainObject> releasedObjList;
    private Map<String, XMLSerializer> visitorMap;

    /** domain objects cache */
    private DomainObjectCache cacheObj;

    /** flag for using read cached domain object */
    private boolean useCache;

    /** DomainObjectHomeImpl: default constructor */
    protected AbstractDomainObjectHome() {
        super();
    }

    /** Returns the domainobject cache.
     *
     * @return org.hip.kernel.bom.DomainObjectCache */
    protected DomainObjectCache cache() {
        synchronized (this) {
            if (this.cacheObj == null) {
                this.cacheObj = new DomainObjectCacheImpl();
            }
        }
        return this.cacheObj;
    }

    /** Sets cache to null. */
    public void clearCache() {
        synchronized (this.cacheObj) {
            this.cacheObj = null; // NOPMD
        }
    }

    /** Creates the select string to fetch all domain objects.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createSelectAllString() throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified key.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createSelectString(KeyObject inKey) throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified key ordered by the specified
     * object.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createSelectString(KeyObject inKey, OrderObject inOrder) throws BOMException;

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @return String the SQL select statemetn
     * @throws BOMException */
    protected abstract String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving)
            throws BOMException;

    /** @param inKey {@link KeyObject}
     * @param inOrder {@link OrderObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @return String the SQL select statement
     * @throws BOMException */
    protected abstract String createSelectString(KeyObject inKey, OrderObject inOrder, HavingObject inHaving,
            GroupByObject inGroupBy) throws BOMException;

    /** Creates the select string to fetch all domain objects ordered by the specified object.
     *
     * @return java.lang.String
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createSelectString(OrderObject inOrder) throws BOMException;

    /** Creates the select string to fetch all domain objects matching the specified key limitied by the specified
     * limit.
     *
     * @param inKey KeyObject
     * @param inLimit LimitObject
     * @return String
     * @throws BOMException */
    protected abstract String createSelectString(KeyObject inKey, LimitObject inLimit) throws BOMException;

    /** Creates a select string for a SQL UNION query.
     *
     * @param inSetHome SetOperatorHome
     * @param inKey KeyObject
     * @throws BOMException */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final KeyObject inKey) throws BOMException {
        inSetHome.setSelectString(createSelectString(inKey));
        inSetHome.setCountString(createCountString(inKey));
    }

    /** @param inSetHome SetOperatorHome
     * @param inKey KeyObject
     * @param inOrder OrderObject
     * @throws BOMException */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final KeyObject inKey, final OrderObject inOrder)
            throws BOMException {
        inSetHome.setSelectString(createSelectString(inKey, inOrder));
        inSetHome.setCountString(createCountString(inKey));
    }

    /** @param inSetHome SetOperatorHome
     * @param inOrder OrderObject
     * @throws BOMException */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final OrderObject inOrder) throws BOMException {
        inSetHome.setSelectString(createSelectString(inOrder));
        inSetHome.setCountString(createCountAllString());
    }

    /** Creates the select sql string counting all table entries corresponding to this home.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createCountAllString() throws BOMException;

    /** Creates the select sql string counting all table entries corresponding to this home and the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    protected abstract String createCountString(KeyObject inKey) throws BOMException;

    /** @param inKey {@link KeyObject}
     * @param inHaving {@link HavingObject}
     * @param inGroupBy {@link GroupByObject}
     * @return String the SQL count statement
     * @throws BOMException */
    protected abstract String createCountString(KeyObject inKey, HavingObject inHaving, GroupByObject inGroupBy)
            throws BOMException;

    /** This method looks for all key columns of the table mapped to the DomainObject managed by this home and creates a
     * list COUNT(keyField). Instead of sending SELECT COUNT(*) FROM ... this SQL-sequence can be used to count all
     * entries in a table with better performance.
     *
     * @return java.lang.String */
    protected abstract String createKeyCountColumnList();

    /** This method creates a QueryStatement as part of the frameworks QueryService.
     *
     * @return org.hip.kernel.bom.QueryStatement */
    @Override
    public QueryStatement createQueryStatement() {
        return new DefaultQueryStatement(this);
    }

    /** This method must be implemented by concrete subclasses to create test objects.
     *
     * @return List&lt;Object> */
    protected abstract List<Object> createTestObjects();

    /** Returns the name of the column which is mapped to the inputed property
     *
     * @return java.lang.String
     * @param inPropertyName java.lang.String */
    @Override
    public String getColumnNameFor(final String inPropertyName) {

        try {
            final PropertyDef lPropertyDef = getPropertyDef(inPropertyName);
            if (lPropertyDef != null) {
                final MappingDef lMapping = lPropertyDef.getMappingDef();
                final String lTableName = (String) lMapping.get(MappingDefDef.tableName);
                if (lTableName.length() == 0) {
                    return (String) lMapping.get(MappingDefDef.columnName);
                }
                return lTableName + "." + (String) lMapping.get(MappingDefDef.columnName);
            }
            return getHidden(inPropertyName);
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return "";
        }
    }

    /** Returns number of entries in database corresponding to this home.
     *
     * @return int numer of database entries.
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public int getCount() throws SQLException, BOMException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        return lStatement.executeQuery(createCountAllString()).intValue();
    }

    /** Returns number of entries in database corresponding to this home and the given key.
     *
     * @return int numer of database entries.
     * @param org.hip.kernel.bom.KeyObject the (partial key)
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public int getCount(final KeyObject inKey) throws SQLException, BOMException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        return lStatement.executeQuery(createCountString(inKey)).intValue();
    }

    /** Returns number of entries in database corresponding to this home and the given filter. <b>Note:</b> You can pass
     * empty filtering objects e.g. if you only need GROUP BY.
     *
     * @param inKeyObject KeyObject
     * @param inHaving HavingObject
     * @param inGroupBy GroupByObject
     * @return int
     * @throws SQLException
     * @throws BOMException
     * @see org.hip.kernel.bom.GeneralDomainObjectHome#getCount */
    @Override
    public int getCount(final KeyObject inKeyObject, final HavingObject inHaving, final GroupByObject inGroupBy) // NOPMD
            throws SQLException, BOMException {
        return 0;
    }

    /** Returns the name of the objects which the concrete home can create. This method is abstract and must be
     * implemented by concrete subclasses.
     *
     * @return java.lang.String */
    @Override
    abstract public String getObjectClassName();

    /** Returns the object definition string of the class managed by this home. Concrete subclasses must implement this
     * method.
     *
     * @return java.lang.String */
    protected abstract String getObjectDefString();

    /** This method returns the PropertyDef object which describes the behaviour of the Property object named
     * inPropertyName.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inPropertyName java.lang.String */
    @Override
    public PropertyDef getPropertyDef(final String inPropertyName) {

        // Pre: inPropertyName not null
        if (VSys.assertNotNull(this, "getPropertyDef", inPropertyName) == Assert.FAILURE) {
            return null;
        }

        return getObjectDef().getPropertyDef(inPropertyName);
    }

    /** This method returns the PropertyDef object which describes the behaviour of the Property object mapped to the
     * table's column named inColumnName.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inColumnName java.lang.String */
    @Override
    public PropertyDef getPropertyDefFor(final String inColumnName) {

        if (VSys.assertNotNull(this, "getPropertyDefFor", inColumnName) == Assert.FAILURE) {
            return null;
        }

        try {
            for (final PropertyDef lPropertyDef : getObjectDef().getPropertyDefs2()) {
                final String lColumnName = (String) lPropertyDef.getMappingDef().get(MappingDefDef.columnName);
                if (inColumnName.equalsIgnoreCase(lColumnName)) {
                    return lPropertyDef;
                }
            }

            // not found
            return null;
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** Returns the testObjects created by the Homes.
     *
     * @return java.util.Iterator */
    @Override
    public Iterator<Object> getTestObjects() {
        return testObjects().iterator();
    }

    /** Returns true if this domain object home caches the domain objects fetched from database over findByKey.
     *
     * @return boolean */
    @Override
    public boolean getUseCache() { // NOPMD
        return this.useCache;
    }

    /** Returns an instance of the visitor named inKey. This visitor can be used to serialize the DomainObject managed
     * by the Home class.
     *
     * @return org.hip.kernel.bom.DomainObjectVisitor
     * @param inKey java.lang.String */
    @Override
    public DomainObjectVisitor getVisitor(final String inKey) {
        return visitors().get(inKey);
    }

    /** This method can be used to initialize a new instance of the class. Subclasses may overrride.<br />
     * <b>Note:</b> Clienst have to call this method explicitly! */
    @Override
    public void initialize() { // NOPMD
        // intentionally left empty
    }

    /** Loads cache with domainObject from the collection. This is a simple way to preload the cache.
     *
     * @param inDomainObjectCollection org.hip.kernel.bom.DomainObjectCollection */
    protected void loadCache(final DomainObjectCollection inDomainObjectCollection) {

        // pre: parameter not null
        if (VSys.assertNotNull(this, "loadCache", inDomainObjectCollection)) {
            return;
        }

        final DomainObjectIterator lDomainObjects = inDomainObjectCollection.elements();
        while (lDomainObjects.hasMoreElements()) {
            cache().put(lDomainObjects.nextElement());
        }
    }

    /** Returns a GeneralDomainObject filled with the values of the inputed ResultSet
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @param result java.sql.ResultSet */
    protected abstract GeneralDomainObject newInstance(ResultSet result) throws BOMException;

    /** Use this method to release a DomainObject. Released objects can act as cache and, therefore, instead of creating
     * a new instance of a DomainObject from scratch, can improve performance.
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    public void release(final GeneralDomainObject inObject) {
        inObject.setVirgin();
        releasedObjects().add(inObject);
    }

    /** Returns all released DomainObjects managed by this home. Released objects can act as cache and, therefore,
     * instead of creating a new instance of a DomainObject from scratch, can improve performance.
     *
     * @return java.util.Vector */
    protected Collection<GeneralDomainObject> releasedObjects() {
        if (this.releasedObjList == null) {
            this.releasedObjList = new ArrayList<GeneralDomainObject>();
        }
        return this.releasedObjList;
    }

    /** This method selects all domain objects of the corresponding table or tables (if join). The returned domain
     * objects are ordered by the table's natural order.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public QueryResult select() throws SQLException, BOMException {
        final QueryStatement statement = this.createQueryStatement();
        statement.setSQLString(this.createSelectAllString());
        return this.select(statement);
    }

    @Override
    public QueryResult select(final AlternativeModelFactory factory) throws SQLException, BOMException {
        final QueryStatement statement = this.createQueryStatement();
        statement.setSQLString(this.createSelectAllString()).setFactory(factory);
        return this.select(statement);
    }

    /** This method allows to invoke a query. It's a normal version of a select. It takes as argument a SQL-string.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param sql java.lang.String
     * @throws java.sql.SQLException */
    @Override
    public QueryResult select(final String sql) throws SQLException {
        if (VSys.assertNotNull(this, "select(String)", sql) == Assert.FAILURE) {
            return new DefaultQueryResult(null, null, null);
        }

        final QueryStatement statement = this.createQueryStatement();
        statement.setSQLString(sql);
        return this.select(statement);
    }

    /** This method selects all domain objects of the corresponding table matching the specified key. The returned
     * domain objects are ordered by the table's natural order.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public QueryResult select(final KeyObject inKey) throws SQLException, BOMException {
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inKey));
        return this.select(lStatement);
    }

    /** This method selects all domain objects of the corresponding table matching the specified key. The returned
     * domain objects are ordered according the specified order object.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder) throws SQLException, BOMException {
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inKey, inOrder));
        return this.select(lStatement);
    }

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
    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving)
            throws SQLException, BOMException {
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inKey, inOrder, inHaving));
        return this.select(lStatement);
    }

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
    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GroupByObject inGroupBy) throws SQLException, BOMException {
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inKey, inOrder, inHaving, inGroupBy));
        return this.select(lStatement);
    }

    /** This method selects all domain objects of the corresponding table or tables (if join). The returned domain
     * objects are ordered according the specified order object.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public QueryResult select(final OrderObject inOrder) throws SQLException, BOMException {
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inOrder));
        return this.select(lStatement);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final LimitObject inLimit) throws SQLException, BOMException { // NOPMD
        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createSelectString(inKey, inLimit));
        return this.select(lStatement);
    }

    /** This method allows to invoke a query. It's a normal version of a select. It takes as argument a QueryStatement.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @param statement org.hip.kernel.bom.QueryStatement
     * @exception java.sql.SQLException The exception description. */
    @Override
    public QueryResult select(final QueryStatement statement) throws SQLException {
        if (VSys.assertNotNull(this, "select(QueryStatement)", statement) == Assert.FAILURE) {
            return new DefaultQueryResult(null, null, null);
        }

        return statement.executeQuery();
    }

    /** This method fills a DomainObject with the data from a ResultSet.
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject
     * @param inResult java.sql.ResultSet */
    protected void setFromResultSet(final GeneralDomainObject inObject, final ResultSet inResult) throws SQLException, // NOPMD
    SettingException {

        final ResultSetMetaData lMetaData = inResult.getMetaData();

        for (int i = 1; i <= lMetaData.getColumnCount(); i++) {
            final String lColumnName = lMetaData.getColumnName(i);
            final PropertyDef lProperty = this.getPropertyDefFor(lColumnName);
            if (lProperty != null) {
                try {
                    final String lName = (String) lProperty.get(PropertyDefDef.propertyName);
                    final String lType = ((String) lProperty.get(PropertyDefDef.valueType)).intern();

                    if (lType == TypeDef.String) {
                        inObject.set(lName, inResult.getString(i));
                    } else if (lType == TypeDef.LongVarchar) {
                        inObject.set(lName, inResult.getAsciiStream(i));
                    } else if (lType == TypeDef.Date) {
                        inObject.set(lName, inResult.getDate(i));
                    } else if (lType == TypeDef.Timestamp) {
                        inObject.set(lName, inResult.getTimestamp(i));
                    } else if (lType == TypeDef.Integer) {
                        inObject.set(lName, Integer.valueOf(inResult.getInt(i)));
                    } else if (lType == TypeDef.BigInteger) {
                        inObject.set(lName, new java.math.BigInteger(inResult.getString(i)));
                    } else if (lType == TypeDef.BigDecimal) {
                        inObject.set(lName, new java.math.BigDecimal(inResult.getString(i)));
                    } else if (lType == TypeDef.Number) {
                        inObject.set(lName, new java.math.BigDecimal(inResult.getString(i)));
                    }
                } catch (final GettingException exc) {
                    DefaultExceptionWriter.printOut(this, exc, true);
                } // catch
            } // if
        } // for
    }

    /** If set true, the domain object home will cache domain objects fetched from the database with the findeByKey.
     *
     * @param inUseCache boolean */
    @Override
    public void setUseCache(final boolean inUseCache) {
        this.useCache = inUseCache;
    }

    /** @return {@link List} */
    private List<Object> testObjects() { // NOPMD
        if (this.testObjList == null) {
            synchronized (this) {
                this.testObjList = createTestObjects();

                // Subclass has no implementation, we will not try again
                if (this.testObjList == null) {
                    this.testObjList = new ArrayList<Object>();
                }
            }
        }
        return this.testObjList;
    }

    /** Returns the visitors which can be used by the DomainObjects.
     *
     * @return java.util.Hashtable */
    private Map<String, XMLSerializer> visitors() {
        if (this.visitorMap == null) {
            this.visitorMap = new Hashtable<String, XMLSerializer>();

            // Register the known vistors
            this.visitorMap.put(GeneralDomainObjectHome.xmlSerializer, new XMLSerializer());
        }
        return this.visitorMap;
    }

    /** Creates a <code>SingleValueQueryStatement</code>. Subclasses may override, e.g. to provide a modified
     * implementation of <code>SingleValueQueryStatement</code>.
     *
     * @return SingleValueQueryStatement */
    protected SingleValueQueryStatement createSingleValueQueryStatement() {
        return new SingleValueQueryStatementImpl();
    }

    /** Returns the <code>DBAdapterType</code> matching the actual DB access configuration.<br/>
     * Subclasses may override, e.g. to provide the adapter type for a configuration of an external DB access.
     *
     * @return {@link DBAdapterType} */
    protected DBAdapterType retrieveDBAdapterType() {
        return DataSourceRegistry.INSTANCE.getAdapterType();
    }

}