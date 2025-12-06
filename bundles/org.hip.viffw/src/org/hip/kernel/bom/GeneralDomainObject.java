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
package org.hip.kernel.bom;

import org.hip.kernel.bom.model.ObjectDef;

/** The general domain object has the whole functionality of read only domain objects (i.e. without insert and delete).
 *
 * @author Benno Luthiger */
public interface GeneralDomainObject extends SemanticObject {
    /** Used to accept a DomainObjectVisitor.
     * 
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    void accept(DomainObjectVisitor inVisitor);

    /** This method returns the home for this domain object.
     *
     * @return org.hip.kernel.bom.GeneralDomainObjectHome */
    GeneralDomainObjectHome getHome();

    /** This Method returns the class name of the home.
     * 
     * @return java.lang.String */
    String getHomeClassName();

    /** This method returns the key of this DomainObject.
     * 
     * @return org.hip.kernel.bom.KeyObject */
    KeyObject getKey();

    /** This method returns the key of this DomainObject. The initial key object is returned if demanded or if this
     * domain objects mode is changed, else the actual key is returned.
     *
     * @param inInitial boolean
     * @return org.hip.kernel.bom.KeyObject */
    KeyObject getKey(boolean inInitial);

    /** Returns the ObjectDef for this object.
     * 
     * @return ObjectDef */
    ObjectDef getObjectDef();

    /** Returns the object name.
     * 
     * @return java.lang.String */
    String getObjectName();

    /** Returns true if any property has been changed.
     * 
     * @return boolean */
    boolean isChanged();

    /** Releases this DomainObject. */
    void release();
}
