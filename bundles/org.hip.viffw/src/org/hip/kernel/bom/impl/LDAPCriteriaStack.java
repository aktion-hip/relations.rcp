/**
	This package is part of the framework used for the application VIF.
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

import org.hip.kernel.bom.KeyObject.BinaryBooleanOperator;

/** Implementation of <code>ICriteriaStack</code> for LDAP filters.
 *
 * @author Luthiger Created on 10.07.2007 */
@SuppressWarnings("serial")
public class LDAPCriteriaStack extends AbstractCriteriaStack { // NOPMD by lbenno 
    private final static String PATTERN_NORMAL = "%s(%s)";
    private final static String PATTERN_BRACKET = "%3$s(%1$s)(%2$s)";

    @Override
    public String render() { // NOPMD by lbenno 
        if (criteria.size() < 1) { // NOPMD by lbenno 
            return "";
        }

        String outPrevious = criteria.remove(0);

        if (criteria.size() < 1) { // NOPMD by lbenno 
            return outPrevious;
        }

        // set up the start
        operators.remove(0);
        BinaryBooleanOperator lPrevOp = operators.remove(0);
        String lPattern = PATTERN_NORMAL;
        outPrevious = String.format("%s(%s)(%s)", lPrevOp.getPrefixOperand(), outPrevious, criteria.remove(0));

        int i = 0; // NOPMD by lbenno 
        for (final String lCriterium : criteria) {
            final BinaryBooleanOperator lOperator = operators.get(i++);
            if (!lPrevOp.equals(lOperator)) {
                lPattern = PATTERN_BRACKET;
            }
            lPrevOp = lOperator;
            outPrevious = String.format(lPattern, outPrevious, lCriterium, lPrevOp.getPrefixOperand());
        }
        reset();
        return outPrevious;
    }

}
