/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

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
package org.hip.kernel.servlet.impl;

import java.text.MessageFormat;

import org.hip.kernel.bom.Page;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.bom.impl.PageImpl;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.Pageable;
import org.hip.kernel.util.XMLRepresentation;

/** Baseclass of all views that shows data of a QueryResult object
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
abstract public class AbstractXSLQueryResultView extends AbstractXSLView implements Pageable {
    // constant
    private final static String SORTED_INFO_TEMPLATE = "<sortedColumn>{0}</sortedColumn>\n<sortDir>{1}</sortDir>\n";

    // instance attributes
    private final QueryResult result;
    private Page page;
    private int numberOfRows;
    private int lastPageNr;
    private int pageSize = 10;
    private String sortedColumn = "";
    private String sortDirection = "";

    /** Class constructor with a QueryResult only
     *
     * @param inResult org.hip.kernel.bom.QueryResult */
    public AbstractXSLQueryResultView(final QueryResult inResult) {
        this(null, inResult);
    }

    /** Class constructor with a specified Context and QueryResult
     *
     * @param inContext org.hip.kernel.servlet.Context
     * @param inResult org.hip.kernel.bom.QueryResult */
    public AbstractXSLQueryResultView(final Context inContext, final QueryResult inResult) {
        super(inContext);
        this.result = inResult;
    }

    /** Returns the number of the last page according to the current page size
     *
     * @return int */
    protected int getLastPageNumber() {

        if (this.lastPageNr > 0) {
            return this.lastPageNr;
        }

        if (this.pageSize == 0) {
            return 0;
        }

        this.lastPageNr = Math.abs(this.numberOfRows / this.pageSize);

        if (this.numberOfRows % this.pageSize != 0) {
            this.lastPageNr++;
        }

        return this.lastPageNr;
    }

    /** Returns the number of rows (of the QueryResult)
     *
     * @return int
     * @see setNumberOfRows() */
    @Override
    public int getNumberOfRows() {
        return this.numberOfRows;
    }

    /** Returns the page
     *
     * @param org.hip.kernel.bom.Page */
    protected Page getPage() {
        return this.page;
    }

    /** Returns the key for to read a page from the context or to write a page to the context.
     *
     * @param java.lang.String */
    abstract protected String getPageKey();

    /** Returns the page size
     *
     * @return int */
    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    /** Returns the query-result-object.
     *
     * @return org.hip.kernel.bom.QueryResult
     * @see org.hip.kernel.bom.QueryResult */
    protected QueryResult getQueryResult() {
        return this.result;
    }

    /** Switches to the next page of entries */
    @Override
    public void nextPage() {
        nextPage(1);
    }

    /** Skips the next inJump-1 pages and jumps to page actual+inJump
     *
     * @param inJump int */
    @Override
    public void nextPage(final int inJump) {
        for (int i = 0; i < inJump; i++) {
            this.page = this.page.getNextPage();
        }

        String lXmlString = "";
        if (this.page.getSerialized() == null || this.page.getSerialized().equals("")) {
            setSerialized(this.page, getLastPageNumber());
        }
        lXmlString = this.page.getSerialized();
        prepareTransformation(new XMLRepresentation(lXmlString));

        setPageToContext(this.page);
    }

    /** Switches to the previous page of entries */
    @Override
    public void previousPage() {
        previousPage(1);
    }

    /** Skips the previous inJump-1 pages and jumps to page actual-inJump
     *
     * @param inJump int */
    @Override
    public void previousPage(final int inJump) {
        for (int i = 0; i < inJump; i++) {
            this.page = this.page.getPreviousPage();
        }

        String lXmlString = "";
        if (this.page.getSerialized() == null || this.page.getSerialized().equals("")) {
            setSerialized(this.page, getLastPageNumber());
        }
        lXmlString = this.page.getSerialized();
        prepareTransformation(new XMLRepresentation(lXmlString));

        setPageToContext(this.page);
    }

    /** Lets the subclasses create a new page instance for paging the query result. */
    protected void setNewPage() {
        this.page = new PageImpl(this.result, null, this.pageSize);
    }

    /** Sets the number of rows the QueryResult has.
     *
     * @param inNumberOfRows int */
    @Override
    public void setNumberOfRows(final int inNumberOfRows) {
        this.numberOfRows = inNumberOfRows;
    }

    /** Sets the page size
     *
     * @param inPageSize int */
    @Override
    public void setPageSize(final int inPageSize) {
        this.pageSize = inPageSize;
    }

    /** Sets the page to the context. */
    private void setPageToContext(final Page inPage) {
        getContext().set(AbstractContext.QUERY_RESULT_VIEW_KEY, inPage);
    }

    /** Set the serialized string
     *
     * @param inPage org.hip.kernel.bom.Page
     * @param inLastPageNumber int */
    abstract protected void setSerialized(Page inPage, int inLastPageNumber);

    /** Sets the information about the sorted columns in the query.
     *
     * @param inSortedColumn String Number of the column that is sorted.
     * @param inSortDir boolean true if the column is sorted DESC, false if ASC. */
    protected void setSortedInfo(final String inSortedColumn, final boolean inSortDir) {
        this.sortedColumn = inSortedColumn;
        this.sortDirection = inSortDir ? "1" : "0";
    }

    /** Returns the information about the sorted columns in the query as XML string.
     *
     * @return String */
    protected String getSortedInfoXML() {
        return MessageFormat.format(SORTED_INFO_TEMPLATE, new Object[] { this.sortedColumn, this.sortDirection });
    }

    /** Set the new page content with the given QueryResult and the modified information about the sorted column.
     *
     * @param inNewQueryResult org.hip.kernel.bom.QueryResult
     * @param inSortedColumn String Number of the column that is sorted.
     * @param inSortDir boolean true if the column is sorted DESC, false if ASC. */
    @Override
    public void setToCurrentPage(final QueryResult inNewQueryResult, final String inSortedColumn,
            final boolean inSortDir) {
        setSortedInfo(inSortedColumn, inSortDir);
        setToCurrentPage(inNewQueryResult);
    }

    /** Set the new page content with the given QueryResult
     *
     * @param inNewQueryResult org.hip.kernel.bom.QueryResult */
    @Override
    public void setToCurrentPage(final QueryResult inNewQueryResult) {

        int lCurrentPageNr = this.page.getPageNumber();
        this.lastPageNr = 0;

        // create new Page for paging the query result
        this.page = new PageImpl(inNewQueryResult, null, this.pageSize);
        setSerialized(this.page, getLastPageNumber());
        setPageToContext(this.page);
        prepareTransformation(new XMLRepresentation(this.page.getSerialized()));

        while (lCurrentPageNr > 1 && !this.page.isLastPage()) {
            nextPage();
            lCurrentPageNr--;
        }
    }

    /** Loads the first data-page. */
    @Override
    public void setToFirstPage() {
        final int lCurrent = this.page.getPageNumber();
        if (lCurrent > 1) { // NOPMD by lbenno
            previousPage(lCurrent - 1);
        }
    }

    /** Loads the last data-page. */
    @Override
    public void setToLastPage() {
        final int lJump = getLastPageNumber() - this.page.getPageNumber();
        if (lJump > 0) {
            nextPage(lJump);
        }
    }

    /** Returns the current Directory view as a HTML string.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return this.getHTMLString();
    }
}
