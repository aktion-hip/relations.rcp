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

package org.hip.kernel.workflow;

/** Error to signal a configuration problem thrown during workflow handling.
 *
 * @author Luthiger Created on 19.09.2007 */
@SuppressWarnings("serial")
public class WorkflowError extends Error { // NOPMD

    /** Constructs a new error with the specified detail message.
     *
     * @param inMessage */
    public WorkflowError(final String inMessage) {
        super(inMessage);
    }

    /** Constructs a new error with the specified cause and a detail message of (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of cause). This constructor is useful for errors that are
     * little more than wrappers for other throwables.
     *
     * @param inCause */
    public WorkflowError(final Throwable inCause) {
        super(inCause);
    }

    /** Constructs a new error with the specified detail message and cause.
     *
     * @param inMessage
     * @param inCause */
    public WorkflowError(final String inMessage, final Throwable inCause) {
        super(inMessage, inCause);
    }

}
