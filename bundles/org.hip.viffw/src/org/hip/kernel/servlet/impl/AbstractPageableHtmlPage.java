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

import java.util.Iterator;

import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.Pageable;

/** Baseclass of all pageable html pages. A pageable html-page implements the <code>Pageable</code> interface. The
 * pageable Page consists of one or more pagable views.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.Pageable */
@SuppressWarnings("serial")
public abstract class AbstractPageableHtmlPage extends AbstractHtmlPage implements Pageable {

    // Number of entries hold in one data-page
    private final static int PAGE_SIZE = 10;
    // Total number of rows (data-lines). Used for calculate the total number of pages
    private int numberOfRows;

    /** AbstractPageableHtmlPage with specified context.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    public AbstractPageableHtmlPage(final Context inContext) {
        super(inContext);
    }

    /** Get the total number of items in the list over all pages.
     *
     * @return int */
    @Override
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /** Returns the size of one page.
     *
     * @return int - number of listitems per page. */
    @Override
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /** Sets the datapage of all pageable views in this html-page to the next dataPage. */
    @Override
    public void nextPage() {
        nextPage(1);
    }

    /** Skips the next inJump-1 pages and jumps to page actual+inJump
     *
     * @param inJump int */
    @Override
    public void nextPage(final int inJump) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).nextPage(inJump);
            }
        }
    }

    /** Sets the datapage of all pageable views in this html-page to the previous dataPage. */
    @Override
    public void previousPage() {
        previousPage(1);
    }

    /** Skips the previous inJump-1 pages and jumps to page actual-inJump
     *
     * @param inJump int */
    @Override
    public void previousPage(final int inJump) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).previousPage(inJump);
            }
        }
    }

    /** Sets the total number of data-rows for all pageable views in this html-page.
     *
     * @see org.hip.kernel.servlet.Pageable#setNumberOfRows */
    @Override
    public void setNumberOfRows(final int inNumberOfRows) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setNumberOfRows(inNumberOfRows);
            }
        }
    }

    /** Sets the size of the datapage of all pageable views in this html-page to the passed size.
     *
     * @see org.hip.kernel.servlet.Pageable#setPageSize */
    @Override
    public void setPageSize(final int inPageSize) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setPageSize(inPageSize);
            }
        }
    }

    /** Sets the datapage of all pageable views in this html-page to the dataPage last shown, but with new data.
     *
     * @param inNewQueryResult -The queryresult contains the data which the views need to update.
     * @see org.hip.kernel.servlet.Pageable#setToCurrentPage */
    @Override
    public void setToCurrentPage(final QueryResult inNewQueryResult) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setToCurrentPage(inNewQueryResult);
            }
        }
    }

    /** Sets the datapage of all pageable views in this html-page to the dataPage last shown, but with new data and
     * different column sorted.
     *
     * @param inNewQueryResult -The queryresult contains the data which the views need to update.
     * @param inSortedColumn String Number of the column that is sorted.
     * @param inSortDir boolean true if the column is sorted DESC, false if ASC.
     * @see org.hip.kernel.servlet.Pageable#setToCurrentPage */
    @Override
    public void setToCurrentPage(final QueryResult inNewQueryResult, final String inSortedColumn,
            final boolean inSortDir) {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setToCurrentPage(inNewQueryResult, inSortedColumn, inSortDir);
            }
        }
    }

    /** Sets the datapage of all pageable views in this html-page to the first dataPage.
     *
     * @see org.hip.kernel.servlet.Pageable */
    @Override
    public void setToFirstPage() {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setToFirstPage();
            }
        }
    }

    /** Sets the datapage of all pageable views in this html-page to the last dataPage.
     *
     * @see org.hip.kernel.servlet.Pageable */
    @Override
    public void setToLastPage() {
        for (final Iterator<?> lViews = getViews().iterator(); lViews.hasNext();) {
            final HtmlView lView = (HtmlView) lViews.next();
            if (lView instanceof Pageable) {
                ((Pageable) lView).setToLastPage();
            }
        }
    }
}
