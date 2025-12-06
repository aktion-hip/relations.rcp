/**
 This package is part of the framework used for the application VIF.
 Copyright (C) 2004-2014, Benno Luthiger

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
import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.sys.VObject;

/** A collection of objects to fill in the placeholders in nested/joined homes.
 *
 * @author Benno Luthiger Created on Nov 8, 2004 */
public class PlacefillerCollection extends VObject { // NOPMD by lbenno
    private final transient Collection<Placefiller> placefillers = new ArrayList<Placefiller>();

    /** The place holder class. */
    public static class Placefiller {
        protected transient GeneralDomainObjectHome home;
        protected transient KeyObject key;
        protected transient String alias;

        /** Placefiller constructor.
         *
         * @param inHome {@link GeneralDomainObjectHome}
         * @param inKey {@link KeyObject}
         * @param inAlias String */
        public Placefiller(final GeneralDomainObjectHome inHome, final KeyObject inKey, final String inAlias) {
            super();
            home = inHome;
            key = inKey;
            alias = inAlias;
        }
    }

    /** Adds a new placefiller to the collection.
     *
     * @param inHome GeneralDomainObjectHome The home that fills the place.
     * @param inKey KeyObject The key to select.
     * @param inAlias String The placeholder's id. */
    public void add(final GeneralDomainObjectHome inHome, final KeyObject inKey, final String inAlias) {
        placefillers.add(new Placefiller(inHome, inKey, inAlias));
    }

    /** Returns an iterator over the placefiller objects.
     *
     * @return Iterator */
    public Iterator<Placefiller> iterator() {
        return placefillers.iterator();
    }

    /** @return int */
    public int size() {
        return placefillers.size();
    }
}
