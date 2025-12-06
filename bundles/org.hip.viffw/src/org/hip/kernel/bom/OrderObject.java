package org.hip.kernel.bom;

/*
	This package is part of the framework used for the application VIF.
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

import org.hip.kernel.util.SortedList;
import org.hip.kernel.util.VInvalidSortCriteriaException;
import org.hip.kernel.util.VInvalidValueException;

/**
 * OrderObjects are used to specify the sort order for a
 * SQL ORDER BY clause.
 * They are a special form of a sorted list.
 * 
 * Created on 13.09.2002
 * @author Benno Luthiger
 * @see org.hip.kernel.util.SortedList
 */
public interface OrderObject extends SortedList {

	/**
	 * This method sets a order item (i.e. column) with the specified name
	 * and sort order at the specified position.
	 *
	 * @param inColumnName java.lang.String
	 * @param inDescending boolean
	 * @param inPosition int
	 * @throws org.hip.kernel.util.VInvalidSortCriteriaException
	 * @throws org.hip.kernel.util.VInvalidValueException
	 */
	void setValue(String inColumnName, boolean inDescending, int inPosition) throws VInvalidSortCriteriaException, VInvalidValueException;

	/**
	 * This method sets an order item (i.e. column) with the specified name
	 * and default sort order (= ascending) at the specified position.
	 *
	 * @param inColumnName java.lang.String
	 * @param inPosition int starting at 1
	 * @throws org.hip.kernel.util.VInvalidSortCriteriaException
	 * @throws org.hip.kernel.util.VInvalidValueException
	 */
	void setValue(String inColumnName, int inPosition) throws VInvalidSortCriteriaException, VInvalidValueException;
}
