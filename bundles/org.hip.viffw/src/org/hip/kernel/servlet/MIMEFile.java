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

import java.io.IOException;

import jakarta.servlet.ServletOutputStream;

/** Represents a file with MIME-type other then html (e.g. DIF = data interchange format). This interface can be used to
 * export data from the server to the clients hard-disk in a format which depends on the used XSLT.
 *
 * @author Benno Luthiger
 * @version 1.0 */
public interface MIMEFile extends View {

    /** Adds a new file to this file-set. Although the type of the inputed class has to be HtmlView, the content of the
     * class has not to be html. The content is dependent of the XSLT transformation applied on the XML data.
     *
     * @param inView org.hip.kernel.servlet.HtmlView */
    void add(HtmlView inFile);

    /** Writes the file as String to the passed ServletOutputStream.
     *
     * @param inStream javax.servlet.ServletOutputStream
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.servlet.RequestException
     * @throws java.io.IOException */
    void renderToStream(ServletOutputStream inStream, String inSessionID) throws RequestException, IOException;

    /** Returns the file's content length.<br />
     * Can be used for <code>HttpServletResponse.setContentLength(MIMEFile.getLength())</code>.
     *
     * @return int the file's content length. */
    int getLength();

    /** Returns the file's content or MIME type.
     *
     * @return String */
    String getMIMEType();

    /** Returns the file name.
     *
     * @return String */
    String getFileName();
}
