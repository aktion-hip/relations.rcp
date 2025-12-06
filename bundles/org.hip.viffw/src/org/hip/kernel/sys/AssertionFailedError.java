/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

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

package org.hip.kernel.sys;

import org.hip.kernel.exc.VError;

/** Objects of this type will be thrown in the Assert.assert methods.
 *
 * @author Benno Luthiger
 * @version 1.0
 * @see org.hip.kernel.sys.Assert */
@SuppressWarnings("serial")
public class AssertionFailedError extends VError {
    // Instance variables
    private final String callerMethod;

    /** AssertionFailedError constructor
     *
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inAssertName java.lang.String */
    public AssertionFailedError(final Object inCaller, final String inCallerMethod, final String inAssertName) {
        super(Assert.prepareAssertText(inCaller, inCallerMethod, inAssertName));
        callerMethod = inCallerMethod;
    }

    /** Returns the String the assert has originally invoked with.
     *
     * @return java.lang.String */
    public String getCallerMethod() {
        return callerMethod;
    }
}
