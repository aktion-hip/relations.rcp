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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hip.kernel.servlet.Task;
import org.hip.kernel.servlet.TaskManager;
import org.hip.kernel.sys.VSys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class of all task managers.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.TaskManager */
@SuppressWarnings("serial")
abstract public class AbstractTaskManager implements TaskManager, Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTaskManager.class);

    public static final String BACK_TASK_NAME = "back";
    public static final String MASTER_TASK_NAME = "master";
    public static final String DEFAULT_TASK_NAME = "startup";
    public static final String ERROR_TASK_NAME = "error";
    public static final String PREVIEW_TASK_NAME = "preview";

    /** singleton instance of taskmanager */
    protected static TaskManager cManager;

    /** table with taskname to task-classname mapping */
    private Map<String, String> taskMappings;

    /** AbstractTaskManager default constructor. */
    protected AbstractTaskManager() {
        super();
        initialize();
    }

    /** Returns an instance of a task. Uses the id <code>inName</code> to find the associated full taskname an creates an
     * new instance of the task.
     *
     * @return org.hip.kernel.servlet.Task
     * @param inName java.lang.String Name or ID of the task */
    @Override
    public Task create(final String inName) {
        final String lTaskClassName = taskMappingTable().get(inName);

        Task outTask = null;
        try {
            if (lTaskClassName == null) {
                outTask = createPluggableTask(inName);
            }
            else {
                outTask = createTask(lTaskClassName);
            }
            if (VSys.assertNotNull(this, "create", outTask)) {
                throw new TaskNotFoundException(inName);
            }
            return outTask;
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | TaskNotFoundException exc) {
            LOG.error("Error encountered while creating task!", exc);
            return new DefaultTaskImpl(exc);
        }
    }

    private Task createTask(final String inTaskClassName) throws InstantiationException,
    IllegalAccessException, ClassNotFoundException {
        // create a task object through reflection
        final Class<?> lTaskClass = Class.forName(inTaskClassName);
        return (Task) lTaskClass.newInstance();
    }

    /** Subclasses may override. Uses the id <code>inName</code> (which is a fully qualified request type) to find the
     * associated task name and bundle class loader to create an instance of the task.
     *
     * @param inName String the fully qualified request type.
     * @return Task the task
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException */
    protected Task createPluggableTask(final String inName) throws ClassNotFoundException, InstantiationException, // NOPMD
    IllegalAccessException {
        return null;
    }

    /** Subclasses have to override this method, which initalizes the mapping taskmappingtable. */
    abstract protected void initialize();

    /** Returns an instance of the taskmapping table.
     *
     * @return java.util.Map<String, String> */
    protected Map<String, String> taskMappingTable() {
        if (taskMappings == null) {
            taskMappings = new HashMap<String, String>();
        }
        return taskMappings;
    }
}
