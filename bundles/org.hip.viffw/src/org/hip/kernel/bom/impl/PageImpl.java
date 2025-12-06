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
package org.hip.kernel.bom.impl;

import java.io.Serializable;
import java.sql.SQLException;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.Page;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.Debug;

/** This class implements the Page interface.
 *
 * <PRE>
 * QueryResult lResult = home.query(&quot;SELECT * FROM XYZ&quot;);
 * Page lPage = new PageImpl(lResult, null, 15);
 * 
 * String lXML = &quot;&quot;;
 * Serializer lSerializer = new XMLSerializer();
 * 
 * while (lPage.hasMoreElements()) {
 *     DomainObject lBOM = lPage.nextElement();
 *     lBOM.accept(lSerializer);
 *     lXML += lSerializer.toString();
 * 
 * }
 * lPage.setSerialized(lXML);
 * lPage.release();
 *
 * </PRE>
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.Page */
@SuppressWarnings("serial")
public class PageImpl extends VObject implements Page, Serializable {
    // Instance variables
    private final QueryResult result;
    private final Page previous;
    private Page next;
    private DomainObjectCollection collection;
    private DomainObjectIterator domainObjIterator;
    private boolean nextLoaded;
    private String xmlCache;
    private int pageSize = Page.DEFAULT_PAGE_SIZE;
    private int pageNumber = 1;

    /** PageImpl constructor for default page size.
     *
     * @param inResult org.hip.kernel.bom.QueryResult
     * @param inPrevious org.hip.kernel.bom.Page */
    public PageImpl(final QueryResult inResult, final Page inPrevious) {
        this(inResult, inPrevious, Page.DEFAULT_PAGE_SIZE);
    }

    /** PageImpl constructor, initializes the a collection of DomainObjects.
     *
     * @param inResult org.hip.kernel.bom.QueryResult
     * @param inPrevious org.hip.kernel.bom.Page
     * @param inPageSize int */
    public PageImpl(final QueryResult inResult, final Page inPrevious, final int inPageSize) {
        super();
        result = inResult;
        previous = inPrevious;
        pageSize = inPageSize;
        this.initCollection();
        if (inPrevious != null) {
            pageNumber = inPrevious.getPageNumber() + 1;
        }

    }

    /** @return org.hip.kernel.bom.Page */
    @Override
    public Page getNextPage() {

        // Lazy load
        if (!nextLoaded) {
            loadNextPage();
        }

        return (next == null) ? this : next;
    }

    /** Returns the collection with all DomainObjects.
     *
     * @return org.hip.kernel.bom.DomainObjectCollection */
    @Override
    public DomainObjectCollection getObjects() {
        return (collection == null) ? new DomainObjectCollectionImpl() : collection;
    }

    /** @return int */
    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    /** @return org.hip.kernel.bom.Page */
    @Override
    public Page getPreviousPage() {
        return (previous == null) ? this : previous;
    }

    /** @return org.hip.kernel.bom.QueryResult */
    @Override
    public QueryResult getQueryResult() {
        return result;
    }

    /** @return java.lang.String */
    @Override
    public String getSerialized() {
        return (xmlCache == null) ? "" : xmlCache;
    }

    /** Returns true if the page has more elements, that is more DomainObjects.
     *
     * @return boolean */
    @Override
    public boolean hasMoreElements() {
        return iterator().hasMoreElements();
    }

    /** @param coll org.hip.kernel.bom.DomainObjectCollection */
    private void initCollection() {
        try {
            synchronized (this) {
                collection = result.nextn(pageSize);
            }
        } catch (final BOMException | SQLException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @return boolean */
    @Override
    public boolean isFirstPage() {
        return getPreviousPage() == this;
    }

    /** @return boolean */
    @Override
    public boolean isLastPage() {
        if (nextLoaded) {
            // If loaded and null, the this is the last page
            return next == null;
        }
        else {
            return !result.hasMoreElements();
        }
    }

    /** Returns an iterator over the included domain objects.
     *
     * @return org.hip.kernel.bom.DomainObjectIterator */
    private DomainObjectIterator iterator() {
        if (domainObjIterator == null) {
            synchronized (this) {
                domainObjIterator = getObjects().elements();
            }
        }
        return domainObjIterator;
    }

    private void loadNextPage() {
        // Already tryed to load
        if (nextLoaded) {
            return;
        }

        // Test if there are more elements in the cursor
        if (!result.hasMoreElements()) {
            return;
        }

        synchronized (this) {
            // Create new page
            next = new PageImpl(result, this, this.pageSize);
            nextLoaded = true;
        }
    }

    /** Returns the next element.
     *
     * @return org.hip.kernel.bom.GeneralDomainObject */
    @Override
    public GeneralDomainObject nextElement() {
        return iterator().nextElement();
    }

    /** Returns the page as XML string. The output is the same as in nextnAsXML in the QueryResult.
     *
     * @return java.lang.String */
    @Override
    public String pageAsXML() {
        if (xmlCache == null) {
            final XMLSerializer lSerializer = new XMLSerializer();
            final DomainObjectIterator lIterator = collection.elements();
            lIterator.accept(lSerializer);
            xmlCache = lSerializer.toString();
        }

        return xmlCache;
    }

    /** This method release every contained DomainObject. Use this with caution. The intention of this method is to
     * minimize object instantiation. */
    @Override
    public void release() { // NOPMD by lbenno
    }

    /** @param inXML java.lang.String */
    @Override
    public void setSerialized(final String inXML) {
        xmlCache = inXML;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return Debug.classMarkupString(this, "PageNumber=" + pageNumber + " PageSize=" + pageSize);
    }
}