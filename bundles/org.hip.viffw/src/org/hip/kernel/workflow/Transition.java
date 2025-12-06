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

/** This interface is used to describe transitions. Transitions come from one state and go to another.
 *
 * Created on 03.07.2003
 *
 * @author Benno Luthiger (inspired by itools by Juan David Ib��ez Palomar <jdavid@itaapy.com>) */
public interface Transition {
    /** Returns the state the transition starts from.
     *
     * @return java.lang.String */
    String getStateFrom();

    /** Returns the state the transition is going to.
     *
     * @return java.lang.String */
    String getStateTo();
}
