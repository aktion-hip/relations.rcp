/**
	This package is part of the framework used for the application VIF.
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
package org.hip.kernel.sys;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

/** Version of the framework.
 *
 * @author: Benno Luthiger
 * @deprecated Use FWActivator.getVersion() instead. */
@Deprecated
public class Version extends VObject { // NOPMD by lbenno
    private transient String version; // NOPMD by lbenno 
    private final static String VERSION_BASE = "versionFW";
    protected final static String VERSION_KEY = "version";

    /** Returns the version number of the framework.
     *
     * @return java.lang.String */
    public String getVersion() {
        if (version == null) {
            try {
                version = PropertyResourceBundle.getBundle(VERSION_BASE).getString(VERSION_KEY);
            } catch (final MissingResourceException exc) {
                version = "?.?.?";
            }
        }
        return "Release " + version;
    }
}
