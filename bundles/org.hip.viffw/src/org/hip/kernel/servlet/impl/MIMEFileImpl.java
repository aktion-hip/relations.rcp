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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.MIMEFile;
import org.hip.kernel.servlet.RequestException;

import jakarta.servlet.ServletOutputStream;

/** Implementation of view returning a file with MIME-type other then html.
 *
 * @author: Benno Luthiger
 * @see org.hip.kernel.servlet.MIMEFile */
@SuppressWarnings("serial")
public class MIMEFileImpl extends AbstractView implements MIMEFile {
    // instance attributes
    private List<HtmlView> files;

    /** MIMEFileImpl default constructor. */
    public MIMEFileImpl() {
        this(null);
    }

    /** MIMEFileImpl constructor with specified context.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    public MIMEFileImpl(final Context inContext) {
        super(inContext);
    }

    /** Adds a new html-view to this page.
     *
     * @param inView org.hip.kernel.servlet.HtmlView */
    @Override
    public void add(final HtmlView inView) {
        this.getViews().add(inView);
    }

    /** Returns list of files (lazy initializing).
     *
     * @return java.util.ArrayList */
    protected List<HtmlView> getViews() {
        if (this.files == null) {
            this.files = new ArrayList<HtmlView>(3);
        }

        return this.files;
    }

    /** Writes xml-transformed representation of all files in this page to the outputstream of the servlet.
     *
     * @see org.hip.kernel.servlet.HtmlView#renderToStream
     * @param inStream {@link ServletOutputStream} - Output stream of servlet response
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.servlet.RequestException */
    @Override
    public void renderToStream(final ServletOutputStream inStream, final String inSessionID) throws RequestException,
    IOException {
        for (final HtmlView lView : this.getViews()) {
            lView.renderToStream(inStream, inSessionID);
        }
    }

    /** Subclasses have to override. */
    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public String getMIMEType() { // NOPMD by lbenno
        return "application/download";
    }

    /** Subclasses have to override. */
    @Override
    public String getFileName() {
        return "";
    }

}
