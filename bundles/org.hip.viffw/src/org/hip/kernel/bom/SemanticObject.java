package org.hip.kernel.bom;

import java.util.Collection;
import java.util.Iterator;

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

/** A class that hold its own Name-Value-pairs
 *
 * @author Benno Luthiger */
public interface SemanticObject {
    /** Returns the Value to a given Name.
     *
     * @return java.lang.Object
     * @param inName java.lang.String
     * @exception org.hip.kernel.bom.GettingException */
    Object get(String inName) throws GettingException;

    /** Returns an Iterator of all property names.
     *
     * @return java.util.Iterator
     * @deprecated Use {@link SemanticObject#getPropertyNames2()} instead. */
    @Deprecated
    Iterator<?> getPropertyNames();

    /** Returns a Collection view of all property names.
     *
     * @return Collection<String> */
    Collection<String> getPropertyNames2();

    /** Sets a name-value pair.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exceptions org.hip.kernel.bom.SettingException */
    void set(String inName, Object inValue) throws SettingException;

    /** Used to set an existing instance to an initial state for that it can be reused. */
    void setVirgin();

    /** Accessor to the property set.
     *
     * @return org.hip.kernel.bom.PropertySet */
    PropertySet propertySet();

    /** We implement the clone method to provide deep cloning.
     *
     * @return Object the cloned object
     * @throws CloneNotSupportedException */
    Object clone() throws CloneNotSupportedException; // NOPMD

}