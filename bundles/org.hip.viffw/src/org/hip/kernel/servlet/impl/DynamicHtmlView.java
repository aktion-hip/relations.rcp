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
package org.hip.kernel.servlet.impl;

import org.hip.kernel.servlet.Context;

/** View that renders dynamically created html code, i.e. without accessing any ressource in the file system.
 *
 * @author Luthiger Created on 25.11.2007 */
@SuppressWarnings("serial")
public class DynamicHtmlView extends AbstractHtmlView {

    /** @param inHTML String The html code to render. */
    public DynamicHtmlView(final String inHTML) {
        super();
        setHTMLString(inHTML);
    }

    /** @param inContext Context
     * @param inHTML String The html code to render. */
    public DynamicHtmlView(final Context inContext, final String inHTML) {
        super(inContext);
        setHTMLString(inHTML);
    }

    @Override
    protected String getXMLName() { // NOPMD by lbenno 
        return null;
    }

}
