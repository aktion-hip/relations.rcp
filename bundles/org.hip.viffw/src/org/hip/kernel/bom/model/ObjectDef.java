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

import java.util.Collection;
import java.util.List;
import java.util.Set;

/** The ObjectDef holds meta information for a domain object.
 *
 * @author Benno Luthiger */
public interface ObjectDef extends ModelObject {

    /** Adds a property def to the propertyDefs.
     *
     * @param inPropertyDef org.hip.kernel.bom.model.PropertyDef */
    void addPropertyDef(PropertyDef inPropertyDef);

    /** Returns the MappingDef associated with the specified property.
     *
     * @return org.hip.kernel.bom.model.MappingDef
     * @param inName java.lang.String */
    MappingDef getMappingDef(String inName);

    /** Returns a Collection view of all <code>MappingDef</code>s.
     *
     * @return Collection<MappingDef> */
    Collection<MappingDef> getMappingDefs2();

    /** Returns a Collection view of <code>MappingDef</code>s for the specified table.
     *
     * @param inTableName String
     * @return Collection<MappingDef> */
    Collection<MappingDef> getMappingDefsForTable2(String inTableName);

    /** Returns the primary key definition. The actual version of this domain object framework does no support secondary
     * primary keys.
     *
     * @return org.hip.kernel.bom.model.KeyDef */
    KeyDef getPrimaryKeyDef();

    /** Returns the PropertyDef for the specified Property.
     *
     * @return org.hip.kernel.bom.model.PropertyDef
     * @param inName java.lang.String */
    PropertyDef getPropertyDef(String inName);

    /** Returns a Collection view of all <code>PropertyDef</code>s with simple property type.
     *
     * @return Vector<PropertyDef>
     * @see PropertyDefDef#propertyTypeSimple */
    List<PropertyDef> getPropertyDefs2();

    /** Returns a Collection view of all PropertyDefs with the specified property type.
     *
     * @param inPropertyType String
     * @return Vector<PropertyDef> */
    List<PropertyDef> getPropertyDefs2(String inPropertyType);

    /** Returns a Set view of all table names contained in this <code>ObjectDef</code> instance.
     *
     * @return Set<String> */
    Set<String> getTableNames2();

}
