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

import java.io.IOException;
import java.io.Serializable;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.hip.kernel.servlet.ISourceCreatorStrategy;

/** Strategy to create a source defined by a fully qualified name of the XSL file.
 *
 * @author Luthiger Created on 24.11.2007 */
@SuppressWarnings("serial")
public class FullyQualifiedNameStrategy implements ISourceCreatorStrategy, Serializable {

    private final String xslName;

    /** FullyQualifiedNameStrategy constructor
     *
     * @param inXSLName String fully qualified path to the XSL file in the file system. */
    public FullyQualifiedNameStrategy(final String inXSLName) {
        xslName = inXSLName;
    }

    @Override
    public Source createSource() throws IOException { // NOPMD by lbenno 
        return new StreamSource("file:" + xslName);
    }

    @Override
    public String getResourceId() { // NOPMD by lbenno 
        return xslName;
    }

}
