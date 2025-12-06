/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2001-2025, Benno Luthiger

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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.BOMInvalidKeyException;
import org.hip.kernel.bom.BOMNotFoundException;
import org.hip.kernel.bom.DBAdapterSimple;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.SingleValueQueryStatement;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.impl.ObjectDefGenerator;
import org.hip.kernel.dbaccess.DataSourceRegistry;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.exc.VException;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.xml.sax.SAXException;

/** This class implements the DomainObjectHome interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObjectHome */
@SuppressWarnings("serial")
public abstract class DomainObjectHomeImpl extends AbstractDomainObjectHome implements DomainObjectHome { // NOPMD

    // Instance variables
    private ObjectDef objectDef;
    private DomainObject temporary;
    private DBAdapterSimple dbAdapter;

    // some optimizations
    private Map<String, String> tableNameMap;

    /** DomainObjectHomeImpl: default constructor */
    protected DomainObjectHomeImpl() {
        super();
        this.dbAdapter = getDBAdapter(); // NOPMD by lbenno
    }

    /** @return org.hip.kernel.bom.model.ObjectDef */
    private ObjectDef createObjectDef() {
        try {
            synchronized (this) {
                return ObjectDefGenerator.getSingleton().createObjectDef(getObjectDefString());
            }
        } catch (final SAXException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** This method creates a new instance of a DomainObject
     *
     * @return org.hip.kernel.bom.DomainObject */
    @Override
    public DomainObject create() throws BOMException {
        final DomainObject outObject = newInstance();
        ((DomainObjectImpl) outObject).initializeForNew();
        return outObject;
    }

    /** Creates select sql string counting all table entries corresponding to this home.
     *
     * @return java.lang.String */
    @Override
    protected String createCountAllString() {
        return this.dbAdapter.createCountAllString(this);
    }

    /** Creates select sql string counting all table entries corresponding to this home.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return java.lang.String */
    @Override
    protected String createCountString(final KeyObject inKey) {
        return this.dbAdapter.createCountString(inKey, this);
    }

    @Override
    protected String createCountString(final KeyObject inKey, final HavingObject inHaving, final GroupByObject inGroupBy) // NOPMD
            throws BOMException {
        return this.dbAdapter.createCountString(inKey, inHaving, inGroupBy, this);
    }

    /** Returns the maximum value of the specified column corresponding to this home.
     *
     * @param inColumnName java.lang.String
     * @return java.math.BigDecimal
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public BigDecimal getMax(final String inColumnName) throws SQLException, BOMException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        final ModifierStrategy lStrategy = new ModifierStrategy(inColumnName, ModifierStrategy.MAX);
        return lStatement.executeQuery(this.dbAdapter.createModifiedString(lStrategy, this));
    }

    /** Returns a collection of calculated values according to the modifiers strategy passed.
     *
     * @param inStrategy ModifierStrategy
     * @return Collection containing <code>BigDecimal</code>s or <code>Timestamp</code>s.
     * @throws SQLException */
    @Override
    public Collection<Object> getModified(final ModifierStrategy inStrategy) throws SQLException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        return lStatement.executeQuery2(this.dbAdapter.createModifiedString(inStrategy, this));
    }

    /** Returns the maximum value of the specified column corresponding to this home and the specified key.
     *
     * @param inColumnName java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return java.math.BigDecimal
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    public BigDecimal getMax(final String inColumnName, final KeyObject inKey) throws SQLException, BOMException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        final ModifierStrategy lStrategy = new ModifierStrategy(inColumnName, ModifierStrategy.MAX);
        return lStatement.executeQuery(this.dbAdapter.createModifiedString(lStrategy, inKey, this));
    }

    /** Returns a collection of calculated values according to the modifiers strategy passed.
     *
     * @param inStrategy ModifierStrategy
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return Collection containing <code>BigDecimal</code>s or <code>Timestamp</code>s.
     * @throws SQLException */
    @Override
    public Collection<Object> getModified(final ModifierStrategy inStrategy, final KeyObject inKey) throws SQLException {
        final SingleValueQueryStatement lStatement = createSingleValueQueryStatement();
        return lStatement.executeQuery2(this.dbAdapter.createModifiedString(inStrategy, inKey, this));
    }

    /** This method looks for all key columns of the table mapped to the DomainObject managed by this home and creates a
     * list COUNT(keyField). Instead of sending SELECT COUNT(*) FROM ... this SQL-sequence can be used to count all
     * entries in a table with better performance.
     *
     * @return java.lang.String */
    @Override
    protected String createKeyCountColumnList() {
        return this.dbAdapter.createKeyCountColumnList(this);
    }

    /** Creates the prepared select SQL string to fetch all domain objects with the specified key managed by this home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject */
    @Override
    public String createPreparedSelectString(final KeyObject inKey) {
        return this.dbAdapter.createPreparedSelectString(inKey, this);
    }

    /** Creates select string to fetch all domain objects.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectAllString() throws BOMException {
        return this.dbAdapter.createSelectAllString();
    }

    /** Creates select SQL string to fetch all domain objects with the specified key managed by this home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey) throws BOMException {
        return this.dbAdapter.createSelectString(inKey, this);
    }

    /** Creates the select string to fetch all domain objects matching the specified key ordered by the specified object.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder) throws BOMException {
        return this.dbAdapter.createSelectString(inKey, inOrder, this);
    }

    /** @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving)
            throws BOMException {
        return this.dbAdapter.createSelectString(inKey, inOrder, inHaving, this);
    }

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @param inGroupBy org.hip.kernel.bom.GroupByObject
     * @return java.lang.String */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GroupByObject inGroupBy) throws BOMException {
        return this.dbAdapter.createSelectString(inKey, inOrder, inHaving, inGroupBy, this);
    }

    /** Creates the select string to fetch all domain objects ordered by the specified object.
     *
     * @return java.lang.String
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final OrderObject inOrder) throws BOMException {
        return this.dbAdapter.createSelectString(inOrder, this);
    }

    /** Creates the select string to fetch all domain objects matching the specified key limitied by the specified limit.
     *
     * @param inKey KeyObject
     * @param inLimit LimitObject
     * @return String
     * @throws BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final LimitObject inLimit) throws BOMException {
        return this.dbAdapter.createSelectString(inKey, inLimit, this);
    }

    /** Creates select SQL string to delete all domain objects with the specified key managed by this home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject */
    protected String createDeleteString(final KeyObject inKey) {
        return this.dbAdapter.createDeleteString(inKey, this);
    }

    /** Returns the SQL string to delete an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createDeleteString(final String inTableName, final DomainObject inDomainObject) {
        return getDBAdapter().createDeleteString(inTableName, inDomainObject);
    }

    /** Returns the SQL string to insert a new entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createInsertString(final String inTableName, final DomainObject inDomainObject) {
        return getDBAdapter().createInsertString(inTableName, inDomainObject);
    }

    /** Returns the SQL string to update an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createUpdateString(final String inTableName, final DomainObject inDomainObject) {
        return getDBAdapter().createUpdateString(inTableName, inDomainObject);
    }

    @Override
    public String createUpdateString(final KeyObject inChange, final KeyObject inWhere) { // NOPMD
        return getDBAdapter().createUpdateString(this, inChange, inWhere);
    }

    /** Returns the SQL string to prepare an update of an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createPreparedUpdateString(final String inTableName, final DomainObject inDomainObject) {
        return getDBAdapter().createPreparedUpdateString(inTableName, inDomainObject);
    }

    /** Returns a Vector of prepared SQL string to insert a new entry.
     *
     * @param java.util.Vector<String> */
    @Override
    public Vector<String> createPreparedInserts() { // NOPMD
        return new Vector<>(createPreparedInserts2()); // NOPMD
    }

    @Override
    public List<String> createPreparedInserts2() { // NOPMD
        return getDBAdapter().createPreparedInserts2();
    }

    /** Returns a Vector of prepared SQL strings to update entries.
     *
     * @param java.util.Vector<String> */
    @Override
    public Vector<String> createPreparedUpdates() { // NOPMD
        return new Vector<>(createPreparedUpdates2()); // NOPMD
    }

    @Override
    public List<String> createPreparedUpdates2() { // NOPMD
        return getDBAdapter().createPreparedUpdates2();
    }

    /** Returns a SQL command usable for a prepared update.
     *
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String */
    @Override
    public String createPreparedUpdate(final KeyObject inChange, final KeyObject inWhere) {
        return getDBAdapter().createPreparedUpdate(this, inChange, inWhere);
    }

    /** Returns the name of the objects which the concrete home can create. This method is abstract and must be
     * implemented by concrete subclasses.
     *
     * @return java.lang.String */
    @Override
    public abstract String getObjectClassName();

    /** Returns the object definition for the class managed by this home.<br />
     * <b>Note:</b> be aware that changes to the <code>ObjectDef</code> instance returned may affect the behavior of
     * this <code>DomainObjectHome</code> instance. To prevent such problems, clone the object returned before using it.
     *
     * @return ObjectDef */
    @Override
    public ObjectDef getObjectDef() {
        if (this.objectDef == null) {
            this.objectDef = createObjectDef();
        }
        return this.objectDef;
    }

    /** No hidden fields, therefore, an empty String is returned.
     *
     * @param propertyName String
     * @return String */
    @Override
    public String getHidden(final String propertyName) { // NOPMD by lbenno
        return "";
    }

    /** Returns the object definition string of the class managed by this home. Concrete subclasses must implement this
     * method.
     *
     * @return java.lang.String */
    @Override
    protected abstract String getObjectDefString();

    /** The temporary can used if the client does not use it for long.
     *
     * @return org.hip.kernel.bom.DomainObject */
    protected DomainObject getTemporary() throws BOMException {
        if (this.temporary == null) {
            this.temporary = newInstance();
        }
        return this.temporary;
    }

    private final DBAdapterSimple getDBAdapter() {
        if (this.dbAdapter == null) {
            this.dbAdapter = retrieveDBAdapterType().getSimpleDBAdapter(getObjectDef());
        }
        return this.dbAdapter;
    }

    /** Returns an empty DomainObject. An empty object is initialized but does not contain any values. The object goes
     * back into this state after releasing.
     *
     * @return org.hip.kernel.bom.DomainObject */
    @Override
    public DomainObject newInstance() throws BOMException {
        try {
            // straightforward
            final Iterator<GeneralDomainObject> released = releasedObjects().iterator();
            if (released.hasNext()) {
                final DomainObject model = (DomainObject) released.next();
                released.remove();
                return model;
            } else {
                final Class<?> lClass = Class.forName(getObjectClassName());
                return (DomainObject) lClass.getDeclaredConstructor().newInstance();
            }
        } catch (final ClassNotFoundException exc) {
            throw new BOMException("ClassNotFound " + exc.getMessage(), exc);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException exc) {
            throw new BOMException(exc.getMessage(), exc);
        }
    }

    /** Returns a DomainObject filled with the values of the inputed ResultSet
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @param result java.sql.ResultSet */
    @Override
    protected GeneralDomainObject newInstance(final ResultSet result) throws BOMException {
        final DomainObjectImpl model = (DomainObjectImpl) this.newInstance();
        model.loadFromResultSet(result);
        return model;
    }

    /** @return java.util.Hashtable */
    @Override
    public final synchronized Hashtable<String, String> tableNames() { // NOPMD by lbenno
        final Hashtable<String, String> out = new Hashtable<>(); // NOPMD by lbenno
        out.putAll(tableNames2());
        return out;
    }

    @Override
    public Map<String, String> tableNames2() { // NOPMD
        if (this.tableNameMap == null) {
            this.tableNameMap = new Hashtable<>(7);
            try {
                for (final PropertyDef propertyDef : getObjectDef().getPropertyDefs2()) {
                    final String tableName = (String) propertyDef.getMappingDef().get("");
                    if (!this.tableNameMap.containsKey(tableName)) {
                        this.tableNameMap.put(tableName, tableName);
                    }
                }
            } catch (final GettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            } // try-catch
        } // if
        return this.tableNameMap;
    }

    /** Returns the table names in a string which can be used to create a SQL select statement.
     *
     * @return java.lang.String */
    protected String tableNameString() {
        return tableNames().values().stream().collect(Collectors.joining(", "));
    }

    /** This method can be implemented by concrete subclasses to create test objects.
     *
     * @return java.util.Vector */
    @Override
    protected List<Object> createTestObjects() {
        return Collections.emptyList();
    }

    /** Use this method to find a DomainObject by its key.
     *
     * @return org.hip.kernel.bom.DomainObject
     * @param inKey org.hip.kernel.bom.KeyObject */
    @Override
    public DomainObject findByKey(final KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException {

        // Pre: inKey not null
        if (VSys.assertNotNull(this, "findByKey", inKey) == Assert.FAILURE) {
            return null;
        }

        DomainObject outDomainObject = null;

        if (getUseCache() && inKey.isPrimaryKey()) {
            outDomainObject = (DomainObject) cache().get(inKey);
        }
        if (outDomainObject == null) {
            // not in the cache, we fetch it from database
            QueryResult lResult = null;
            try {
                lResult = this.select(inKey);
                outDomainObject = (DomainObject) lResult.nextAsDomainObject();
            } catch (final SQLException | BOMException exc) {
                throw new BOMInvalidKeyException(exc.toString(), exc);
            }
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

    /** This method is a limited version of a delete. It takes as argument a KeyObject.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inCommit boolean true if the statement has to bo commited after execution.
     * @exception java.sql.SQLException The exception description. */
    @Override
    public void delete(final KeyObject inKey, final boolean inCommit) throws SQLException {

        if (VSys.assertNotNull(this, "delete(KeyObject)", inKey) == Assert.FAILURE) {
            return;
        }

        final QueryStatement lStatement = this.createQueryStatement();
        lStatement.setSQLString(this.createDeleteString(inKey));
        lStatement.executeUpdate(inCommit);
    }

    /** Use this method to delete an entry from the table. (Contrary of insert.)
     *
     * @param inKey org.hip.kernel.bom.KeyObject */
    @Override
    public void remove(final KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException { // NOPMD by lbenno
        // not implemented yet
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.objectDef);
        out.writeObject(this.temporary);
        out.writeObject(this.tableNameMap);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.objectDef = (ObjectDef) inStream.readObject();
        this.temporary = (DomainObject) inStream.readObject();
        this.tableNameMap = (HashMap<String, String>) inStream.readObject();
        this.dbAdapter = getDBAdapter();
    }

    /** Checks whether the database table structure corresponds to the domain object definition. The test compares the
     * number of columns and the column names.
     *
     * @param schemaPattern String table schema (may be <code>null</code>)
     * @return boolean <code>true</code> if the table structure in the database corresponds to the structur according to
     *         the home's object definition.
     * @throws SQLException
     * @throws VException */
    @Override
    public boolean checkStructure(final String schemaPattern) throws SQLException, VException {
        final String tableName = getTableName();
        final Collection<String> columnNamesDef = getColumnNamesDef();
        final Collection<String> columnNamesDB = getColumnNamesDB(schemaPattern, tableName);

        // compare
        if (columnNamesDB.size() != columnNamesDef.size()) {
            return false;
        }

        for (final String columnName : columnNamesDB) {
            if (!columnNamesDef.contains(columnName)) {
                return false;
            }
        }
        return true;
    }

    private String getTableName() {
        return getObjectDef().getTableNames2().toArray(new String[1])[0];
    }

    private Collection<String> getColumnNamesDef() {
        final Collection<String> columnNamesDef = new ArrayList<>();
        for (final PropertyDef propertyDef : getObjectDef().getPropertyDefs2()) {
            columnNamesDef.add(propertyDef.getMappingDef().getColumnName().toUpperCase());
        }
        return columnNamesDef;
    }

    /** Returns a collection of column names in the specified table.
     *
     * @param schemaPattern String, may be null
     * @param tableName String, must not be null
     * @return Collection of column names in the specified table.
     * @throws SQLException
     * @throws VException */
    private Collection<String> getColumnNamesDB(final String schemaPattern, final String tableName)
            throws SQLException, VException {
        final Collection<String> columnNamesDB = new ArrayList<>();

        try (Connection connection = DataSourceRegistry.INSTANCE.getConnection();
                final ResultSet data = connection.getMetaData().getColumns(null, schemaPattern,
                        tableName.toUpperCase(Locale.getDefault()), null)) {
            while (data.next()) {
                columnNamesDB.add(data.getString(4).toUpperCase());
            }
            return columnNamesDB;
        }
    }

}