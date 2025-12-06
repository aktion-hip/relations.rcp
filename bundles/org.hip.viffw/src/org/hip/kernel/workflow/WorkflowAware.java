/**
	This class is part of the workflow framework of the application VIF.
	Copyright (C) 2003-2014, Benno Luthiger

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

/** This interface has to be implemented by workflow aware objects.<br/>
 * Specific application semantics for states and transitions can be implemented as methods of the class implementing
 * this interface.<br/>
 * These methods get associated with the individual states and transitions by a simple naming scheme. For example, if a
 * Workflow has two states 'Private' and 'Public', and a transition 'Publish' that goes from 'Private' to 'Public', the
 * following happens when the transition is executed:
 *
 * 1. if implemented, the method <code>onLeave_Private</code> is called (it is called each time the object leaves the
 * 'Private' state)
 *
 * 2. if implemented, the method <code>onTransition_Publish</code> is called (it is called whenever this transition is
 * executed)
 *
 * 3. if implemented, the method <code>onEnter_Public</code> is called (it is called each time the object enters the
 * 'Public' state)
 *
 * @author Benno Luthiger (inspired by itools by Juan David Ibanez Palomar <jdavid@itaapy.com>) */
public interface WorkflowAware {
    /** [Re-]Bind this object to the specified workflow. The Workflow must provide a default initial State.
     *
     * @param inWorkflow Workflow The Workflow the workflow aware object has to be bound to.
     * @param inArgs java.langObject[] Arguments passed down to all handlers called.
     * @throws WorkflowException */
    void enterWorkflow(Workflow inWorkflow, Object... inArgs) throws WorkflowException;

    /** [Re-]Bind this object to the specified workflow. The <code>inInitialStateName</code> parameter is the workflow
     * State that should be taken on initially.
     *
     * @param inWorkflow Workflow The Workflow the workflow aware object has to be bound to.
     * @param inInitialStateName java.lang.String The name of the initial State.
     * @param inArgs java.langObject[] Arguments passed down to all handlers called.
     * @throws WorkflowException */
    void enterWorkflow(Workflow inWorkflow, String inInitialStateName, Object... inArgs) throws WorkflowException;

    /** Performs the transition with the specified name, changes the state of the object and runs any defined
     * state/transition handlers. Extra arguments are passed down to all handlers called.
     *
     * @param inTransitionName java.lang.String
     * @param inArgs java.lang.Object[] Arguments passed down to all handlers called.
     * @throws WorkflowException */
    void doTransition(String inTransitionName, Object... inArgs) throws WorkflowException;

    /** Returns the name of the current State.
     *
     * @return java.lang.String */
    String getStateName();

    /** Returns the current State instance.
     *
     * @return State
     * @throws WorkflowException */
    State getState() throws WorkflowException;
}
