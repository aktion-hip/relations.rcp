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

import java.util.Enumeration;

import jakarta.servlet.ServletRequest;

/** A tool to get information out of a servlet request
 *
 * @author Benno Luthiger
 * @deprecated use {@link ServletRequestHelper} instead */
@Deprecated
public class ServletRequestInspector {
    // instance attributes
    private final ServletRequest request;

    public ServletRequestInspector(final ServletRequest inRequest) { // NOPMD by lbenno
        super();
        this.request = inRequest;
    }

    /** @return boolean
     * @param inName java.lang.String */
    public boolean containsParameter(final String inName) {
        return containsParameter(this.request, inName);
    }

    /** @return boolean
     * @param inRequest ServletRequest
     * @param inName java.lang.String */
    public static boolean containsParameter(final ServletRequest inRequest, final String inName) {
        boolean outContains = false;
        for (final Enumeration<?> lParameters = inRequest.getParameterNames(); lParameters.hasMoreElements();) {
            if (lParameters.nextElement().equals(inName)) {
                outContains = true;
                break;
            }
        }
        return outContains;
    }

    /** @return java.lang.String
     * @param inName java.lang.String */
    public String getParameterValue(final String inName) {
        return getParameterValue(this.request, inName);
    }

    /** @return java.lang.String
     * @param inRequest javax.servlet.ServletRequest
     * @param inName java.lang.String */
    public static String getParameterValue(final ServletRequest inRequest, final String inName) {
        if (containsParameter(inRequest, inName)) {
            final String[] lValues = inRequest.getParameterValues(inName);
            return lValues[0];
        }
        else {
            return null;
        }
    }

    /** @return javax.servlet.ServletRequest */
    public ServletRequest getRequest() {
        return this.request;
    }
}
