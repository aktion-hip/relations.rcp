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

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.BOMNotFoundException;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.Page;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.QueryStatement;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.sys.VObject;

/** This is the abstract implementation of the QueryResult interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.QueryResult */
public abstract class AbstractQueryResult extends VObject implements QueryResult {

    // Instance variables
    // private ResultSet result;
    private final GeneralDomainObjectHome home;

    private QueryStatement statement;
    private Page currentPage;

    private final Data data;
    private Cursor cursor;

    // class attributes
    private static XMLCharacterFilter cCharacterFilter;

    /** AbstractQueryResult default constructor.
     *
     * @param home org.hip.kernel.bom.GeneralDomainObjectHome */
    public AbstractQueryResult(final GeneralDomainObjectHome home) {
        this.home = home;
        this.data = new Data(Collections.emptyList(), null);
        this.cursor = this.data.get(0);
    }

    /** AbstractQueryResult constructor. Initializes the instance variables and initializes the first DomainObject with
     * data from the ResultSet.
     *
     * @param home org.hip.kernel.bom.GeneralDomainObjectHome
     * @param result java.sql.ResultSet
     * @param statement org.hip.kernel.bom.QueryStatement */
    public AbstractQueryResult(final GeneralDomainObjectHome home, final ResultSet result,
            final QueryStatement statement) {
        this.home = home;
        this.statement = statement;
        this.data = loadData(home, result);
        this.cursor = this.data.get(0);
    }

    private Data loadData(final GeneralDomainObjectHome home, final ResultSet result) {
        try {
            final List<GeneralDomainObject> objects = new ArrayList<>(result.getFetchSize());
            while (result.next()) {
                objects.add(((AbstractDomainObjectHome) home).newInstance(result));
            }
            return new Data(objects, result.getMetaData());
        } catch (BOMException | SQLException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return new Data(Collections.emptyList(), null);
    }

    /** @return org.hip.kernel.bom.GeneralDomainObject */
    @Override
    public GeneralDomainObject getCurrent() {
        return this.cursor.model();
    }

    /** @return org.hip.kernel.bom.Page */
    @Override
    public Page getCurrentPage() {
        return this.currentPage;
    }

    /** @return org.hip.kernel.bom.impl.XMLCharacterFilter */
    private XMLCharacterFilter getFilter() {
        synchronized (this) {
            if (cCharacterFilter == null) {
                cCharacterFilter = XMLCharacterFilter.DEFAULT_FILTER;
            }
        }
        return cCharacterFilter;
    }

    /** Returns the key of the current domain object.
     *
     * @return org.hip.kernel.bom.KeyObject
     * @exception org.hip.kernel.bom.BOMNotFoundException */
    @Override
    public KeyObject getKey() throws BOMNotFoundException {
        if (this.cursor == null || !this.cursor.isValid()) {
            throw new BOMNotFoundException();
        }
        else {
            return this.cursor.model().getKey();
        }
    }

    /** Returns the ResultSet MetaData.
     *
     * @return java.sql.ResultSetMetaData */
    protected ResultSetMetaData getMetaData() {
        return this.data.metaData();
    }

    /** @return boolean <code>true</code> if the result set still has more elements, <code>false</code> if the cursor is
     *         at the end. */
    @Override
    public boolean hasMoreElements() {
        return !this.data.objects.isEmpty() && this.cursor.isValid() && this.cursor.index() < this.data.objects.size();
    }

    /** Returns a new Instance of the DomainObject initialized with data from the QueryResult.
     *
     * @exception <code>SQLException </code>
     * @exception org.hip.kernel.bom.BOMException
     * @return instance of next domain object. Returns null if query result has no more elements. */
    @Override
    public GeneralDomainObject next() throws SQLException, BOMException {
        final GeneralDomainObject current = this.cursor.model;
        this.cursor = this.data.get(this.cursor.index() + 1);
        return current;
    }

    /** @return org.hip.kernel.bom.GeneralDomainObject
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public GeneralDomainObject nextAsDomainObject() throws SQLException, BOMException {
        return next();
    }

    /** Returns the next entry in form of an XML string. No Domain object will be instatiated. This method is useful if
     * the data is used in browsers. Application then will instantiate a DomainObject through the findByPrimaryKey
     * method.
     *
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextAsXMLString() throws SQLException, BOMException {
        return nextAsXMLString(new XMLSerializer(getFilter()));
    }

    /** Returns the next entry in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextAsXMLString(final String inSerializerName) throws SQLException, BOMException {
        return nextAsXMLString(inSerializerName, true);
    }

    /** Returns the next entry in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextAsXMLString(final String inSerializerName, final boolean inUseFilter) throws SQLException,
    BOMException {
        return nextAsXMLString(inSerializerName, inUseFilter, null);
    }

    /** @see QueryResult#nextAsXMLString(String inSerializerName, boolean inUseFilter)
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @param inLocale The locale to format date values.
     * @return java.lang.String
     * @throws SQLException
     * @throws BOMException */
    @Override
    public String nextAsXMLString(final String inSerializerName, final boolean inUseFilter, final Locale inLocale)
            throws SQLException, BOMException {
        try {
            final Class<?> lClass = Class.forName(inSerializerName);
            final XMLSerializer lSerializer = (XMLSerializer) lClass.getDeclaredConstructor().newInstance();
            if (inUseFilter) {
                lSerializer.setFilter(getFilter());
            }
            lSerializer.setLocale(inLocale);
            return nextAsXMLString(lSerializer);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
            throw new BOMException(exc.getMessage(), exc);
        }
    }

    private String nextAsXMLString(final DomainObjectVisitor inSerializer) {
        String outXML = null;
        try {
            final GeneralDomainObject lObject = nextAsDomainObject();
            if (lObject == null) {
                return null;
            }
            lObject.accept(inSerializer);
            outXML = inSerializer.toString();

            lObject.release();
            return outXML;
        } catch (final BOMException | SQLException exc) {
            DefaultExceptionHandler.instance().handle(exc);
            return null;
        }
    }

    /** Returns a specified amount of DomainObjects
     *
     * @return org.hip.kernel.bom.DomainObjectCollection
     * @param inHowMany int
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public DomainObjectCollection nextn(final int inHowMany) throws SQLException, BOMException {

        final DomainObjectCollection outObject = new DomainObjectCollectionImpl();
        try {
            for (int i = 0; this.hasMoreElements() && i < inHowMany; i++) {
                final GeneralDomainObject lObject = this.next();
                if (lObject == null) {
                    continue;
                }
                outObject.add(lObject);
            }
        } catch (final BOMException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return outObject;
    }

    /** Returns the next n entries in form of an XML string. No Domain object will be instatiated. This method is useful
     * if the data is used in browsers. Application then will instantiate a DomainObject through the findByPrimaryKey
     * method.
     *
     * @return java.lang.String
     * @param inHowMany int number of serialized DomainObjects to return
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextnAsXMLString(final int inHowMany) throws SQLException, BOMException {

        final DomainObjectIterator lIterator = nextn(inHowMany).elements();
        final XMLSerializer lSerializer = new XMLSerializer(getFilter());
        lIterator.accept(lSerializer);

        return lSerializer.toString();
    }

    /** Returns the next n entries in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @return java.lang.String
     * @param inHowMany int number of serialized DomainObjects to return.
     * @param inSerializerName java.lang.String The name of the serializer to be used.
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextnAsXMLString(final int inHowMany, final String inSerializerName) throws SQLException,
    BOMException {
        return nextnAsXMLString(inHowMany, inSerializerName, true);
    }

    /** Returns the next n entries in form of an XML string. No Domain object will be instatiated. This method is useful
     * if the data is used in browsers. Application then will instantiate a DomainObject through the findByPrimaryKey
     * method.
     *
     * @return java.lang.String
     * @param inHowMany int number of serialized DomainObjects to return.
     * @param inSerializerName java.lang.String The name of the serializer to be used.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    @Override
    public String nextnAsXMLString(final int inHowMany, final String inSerializerName, final boolean inUseFilter)
            throws SQLException, BOMException {
        return nextnAsXMLString(inHowMany, inSerializerName, inUseFilter, null);
    }

    /** @see QueryResult#nextnAsXMLString(int inHowMany, String inSerializerName, boolean inUseFilter)
     *
     * @param inHowMany int number of serialized DomainObjects to return.
     * @param inSerializerName java.lang.String The name of the serializer to be used.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @param inLocale The locale to format date values.
     * @return java.lang.String
     * @throws SQLException
     * @throws BOMException */
    @Override
    public String nextnAsXMLString(final int inHowMany, final String inSerializerName, final boolean inUseFilter,
            final Locale inLocale) throws SQLException, BOMException {
        try {
            final Class<?> lClass = Class.forName(inSerializerName);
            final Class<?>[] lParameters = { XMLCharacterFilter.class };
            final Object[] lInitArgs = { null };
            if (inUseFilter) {
                lInitArgs[0] = getFilter();
            }
            final XMLSerializer lSerializer = (XMLSerializer) lClass.getConstructor(lParameters).newInstance(lInitArgs);
            lSerializer.setLocale(inLocale);

            final DomainObjectIterator lIterator = nextn(inHowMany).elements();
            lIterator.accept(lSerializer);
            return lSerializer.toString();
        } catch (final ClassNotFoundException exc) {
            throw new BOMException("ClassNotFound " + exc.getMessage(), exc);
        } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
            throw new BOMException(exc.getMessage(), exc);
        }
    }

    /** @param inPage org.hip.kernel.bom.Page */
    @Override
    public void setCurrentPage(final Page inPage) {
        this.currentPage = inPage;
    }

    // ===

    private static record Data(List<GeneralDomainObject> objects, ResultSetMetaData metaData) {
        public Cursor get(final int index) {
            return index < this.objects.size() ? new Cursor(this.objects.get(index), index) : new Cursor(null, -1);
        }

    }

    private static record Cursor(GeneralDomainObject model, int index) {
        public boolean isValid() {
            return this.model != null;
        }
    }

}