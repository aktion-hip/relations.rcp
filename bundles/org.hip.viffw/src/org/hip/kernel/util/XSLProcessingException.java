/**
 This package is part of the xml extendsions used for the application VIF.
 Copyright (C) 2001-2014, Benno Luthiger

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
package org.hip.kernel.util;

/** Exception to wrap and catch exceptions thrown during XSL prosessing.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class XSLProcessingException extends Exception {

    /** Constructor for XSLProcessingException. */
    public XSLProcessingException() {
        super();
    }

    /** Constructor for XSLProcessingException.
     *
     * @param inMsg String */
    public XSLProcessingException(final String inMsg) {
        super(inMsg);
    }

    /** Constructor for XSLProcessingException.
     *
     * @param inMsg String */
    public XSLProcessingException(final String inMsg, final Throwable inExc) {
        super(inMsg, inExc);
    }

}
