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

package org.hip.kernel.bom.impl; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DBAdapterSimple;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.bom.ReadOnlyDomainObject;
import org.hip.kernel.bom.StatisticsHome;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.impl.ObjectDefGenerator;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.ListJoiner;
import org.xml.sax.SAXException;

/** This class implements the StatisticsHome interface.
 *
 * @author B. Luthiger
 * @version 1.0
 * @see org.hip.kernel.bom.StatisticsHome */
@SuppressWarnings("serial")
abstract public class AbstractStatisticsHomeImpl extends AbstractDomainObjectHome implements StatisticsHome {

    // Instance variables
    private ObjectDef internObjectDef;
    private ObjectDef externObjectDef;
    private DBAdapterSimple dbAdapter;

    // some optimizations
    private String columnListIntern;
    private Map<String, String> tables;

    /** AbstractStatisticsHomeImpl default constructor */
    protected AbstractStatisticsHomeImpl() {
        super();
    }

    /** This method creates a new instance of a ReadOnlyDomainObject
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject */
    @Override
    public ReadOnlyDomainObject create() throws BOMException {
        final ReadOnlyDomainObject out = newInstance();
        ((DomainObjectImpl) out).initializeForNew();
        return out;
    }

    /** This method creates a list of all columns of the table mapped to the DomainObject managed by this home. This
     * method can be used to create the SQL string.
     *
     * @return java.lang.String */
    protected String createColumnListIntern() {
        if (columnListIntern == null) {
            columnListIntern = "";
            try {
                final ListJoiner lColumns = new ListJoiner();
                for (final PropertyDef lPropertyDef : getObjectDefIntern().getPropertyDefs2()) {
                    final MappingDef lMappingDef = lPropertyDef.getMappingDef();
                    lColumns.addEntry((String) lMappingDef.get("lColumnName"), "%2$s.%1$s",
                            (String) lMappingDef.get("lTableName"));
                }
                columnListIntern += lColumns.joinSpaced(",");
            } catch (final GettingException exc) {
                DefaultExceptionHandler.instance().handle(exc);
            }
        }

        return columnListIntern;
    }

    /** @return org.hip.kernel.bom.model.ObjectDef */
    private ObjectDef createObjectDefExtern() {
        try {
            synchronized (this) {
                final ObjectDef outObjectDef = ObjectDefGenerator.getSingleton()
                        .createObjectDef(getObjectDefStringExtern());
                return outObjectDef;
            }
        } catch (final SAXException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** @return org.hip.kernel.bom.model.ObjectDef */
    private ObjectDef createObjectDefIntern() {
        try {
            synchronized (this) {
                final ObjectDef outObjectDef = ObjectDefGenerator.getSingleton()
                        .createObjectDef(getObjectDefStringIntern());
                return outObjectDef;
            }
        } catch (final SAXException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** Delegates to the adapter's createPreparedSelectString().
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject */
    protected String createPreparedSelectString(final KeyObject inKey) {
        return dbAdapter.createPreparedSelectString(inKey, this);
    }

    /** This method creates a QueryStatement as part of the frameworks QueryService.
     *
     * @return org.hip.kernel.bom.QueryStatement */
    @Override
    public QueryStatement createQueryStatement() {
        return new DefaultQueryStatement(this);
    }

    /** This is the very basic implementation to create the select statement .
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject */
    @Override
    protected String createSelectString(final KeyObject inKey) {
        return dbAdapter.createSelectString(inKey, this);
    }

    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder) { // NOPMD by lbenno
        return dbAdapter.createSelectString(inKey, inOrder, this);
    }

    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving) { // NOPMD
        return dbAdapter.createSelectString(inKey, inOrder, inHaving, this);
    }

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @param inGroupBy org.hip.kernel.bom.GroupByObject
     * @return java.lang.String */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GroupByObject inGroupBy) throws BOMException {
        return dbAdapter.createSelectString(inKey, inOrder, inHaving, inGroupBy, this);
    }

    @Override
    protected String createSelectString(final OrderObject inOrder) { // NOPMD by lbenno
        return dbAdapter.createSelectString(inOrder, this);
    }

    /** This method must be implemented by concrete subclasses to create test objects.
     *
     * @return java.util.Vector<Object> */
    @Override
    protected abstract List<Object> createTestObjects();

    /** Returns the name of the column which is mapped to the inputed property
     *
     * @return java.lang.String
     * @param inPropertyName java.lang.String */
    public String getColumnNameInternFor(final String inPropertyName) {

        try {
            final MappingDef lMapping = getPropertyDefIntern(inPropertyName).getMappingDef();
            return (String) lMapping.get("tableName") + "." + lMapping.get("columnName");
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return "";
        }
    }

    /** Returns the name of the objects which the concrete home can create. This method is abstract and must be
     * implemented by concrete subclasses.
     *
     * @return java.lang.String */
    @Override
    abstract public String getObjectClassName();

    /** Returns the object definition for the class managed by this home.
     *
     * @return ObjectDef */
    @Override
    public ObjectDef getObjectDef() {
        if (externObjectDef == null) {
            externObjectDef = createObjectDefExtern();
        }
        return externObjectDef;
    }

    /** No hidden fields, therefore, an empty String is returned.
     *
     * @param inPropertyName String
     * @return String */
    @Override
    public String getHidden(final String inPropertyName) { // NOPMD by lbenno
        return "";
    }

    /** Returns the object definition for the class managed by this home.
     *
     * @return ObjectDef */
    private ObjectDef getObjectDefIntern() {
        if (internObjectDef == null) {
            internObjectDef = createObjectDefIntern();
        }
        return internObjectDef;
    }

    /** Returns the object definition string of the class managed by this home. Concrete subclasses must implement this
     * method.
     *
     * @return java.lang.String */
    protected abstract String getObjectDefStringExtern();

    /** Returns the object definition string for the internal definitions. Concrete subclasses must implement this
     * method.
     *
     * @return java.lang.String */
    protected abstract String getObjectDefStringIntern();

    /** This method returns the PropertyDef object which describes the behaviour of the Property object mapped to the
     * tablecolumne named inColumnName.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inColumnName java.lang.String */
    public PropertyDef getPropertyDefForIntern(final String inColumnName) {

        if (VSys.assertNotNull(this, "getPropertyDefForIntern", inColumnName) == Assert.FAILURE) {
            return null;
        }

        try {
            for (final PropertyDef outPropertyDef : getObjectDef().getPropertyDefs2()) {
                final String lColumnName = (String) outPropertyDef.getMappingDef().get("inColumnName");
                if (inColumnName.equals(lColumnName)) {
                    return outPropertyDef;
                }
            }

            // not found
            return null;
        } catch (final GettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** This method returns the PropertyDef object which describes the behaviour of the Property object named
     * propertyName.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inPropertyName java.lang.String */
    public PropertyDef getPropertyDefIntern(final String inPropertyName) {

        // Pre: propertyName not null
        if (VSys.assertNotNull(this, "getPropertyDef", inPropertyName) == Assert.FAILURE) {
            return null;
        }

        return getObjectDefIntern().getPropertyDef(inPropertyName);
    }

    /** This method can be used to initialize a new instance of the class. Subclasses may overrride.<br />
     * <b>Note:</b> Clienst have to call this method explicitly! */
    @Override
    public void initialize() {
        dbAdapter = getDBAdapter();
    }

    private DBAdapterSimple getDBAdapter() {
        if (dbAdapter == null) {
            dbAdapter = retrieveDBAdapterType().getSimpleDBAdapter(getObjectDef());
        }
        return dbAdapter;
    }

    /** Returns a ReadOnlyDomainObject filled with the values of the inputed ResultSet
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @param inResult java.sql.ResultSet */
    @Override
    protected GeneralDomainObject newInstance(final ResultSet inResult) throws BOMException {
        final DomainObjectImpl retVal = (DomainObjectImpl) this.newInstance();
        retVal.loadFromResultSet(inResult);
        return retVal;
    }

    /** @return java.util.Hashtable */
    private final Map<String, String> tableNames() {
        synchronized (this) {
            if (tables == null) {
                tables = new HashMap<String, String>();
                try {
                    for (final PropertyDef lPropertyDef : getObjectDefIntern().getPropertyDefs2()) {
                        final String lTableName = (String) lPropertyDef.getMappingDef().get("tableName");
                        if (!tables.containsKey(lTableName)) {
                            tables.put(lTableName, lTableName);
                        }
                    }
                } catch (final GettingException exc) {
                    DefaultExceptionWriter.printOut(this, exc, true);
                } // try-catch
            } // if
        }
        return tables;
    }

    /** Returns the table names in a string which can be used to create a SQL select statement. We have to overwrite
     * super.tableNameString() for that we can get the internal table names accessed via this.tableNames().
     *
     * @return java.lang.String */
    @Override
    public String tableNameString() {
        String outTabelNames = "";
        boolean lFirst = true;
        for (final String lTableName : tableNames().values()) {
            if (!lFirst) {
                outTabelNames += ", "; // NOPMD by lbenno
            }
            lFirst = false;
            outTabelNames += lTableName; // NOPMD by lbenno
        }
        return outTabelNames;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(internObjectDef);
        out.writeObject(externObjectDef);
        out.writeObject(columnListIntern);
        out.writeObject(tables);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        internObjectDef = (ObjectDef) inStream.readObject();
        externObjectDef = (ObjectDef) inStream.readObject();
        columnListIntern = (String) inStream.readObject();
        tables = (Map<String, String>) inStream.readObject();
        dbAdapter = getDBAdapter();
    }

}