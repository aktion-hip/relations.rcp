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

/** The taskmanager is a factory for tasks.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.Task */

public interface TaskManager {
    /** Returns an instance of a task. Uses the id <code>inName</code> to find the associated full taskname an creates an
     * new instance of the task.
     *
     * @return org.hip.kernel.servlet.Task
     * @param inName java.lang.String Name or ID of the task */
    Task create(String inName);

    /** Returns an instance of the default task.
     *
     * @return org.hip.kernel.servlet.Task */
    Task getDefaultTask();

    /** Returns an instance of the error task.
     *
     * @return org.hip.kernel.servlet.Task */
    Task getErrorTask();
}
