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
package org.hip.kernel.bom.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;

/** This is the default implementation of all domain object iterator classes.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObjectIterator */
public class DomainObjectIteratorImpl extends AbstractObjectIteratorImpl {
    // Instance variables
    private transient List<?> objs;
    private transient Iterator<?> iterator;

    /** DomainObjectIteratorImpl constructor.
     *
     * @param inObjects java.util.Vector Objects to iterate through. */
    public DomainObjectIteratorImpl(final List<?> inObjects) {
        super();
        objs = inObjects;
    }

    /** Sets the specified inVisitor. This method implements the inVisitor pattern.
     *
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    @Override
    public void accept(final DomainObjectVisitor inVisitor) {
        inVisitor.visitDomainObjectIterator(this);
    }

    /** Returns an enumeration of DomainObjects
     *
     * @return java.util.Iterator */
    private final Iterator<?> enumeration() {
        synchronized (this) {
            if (iterator == null) {
                iterator = objects().iterator();
            }
        }
        return iterator;
    }

    /** @return boolean */
    @Override
    public boolean hasMoreElements() {
        return enumeration().hasNext();
    }

    /** @return org.hip.kernel.bom.DomainObject */
    @Override
    public GeneralDomainObject nextElement() {
        return (GeneralDomainObject) enumeration().next();
    }

    private final List<?> objects() {
        synchronized (this) {
            if (objs == null) {
                objs = new ArrayList<Object>();
            }
        }
        return objs;
    }
}