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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.sys.VObject;

/** List of links to JavaScripts defined in a file.
 *
 * @author: Benno Luthiger */
@SuppressWarnings("serial")
public class ScriptLinkList extends VObject implements Serializable { // NOPMD by lbenno 
    private List<ScriptLink> scriptLinks; // NOPMD by lbenno 

    /** Adds the ScriptLink to the list if not added before.
     *
     * @param inLink ScriptLink */
    public void addScriptLink(final ScriptLink inLink) {

        // pre: parameter not null and link not added before
        if (inLink == null) {
            return;
        }
        if (scriptLinks().contains(inLink)) {
            return;
        }

        scriptLinks().add(inLink);
    }

    /** Returns Vector of ScriptLinks added to this list.
     *
     * @return java.util.Vector */
    protected List<ScriptLink> scriptLinks() {
        if (scriptLinks == null) {
            scriptLinks = new ArrayList<ScriptLink>(5);
        }

        return scriptLinks;
    }

    /** Returns an Iterator of the links this list contains.
     *
     * @return java.util.Iterator */
    public Iterator<ScriptLink> getScriptLinks() {
        return scriptLinks().iterator();
    }

    /** Returns HTML-String-Representation of all css-links in this list.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        final StringBuilder outLinks = new StringBuilder();
        final Iterator<ScriptLink> lLinks = getScriptLinks();
        while (lLinks.hasNext()) {
            outLinks.append(lLinks.next()).append('\n');
        }

        return new String(outLinks);
    }
}
