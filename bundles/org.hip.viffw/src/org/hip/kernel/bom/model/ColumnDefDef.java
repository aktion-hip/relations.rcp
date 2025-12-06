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
package org.hip.kernel.bom.model;

/** Holds information about columns used for the joining of domain objects.
 *
 * @author Benno Luthiger */
public final class ColumnDefDef {
    public final static String columnName = "columnName".intern(); // NOPMD
    public final static String domainObject = "domainObject".intern(); // NOPMD
    public final static String alias = "alias".intern(); // NOPMD by lbenno
    public final static String as = "as".intern(); // NOPMD
    public final static String nestedObject = "nestedObject".intern(); // NOPMD
    public final static String modifier = "modifier".intern(); // NOPMD
    public final static String template = "template".intern(); // NOPMD
    public final static String valueType = "valueType".intern(); // NOPMD

    private ColumnDefDef() {
        // prevent instantiation
    }
}
