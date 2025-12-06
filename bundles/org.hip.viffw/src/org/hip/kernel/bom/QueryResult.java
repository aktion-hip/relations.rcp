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
import java.util.Locale;

/** The QueryResult is a kind of iterator. It is the return object of the DomainHome.query method.
 *
 * @author Benno Luthiger */
public interface QueryResult { // NOPMD

    // Class variables
    int DEFAULT_PAGE_LENGTH = 20;

    /** @return org.hip.kernel.bom.GeneralDomainObject may be <code>null</code> */
    GeneralDomainObject getCurrent();

    /** @return org.hip.kernel.bom.Page */
    Page getCurrentPage();

    /** This method returns the KeyObj of the current DomainObject. Null is returned if there is no current object
     *
     * @return org.hip.kernel.bom.KeyObject */
    KeyObject getKey() throws BOMNotFoundException;

    /** @return boolean */
    boolean hasMoreElements();

    /** Returns the next DomainObject.
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    GeneralDomainObject next() throws SQLException, BOMException;

    /** Returns the next DomainObject.
     *
     * @return org.hip.kernel.bom.GeneralDomainObject
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    GeneralDomainObject nextAsDomainObject() throws SQLException, BOMException;

    /** Returns the next entry in form of an XML string. No Domain object will be instatiated. This method is useful if
     * the data is used in browsers. Application then will instantiate a DomainObject through the findByPrimaryKey
     * method.
     *
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    String nextAsXMLString() throws SQLException, BOMException;

    /** Returns the next entry in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    String nextAsXMLString(String inSerializerName) throws SQLException, BOMException;

    /** Returns the next entry in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @return java.lang.String
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    String nextAsXMLString(String inSerializerName, boolean inUseFilter) throws SQLException, BOMException;

    /** @see QueryResult#nextAsXMLString(String inSerializerName, boolean inUseFilter)
     *
     * @param inSerializerName java.lang.String The name of the serializer class.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @param inLocale The locale to format date values.
     * @return java.lang.String
     * @throws SQLException
     * @throws BOMException */
    String nextAsXMLString(String inSerializerName, boolean inUseFilter, Locale inLocale) throws SQLException,
    BOMException;

    /** Returns a DomainObjectCollection containing the specified amount of DomainObjects.
     *
     * @return org.hip.kernel.bom.DomainObjectCollection
     * @param howMany int
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    DomainObjectCollection nextn(int inHowMany) throws SQLException, BOMException;

    /** Returns the next n entries in form of an XML string. No Domain object will be instatiated. This method is useful
     * if the data is used in browsers. Application then will instantiate a DomainObject through the findByPrimaryKey
     * method.
     *
     * @return java.lang.String
     * @param howMany int number of serialized DomainObjects to return
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    String nextnAsXMLString(int inHowMany) throws SQLException, BOMException;

    /** Returns the next n entries in form of an XML string using the specified serializer. No Domain object will be
     * instatiated. This method is useful if the data is used in browsers. Application then will instantiate a
     * DomainObject through the findByPrimaryKey method.
     *
     * @return java.lang.String
     * @param inHowMany int number of serialized DomainObjects to return.
     * @param inSerializerName java.lang.String The name of the serializer to be used.
     * @exception java.sql.SQLException
     * @exception org.hip.kernel.bom.BOMException */
    String nextnAsXMLString(int inHowMany, String inSerializerName) throws SQLException, BOMException;

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
    String nextnAsXMLString(int inHowMany, String inSerializerName, boolean inUseFilter) throws SQLException,
    BOMException;

    /** @see QueryResult#nextnAsXMLString(int inHowMany, String inSerializerName, boolean inUseFilter)
     *
     * @param inHowMany int number of serialized DomainObjects to return.
     * @param inSerializerName java.lang.String The name of the serializer to be used.
     * @param inUseFilter boolean True, if the serializer should be constructed with filter.
     * @param inLocale The locale to format date values.
     * @return java.lang.String
     * @throws SQLException
     * @throws BOMException */
    String nextnAsXMLString(int inHowMany, String inSerializerName, boolean inUseFilter, Locale inLocale)
            throws SQLException, BOMException;

    /** @param org.hip.kernel.bom.Page */
    void setCurrentPage(Page inPage);

}
