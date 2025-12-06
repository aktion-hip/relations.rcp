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

import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;

/** The interface PropertyDef defines the behaviour of Poperty definition object. The current version provides only a
 * very limited informations about a property.
 *
 * @author Benno Luthiger */
public interface PropertyDef extends ModelObject {

    // Class Variables
    String NAME = "name";
    String TYPE = "type";
    String MIN_LEN = "minLen";
    String MAX_LEN = "maxLen";
    String FORMAT = "format";

    String DFT_VALUE_TYPE = "java.lang.String";

    /** Creates a Property which is member of the specified PropertySet
     *
     * @return org.hip.kernel.bom.Property
     * @param inSet org.hip.kernel.bom.PropertySet */
    Property create(PropertySet inSet);

    /** Returns the mapping definition of this property.
     *
     * @return org.hip.kernel.bom.model.MappingDef */
    MappingDef getMappingDef();

    /** This method returns the name of the property definition. This must be the same as the property name.
     *
     * @return java.lang.String */
    String getName();

    /** This method returns the type of the property definition (simple, objectRef).
     *
     * @return java.lang.String */
    String getPropertyType();

    /** Returns the mapping definition of this property.
     *
     * @return org.hip.kernel.bom.model.MappingDef */
    RelationshipDef getRelationshipDef();

    /** Returns the corresponding java class name to the values type according to the type information in the defining
     * XML.
     *
     * @return java.lang.String */
    String getValueClassType();

    /** This method returns the type of the value. This type information is read from the defining XML.
     *
     * @return java.lang.String */
    String getValueType();

    /** Associates a mapping definition with this property.
     *
     * @param inMappingDef org.hip.kernel.bom.model.MappingDef */
    void setMappingDef(MappingDef inMappingDef);

    /** Associates a RelationshipDef with this property.
     *
     * @param inRelationshipDef org.hip.kernel.bom.model.RelationshipDef */
    void setRelationshipDef(RelationshipDef inRelationshipDef);
}
