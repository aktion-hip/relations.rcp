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
package org.hip.kernel.bom.impl; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.BOMInvalidKeyException;
import org.hip.kernel.bom.BOMNotFoundException;
import org.hip.kernel.bom.DBAdapterJoin;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.JoinedDomainObjectHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.ReadOnlyDomainObject;
import org.hip.kernel.bom.impl.PlacefillerCollection.Placefiller;
import org.hip.kernel.bom.model.JoinDef;
import org.hip.kernel.bom.model.JoinedObjectDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.impl.JoinedObjectDefGenerator;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.xml.sax.SAXException;

/** This class implements the responsibilities of "Homes" for joined domain objects.
 *
 * @author: Benno Luthiger */
@SuppressWarnings("serial")
abstract public class JoinedDomainObjectHomeImpl extends AbstractDomainObjectHome implements JoinedDomainObjectHome { // NOPMD
    private ObjectDef objectDef;
    private JoinedObjectDef joinedObjectDef;
    private ReadOnlyDomainObject temporary;
    private DBAdapterJoin dbAdapter;

    /** JoinedDomainObjectHomeImpl default constructor. */
    protected JoinedDomainObjectHomeImpl() {
        super();
        this.dbAdapter = getDBAdapter();
    }

    /** This method can be implemented by concrete subclasses to create test objects.
     *
     * @return List<Object> */
    @Override
    protected List<Object> createTestObjects() { // NOPMD by lbenno
        return null;
    }

    /** This method creates a new instance of a DomainObject
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject */
    @Override
    public ReadOnlyDomainObject create() throws BOMException {
        final ReadOnlyDomainObject outDomainObject = newInstance();
        ((DomainObjectImpl) outDomainObject).initializeForNew();
        return outDomainObject;
    }

    /** Creates the select string to fetch all domain objects.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectAllString() throws BOMException {
        return this.dbAdapter.createSelectAllSQL();
    }

    /** Creates the select string to fetch all domain objects matching the specified key.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey) throws BOMException {
        return this.dbAdapter.createSelectSQL(inKey, this);
    }

    /** Creates the select string to fetch all domain objects matching the specified key ordered by the specified object.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder) throws BOMException {
        return this.dbAdapter.createSelectSQL(inKey, inOrder, this);
    }

    /** Creates the select string to fetch all domain objects matching the specified key and meeting the specified having
     * criterion sorted by the specified order. Note: To specify a clause only using e.g. the HAVING part, you can
     * provide empty objects for key and order.
     *
     * @param inKey KeyObject
     * @param inOrder OrderObject
     * @param inHaving HavingObject
     * @return String
     * @throws BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving)
            throws BOMException {
        return this.dbAdapter.createSelectSQL(inKey, inOrder, inHaving, this);
    }

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @param inGroupBy org.hip.kernel.bom.GroupByObject
     * @return java.lang.String */
    @Override
    protected String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GroupByObject inGroupBy) throws BOMException {
        return this.dbAdapter.createSelectSQL(inKey, inOrder, inHaving, inGroupBy, this);
    }

    /** Creates the select string to fetch all domain objects ordered by the specified object.
     *
     * @return java.lang.String
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createSelectString(final OrderObject inOrder) throws BOMException {
        return this.dbAdapter.createSelectSQL(inOrder, this);
    }

    /** Creates the select string to fetch all domain objects matching the specified key limitied by the specified limit.
     *
     * @param inKey KeyObject
     * @param inLimit LimitObject
     * @return String
     * @throws BOMException */
    @Override
    protected String createSelectString(final KeyObject inKey, final LimitObject inLimit) throws BOMException {
        return this.dbAdapter.createSelectSQL(inKey, inLimit, this);
    }

    /** Creates the select sql string counting all table entries corresponding to this home.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createCountAllString() throws BOMException {
        return this.dbAdapter.createCountAllSQL();
    }

    /** Creates the select sql string counting all table entries corresponding to this home and the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    @Override
    protected String createCountString(final KeyObject inKey) throws BOMException {
        return this.dbAdapter.createCountSQL(inKey, this);
    }

    @Override
    protected String createCountString(final KeyObject inKey, final HavingObject inHaving, final GroupByObject inGroupBy) // NOPMD
            // by
            // lbenno
            throws BOMException {
        return this.dbAdapter.createCountSQL(inKey, inHaving, inGroupBy, this);
    }

    /** This method looks for all key columns of the table mapped to the DomainObject managed by this home and creates a
     * list COUNT(keyField). Instead of sending SELECT COUNT(*) FROM ... this SQL-sequence can be used to count all
     * entries in a table with better performance.
     *
     * @return java.lang.String */
    @Override
    protected String createKeyCountColumnList() { // NOPMD by lbenno
        return "";
    }

    /** @return org.hip.kernel.bom.model.JoinedObjectDef */
    private JoinedObjectDef createObjectDef() {
        try {
            synchronized (this) {
                final JoinedObjectDef outVal = JoinedObjectDefGenerator.getSingleton().createJoinedObjectDef(
                        getObjectDefString());
                return outVal;
            }
        } catch (final SAXException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** Use this method to find a DomainObject by its key.
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.bom.BOMNotFoundException
     * @throws org.hip.kernel.bom.BOMInvalidKeyException */
    @Override
    public ReadOnlyDomainObject findByKey(final KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException {

        // Pre: inKey not null
        if (VSys.assertNotNull(this, "findByKey", inKey) == Assert.FAILURE) {
            return null;
        }

        ReadOnlyDomainObject outDomainObject = null;

        if (getUseCache() && inKey.isPrimaryKey()) {
            outDomainObject = (ReadOnlyDomainObject) cache().get(inKey);
        }
        if (outDomainObject == null) {
            // not in the cache, we fetch it from database
            QueryResult lResult = null;
            try {
                lResult = this.select(inKey);
                outDomainObject = (ReadOnlyDomainObject) lResult.nextAsDomainObject();
                if (outDomainObject == null) {
                    throw new BOMNotFoundException();
                }
                if (getUseCache() && inKey.isPrimaryKey()) { // cache it
                    cache().put(outDomainObject);
                }
            } catch (final SQLException exc) {
                throw new BOMInvalidKeyException(exc.toString(), exc);
            } catch (final BOMException exc) {
                throw new BOMInvalidKeyException(exc.toString(), exc);
            }
        }

        return outDomainObject;
    }

    /** Returns the joined object definition.
     *
     * @return org.hip.kernel.bom.model.JoinedObjectDef */
    private JoinedObjectDef getJoinedObjectDef() {
        if (this.joinedObjectDef == null) {
            this.joinedObjectDef = createObjectDef();
        }
        return this.joinedObjectDef;
    }

    /** Returns the object definition for the class managed by this home.
     *
     * @return ObjectDef */
    @Override
    public ObjectDef getObjectDef() {
        if (this.objectDef == null) {
            this.objectDef = getJoinedObjectDef().getDomainObjectDef();
        }
        return this.objectDef;
    }

    /** Returns the mappings of hidden fields or an empty String.
     *
     * @param inPropertyName String
     * @return String */
    @Override
    public String getHidden(final String inPropertyName) {
        return getJoinedObjectDef().getHidden(inPropertyName);
    }

    /** The temporary can used if the client does not use it for long.
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject */
    protected ReadOnlyDomainObject getTemporary() throws BOMException {
        if (this.temporary == null) {
            this.temporary = newInstance();
        }
        return this.temporary;
    }

    private DBAdapterJoin getDBAdapter() {
        if (this.dbAdapter == null) {
            this.dbAdapter = retrieveDBAdapterType().getJoinDBAdapter(getJoinedObjectDef());
        }
        return this.dbAdapter;
    }

    /** Returns an empty DomainObject. An empty object is initialized but does not contain any values. The object goes
     * back into this state after releasing.
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject */
    @Override
    public ReadOnlyDomainObject newInstance() throws BOMException {
        try {
            // straightforward
            final Iterator<GeneralDomainObject> lReleased = releasedObjects().iterator();
            if (lReleased.hasNext()) {
                final ReadOnlyDomainObject outDomainObject = (ReadOnlyDomainObject) lReleased.next();
                lReleased.remove();
                return outDomainObject;
            }
            else {
                final Class<?> lClass = Class.forName(getObjectClassName());
                return (ReadOnlyDomainObject) lClass.newInstance();
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException exc) {
            throw new BOMException(exc.getMessage(), exc);
        }
    }

    /** Returns a GeneralDomainObject filled with the values of the inputed ResultSet
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @param inResult java.sql.ResultSet */
    @Override
    protected GeneralDomainObject newInstance(final ResultSet inResult) throws BOMException {
        final DomainObjectImpl retVal = (DomainObjectImpl) this.newInstance();
        retVal.loadFromResultSet(inResult);
        return retVal;
    }

    @Override
    public QueryResult select(final PlacefillerCollection inPlacefillers) throws SQLException, BOMException { // NOPMD
        // by
        // lbenno
        processPlacefillers(inPlacefillers);
        return select();
    }

    @Override
    public QueryResult select(final KeyObject inKey, final PlacefillerCollection inPlacefillers) throws SQLException, // NOPMD
    // by
    // lbenno
    BOMException {
        processPlacefillers(inPlacefillers);
        return select(inKey);
    }

    @Override
    public QueryResult select(final KeyObject inKey, final OrderObject inOrder, // NOPMD by lbenno
            final PlacefillerCollection inPlacefillers) throws SQLException, BOMException {
        processPlacefillers(inPlacefillers);
        return select(inKey, inOrder);
    }

    private void processPlacefillers(final PlacefillerCollection inPlacefillers) throws BOMException {
        for (final Iterator<Placefiller> lPlacefillers = inPlacefillers.iterator(); lPlacefillers.hasNext();) {
            final PlacefillerCollection.Placefiller lPlacefiller = lPlacefillers.next();
            final String lAlias = lPlacefiller.alias;
            final StringBuilder lSQL = new StringBuilder("(");
            lSQL.append(((AbstractDomainObjectHome) lPlacefiller.home).createSelectString(lPlacefiller.key))
                    .append(") AS ").append(lAlias);
            final JoinDef lPlaceholdersDef = traverseJoinDefs(this.joinedObjectDef.getJoinDef(), lAlias);
            if (lPlaceholdersDef != null) {
                lPlaceholdersDef.fillPlaceholder(lAlias, new String(lSQL));
            }
        }
        if (inPlacefillers.size() > 0) {
            this.dbAdapter.reset();
        }
    }

    private JoinDef traverseJoinDefs(final JoinDef inJoinDef, final String inAlias) throws BOMException {
        if (inJoinDef.hasPlaceholder(inAlias)) {
            return inJoinDef;
        }
        else {
            final JoinDef lJoinDef = inJoinDef.getChildJoinDef();
            if (lJoinDef == null) {
                return null;
            }
            return traverseJoinDefs(lJoinDef, inAlias);
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(this.objectDef);
        out.writeObject(this.joinedObjectDef);
        out.writeObject(this.temporary);
    }

    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.objectDef = (ObjectDef) inStream.readObject();
        this.joinedObjectDef = (JoinedObjectDef) inStream.readObject();
        this.temporary = (ReadOnlyDomainObject) inStream.readObject();
        this.dbAdapter = getDBAdapter();
    }

}