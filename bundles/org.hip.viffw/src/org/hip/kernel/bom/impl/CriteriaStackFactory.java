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

import org.hip.kernel.bom.ICriteriaStack;

/** A simple factory to create instances of <code>ICriteriaStack</code>.
 *
 * @author Luthiger Created on 13.07.2007 */
public class CriteriaStackFactory {
    /** Stack type enumeration. */
    public enum StackType {
        SQL, LDAP, FLAT_JOIN
    };

    private transient final StackType stackType;
    private transient String join;

    /** CriteriaStackFactory constructor.
     *
     * @param inStackType {@link StackType} */
    public CriteriaStackFactory(final StackType inStackType) {
        this(inStackType, null);
    }

    /** CriteriaStackFactory constructor.
     *
     * @param inStackType {@link StackType}
     * @param inJoin String */
    public CriteriaStackFactory(final StackType inStackType, final String inJoin) {
        stackType = inStackType;
        join = inJoin;
    }

    /** Sets the join operator.
     *
     * @param inJoin String */
    public void setJoin(final String inJoin) {
        join = inJoin;
    }

    /** Returns the stack of criteria expressions.
     *
     * @return {@link ICriteriaStack} */
    public ICriteriaStack getCriteriaStack() {
        // default
        ICriteriaStack outStack = new SQLCriteriaStack();
        // else
        if (StackType.LDAP.equals(stackType)) {
            outStack = new LDAPCriteriaStack();
        }
        else if (StackType.FLAT_JOIN.equals(stackType)) {
            outStack = new FlatJoinCriteriaStack();
        }
        if (join != null && !join.isEmpty()) {
            outStack.setJoin(join);
        }
        return outStack;
    }

}
