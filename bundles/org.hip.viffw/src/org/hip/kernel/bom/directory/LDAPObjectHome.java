/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006-2025, Benno Luthiger

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

package org.hip.kernel.bom.directory; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.BOMInvalidKeyException;
import org.hip.kernel.bom.BOMNotFoundException;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectCache;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderItem;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.SetOperatorHome;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.impl.DomainObjectCacheImpl;
import org.hip.kernel.bom.impl.ModifierStrategy;
import org.hip.kernel.bom.impl.XMLSerializer;
import org.hip.kernel.bom.model.IMappingDefCreator;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.MappingDefDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.ObjectDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.impl.MappingDefImpl;
import org.hip.kernel.bom.model.impl.ObjectDefGenerator;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.SortableItem;
import org.hip.kernel.util.VInvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/** Abstract home class for LDAP objects, providing general functionality to retrieve entries from a LDAP server.
 *
 * @author Luthiger */
@SuppressWarnings("serial")
public abstract class LDAPObjectHome implements DomainObjectHome, Serializable { // NOPMD by lbenno
    private static final Logger LOG = LoggerFactory.getLogger(LDAPObjectHome.class);

    private List<Object> testObjs;
    private List<GeneralDomainObject> releasedObjs;
    private Map<String, XMLSerializer> visitorMap;

    private ObjectDef objectDef;

    private LDAPAdapter adapter;

    // some optimizations
    private Map<String, String> tableNameMap;

    /** domain objects cache */
    private DomainObjectCache cacheObj;
    private boolean useCache;

    /** LDAPObjectHome constructor */
    public LDAPObjectHome() {
        super();
        this.adapter = new LDAPAdapter();
    }

    /** Returns the base directory where the objects of the LDAP server has to be retrieved.
     *
     * @return String
     * @throws GettingException */
    protected String getBaseDir() throws GettingException {
        return getObjectDef().get(ObjectDefDef.baseDir).toString();

    }

    @Override
    public QueryStatement createQueryStatement() { // NOPMD by lbenno
        try {
            return new LDAPQueryStatement(this, getBaseDir());
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    private QueryStatement createQueryStatement(final OrderObject inOrder, final LimitObject inLimit) {
        final LDAPQueryStatement outStatement = (LDAPQueryStatement) createQueryStatement();
        if (inLimit != null) {
            outStatement.setLimit(inLimit);
        }
        if (inOrder != null) {
            try {
                outStatement.setOrder(renderOrder(inOrder));
            } catch (final IOException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
        return outStatement;
    }

    private String[] renderOrder(final OrderObject inOrder) {
        final Collection<String> outSortBy = new ArrayList<String>();
        for (final SortableItem lItem : inOrder.getItems2()) {
            outSortBy.add(getColumnNameFor(((OrderItem) lItem).getColumnName()));
        }
        return outSortBy.toArray(new String[] {});
    }

    /** @return String */
    protected String createSelectAllString() {
        return this.adapter.createSelectAllString();
    }

    /** @param inKey {@link KeyObject}
     * @return String
     * @throws BOMException */
    protected String createSelectString(final KeyObject inKey) throws BOMException {
        return this.adapter.createSelectString(inKey, this);
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.GeneralDomainObjectHome#createSelectString(org.hip.kernel.bom.SetOperatorHome,
     *      org.hip.kernel.bom.KeyObject) */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final KeyObject inKey) throws BOMException { // NOPMD
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.GeneralDomainObjectHome#createSelectString(org.hip.kernel.bom.SetOperatorHome,
     *      org.hip.kernel.bom.KeyObject, org.hip.kernel.bom.OrderObject) */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final KeyObject inKey, final OrderObject inOrder) // NOPMD
            throws BOMException {
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.GeneralDomainObjectHome#createSelectString(org.hip.kernel.bom.SetOperatorHome,
     *      org.hip.kernel.bom.OrderObject) */
    @Override
    public void createSelectString(final SetOperatorHome inSetHome, final OrderObject inOrder) throws BOMException { // NOPMD
        // intentionally left empty
    }

    @Override
    public String getColumnNameFor(final String inPropertyName) { // NOPMD by lbenno
        try {
            final PropertyDef lPropertyDef = getPropertyDef(inPropertyName);
            if (lPropertyDef != null) {
                final MappingDef lMapping = lPropertyDef.getMappingDef();
                return lMapping.get(MappingDefDef.columnName).toString();
            }
            return getHidden(inPropertyName);
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return "";
        }
    }

    @Override
    public int getCount() throws SQLException, BOMException { // NOPMD
        LOG.debug("Invoked getCount()");
        return ((LDAPQueryResult) select()).getCount();
    }

    @Override
    public int getCount(final KeyObject inKeyObject) throws SQLException, BOMException { // NOPMD by lbenno
        return ((LDAPQueryResult) select(inKeyObject)).getCount();
    }

    @Override
    public int getCount(final KeyObject inKeyObject, final HavingObject inHaving, final GroupByObject inGroupBy) // NOPMD
            throws SQLException, BOMException {
        return getCount(inKeyObject);
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.GeneralDomainObjectHome#getHidden(java.lang.String) */
    @Override
    public String getHidden(final String inPropertyName) { // NOPMD
        return "";
    }

    @Override
    public ObjectDef getObjectDef() { // NOPMD by lbenno
        if (this.objectDef == null) {
            this.objectDef = createObjectDef();
        }
        return this.objectDef;
    }

    /** @return org.hip.kernel.bom.model.ObjectDef */
    private ObjectDef createObjectDef() {
        try {
            synchronized (this) {
                final ObjectDef outObjectDef = ObjectDefGenerator.getSingleton().createObjectDef(
                        getObjectDefString(),
                        new IMappingDefCreator() {
                            @Override
                            public MappingDef createMappingDef() { // NOPMD by lbenno
                                return new LDAPMappingDef();
                            }
                        }
                        );
                return outObjectDef;
            }
        } catch (final SAXException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** Returns the object definition string of the class managed by this home. Concrete subclasses must implement this
     * method.
     *
     * @return java.lang.String */
    protected abstract String getObjectDefString();

    @Override
    public PropertyDef getPropertyDef(final String inPropertyName) { // NOPMD by lbenno
        // Pre: inPropertyName not null
        if (VSys.assertNotNull(this, "getPropertyDef", inPropertyName) == Assert.FAILURE) {
            return null;
        }

        return getObjectDef().getPropertyDef(inPropertyName);
    }

    @Override
    public PropertyDef getPropertyDefFor(final String inColumnName) { // NOPMD by lbenno
        if (VSys.assertNotNull(this, "getPropertyDefFor", inColumnName) == Assert.FAILURE) {
            return null;
        }

        try {
            for (final PropertyDef lPropertyDef : getObjectDef().getPropertyDefs2()) {
                final String lColumnName = (String) lPropertyDef.getMappingDef().get(MappingDefDef.columnName);
                if (inColumnName.equals(lColumnName)) {
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

    @Override
    public Iterator<Object> getTestObjects() { // NOPMD by lbenno
        return testObjects().iterator();
    }

    /** @return java.util.Vector */
    private synchronized List<Object> testObjects() { // NOPMD by lbenno
        if (this.testObjs == null) {
            this.testObjs = createTestObjects();

            // Subclass has no implementation, we will not try again
            if (this.testObjs == null) {
                this.testObjs = new ArrayList<Object>();
            }
        }
        return this.testObjs;
    }

    /** This method can be implemented by concrete subclasses to create test objects.
     *
     * @return java.util.List */
    protected List<Object> createTestObjects() { // NOPMD
        return null;
    }

    @Override
    public boolean getUseCache() { // NOPMD by lbenno
        return this.useCache;
    }

    @Override
    public DomainObjectVisitor getVisitor(final String inKey) { // NOPMD by lbenno
        return visitors().get(inKey);
    }

    /** Returns the visitors which can be used by the DomainObjects.
     *
     * @return java.util.Hashtable */
    private Map<String, XMLSerializer> visitors() {
        if (this.visitorMap == null) {
            this.visitorMap = new HashMap<String, XMLSerializer>();

            // Register the known vistors
            this.visitorMap.put(GeneralDomainObjectHome.xmlSerializer, new XMLSerializer());
        }
        return this.visitorMap;
    }

    @Override
    public void initialize() { // NOPMD
        // intentionally left empty
    }

    @Override
    public void release(final GeneralDomainObject inObject) { // NOPMD by lbenno
        inObject.setVirgin();
        releasedObjects().add(inObject);
    }

    @Override
    public DomainObject findByKey(final KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException { // NOPMD
        DomainObject outDomainObject = null;
        if (getUseCache() && inKey.isPrimaryKey()) {
            outDomainObject = (DomainObject) cache().get(inKey);
        }
        if (outDomainObject != null) {
            return outDomainObject;
        }

        QueryResult lResult = null;
        try {
            lResult = select(inKey);
            outDomainObject = (DomainObject) lResult.nextAsDomainObject();
        } catch (final SQLException exc) {
            throw new BOMInvalidKeyException(exc.toString(), exc);
        } catch (final BOMException exc) {
            throw new BOMInvalidKeyException(exc.toString(), exc);
        }
        if (outDomainObject == null) {
            throw new BOMNotFoundException();
        }
        if (getUseCache() && inKey.isPrimaryKey()) {
            // cache it
            cache().put(outDomainObject);
        }
        return outDomainObject;
    }

    @Override
    public QueryResult select() throws SQLException, BOMException { // NOPMD by lbenno
        final QueryStatement lStatement = createQueryStatement();
        lStatement.setSQLString(createSelectAllString());
        return select(lStatement);
    }

    @Override
    public QueryResult select(final String inFilter) throws SQLException { // NOPMD by lbenno
        if (VSys.assertNotNull(this, "select(String)", inFilter) == Assert.FAILURE) {
            return new LDAPQueryResult(null, null, -1, null);
        }

        final QueryStatement lStatement = createQueryStatement();
        lStatement.setSQLString(inFilter);
        return select(lStatement);
    }

    @Override
    public QueryResult select(final KeyObject inKey) throws SQLException, BOMException { // NOPMD
        final QueryStatement lStatement = createQueryStatement();
        lStatement.setSQLString(createSelectString(inKey));
        return select(lStatement);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder) throws SQLException, BOMException { // NOPMD
        // by
        // lbenno
        final QueryStatement lStatement = createQueryStatement(inOrder, null);
        lStatement.setSQLString(createSelectString(inKey));
        return select(lStatement);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving) // NOPMD by
    // lbenno
            throws SQLException, BOMException {
        return select(inKey, inOrder);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving, // NOPMD by
            // lbenno
            final GroupByObject inGroupBy) throws SQLException, BOMException {
        return select(inKey, inOrder);
    }

    @Override
    public QueryResult select(final OrderObject inOrder) throws SQLException, BOMException { // NOPMD by lbenno
        final QueryStatement lStatement = createQueryStatement(inOrder, null);
        lStatement.setSQLString(createSelectAllString());
        return select(lStatement);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final LimitObject inLimit) throws SQLException, BOMException { // NOPMD
        final QueryStatement lStatement = createQueryStatement(null, inLimit);
        lStatement.setSQLString(createSelectString(inKey));
        return select(lStatement);
    }

    @Override
    public QueryResult select(final QueryStatement inStatement) throws SQLException { // NOPMD by lbenno
        if (VSys.assertNotNull(this, "select(QueryStatement)", inStatement) == Assert.FAILURE) {
            return new LDAPQueryResult(null, null, -1, null);
        }

        LOG.debug("select {}", inStatement.getSQLString());
        return inStatement.executeQuery();
    }

    /** Creates a new object instance.
     *
     * @param inResult {@link SearchResult}
     * @return {@link GeneralDomainObject}
     * @throws BOMException */
    public GeneralDomainObject newInstance(final SearchResult inResult) throws BOMException {
        final LDAPObject outObject = (LDAPObject) this.newInstance();
        outObject.loadFromResultSet(inResult);
        return outObject;
    }

    /** Returns an empty DomainObject. An empty object is initialized but does not contain any values. The object goes
     * back into this state after releasing.
     *
     * @return org.hip.kernel.bom.DomainObject */
    @Override
    public DomainObject newInstance() throws BOMException {
        try {
            // straightforward
            final Iterator<GeneralDomainObject> lReleased = releasedObjects().iterator();
            if (lReleased.hasNext()) {
                final DomainObject outDomainObject = (DomainObject) lReleased.next();
                lReleased.remove();
                return outDomainObject;
            }
            else {
                final Class<?> lClass = Class.forName(getObjectClassName());
                return (DomainObject) lClass.newInstance();
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException exc) {
            throw new BOMException("ClassNotFound " + exc.getMessage(), exc);
        }
    }

    private List<GeneralDomainObject> releasedObjects() {
        if (this.releasedObjs == null) {
            this.releasedObjs = new ArrayList<GeneralDomainObject>();
        }
        return this.releasedObjs;
    }

    @Override
    public DomainObject create() throws BOMException { // NOPMD by lbenno
        final DomainObject outObject = newInstance();
        ((LDAPObject) outObject).initializeForNew();
        return outObject;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createDeleteString(java.lang.String, org.hip.kernel.bom.DomainObject) */
    @Override
    public String createDeleteString(final String inTableName, final DomainObject inDomainObject) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createInsertString(java.lang.String, org.hip.kernel.bom.DomainObject) */
    @Override
    public String createInsertString(final String inTableName, final DomainObject inDomainObject) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createPreparedInserts() */
    @Override
    public Vector<String> createPreparedInserts() { // NOPMD
        return null;
    }

    @Override
    public List<String> createPreparedInserts2() { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createPreparedSelectString(org.hip.kernel.bom.KeyObject) */
    @Override
    public String createPreparedSelectString(final KeyObject inKey) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createPreparedUpdate(org.hip.kernel.bom.KeyObject,
     *      org.hip.kernel.bom.KeyObject) */
    @Override
    public String createPreparedUpdate(final KeyObject inChange, final KeyObject inWhere) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createPreparedUpdateString(java.lang.String,
     *      org.hip.kernel.bom.DomainObject) */
    @Override
    public String createPreparedUpdateString(final String inTableName, final DomainObject inDomainObject) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createPreparedUpdates() */
    @Override
    public Vector<String> createPreparedUpdates() { // NOPMD
        return null;
    }

    @Override
    public List<String> createPreparedUpdates2() { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createUpdateString(java.lang.String, org.hip.kernel.bom.DomainObject) */
    @Override
    public String createUpdateString(final String inTableName, final DomainObject inDomainObject) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#createUpdateString(java.lang.String, org.hip.kernel.bom.DomainObject) */
    @Override
    public String createUpdateString(final KeyObject inChange, final KeyObject inWhere) { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#delete(org.hip.kernel.bom.KeyObject, boolean) */
    @Override
    public void delete(final KeyObject inKey, final boolean inCommit) throws SQLException { // NOPMD
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#getMax(java.lang.String) */
    @Override
    public BigDecimal getMax(final String inColumnName) throws SQLException, BOMException { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#getMax(java.lang.String, org.hip.kernel.bom.KeyObject) */
    @Override
    public BigDecimal getMax(final String inColumnName, final KeyObject inKey) throws SQLException, BOMException { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#getModified(org.hip.kernel.bom.impl.ModifierStrategy) */
    @Override
    public Collection<Object> getModified(final ModifierStrategy inStrategy) throws SQLException { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#getModified(org.hip.kernel.bom.impl.ModifierStrategy,
     *      org.hip.kernel.bom.KeyObject) */
    @Override
    public Collection<Object> getModified(final ModifierStrategy inStrategy, final KeyObject inKey) throws SQLException { // NOPMD
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#remove(org.hip.kernel.bom.KeyObject) */
    @Override
    public void remove(final KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException { // NOPMD
        // intentionally left empty
    }

    @Override
    public Hashtable<String, String> tableNames() { // NOPMD by lbenno
        final Hashtable<String, String> out = new Hashtable<String, String>(); // NOPMD
        out.putAll(tableNames2());
        return out;
    }

    @Override
    public Map<String, String> tableNames2() { // NOPMD by lbenno
        if (this.tableNameMap == null) {
            this.tableNameMap = new Hashtable<String, String>(7);
            try {
                for (final PropertyDef lPropertyDef : getObjectDef().getPropertyDefs2()) {
                    final String lTableName = (String) lPropertyDef.getMappingDef().get("");
                    if (!this.tableNameMap.containsKey(lTableName)) {
                        this.tableNameMap.put(lTableName, lTableName);
                    }
                }
            } catch (final GettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            } // try-catch
        } // if
        return this.tableNameMap;
    }

    /** Not applicable here.
     *
     * @see org.hip.kernel.bom.DomainObjectHome#checkStructure(java.lang.String) */
    @Override
    public boolean checkStructure(final String inSchemaPattern) throws SQLException, NamingException {
        return false;
    }

    /** Returns the attribute's name mapped to the domain object's key.
     *
     * @return String the name of the key column/attribute, e.g. 'cn'. */
    protected String getKeyColumn() {
        return getColumnNameFor(getObjectDef().getPrimaryKeyDef().getKeyName(0));
    }

    /** Returns the domain object cache.
     *
     * @return org.hip.kernel.bom.DomainObjectCache */
    protected DomainObjectCache cache() {
        if (this.cacheObj == null) {
            this.cacheObj = new DomainObjectCacheImpl();
        }
        return this.cacheObj;
    }

    /** Sets cache to null. */
    public void clearCache() {
        this.cacheObj = null; // NOPMD by lbenno
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

    @Override
    public void setUseCache(final boolean inUseCache) { // NOPMD by lbenno
        this.useCache = inUseCache;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.objectDef);
        out.writeObject(this.tableNameMap);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.objectDef = (ObjectDef) inStream.readObject();
        this.tableNameMap = (Map<String, String>) inStream.readObject();
        this.adapter = new LDAPAdapter();
    }

    // ---
    /** We don't want the 'columns' uppercase, therefore, we create a <code>MappingDef</code> class and override the
     * method MappingDef.set(). */
    private class LDAPMappingDef extends MappingDefImpl { // NOPMD by lbenno
        @Override
        public void set(final String inName, final Object inValue) throws SettingException { // NOPMD by lbenno
            final Property lProperty = (Property) propertySet().get(inName);
            try {
                lProperty.setValue(inValue);
            } catch (final VInvalidValueException exc) {
                throw new SettingException(exc.getMessage(), exc);
            }
        }
    }

}
