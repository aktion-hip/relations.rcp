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

import java.io.IOException;

import javax.xml.transform.Source;

/** Interface to strategies to find the XSL stylesheets that are used as sources of the XSL transformations.
 *
 * @author Luthiger Created on 24.11.2007 */
public interface ISourceCreatorStrategy {

    /** Find and create the <code>Source</code>.
     * 
     * @return Source
     * @throws IOException */
    Source createSource() throws IOException;

    /** A meaningful identification of the XSL used to create the source, e.g. the file's fully qualified path.
     * 
     * @return String the resource's (i.e. XSL file) identification. */
    String getResourceId();

}
