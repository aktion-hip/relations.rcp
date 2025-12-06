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

import org.hip.kernel.exc.VRuntimeException;

/** The Assert class provides methods to assert. An assert checks the condition. If it is evaluated to false the
 * AssertionFailedError will be thrown.
 *
 * @author Benno Luthiger
 * @version 1.0
 * @see java.lang.Object */
public final class Assert extends VObject {

    // Class variables
    /** An assert-failure is printed out on the VSys.err print stream. */
    public static final int ERROR_OUT = 1;
    /** An assert-failure is logged in the system assert error log */
    public static final int EXCEPTION = 2;
    /** An assert-failure will throw the AssertionFailedError. */
    public static final int ERROR = 3;

    /** Constants */
    public static final boolean FAILURE = true;
    public static final boolean NO_FAILURE = false;

    /** Holds the default assert level. */
    public static final int DEFAULT_LEVEL = ERROR_OUT;

    private Assert() {
        // prevent instantiation
        super();
    }

    /** This method checks the condition. If it evaluates to false, it will print out an assert failure message or throw
     * a AssertionFailedError.
     *
     * @return boolean
     * @param inAssertLevel int
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inObject java.lang.Object */
    public static boolean assertNotNull(final int inAssertLevel
            , final Object inCaller
            , final String inCallerMethod
            , final Object inObject) {

        if (inObject == null) {
            return fail(inAssertLevel, inCaller, inCallerMethod, "assertNotNull");
        }
        else {
            return Assert.NO_FAILURE;
        }
    }

    /** This method checks the condition. If it evaluates to false, it will print out an assert failure message or throw
     * a AssertionFailedError.
     *
     * @return boolean
     * @param inAssertLevel int
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inCondition boolean */
    public static boolean assertTrue(final int inAssertLevel
            , final Object inCaller
            , final String inCallerMethod
            , final boolean inCondition) {

        if (inCondition) {
            return Assert.NO_FAILURE;
        }
        else {
            return fail(inAssertLevel, inCaller, inCallerMethod, "assertTrue");
        }

    }

    /** This method prints out an assert failure message or throws an AssertionFailedError.
     *
     * @return boolean
     * @param inAssertLevel int
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inAssertName java.lang.String */
    protected static boolean fail(final int inAssertLevel, final Object inCaller,
            final String inCallerMethod, final String inAssertName) {

        switch (inAssertLevel) {
        // Simple output on err-stream
        case Assert.ERROR_OUT:
            VSys.err.println(prepareAssertText(inCaller, inCallerMethod, inAssertName));
            return Assert.FAILURE;

            // Simple output on err stream and throw VRuntimeException
        case Assert.EXCEPTION:
            VSys.err.println(prepareAssertText(inCaller, inCallerMethod, inAssertName));
            throw new VRuntimeException();

            // Throw error (AssertionFailedError)
        case Assert.ERROR:
            throw new AssertionFailedError(inCaller, inCallerMethod, inAssertName);

            // Default is same as ERROR_OUT
        default:
            VSys.err.println(prepareAssertText(inCaller, inCallerMethod, inAssertName));
            return Assert.FAILURE;

        } // switch

    } // fail

    /** This method prepares the assert text from the passed objects.
     *
     * @return java.lang.String
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inAssertName java.lang.String */
    protected static String prepareAssertText(final Object inCaller
            , final String inCallerMethod
            , final String inAssertName) {

        // Quite simple text for the assert
        final String lClassName = (inCaller == null) ? "unknownClass" : inCaller.getClass().getName();
        final String lMethodName = (inCallerMethod == null) ? "unknownMethod" : inCallerMethod;

        return "Assertion failure (" + inAssertName + ") in: " + lClassName + ":" + lMethodName;
    }
}
