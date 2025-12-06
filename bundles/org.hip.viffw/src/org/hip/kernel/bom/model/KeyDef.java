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
package org.hip.kernel.bom.model;

import java.util.Iterator;
import java.util.List;

/** Defines keys for objects. A key identifies an object. We can distinguish between a primaryKey and a secondaryPrimary
 * Key. A primary key is used to establish associations between objects. Today, primary keys are surrogates such as the
 * DCE UUId, the CORBA IOR etc. If good designed, they are universally unique. Secondary primary keys are unique only
 * within a given context. Examples of secondary primary keys are: ISBN for books, EAN for partners. The actual version
 * of this framework does NOT support secondary primary keys. (Even if this support is prepared.)
 *
 * @author Benno Luthiger */
public interface KeyDef extends ModelObject {

    // Class variables
    String TYPE_PRIMARY_KEY = "primaryKey".intern();
    String SCHEMA_PRIMARY_KEY = TYPE_PRIMARY_KEY;

    String keyPropertyName = "keyPropertyName".intern(); // NOPMD by lbenno 

    /** Inserts a Property named inKeyName at the specified inPosition to the PropertySet.
     *
     * @param inKeyName java.lang.String
     * @param inPosition int */
    void addKeyNameAt(String inKeyName, int inPosition);

    /** Returns the name of the key property with the given ordinality. This method returns an empty string if the
     * ordinal number is not valid. The numbering starts with 0.
     *
     * @return java.lang.String
     * @param inWhich int */
    String getKeyName(int inWhich);

    /** Returns an Iterator over the key names.
     *
     * @return Iterator<String>
     * @deprecated Use {@link KeyDef#getKeyNames2()} instead. */
    @Deprecated
    Iterator<String> getKeyNames();

    /** Returns a Collection view of the key names contained in this <code>KeyDef</code> instance.
     *
     * @return Vector<String> */
    List<String> getKeyNames2();

    /** Returns true if it is a primary key def.
     *
     * @return boolean */
    boolean isPrimaryKeyDef();
}
