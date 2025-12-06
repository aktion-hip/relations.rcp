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

/** Defines keys for objects. A key identifies an object. We can distinguish between a primaryKey and a secondaryPrimary
 * Key. A primary key is used to establish assosiations between objects. Today, primary keys are surrogates such as the
 * DCE UUId, the CORBA IOR etc. If good designed, they are universelly unique. Secondary primary keys are unique only
 * within a given context. Examples of secondary primary keys are: ISBN for books, EAN for partners. The actual version
 * of this framework does NOT support secondary primary keys. (Even if this support is prepared.)
 *
 * @author Benno Luthiger */
public interface RelationshipDef extends ModelObject {

    // Class variables
    String TYPE_PRIMARY_KEY = "primaryKey".intern();
    String SCHEMA_PRIMARY_KEY = TYPE_PRIMARY_KEY;

    /** @return java.lang.String */
    String getHomeName();

    /** @return java.lang.String */
    String getKeyDefName();
}
