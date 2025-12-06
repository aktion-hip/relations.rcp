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

/** This interface defines the responsibilities of "Statistics-Homes". Statistics-Homes are special insofar as they hold
 * two ObjectDefinitions. One ObjectDefinition is internal, used to construct complcated query strings. The second
 * ObjectDefinition defines the structure of the created DomainObject, which holds statistical information and thus is
 * read only.
 * 
 *
 * @author B. Luthiger */
public interface StatisticsHome extends Home {
    // Clas variables
    String dftSerializer = "dft"; // NOPMD // Key name for default Serializer
    String xmlSerializer = "xml"; // NOPMD // Key name for XML Serializer

    /** This method creates a new instance of a DomainObject
     *
     * @return org.hip.kernel.bom.ReadOnlyDomainObject
     * @throws com.zurich.chz.bom.BOMException */
    ReadOnlyDomainObject create() throws BOMException;

    /** Returns an empty DomainObject. An empty object is initialized but does not contain any values. The object goes
     * back into this state after releasing.
     * 
     * @return org.hip.kernel.bom.ReadOnlyDomainObject
     * @throws com.zurich.chz.bom.BOMException */
    ReadOnlyDomainObject newInstance() throws BOMException;

    /** Returns the table names in a string which can be used to create a SQL select statement. We have to overwrite
     * super.tableNameString() for that we can get the internal table names accessed via this.tableNames().
     *
     * @return java.lang.String */
    String tableNameString();
}
