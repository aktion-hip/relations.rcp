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

/** The Page is a special object that can be accessed by the QueryResult interface.
 *
 * @author Benno Luthiger */
public interface Page { // NOPMD

    // class variables
    int DEFAULT_PAGE_SIZE = 5;

    /** Returns the next page. It will return itself if it is the lastPage.
     *
     * @return org.hip.kernel.bom.Page */
    Page getNextPage();

    /** Returns the collection with all DomainObjects.
     *
     * @return org.hip.kernel.bom.DomainObjectCollection */
    DomainObjectCollection getObjects();

    /** @return int */
    int getPageNumber();

    /** Returns the previous page. It returns itself if it is the first page.
     *
     * @return org.hip.kernel.bom.Page */
    Page getPreviousPage();

    /** @return org.hip.kernel.bom.QueryResult */
    QueryResult getQueryResult();

    /** @return java.lang.String */
    String getSerialized();

    /** Returns true if the current cursor has more elements.
     *
     * @return boolean */
    boolean hasMoreElements();

    /** Returns true if this is the first page in a query result.
     *
     * @return boolean */
    boolean isFirstPage();

    /** Returns true if this is the last page in a query result.
     *
     * @return boolean */
    boolean isLastPage();

    /** @return org.hip.kernel.bom.GeneralDomainObject */
    GeneralDomainObject nextElement();

    /** Returns the page as XML string. The output is the same as in nextnAsXML in the QueryResult.
     *
     * @return java.lang.String */
    String pageAsXML();

    /** This method release the objects hold within the page. */
    void release();

    /** @param inSerializedString java.lang.String */
    void setSerialized(String inSerializedString);
}
