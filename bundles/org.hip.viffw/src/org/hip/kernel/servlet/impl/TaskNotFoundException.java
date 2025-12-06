/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2005-2015, Benno Luthiger

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

import java.text.MessageFormat;

import org.hip.kernel.exc.VException;

/** Convenience exception.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class TaskNotFoundException extends VException {
    private final static String MESSAGE1 = "No task found for \"{0}\".";
    private final static String MESSAGE2 = "No task found.";

    /** TaskNotFoundException default constructor. */
    public TaskNotFoundException() {
        super(MESSAGE2);
    }

    /** TaskNotFoundException notifing about the task name causing the exception.
     *
     * @param inTaskName Name of the task that is missing. */
    public TaskNotFoundException(final String inTaskName) {
        super(MessageFormat.format(MESSAGE1, new Object[] { inTaskName }));
    }

    /** @param inMsgKey
     * @param inMsgParameters */
    public TaskNotFoundException(final String inMsgKey, final Object... inMsgParameters) {
        super(inMsgKey, inMsgParameters);
    }

    /** @param inMsgSource
     * @param inMsgKey
     * @param inMsgParameters */
    public TaskNotFoundException(final String inMsgSource, final String inMsgKey, final Object... inMsgParameters) {
        super(inMsgSource, inMsgKey, inMsgParameters);
    }
}
