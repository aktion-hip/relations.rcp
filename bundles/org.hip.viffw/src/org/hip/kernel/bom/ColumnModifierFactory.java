/**
	This package is part of the application VIF.
	Copyright (C) 2012-2014, Benno Luthiger

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.bom;

import org.hip.kernel.bom.impl.DefaultColumnModifier;
import org.hip.kernel.bom.impl.MySQLColumnModifierUCase;
import org.hip.kernel.bom.impl.OracleColumnModifierUCase;

/** Returns column modfier instances.
 *
 * @author Luthiger Created: 01.03.2012 */
public final class ColumnModifierFactory {
    private ColumnModifierFactory() {
        // prevent instantiation
    }

    /** @return an instance of <code>DefaultColumnModifier</code> doing nothing */
    public static ColumnModifier getDefaultColumnModifier() {
        return new DefaultColumnModifier();
    }

    /** @return an instance of <code>MySQLColumnModifierUCase</code> modifying to <code>UCASE()</code> */
    public static ColumnModifier getMySQLColumnModifierUCase() {
        return new MySQLColumnModifierUCase();
    }

    /** @return an instance of <code>OracleColumnModifierUCase</code> modifying to <code>UPPER()</code> */
    public static ColumnModifier getOracleColumnModifierUCase() {
        return new OracleColumnModifierUCase();
    }

}
