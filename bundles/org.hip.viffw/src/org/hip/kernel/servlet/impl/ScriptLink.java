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

/** Represents a Link for a JavaScipt defined in a file.
 *
 * @author: Benno Luthiger */
@SuppressWarnings("serial")
public class ScriptLink extends AbstractResourceLink {
    // Constant for the resouce type
    private final static String RESOURCE_TYPE = "text/javascript";
    private final static String TMPL_LINK = "<script type=\"text/javascript\" src=\"%s%s\"></script>";

    /** The Path to the javascript file has to be relative to the specified resources root directory. This is defined in
     * the VSys.properties.
     *
     * @param inHref java.lang.String - the relative path to the javascript file */
    public ScriptLink(final String inHref) {
        super(inHref);
    }

    /** Constructor with specified path and media
     *
     * @param inHref java.lang.String - relative path to the javascript file
     * @param inMedia java.lang.String - media for which the javascript is used */
    public ScriptLink(final String inHref, final String inMedia) {
        super(inHref, inMedia);
    }

    /** @return String the resource type */
    protected String getType() {
        return RESOURCE_TYPE;
    }

    /** Returns HTML-String-Representation of a script-link, e.g.
     *
     * <pre>
     * &lt;script type="text/javascript" src="c:/vif/website/css/my.js">&lt;/script>
     * </pre>
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return String.format(TMPL_LINK, getWebCssRoot(), getHref());
    }
}
