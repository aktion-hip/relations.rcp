/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2004-2015, Benno Luthiger

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

import org.hip.kernel.exc.VError;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** This class provides generic functionality for external resources linked to the html file.
 *
 * @author Benno Luthiger Created on Jan 23, 2004 */
@SuppressWarnings("serial")
public abstract class AbstractResourceLink extends VObject implements Serializable {
    /** Constant for Outputmedia Screen */
    public final static String MEDIA_SCREEN = "screen";
    /** Constant for Outputmedia Printer */
    public final static String MEDIA_PRINT = "print";
    /** Constant for all Outputmedia */
    public final static String MEDIA_ALL = "all";

    /** Root-Directory of the css stylesheets */
    protected String webCssRoot = ""; // NOPMD

    /** Id of css-rootdirectory property */
    private static final String WEB_CSS_ROOT_PROPERTY_NAME = "org.hip.vif.website.root";

    /** The outpoutmedia paramter defines for which outputmedia the stylesheet will be active. Default is set to all */
    private String media = MEDIA_ALL;
    /** The Path to the css-file (can be relative or a URL) */
    private String href;

    /** AbstractResourceLink constructor. */
    public AbstractResourceLink(final String inHref) {
        super();
        setHref(inHref);
    }

    /** Constructor with specified path and media
     *
     * @param inHref java.lang.String - relative path to the css-file
     * @param inMedia java.lang.String - media for which the stylesheet is used */
    public AbstractResourceLink(final String inHref, final String inMedia) {
        this(inHref);
        setMedia(inMedia);
    }

    /** Set the relative path to the css file. (Path has to be relative to the css rootdirectory path! Defined in
     * vif.properties).
     *
     * @param inHref java.lang.String */
    public void setHref(final String inHref) {
        // pre: inHref not null
        if (inHref == null) {
            return;
        }

        href = inHref;
        // we initialize the stylesheets root only when a relative path has been provided
        if (isAbsolute(href)) {
            webCssRoot = "";
            return;
        }
        try {
            webCssRoot = VSys.getVSysProperties().getProperty(WEB_CSS_ROOT_PROPERTY_NAME);
        } catch (final IOException exc) {
            throw new VError("AbstractResourceLink.initialize(): error during getting css root properties", exc);
        }
    }

    /** @return String */
    protected String getWebCssRoot() {
        return webCssRoot;
    }

    /** CssLinks are equal if their Media and Href is equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof AbstractResourceLink)) {
            return false;
        }

        final AbstractResourceLink lLink = (AbstractResourceLink) inObject;
        return getMedia().equals(lLink.getMedia()) && getHref().equals(lLink.getHref());
    }

    /** Returns relative path to the css file.
     *
     * @return java.lang.String */
    public String getHref() {
        return href;
    }

    /** Returns the media for which this stylesheet is used.
     *
     * @return java.lang.String */
    public String getMedia() {
        return media;
    }

    @Override
    public int hashCode() { // NOPMD
        return getHref().hashCode() ^ getMedia().hashCode();
    }

    private boolean isAbsolute(final String inHref) {
        return inHref.startsWith("http://") || inHref.startsWith("https://");
    }

    /** Sets the media for which the stylesheet is used.
     *
     * ex: <data>screen</data> whend the stylesheet is only for outputs on the screen <data>print</data> for output on a
     * printer <data>all</data> for every outputmedia
     *
     * There are more, like tv, handheld, ... but even the ones above are not supported in all version of the IE 4.0 or
     * Netscape 4.0.
     *
     * @param inMedia java.lang.String */
    public void setMedia(final String inMedia) {

        // pre: inMedia not null
        if (inMedia == null) {
            return;
        }

        media = inMedia;
    }
}
