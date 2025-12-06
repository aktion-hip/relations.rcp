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

/** Represents a Link for a Css-Stylesheet defined in a file.
 *
 * @author: Benno Luthiger */
@SuppressWarnings("serial")
public class CssLink extends AbstractResourceLink {
    private final static String TMPL_LINK = "<link rel=\"stylesheet\" media=\"%s\" type=\"text/css\" href=\"%s%s\"/>";

    /** The Path to the css file has to be relative to the specified css rootdirectory. This is defined in the
     * VSys.properties.
     *
     * @param inHref java.lang.String - the relative path to the css-file */
    public CssLink(final String inHref) {
        super(inHref);
    }

    /** Constructor with specified path and media
     *
     * @param inHref java.lang.String - relative path to the css-file
     * @param inMedia java.lang.String - media for which the stylesheet is used */
    public CssLink(final String inHref, final String inMedia) {
        super(inHref, inMedia);
    }

    /** Returns HTML-String-Representation of a css-link, e.g.
     *
     * <pre>
     * &lt;link rel="stylesheet" media="screen" type="text/css" href="c:/vif/website/css/my.css">
     * </pre>
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return String.format(TMPL_LINK, getMedia(), getWebCssRoot(), getHref());
    }
}
