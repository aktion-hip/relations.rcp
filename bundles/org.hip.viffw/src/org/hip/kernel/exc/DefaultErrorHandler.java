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

/** Concrete default implementation for ErrorHandler. The actual implementation simply prints out the error-text to
 * standard error.
 *
 * @author Benno Luthiger */

public class DefaultErrorHandler extends AbstractExceptionHandler {

    // class attributes
    private static ExceptionHandler cInstance;

    /** @return java.lang.Throwable
     * @param inThrowable java.lang.Throwable
     * @param inId java.lang.String
     * @param inToFatal boolean */

    @Override
    public Throwable convert(final Throwable inThrowableToBeConverted, final String inId) {

        final VThrowable outException = new VError(inId);
        outException.setRootCause(inThrowableToBeConverted);

        // remove constructor statements from stackTrace
        ((Throwable) outException).fillInStackTrace();

        return (Throwable) outException;
    }

    /** Returns the single instance of this handler class.
     *
     * @return org.hip.kernel.exc.ExceptionHandler */

    public static synchronized ExceptionHandler instance() { // NOPMD
        if (cInstance == null) {
            cInstance = new DefaultErrorHandler();
        }
        return cInstance;
    }

    /** A Default implementation to handle the exception. Simply prints the exception to the standard error stream.
     *
     * @param inCatchingObject java.lang.Object
     * @param inThrowable java.lang.Throwable
     * @param inPrintStackTrace boolean */

    @Override
    protected void protectedHandle(final Object inCatchingObject, final Throwable inThrowable,
            final boolean inPrintStackTrace) {

        // This implementation simply prints out the exception.
        DefaultExceptionWriter.printOut(inCatchingObject, inThrowable, inPrintStackTrace);
    }
}
