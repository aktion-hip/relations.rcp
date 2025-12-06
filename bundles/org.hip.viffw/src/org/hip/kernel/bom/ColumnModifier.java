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

package org.hip.kernel.bom;

/**
 * This interface defines the behaviour of classes which modify the
 * WHERE clause of SQL statements, i.g. it offers the possibility to
 * create statements like UCASE(columnName).
 * Because the concrete modification (i.e. the field function) depends
 * on the chosen database, each modifier/function has to be implemented for each
 * database adapter.
 * 
 * Created on 11.09.2002
 * @author Benno Luthiger
 */
public interface ColumnModifier {

	/**
	 * Returns the modified column string.
	 * 
	 * @param inColumnName java.lang.String
	 * @return java.lang.String
	 */
	String modifyColumn(String inColumnName);
}
