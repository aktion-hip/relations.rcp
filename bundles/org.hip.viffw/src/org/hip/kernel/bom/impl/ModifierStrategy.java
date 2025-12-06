/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2006-2014, Benno Luthiger

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

import java.util.Arrays;

import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.sys.VObject;

/** This strategy class can be used to create SQL SELECT statments with column modifiers, e.g. to retrieve the maximum or
 * average value of a column.
 *
 * @author Benno Luthiger Created on 25.05.2006
 * @see org.hip.kernel.bom.SingleValueQueryStatement */
public class ModifierStrategy extends VObject {
    // Modifier constants
    public final static int MAX = 0;
    public final static int MIN = 1;
    public final static int COUNT = 2;
    public final static int SUM = 3;
    public final static int AVERAGE = 4;
    public final static int VARIANCE = 5;
    public final static int STDDEV = 6;

    private final static String[] MODIFIERS = { "MAX", "MIN", "COUNT", "SUM", "AVG", "VARIANCE", "STDDEV" };

    private final transient String[] columnNames;
    private final transient String[] modifiers;

    /** ModifierStrategy constructor. Use this constructor to modify all columns in the same way.
     *
     * @param inColumnName String The name of the column that shall be modified
     * @param inModifier int The modifier to apply to the column. */
    public ModifierStrategy(final String inColumnName, final int inModifier) {
        this(new String[] { inColumnName }, inModifier);
    }

    /** ModifierStrategy constructor. Use this constructor to modify all columns in the same way.
     *
     * @param inColumnNames String[] The name of the columns that shall be modified
     * @param inModifier int The modifier to apply to the columns. */
    public ModifierStrategy(final String[] inColumnNames, final int inModifier) {
        super();
        columnNames = inColumnNames.clone();
        modifiers = initModifiers(inColumnNames.length);
        try {
            Arrays.fill(modifiers, MODIFIERS[inModifier]);
        } catch (final IndexOutOfBoundsException exc) { // NOPMD by lbenno
            // left empty intentionally
        }
    }

    /** ModifierStrategy constructor. Use this constructor if the different columns are modified differently. Both arrays
     * passed as parameters must be of equal length.
     *
     * @param inColumnNames String[] The name of the columns that shall be modified.
     * @param inModifiers int[] The modifiers to apply to the columns. */
    public ModifierStrategy(final String[] inColumnNames, final int... inModifiers) {
        super();
        columnNames = inColumnNames.clone();
        modifiers = initModifiers(inColumnNames.length);
        try {
            for (int i = 0; i < inModifiers.length; i++) {
                modifiers[i] = MODIFIERS[inModifiers[i]];
            }
        } catch (final IndexOutOfBoundsException exc) { // NOPMD by lbenno
            // left empty intentionally
        }
    }

    private String[] initModifiers(final int inLength) {
        final String[] outModifiers = new String[inLength];
        Arrays.fill(outModifiers, MODIFIERS[MAX]);
        return outModifiers;
    }

    /** Creates the SQL statement with the modified column names, e.g. "MAX(pressure), AVG(pressure)"
     *
     * @param inDomainObjectHome DomainObjectHome
     * @return StringBuilder The SQL with the modified columns */
    public StringBuilder createModifiedSQL(final DomainObjectHome inDomainObjectHome) {
        final StringBuilder outSQL = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i < columnNames.length; i++) {
            if (!isFirst) {
                outSQL.append(", ");
            }
            isFirst = false;
            outSQL.append(modifiers[i]).append('(').append(inDomainObjectHome.getColumnNameFor(columnNames[i]))
                    .append(')');
        }
        return outSQL;
    }

}
