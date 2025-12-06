/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006, Benno Luthiger

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

import java.util.ArrayList;
import java.util.List;

import org.hip.kernel.bom.ICriteriaStack;
import org.hip.kernel.bom.KeyObject.BinaryBooleanOperator;

/** Abstract class for <code>IOperandStack</code>.
 *
 * @author Luthiger Created on 10.07.2007 */
@SuppressWarnings("serial")
public abstract class AbstractCriteriaStack implements ICriteriaStack { // NOPMD
    protected List<String> criteria = new ArrayList<String>();
    protected List<BinaryBooleanOperator> operators = new ArrayList<BinaryBooleanOperator>();
    protected String join = "";

    @Override
    public void addCriterium(final StringBuffer inCriterium) { // NOPMD
        criteria.add(new String(inCriterium));
    }

    @Override
    public void addCriterium(final StringBuilder inCriterium) { // NOPMD
        criteria.add(new String(inCriterium));
    }

    @Override
    public void addOperator(final BinaryBooleanOperator inBinaryOperator) { // NOPMD
        operators.add(inBinaryOperator);
    }

    @Override
    public void setJoin(final String inJoin) { // NOPMD
        join = inJoin;
    }

    /** To be called after rendering. */
    protected void reset() {
        criteria = new ArrayList<String>();
        operators = new ArrayList<BinaryBooleanOperator>();
    }

}
