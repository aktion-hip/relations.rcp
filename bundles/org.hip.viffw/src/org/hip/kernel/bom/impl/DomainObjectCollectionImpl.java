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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.QueryResult;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.util.Debug;

/** This is the base implementation of the DomainObjectCollection interface.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class DomainObjectCollectionImpl extends VObject implements DomainObjectCollection, Serializable {

    // Instance variables
    private List<GeneralDomainObject> domainObjects;

    /** DomainObjectCollectionImpl default constructor. */
    public DomainObjectCollectionImpl() {
        super();
    }

    /** DomainObjectCollectionImpl constructor. Creates a collection of DomainObjects and fills them with data from the
     * QueryResult.
     *
     * @param inQueryResult org.hip.kernel.bom.QueryResult */
    public DomainObjectCollectionImpl(final QueryResult inQueryResult) {
        super();
        initializeFrom(inQueryResult);
    }

    /** Adds the specified DomainObject to the collection.
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    public void add(final GeneralDomainObject inObject) {
        // Pre: inObject not null
        if (inObject == null) {
            return;
        }

        // Post: register the inObject
        getObjects().add(inObject);
    }

    /** Returns a DomainObjectIterator.
     *
     * @return org.hip.kernel.bom.DomainObjectIterator */
    @Override
    public DomainObjectIterator elements() {
        return new DomainObjectIteratorImpl(getObjects());
    }

    /** This method fills a collection with DomainObjects and initializes them with the data in the specified
     * QueryResult.
     *
     * @param inQueryResult org.hip.kernel.bom.QueryResult */
    private void initializeFrom(final QueryResult inQueryResult) {
        if (inQueryResult == null) {
            return;
        }

        synchronized (this) {
            try {
                // Read ahead
                GeneralDomainObject lObject = inQueryResult.nextAsDomainObject();
                while (lObject != null) {
                    getObjects().add(lObject);
                    // read next
                    lObject = inQueryResult.nextAsDomainObject();
                } // while
            } catch (BOMException | SQLException exc) {
                DefaultExceptionHandler.instance().handle(exc);
            }
        }
    }

    /** Private accessor to the objects.
     *
     * @return java.util.Vector */
    private final List<GeneralDomainObject> getObjects() {
        synchronized (this) {
            if (this.domainObjects == null) {
                this.domainObjects = new ArrayList<GeneralDomainObject>();
            }
        }
        return this.domainObjects;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lMarkups = new StringBuilder();
        for (final GeneralDomainObject lObject : getObjects()) {
            lMarkups.append(lObject.toString()).append('\n');
        }
        return Debug.classMarkupString(this, "size=" + getObjects().size(), new String(lMarkups));
    }
}