/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

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

import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.util.NameValueList;

/** DomainObjects hold its state in properties. The PropertySet groups all properties.
 * 
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObjectHome */
public interface PropertySet extends NameValueList, java.io.Serializable {

    /** @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    void accept(DomainObjectVisitor inVisitor);

    /** Returns an iterator over the changed properties.
     *
     * @return java.utilEnumeration<Property>
     * @deprecated Use {@link PropertySet#getChangedProperties2()} instead. */
    @Deprecated
    Iterator<Property> getChangedProperties();

    /** Returns the <code>Collection</code> of changed <code>Property</code> elements.
     * 
     * @return Collection<Property> */
    Collection<Property> getChangedProperties2();

    /** @return org.hip.kernel.bom.SemanticObject */
    SemanticObject getParent();

    /** Sets this PropertySet virgin. */
    void setVirgin();

    /** Sets the notification status of the Properties in this PropertySet. Notification set to true means that the
     * Properties in this set are marked as changed when they are initialized. Default: false.
     * 
     * @param inNotification boolean */
    void notifyInit(boolean inNotification);
}
