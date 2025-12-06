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
package org.hip.kernel.servlet.impl;

import org.hip.kernel.exc.VException;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.Task;

/** Baseclass of all tasks.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.Task */
public abstract class AbstractTask implements Task { // NOPMD
    // instance attributes
    private Context context;

    /** Returns the context set for this task.
     *
     * @return org.hip.kernel.servlet.Context The tasks context
     * @see org.hip.kernel.servlet.Context */
    @Override
    public Context getContext() {
        return context;
    }

    /** Runs this task */
    @Override
    public abstract void run() throws VException;

    /** Sets the context of this Task
     *
     * @param inContext org.hip.kernel.servlet.Context The context for this task */
    @Override
    public void setContext(final Context inContext) {
        context = inContext;
    }
}
