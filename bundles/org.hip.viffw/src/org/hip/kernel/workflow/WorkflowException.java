/**
	This class is part of the workflow framework of the application VIF.
	Copyright (C) 2003, Benno Luthiger

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

/** Exception to signal problems thrown during workflow handling.
 *
 * Created on 04.07.2003
 *
 * @author Benno Luthiger (inspired by itools by Juan David Ib��ez Palomar <jdavid@itaapy.com>) */
@SuppressWarnings("serial")
public class WorkflowException extends Exception {

    /** WorkflowException default constructor. */
    public WorkflowException() {
        super();
    }

    /** WorkflowException constructor with specified message.
     *
     * @param inSimpleMessage */
    public WorkflowException(final String inSimpleMessage) {
        super(inSimpleMessage);
    }

    /** @param inExc {@link Throwable} */
    public WorkflowException(final Throwable inExc) {
        super(inExc);
    }
}
