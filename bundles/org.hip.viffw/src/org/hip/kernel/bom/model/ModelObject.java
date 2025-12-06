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

import org.hip.kernel.bom.DomainObject;

/** This is the base interface for all meta-data objects. Every system that must be configurable and extendable must
 * support some model informations.
 *
 * @author Benno Luthiger */
public interface ModelObject extends DomainObject {

    public static final String objectDef = "objectDef".intern(); // NOPMD
    public static final String propertyDef = "propertyDef".intern(); // NOPMD
    public static final String mappingDef = "mappingDef".intern(); // NOPMD
    public static final String keyDef = "keyDef".intern(); // NOPMD
    public static final String keyItemDef = "keyItemDef".intern(); // NOPMD
    public static final String relationshipDef = "relationshipDef".intern(); // NOPMD
    public static final String joinedObjectDef = "joinedObjectDef".intern(); // NOPMD
    public static final String columnDef = "columnDef".intern(); // NOPMD
    public static final String hidden = "hidden".intern(); // NOPMD
    public static final String joinDef = "joinDef".intern(); // NOPMD
    public static final String objectDesc = "objectDesc".intern(); // NOPMD
    public static final String joinCondition = "joinCondition".intern(); // NOPMD
    public static final String objectNested = "objectNested".intern(); // NOPMD
    public static final String resultGrouping = "resultGrouping".intern(); // NOPMD
    public static final String objectPlaceholder = "objectPlaceholder".intern(); // NOPMD

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    MetaModelObject getMetaModelObject();
}
