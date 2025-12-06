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
package org.hip.kernel.exc;

import java.util.Locale;

@SuppressWarnings("serial")
public class VRuntimeException extends RuntimeException implements VThrowable {

    // instance attributes
    private Throwable rootCause;
    protected ExceptionData exceptionData;

    /** Default constructor */
    public VRuntimeException() {
        super();
    }

    /** @param inSimpleMessage java.lang.String */
    public VRuntimeException(final String inSimpleMessage) {
        super();
        exceptionData = new ExceptionData(this, inSimpleMessage);
    }

    /** @param inMsgKey java.lang.String
     * @param inMsgParameters java.lang.Object[] */
    public VRuntimeException(final String inMsgKey, final Object[] inMsgParameters) { // NOPMD
        this(null, inMsgKey, inMsgParameters);
    }

    /** @param inMsgKey java.lang.String
     * @param inMsgKey java.lang.String
     * @param inMsgParameters java.lang.Object[] */
    public VRuntimeException(final String inMsgSource, final String inMsgKey, final Object[] inMsgParameters) { // NOPMD
        super();
        exceptionData = new ExceptionData(this, inMsgSource, inMsgKey, inMsgParameters);
    }

    /** A hook to allow subclasses to define their own ExceptionData classes.
     *
     * @return org.hip.kernel.exc.ExceptionData */
    protected ExceptionData createExceptionData() {
        return new ExceptionData(this);
    }

    /** @return org.hip.kernel.exc.ExceptionData */
    private ExceptionData excData() {
        if (exceptionData == null) {
            exceptionData = createExceptionData();
        }
        return exceptionData;
    }

    /** @return java.lang.String */
    @Override
    public String getLocalizedMessage() {
        return excData().getLocalizedMessage();
    }

    /** @return java.lang.String
     * @param inLanguage java.util.Locale */
    @Override
    public String getLocalizedMessage(final Locale inLanguage) {
        return excData().getLocalizedMessage(inLanguage);
    }

    /** @return java.lang.String */
    @Override
    public String getMessage() {
        return excData().getMessage();
    }

    /** Returns the original exception if any.
     *
     * @return java.lang.Throwable */
    @Override
    public Throwable getRootCause() {
        return rootCause;
    }

    /** Returns true if this exception contains a root cause exception.
     *
     * @return boolean */
    @Override
    public boolean hasRootCause() {
        return rootCause != null;
    }

    /** Setter for the root cause exception.
     *
     * @param inRootCause java.lang.Throwable */
    @Override
    public void setRootCause(final Throwable inRootCause) {
        rootCause = inRootCause;
    }

    /** We override this method to invoke the getMessage.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        final String lMessage = (excData().get(ExceptionData.DATA_MSG_KEY) == null) ? excData().getMessage()
                : excData().get(ExceptionData.DATA_MSG_KEY).toString();

        return this.getClass().getName() + ": " + lMessage;
    }
}
