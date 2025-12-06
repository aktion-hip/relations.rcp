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

import org.hip.kernel.util.SortableItem;

/**
 * Interface for a single item in a OrderObject, i.e. a column
 * in a SQL ORDER BY clause.
 * 
 * Created on 13.09.2002
 * @author Benno Luthiger
 */
public interface OrderItem extends SortableItem {

	/**
	 * Column name of item.
	 * 
	 * @return java.lang.String
	 */
	String getColumnName();
	
	/**
	 * Order direction.
	 * 
	 * @return boolean Returns true if descending.
	 */
	boolean isDescending();
	
	/**
	 * Position of item.
	 * 
	 * @return int
	 */
	int getPosition();
}
