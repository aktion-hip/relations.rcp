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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.bom.DomainObjectCache;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.Debug;

/** See interface <code>DomainObjectCache</code>
 *
 * @author: Benno Luthiger */
public class DomainObjectCacheImpl implements DomainObjectCache { // NOPMD by lbenno
    /** the cached domain objects */
    private transient final List<GeneralDomainObject> cachedDomainObjects = new ArrayList<GeneralDomainObject>();

    /** Returns cached domainObject. Returns <code>null</code> if not found.
     *
     * @param inKey key of the domain object looking for */
    @Override
    public GeneralDomainObject get(final KeyObject inKey) {
        if (VSys.assertNotNull(this, "get", inKey)) {
            return null;
        }

        synchronized (cachedDomainObjects) {
            for (final GeneralDomainObject outDomainObject : cachedDomainObjects) {
                if (inKey.equals(outDomainObject.getKey())) {
                    return outDomainObject;
                }
            }
        }
        return null;
    }

    /** Adds a domain object to the cache.
     *
     * @param inDomainObject org.hip.kernel.bom.GeneralDomainObject DomainObject to add.
     * @return org.hip.kernel.bom.GeneralDomainObject old DomainObject in cache, <code>null</code> if no exists. */
    @Override
    public GeneralDomainObject put(final GeneralDomainObject inDomainObject) {
        if (VSys.assertNotNull(this, "put", inDomainObject)) {
            return null;
        }

        GeneralDomainObject outOld = null;

        synchronized (cachedDomainObjects) {
            outOld = get(inDomainObject.getKey());
            if (outOld != null) {
                cachedDomainObjects.remove(outOld);
            }
            cachedDomainObjects.add(inDomainObject);
        }

        return outOld;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lMarkups = new StringBuilder(100);
        for (final Iterator<GeneralDomainObject> lObjects = cachedDomainObjects.iterator(); lObjects.hasNext();) {
            lMarkups.append(lObjects.next().toString()).append('\n');
        }
        return Debug.classMarkupString(this, "numberOfCashed=" + cachedDomainObjects.size(), new String(lMarkups));
    }
}
