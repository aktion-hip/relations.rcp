/**
 This package is part of the servlet framework used for the application VIF.
 Copyright (C) 2003-2015, Benno Luthiger

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hip.kernel.mail;

import org.hip.kernel.exc.VException;

/** This exception is used when an error occurs while generating a mail.
 *
 * Created on 16.05.2003
 *
 * @author Luthiger */
@SuppressWarnings("serial")
public class MailGenerationException extends VException {

    /** MailGenerationException default constructor. */
    public MailGenerationException() {
        super();
    }

    /** MailGenerationException default constructor.
     *
     * @param inSimpleMessage */
    public MailGenerationException(final String inSimpleMessage) {
        super(inSimpleMessage);
    }

    /** MailGenerationException default constructor.
     *
     * @param inMsgKey
     * @param inMsgParameters */
    public MailGenerationException(final String inMsgKey, final Object... inMsgParameters) {
        super(inMsgKey, inMsgParameters);
    }

    /** MailGenerationException default constructor.
     *
     * @param inMsgSource
     * @param inMsgKey
     * @param inMsgParameters */
    public MailGenerationException(final String inMsgSource, final String inMsgKey, final Object... inMsgParameters) {
        super(inMsgSource, inMsgKey, inMsgParameters);
    }
}
