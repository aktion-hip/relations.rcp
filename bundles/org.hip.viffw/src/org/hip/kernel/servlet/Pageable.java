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
package org.hip.kernel.servlet;

import org.hip.kernel.bom.QueryResult;

/** Interface for views supporting a navigation over pages with data (list).
 * <p/>
 *
 * For example: A view shows a part of a list of something (first page). This interfaces defines some methods for
 * navigating over the hole data (list).
 * <p/>
 *
 * e.g.: To load data of next page (in this case page means a list of data) into the view, call the method
 * <code>nextPage()</code>.
 *
 * @author Benno Luthiger */

public interface Pageable {
    /** Get the total number of items in the list over all pages.
     *
     * @return int */
    int getNumberOfRows();

    /** Returns the size of one page.
     *
     * @return int - number of listitems per page. */
    int getPageSize();

    /** Loads next data (page) of a set of data. */
    void nextPage();

    /** Skips the next inJump-1 pages and jumps to page actual+inJump
     *
     * @param inJump int */
    void nextPage(int inJump);

    /** Load previous data-page. */
    void previousPage();

    /** Skips the previous inJump-1 pages and jumps to page actual-inJump
     *
     * @param inJump int */
    void previousPage(int inJump);

    /** Set the total number of entries.
     *
     * @param inNumberOfRows int */
    void setNumberOfRows(int inNumberOfRows);

    /** Sets the size of one page.
     *
     * @param inPageSize int */
    void setPageSize(int inPageSize);

    /** Loads the current data-page again.
     *
     * @param inNewQueryResult for reloading the data of the current page */
    void setToCurrentPage(QueryResult inNewQueryResult);

    /** Loads the current data-page again with different column sorted.
     *
     * @param inNewQueryResult for reloading the data of the current page
     * @param inSortedColumn String Number of the column that is sorted.
     * @param inSortDir boolean true if the column is sorted DESC, false if ASC. */
    void setToCurrentPage(QueryResult inNewQueryResult, String inSortedColumn, boolean inSortDir);

    /** Loads the first data-page. */
    void setToFirstPage();

    /** Loads the last data-page. */
    void setToLastPage();
}
