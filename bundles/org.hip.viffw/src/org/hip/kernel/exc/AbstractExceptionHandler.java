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

import org.hip.kernel.sys.VSys;

/** This is the base class for those implementations which want to subclass from this exception handler.
 *
 * @author Benno Luthiger */
abstract public class AbstractExceptionHandler implements ExceptionHandler {

    // class attributes
    protected static boolean printStackTrace;

    /** @param inThrowable java.lang.Throwable
     * @return java.lang.Throwable */
    @Override
    public Throwable convert(final Throwable inThrowable) {
        return convert(inThrowable, null);
    }

    /** @param inCatchingObject java.lang.Object
     * @param inThrowable java.lang.Throwable */
    @Override
    public void handle(final Object inCatchingObject, final Throwable inThrowable) {
        this.handle(inCatchingObject, inThrowable, printStackTrace);
    }

    /** @param inCatchingObject java.lang.Object
     * @param inThrowable java.lang.Throwable
     * @param inPrintStackTrace boolean */
    @Override
    public void handle(final Object inCatchingObject, final Throwable inThrowable, final boolean inPrintStackTrace) {
        // Pre: throwable not null
        VSys.assertNotNull(this, "handle", inThrowable);

        // Delegate to concrete implementation
        protectedHandle(inCatchingObject, inThrowable, inPrintStackTrace);
    }

    /** @param inThrowable java.lang.Throwable */
    @Override
    public void handle(final Throwable inThrowable) {
        this.handle(null, inThrowable);
    }

    /** @param inCatchingObject java.lang.Object
     * @param inPrintStackTrace boolean */
    @Override
    public void handle(final Throwable inThrowable, final boolean inPrintStackTrace) {
        this.handle(null, inThrowable, inPrintStackTrace);
    }

    /** @return boolean
     * @param inThrowable java.lang.Throwable */
    protected final static boolean isVException(final Throwable inThrowable) {
        VSys.assertNotNull(AbstractExceptionHandler.class, "isVException", inThrowable);

        return inThrowable instanceof VError
                || inThrowable instanceof VRuntimeException
                || inThrowable instanceof VException;
    }

    /** Called from the AbstractExceptionHandler.
     *
     * @param inCatchingObject java.lang.Object
     * @param inThrowable java.lang.Throwable
     * @param inPrintStackTrace boolean */
    abstract protected void protectedHandle(Object inCatchingObject, Throwable inThrowable, boolean inPrintStackTrace);

    /** @param inThrowable java.lang.Throwable
     * @exception java.lang.Throwable The exception description. */
    @Override
    public void rethrow(final Throwable inThrowable) throws Throwable {
        throw convert(inThrowable).fillInStackTrace();
    }
}
