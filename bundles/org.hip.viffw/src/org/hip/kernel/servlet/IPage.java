/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006-2015, Benno Luthiger

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

/** Inteface for html pages. A page contains a set of <code>HtmlView</code>s. A page corresponds to all html code that
 * defines the content displayed on a browser page.
 *
 * @author Luthiger Created on 21.01.2008 */
public interface IPage extends HtmlView {

    /** Adds a new html-view to this page.
     *
     * @param inView HtmlView */
    void add(HtmlView inView);

    /** Sets status-message which will be added to the content of this page. After sending the html-representation of
     * this page to the client, including the status-message, the status-message will be cleared and has to be set again
     * if needed.
     *
     * @param inMessage java.lang.String */
    void setStatusMessage(String inMessage);

    /** Clears all status messages from the views. */
    void clearStatusMessage();

    /** Sets ErrorMessage which will be added to the content of this page. After sending the html-representation of this
     * page to the client, including the error-message, the error-message will be cleared and has to be set again if
     * needed.
     *
     * @param inErrorMessage java.lang.String */
    void setErrorMessage(String inErrorMessage);

    /** Clears all error messages from the views. */
    void clearErrorMessage();

}
