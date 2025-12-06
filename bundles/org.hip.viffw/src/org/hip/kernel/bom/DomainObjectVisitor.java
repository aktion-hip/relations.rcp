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

/** A Visitor for DomainObjects
 *
 * @author Benno Luthiger */
public interface DomainObjectVisitor {
    String PROPERTY_SET_TAG = "propertySet";

    /** Execute the visit of a DomainObject.
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    void visitDomainObject(GeneralDomainObject inObject);

    /** Execute the visit of a DomainObjectIterator.
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    void visitDomainObjectIterator(DomainObjectIterator inIterator);

    /** Execute the visit of a property.
     *
     * @param inProperty org.hip.kernel.bom.Property */
    void visitProperty(Property inProperty);

    /** Execute the visit of a PropertySet.
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    void visitPropertySet(PropertySet inSet);

    /** Execute the visit of a SortedArray.
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    void visitSortedArray(SortedArray inSortedArray);
}
