package org.hip.kernel.bom;

import java.sql.SQLException;

import org.hip.kernel.bom.impl.PlacefillerCollection;

/*
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001, Benno Luthiger

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

/**
 * This interface defines the responsibilities of "Homes" for joined
 * domain objects.
 *
 * @author Benno Luthiger
 */
public interface JoinedDomainObjectHome extends GeneralDomainObjectHome {
	/**
	 * This method creates a new instance of a ReadOnlyDomainObject
	 * 
	 * @return org.hip.kernel.bom.ReadOnlyDomainObject
	 * @throws com.zurich.chz.bom.BOMException
	 */
	ReadOnlyDomainObject create() throws BOMException;

	/**
	 * Use this method to find a DomainObject by its inKey.
	 *
	 * @return org.hip.kernel.bom.ReadOnlyDomainObject
	 * @param inKey org.hip.kernel.bom.KeyObject
	 */
	ReadOnlyDomainObject findByKey(KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException;

	/**
	 * 	Returns an empty ReadOnlyDomainObject. An empty object is
	 *	initialized but does not contain any values.
	 *	The object goes back into this state after releasing.
	 * 
	 * 	@return org.hip.kernel.bom.ReadOnlyDomainObject
	 */
	ReadOnlyDomainObject newInstance() throws BOMException;
	
	/**
	 * Select with fillers for placeholders.
	 * 
	 * @param inPlacefillers PlacefillerCollection
	 * @return QueryResult
	 * @throws SQLException
	 * @throws BOMException
	 */
	QueryResult select(PlacefillerCollection inPlacefillers) throws SQLException, BOMException;
	/**
	 * Select with fillers for placeholders.
	 * 
	 * @param inKey KeyObject
	 * @param inPlacefillers PlacefillerCollection
	 * @return QueryResult
	 * @throws SQLException
	 * @throws BOMException
	 */
	QueryResult select(KeyObject inKey, PlacefillerCollection inPlacefillers) throws SQLException, BOMException;
	/**
	 * Select with fillers for placeholders.
	 * 
	 * @param inKey KeyObject
	 * @param inOrder OrderObject
	 * @param inPlacefillers PlacefillerCollection
	 * @return QueryResult
	 * @throws SQLException
	 * @throws BOMException
	 */
	QueryResult select(KeyObject inKey, OrderObject inOrder, PlacefillerCollection inPlacefillers) throws SQLException, BOMException;
}
