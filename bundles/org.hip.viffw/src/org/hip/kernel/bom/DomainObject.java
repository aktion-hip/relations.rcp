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

import java.sql.SQLException;
import java.util.Iterator;

import org.hip.kernel.exc.VException;

/** The DomainObject is the base interface for all domain related objects. We use the name domain object in the same way
 * as Ivar Jacobson does in his book "UML Unified Software Development Process"
 *
 * @author Benno Luthiger */
public interface DomainObject extends GeneralDomainObject {
    /** Delete this domain object from the table.
     *
     * @exception java.sql.SQLException */
    void delete() throws SQLException;

    /** Delete this domain object from the table with or without commit.
     *
     * @param inCommit boolean Trigger a COMMIT after the insert?
     * @exception java.sql.SQLException */
    void delete(boolean inCommit) throws SQLException;

    /** Insert the new DomainObject in the table of the DB.
     *
     * @return Long The auto-generated value of the new entry or 0, if there's no autoincrement column.
     * @throws java.sql.SQLException
     * @throws VException */
    Long insert() throws SQLException, VException;

    /** Insert the new DomainObject in the table of the DB with or without commit.
     *
     * @param inCommit boolean Trigger a COMMIT after the insert?
     * @return Long The auto-generated value of the new entry or 0, if there's no autoincrement column.
     * @throws java.sql.SQLException
     * @throws VException */
    Long insert(boolean inCommit) throws SQLException, VException;

    /** Update the DomainObject. The default implementation is without commit (for safety reasons).
     *
     * @exception java.sql.SQLException */
    void update() throws SQLException;

    /** Update the DomainObject.
     *
     * @param inCommit boolean Trigger a COMMIT after the insert?
     * @exception java.sql.SQLException */
    void update(boolean inCommit) throws SQLException;

    /** Returns the changed properties of this domain object.
     * 
     * @return java.util.Iterator<Property> */
    Iterator<Property> getChangedProperties();

}
