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

import org.hip.kernel.bom.Home;
import org.hip.kernel.bom.model.impl.MetaModelHomeImpl;

/** The MetaModelHome is responsible to create and manage MetaModelObjects.
 *
 * @author Benno Luthiger */
public interface MetaModelHome extends Home {
    String OBJECT_DEF_DEF = "ObjectDefDef".intern();
    String PROPERTY_DEF_DEF = "PropertyDefDef".intern();
    String PROPERTY_SET_DEF_DEF = "PropertySetDefDef".intern();
    String MAPPING_DEF_DEF = "MappingDefDef".intern();
    String KEY_DEF_DEF = "KeyDefDef".intern();
    String JOIN_DEF_DEF = "JoinDefDef".intern();
    String JOINED_OBJECT_DEF_DEF = "JoinedObjectDefDef".intern();
    String GROUPING_DEF_DEF = "GroupingDefDef".intern();
    String NESTED_DEF_DEF = "NestedDefDef".intern();

    String[] VALID_TYPES = {
            OBJECT_DEF_DEF
            , PROPERTY_SET_DEF_DEF
            , PROPERTY_DEF_DEF
            , MAPPING_DEF_DEF
            , KEY_DEF_DEF
            , JOIN_DEF_DEF
            , JOINED_OBJECT_DEF_DEF
            , GROUPING_DEF_DEF
            , NESTED_DEF_DEF
    };

    MetaModelHome singleton = MetaModelHomeImpl.getSingleton(); // NOPMD by lbenno

    /** Returns the descriptor for the given modelObjectType. Actually, the system supports the following types:
     * <UL>
     * <LI>ObjectDefDef</LI>
     * <LI>PropertySetDefDef</LI>
     * <LI>PropertyDefDef</LI>
     * <LI>MappingDefDef</LI>
     * <LI>KeyDefDef</LI>
     * <LI>JoinDefDef</LI>
     * <LI>JoinedObjectDefDef</LI>
     * <LI>GroupingDefDef</LI>
     * <LI>NestedDefDef</LI>
     * </UL>
     *
     * @return org.hip.kernel.bom.model.MetaModelObject
     * @param inModelObjectType java.lang.String */
    MetaModelObject getMetaModelObject(String inModelObjectType);

    /** Returns the definition of the JoinDef.
     *
     * @return org.hip.kernel.bom.model.JoinDefDef */
    JoinDefDef getJoinDefDef();

    /** Returns the definition of the JoinedObjectDef.
     *
     * @return org.hip.kernel.bom.model.JoinedObjectDefDef */
    JoinedObjectDefDef getJoinedObjectDefDef();

    /** Returns the definition of the KeyDef.
     *
     * @return org.hip.kernel.bom.model.KeyDefDef */
    KeyDefDef getKeyDefDef();

    /** Returns the definition of the MappingDef.
     *
     * @return org.hip.kernel.bom.model.MappingDefDef */
    MappingDefDef getMappingDefDef();

    /** Returns the definition of the ObjectDef.
     *
     * @return org.hip.kernel.bom.model.ObjectDefDef */
    ObjectDefDef getObjectDefDef();

    /** Returns the definition of the PropertyDef.
     *
     * @return org.hip.kernel.bom.model.PropertyDefDef */
    PropertyDefDef getPropertyDefDef();

    /** Returns the definition of the RelationshipDef.
     *
     * @return org.hip.kernel.bom.model.RelationshipDefDef */
    RelationshipDefDef getRelationshipDefDef();

    /** Returns the definition of the GroupingDefDef.
     *
     * @return GroupingDefDef */
    GroupingDefDef getGroupingDefDef();

    /** Returns the definition of the NestedDefDef.
     *
     * @return GroupingDefDef */
    NestedDefDef getNestedDefDef();

    /** Returns the definition of the PlaceholderDefDef.
     *
     * @return PlaceholderDefDef */
    PlaceholderDefDef getPlaceholderDefDef();
}
