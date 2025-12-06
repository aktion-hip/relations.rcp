/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

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
package org.hip.kernel.bom.impl;

import java.util.HashMap;
import java.util.Map;

import org.hip.kernel.bom.Home;
import org.hip.kernel.bom.HomeManager;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;

/** This class implements the HomeManager interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.HomeManager */
public final class HomeManagerImpl extends VObject implements HomeManager {

    // Class variables
    private static volatile HomeManager cSingleton; // NOPMD by lbenno

    // Instance variables
    private transient Map<String, Home> loadedHomeMap;

    /** HomeManagerImpl singleton constructor. */
    private HomeManagerImpl() {
        super();
    }

    /** Returns the specified Home.
     *
     * @return org.hip.kernel.bom.Home
     * @param inHomeName java.lang.String */
    @Override
    public Home getHome(final String inHomeName) {

        // Pre: inHomeName not null
        if (VSys.assertNotNull(this, "getHome", inHomeName) == Assert.FAILURE) {
            return null;
        }

        // No loaded found. We try to create
        try {
            synchronized (this) {
                // We try to find a loaded home
                Home outHome = null;
                outHome = loadedHomes().get(inHomeName);
                if (outHome != null) {
                    return outHome;
                }

                // find class
                final Class<?> lClass = Class.forName(inHomeName);
                // Create new instance
                outHome = (Home) lClass.newInstance();
                // Add to loadedHomes
                loadedHomes().put(inHomeName, outHome);
                return outHome;
            }

            // Handling various exceptions
        } catch (final NoClassDefFoundError err) {
            DefaultExceptionWriter.printOut(this, err, true);
            return null;
        } catch (final ClassNotFoundException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        } catch (final InstantiationException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        } catch (final IllegalAccessException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return null;
        }
    }

    /** @return org.hip.kernel.bom.HomeManager */
    public synchronized static HomeManager getSingleton() { // NOPMD by lbenno 
        if (cSingleton == null) {
            cSingleton = new HomeManagerImpl();
        }
        return cSingleton;
    }

    /** @return java.util.Hashtable */
    private Map<String, Home> loadedHomes() {
        if (loadedHomeMap == null) {
            loadedHomeMap = new HashMap<String, Home>(67);
        }
        return loadedHomeMap;
    }
}
