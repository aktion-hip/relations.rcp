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

package org.hip.kernel.bom;

import java.io.Serializable;

import org.hip.kernel.bom.KeyObject.BinaryBooleanOperator;

/** This interface defines a stack of criteria and operators. The aim of this interface is the correct rendering of a
 * sequence of criteria and operators placing the brackets so that a logical correct statement is created which has the
 * intended effect, e.g. ((crit1 AND crit2 AND crit3) OR crit4)
 *
 * @author Luthiger Created on 09.07.2007 */
public interface ICriteriaStack extends Serializable {
    /** Adds the rendered criteriums
     *
     * @param inCriterium StringBuffer the rendered <code>KeyCriterium</code>.
     * @deprecated Use addCriterium(StringBuilder) instead */
    @Deprecated
    void addCriterium(StringBuffer inCriterium);

    /** Adds the rendered criteriums
     *
     * @param inCriterium StringBuffer the rendered <code>KeyCriterium</code>. */
    void addCriterium(StringBuilder inCriterium);

    /** Adds the binary operand joining the added criterium in the preceding run with the actually added criterium.
     *
     * @param inBinaryOperator BinaryBooleanOperator. */
    void addOperator(BinaryBooleanOperator inBinaryOperator);

    /** Renders the stack.
     *
     * @return String */
    String render();

    /** Sets the string that joins the criteria.
     *
     * @param inJoin String */
    void setJoin(String inJoin);
}
