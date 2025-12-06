/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

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

import org.hip.kernel.exc.VException;

/** This class simply inherits from org.hip.kernel.exc.VException. Actually it does not add any functionality.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class BOMException extends VException {

    /** BOMException default constructor. */
    public BOMException() {
        super();
    }

    /** BOMException constructor.
     *
     * @param inExc {@link Throwable} */
    public BOMException(final Throwable inExc) {
        super(inExc);
    }

    /** Calls parent constructor.
     *
     * @param inMessage java.lang.String */
    public BOMException(final String inMessage) {
        super(inMessage);
    }

    /** Calls parent constructor.
     *
     * @param inMessage java.lang.String
     * @param inExc {@link Throwable} the root cause */
    public BOMException(final String inMessage, final Throwable inExc) {
        super(inMessage, inExc);
    }
}
