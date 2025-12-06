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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.hip.kernel.exc.VError;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.RequestException;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.TransformerProxy;
import org.hip.kernel.util.XSLProcessingException;

import jakarta.servlet.ServletOutputStream;

/** Baseclass of all html views
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
abstract public class AbstractHtmlView extends AbstractView implements HtmlView {
    // constants
    public static final String DOCS_ROOT_PROPERTY_NAME = "org.hip.vif.docs.root";
    public static final String WEB_ROOT_PROPERTY_NAME = "org.hip.vif.website.root";

    // class attributes
    protected static String DOCS_ROOT = ""; // NOPMD
    protected static String WEB_ROOT = ""; // NOPMD

    // instance attributes
    private String htmlString;
    private TransformerProxy xsltTransformer;
    private String subAppPath = "";

    /** AbstractHtmlView default constructor. */
    protected AbstractHtmlView() {
        this(null);
    }

    /** Initializes properties of this view.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    protected AbstractHtmlView(final Context inContext) {
        super(inContext);
        if (inContext != null) {
            this.subAppPath = inContext.getServletPath() + "/";
        }
        this.initialize();
    }

    /** Returns the html-representation of this view as string. If it wasn't set this method returns null.
     *
     * @return java.lang.String - html-string of view. */
    public String getHTMLString() {
        return this.htmlString;
    }

    private String getRelativeHTMLName() {
        return DOCS_ROOT + getSubAppPath() + this.getLanguage() + "/" + getXMLName();
    }

    /** The path to the sub application in the same webapp of the servlet-engine. Subclasses can overwrite this method.
     *
     * @return String */
    protected String getSubAppPath() {
        return this.subAppPath;
    }

    /** Returns the relative path and name of the HTML or XSL file (relative to the root of the websites)
     *
     * @return java.lang.String */
    abstract protected String getXMLName();

    /** Initializes the root directories of the websites and the documents (relative and absolute paths). These
     * directories are defined externally in the vif.properties file.
     *
     * @see org.hip.kernel.sys.VSys */
    private void initialize() {
        try {
            DOCS_ROOT = VSys.getVSysCanonicalPath(DOCS_ROOT_PROPERTY_NAME);
            if (DOCS_ROOT.length() == 0) {
                final String lProperty = ServletContainer.getInstance().getBasePath();
                if (lProperty != null) {
                    DOCS_ROOT = VSys.getVSysCanonicalPath(DOCS_ROOT_PROPERTY_NAME, lProperty);
                }
            }
            WEB_ROOT = VSys.getVSysProperties().getProperty(WEB_ROOT_PROPERTY_NAME);
        } catch (final IOException exc) {
            throw new VError("AbstractHtmlView.initialize(): error during getting web and docs root properties", exc);
        }
    }

    /** Reads HTML file of the view.
     *
     * @return java.lang.String */
    protected String readHTML() {
        File lHtmlFile;
        FileReader lHtmlInReader = null;
        char[] lBuffer;
        int lSize = 0;
        int lChars = 0;

        try {
            lHtmlFile = new File(getRelativeHTMLName());
            lSize = (int) lHtmlFile.length();
            lHtmlInReader = new FileReader(lHtmlFile);
            lBuffer = new char[lSize];

            while (lChars < lSize) {
                lChars += lHtmlInReader.read(lBuffer, lChars, lSize - lChars);
            }

            return new String(lBuffer);
        } catch (final IOException exc) {
            VSys.err.println(exc.getMessage());
        } finally {
            try {
                if (lHtmlInReader != null) {
                    lHtmlInReader.close();
                }
            } catch (final IOException exc) {
                VSys.err.println(exc.getMessage());
            }
        }
        return null;
    }

    /** Writes the view as html-String to the passed servlet output stream.<br/>
     * <b>Note:</b> Use this method to output bit streams.<br/>
     * To render the view with the correct encodings set, use <code>renderToWriter(PrintWriter, String)</code> instead.
     *
     * @param inStream {@link ServletOutputStream} - Output stream of servlet response
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.servlet.RequestException */
    @Override
    public void renderToStream(final ServletOutputStream inStream, final String inSessionID) throws RequestException {
        try {
            renderToWriter(new PrintWriter(new OutputStreamWriter(inStream, "UTF-8")), inSessionID);
        } catch (final UnsupportedEncodingException exc) {
            throw new RequestException(exc);
        }
    }

    @Override
    public void renderToWriter(final PrintWriter inWriter, final String inSessionID) throws RequestException { // NOPMD
        try {
            if (this.xsltTransformer == null) {
                if (this.htmlString == null) {
                    inWriter.println("No HTML for view!");
                }
                else {
                    inWriter.print(this.htmlString);
                }
                return;
            }
            this.xsltTransformer.renderToWriter(inWriter, inSessionID);
        } catch (final XSLProcessingException exc) {
            throw new RequestException(exc);
        }
    }

    /** Sets the HTML representation of this view.
     *
     * @param inHTMLString java.lang.String - html-representation of this view. */
    public void setHTMLString(final String inHTMLString) {
        this.htmlString = inHTMLString;
    }

    /** Sets the Transformer to generate this view.
     *
     * @param inTransformer org.hip.kernel.util.TransformerProxy */
    @Override
    public void setTransformer(final TransformerProxy inTransformer) {
        this.xsltTransformer = inTransformer;
    }

    /** Returns the Transformer which generates this view. If it wasn't set the method returns null.
     *
     * @return org.hip.kernel.util.TransformerProxy */
    @Override
    public TransformerProxy getTransformer() {
        return this.xsltTransformer;
    }

    @Override
    public int hashCode() { // NOPMD
        final int prime = 31; // NOPMD
        int result = 1;
        result = prime * result + (this.htmlString == null ? 0 : this.htmlString.hashCode());
        return result;
    }

    /** Two views are equal if their html strings are equal. */
    @Override
    public boolean equals(final Object inObject) {
        if (this == inObject) {
            return true;
        }
        if (inObject == null) {
            return false;
        }
        if (getClass() != inObject.getClass()) {
            return false;
        }
        final AbstractHtmlView lOther = (AbstractHtmlView) inObject;
        if (this.htmlString == null) {
            if (lOther.getHTMLString() != null) {
                return false;
            }
        }
        else if (!this.htmlString.equals(lOther.getHTMLString())) {
            return false;
        }
        return true;
    }

}
