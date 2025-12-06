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

import java.io.PrintWriter;

import org.hip.kernel.util.TransformerProxy;

import jakarta.servlet.ServletOutputStream;

/** Represents an view based on html.
 *
 * @author Benno Luthiger
 * @version 1.0 */
public interface HtmlView extends View {

    /** Writes the view as html-String to the passed servlet output stream.<br/>
     * <b>Note:</b> Use this method to output bit streams.<br/>
     * To render the view with the correct encodings set, use <code>renderToWriter(PrintWriter, String)</code> instead.
     *
     * @param inStream javax.servlet.ServletOutputStream
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.servlet.RequestException */
    void renderToStream(ServletOutputStream inStream, String inSessionID) throws RequestException;

    /** Writes the view as html-String to the passed servlet's response writer.<br />
     * Using the servlet's writer to stream the view, you can use
     * <code>ServletResponse.setCharacterEncoding(ENCODING)</code> to set the view's encoding correctly.
     *
     * @param inWriter PrintWriter
     * @param inSessionID String
     * @throws RequestException */
    void renderToWriter(PrintWriter inWriter, String inSessionID) throws RequestException;

    /** Sets the Transformer to generate this view.
     *
     * @param inTransformer org.hip.kernel.util.TransformerProxy */
    void setTransformer(TransformerProxy inTransformer);

    /** Returns the Transformer which generates this view. If it wasn't set the method returns null.
     *
     * @return org.hip.kernel.util.TransformerProxy */
    TransformerProxy getTransformer();
}
