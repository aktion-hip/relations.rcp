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
package org.hip.kernel.servlet;

import org.hip.kernel.exc.VException;

/** Interface for all Task in the servlet-framework. Tasks implementing this interface have basicly to implement the
 * <code>run()</code> method, which shoud contain all the actions of this task.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.impl.AbstractTask */

public interface Task { // NOPMD
    /** Returns the context set for this task.
     *
     * @return org.hip.kernel.servlet.Context The tasks context
     * @see org.hip.kernel.servlet.Context */
    Context getContext();

    /** Runs this task */
    void run() throws VException;

    /** Sets the context of this Task
     *
     * @param inContext org.hip.kernel.servlet.Context The context for this task */
    void setContext(Context inContext);
}
