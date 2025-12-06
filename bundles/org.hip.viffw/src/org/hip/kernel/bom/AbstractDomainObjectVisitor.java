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

import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.NameValue;

/** A Visitor for DomainObjects
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObjectVisitor */
abstract public class AbstractDomainObjectVisitor extends VObject implements DomainObjectVisitor { // NOPMD

    // Instance variables
    private int level;

    /** AbstractDomainObjectVisitor default constructor initializes level 0. */
    public AbstractDomainObjectVisitor() {
        this(0);
        start();
    }

    /** AbstractDomainObjectVisitor for inputed level.
     *
     * @param inLevel int */
    public AbstractDomainObjectVisitor(final int inLevel) {
        super();
        level = inLevel;
    }

    /** Sequence ending the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    abstract protected void endDomainObject(GeneralDomainObject inObject);

    /** Sequence ending the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    abstract protected void endIterator(DomainObjectIterator inIterator);

    /** Sequence ending the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    abstract protected void endProperty(Property inProperty);

    /** Sequence ending the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    abstract protected void endPropertySet(PropertySet inSet);

    /** Sequence ending the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    abstract protected void endSortedArray(SortedArray inSortedArray);

    /** @return int */
    protected int getLevel() {
        return level;
    }

    protected void incrementLevel() { // NOPMD
        level += 1;
    }

    protected void decrementLevel() { // NOPMD
        level -= 1;
    }

    protected void setLevel(final int inLevel) { // NOPMD
        level = inLevel;
    }

    protected abstract void start(); // NOPMD

    /** Sequence starting the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    abstract protected void startDomainObject(GeneralDomainObject inObject);

    /** Sequence starting the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    abstract protected void startIterator(DomainObjectIterator inIterator);

    /** Sequence starting the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    abstract protected void startProperty(Property inProperty);

    /** Sequence starting the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    abstract protected void startPropertySet(PropertySet inSet);

    /** Sequence starting the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    abstract protected void startSortedArray(SortedArray inSortedArray);

    /** Execute the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    public void visitDomainObject(final GeneralDomainObject inObject) {
        this.startDomainObject(inObject);
        incrementLevel();
        inObject.propertySet().accept(this);
        decrementLevel();
        this.endDomainObject(inObject);
    }

    /** Execute the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    public void visitDomainObjectIterator(final DomainObjectIterator inIterator) {
        this.startIterator(inIterator);
        incrementLevel();
        while (inIterator.hasMoreElements()) {
            inIterator.nextElement().accept(this);
        }
        decrementLevel();
        this.endIterator(inIterator);
    }

    /** Execute the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    public void visitProperty(final Property inProperty) {
        this.startProperty(inProperty);
        this.endProperty(inProperty);
    }

    /** Execute the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    public void visitPropertySet(final PropertySet inSet) {

        this.startPropertySet(inSet);
        incrementLevel();
        for (final NameValue lProperty : inSet.getNameValues2()) {
            ((Property) lProperty).accept(this);
        }
        decrementLevel();
        this.endPropertySet(inSet);
    }

    /** Execute the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    public void visitSortedArray(final SortedArray inSortedArray) {
        this.startSortedArray(inSortedArray);
        incrementLevel();
        for (int i = 0; i < inSortedArray.size(); i++) {
            inSortedArray.elementAt(i).accept(this);
        }
        decrementLevel();
        this.endSortedArray(inSortedArray);
    }
}
