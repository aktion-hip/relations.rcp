/**
	This package is part of the structured text framework used for the application VIF.
	Copyright (C) 2003-2015, Benno Luthiger

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
package org.hip.kernel.stext;

import org.hip.kernel.sys.VObject;

/** This class provides generic functionality to serialize StructuredText.
 *
 * @author: Benno Luthiger */
public abstract class AbstractStructuredTextSerializer extends VObject { // NOPMD by lbenno
    // Instance variables
    private final transient StringBuffer buffer = new StringBuffer(1024); // NOPMD by lbenno
    private transient String nl; // NOPMD by lbenno

    /** Emits a string, i.e. writes the specified string to the buffer.
     *
     * @param inWhat java.lang.String */
    protected final synchronized void emit(final String inWhat) { // NOPMD by lbenno
        buffer.append(inWhat);
    }

    /** Emits a string, i.e. writes the specified string to the buffer.
     *
     * @param inWhat java.lang.StringBuffer */
    protected final synchronized void emit(final StringBuffer inWhat) { // NOPMD by lbenno
        buffer.append(inWhat);
    }

    /** Emits a new line.
     *
     * @param java.lang.String */
    protected final synchronized void emit_nl() { // NOPMD by lbenno
        buffer.append(nl());
    }

    /** Returns a new line, lazy initialization.
     *
     * @return java.lang.String */
    protected String nl() { // NOPMD by lbenno
        if (nl == null) {
            nl = System.getProperty("line.separator");
        }
        return nl;
    }

    /** Returns the serialized object as String.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        return new String(buffer);
    }
}
