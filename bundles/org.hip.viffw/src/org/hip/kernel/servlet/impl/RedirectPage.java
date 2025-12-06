/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2003-2015, Benno Luthiger

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hip.kernel.servlet.impl;

import org.hip.kernel.servlet.Context;

/** This page can be used to redirect the output to a specified page by using the meta refresh tag.
 *
 * <pre>
 * <meta http-equiv="refresh" content="0; URL=./redirected.html">
 * </pre>
 *
 * Created on 01.05.2003
 *
 * @author Luthiger */
@SuppressWarnings("serial")
public class RedirectPage extends AbstractHtmlPage {
    // constants
    private final static String META1 = "<meta http-equiv=\"refresh\" content=\"";
    private final static String META2 = "; URL=";
    private final static String META3 = "\"/>";
    private final static String BODY = "<body>";

    // instance variables
    private String url = "";
    private String seconds = "0";

    /** RedirectPage constructor with redirection url.
     *
     * @param inUrl java.lang.String */
    public RedirectPage(final String inUrl) {
        super();
        url = inUrl;
    }

    /** RedirectPage constructor with context and redirection url.
     *
     * @param inContext
     * @param inUrl java.lang.String */
    public RedirectPage(final Context inContext, final String inUrl) {
        super(inContext);
        url = inUrl;
    }

    @Override
    protected String getXMLName() { // NOPMD by lbenno 
        return null;
    }

    /** Returns begin of this page as html-string. Contains
     *
     * <pre>
     * <meta http-equiv="refresh" content="0; URL=./redirected.html">
     * </pre>
     *
     * .
     *
     * @return java.lang.String */
    @Override
    protected String createBegin() {
        final StringBuffer outBegin = new StringBuffer(HTML_BEGIN);
        outBegin.append(HEAD_BEGIN).append(META1).append(seconds).append(META2).append(url).append(META3)
                .append(HEAD_END).append(BODY);
        return new String(outBegin);
    }

    /** Setter for the URL where the request has to be redirected.
     *
     * @param inUrl java.lang.String */
    public void setURL(final String inUrl) {
        url = inUrl;
    }

    /** Setter for the delay of the redirect.
     *
     * @param inSecondes int, default = 0 */
    public void setDelay(final int inSecondes) {
        seconds = String.valueOf(inSecondes);
    }
}
