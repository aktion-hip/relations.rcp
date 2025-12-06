/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2003-2014, Benno Luthiger

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

import java.sql.SQLException;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.BOMNotFoundException;
import org.hip.kernel.bom.DomainObjectCollection;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.KeyObject;

/** Implementation of an empty query result.
 *
 * Created on 10.08.2003
 *
 * @author Luthiger
 * @see org.hip.kernel.bom.impl.AbstractQueryResult */
@SuppressWarnings("serial")
public class EmptyQueryResult extends AbstractQueryResult {
    private final KeyObject key;

    /** EmptyQueryResult default constructor.
     *
     * @param inHome GeneralDomainObjectHome
     * @throws BOMException */
    public EmptyQueryResult(final DomainObjectHome inHome) throws BOMException {
        super(inHome);
        key = inHome.create().getKey();
    }

    /** Returns the key of the current domain object.
     *
     * @return org.hip.kernel.bom.KeyObject
     * @exception org.hip.kernel.bom.BOMNotFoundException */
    @Override
    public KeyObject getKey() throws BOMNotFoundException {
        if (key == null) {
            throw new BOMNotFoundException();
        }
        else {
            return key;
        }
    }

    @Override
    public DomainObjectCollection nextn(final int inHowMany) throws SQLException, BOMException { // NOPMD by lbenno
        return new DomainObjectCollectionImpl();
    }
}
