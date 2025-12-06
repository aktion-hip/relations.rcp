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
package org.hip.kernel.exc;

/** This interface defines the basic behavior of every Exception Handler.
 *
 * @author Benno Luthiger */

public interface ExceptionHandler {

    // class attributes
    ExceptionHandler cDefaultHandler = DefaultExceptionHandler.instance(); // NOPMD
    ExceptionHandler cDefaultRuntimeHandler = DefaultRuntimeExceptionHandler.instance(); // NOPMD
    ExceptionHandler cDefaultErrorHandler = DefaultErrorHandler.instance(); // NOPMD

    /** @return java.lang.Throwable
     * @param inThrowable java.lang.Throwable */
    Throwable convert(Throwable inThrowable);

    /** @return java.lang.Throwable
     * @param inThrowable java.lang.Throwable
     * @param inId java.lang.String */
    Throwable convert(Throwable inThrowable, String inId);

    /** Subclasses must implement this method. The interface makes no assumptions about the concrete handling.
     *
     * @param throwable java.lang.Throwable The Error or Exception to handle
     * @param printStackTrace boolean Flag to control the output of the stack trace. */
    void handle(Object catchingObject, Throwable throwable);

    /** Subclasses must implement this method. The interface makes no assumptions about the concrete handling.
     *
     * @param inCatchingObject java.lang.Object
     * @param inThrowable java.lang.Throwable
     * @param inPrintStackTrace boolean */
    void handle(Object inCatchingObject, Throwable inThrowable, boolean inPrintStackTrace);

    /** Subclasses must implement this method. The interface makes no assumptions about the concrete handling.
     *
     * @param inThrowable java.lang.Throwable */
    void handle(Throwable inThrowable);

    /** Subclasses must implement this method. The interface makes no assumptions about the concrete handling.
     *
     * @param inThrowable java.lang.Throwable
     * @param inPrintStackTrace boolean */
    void handle(Throwable inThrowable, boolean inPrintStackTrace);

    /** @param inThrowable java.lang.Throwable
     * @exception java.lang.Throwable The exception description. */
    void rethrow(Throwable inThrowable) throws Throwable;
}
