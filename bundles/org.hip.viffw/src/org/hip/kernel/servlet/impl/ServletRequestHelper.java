/**
	This package is part of the application VIF.
	Copyright (C) 2010-2015, Benno Luthiger

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
package org.hip.kernel.servlet.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.RequestException;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.DefaultNameValue;
import org.hip.kernel.util.DefaultNameValueList;
import org.hip.kernel.util.NameValueList;

import jakarta.servlet.http.HttpServletRequest;

/** Helper class for inspecting the servlet request. This class knows how to parse mulitpart requests.
 *
 * @author Luthiger Created: 21.09.2010 */
public class ServletRequestHelper {
    private static final String KEY_MAX_SIZE = "org.hip.vif.upload.size";
    private static final int DFLT_SIZE = 50;
    private static final int MEGA = 1048576;

    private final IInspector inspector;

    /** ServletRequestHelper constructor.
     *
     * @param inRequest {@link HttpServletRequest}
     * @throws RequestException */
    public ServletRequestHelper(final HttpServletRequest inRequest) throws RequestException {
        this.inspector = getInspector(inRequest);
        this.inspector.parse();
    }

    private IInspector getInspector(final HttpServletRequest inRequest) {
        if (JakartaServletFileUpload.isMultipartContent(inRequest)) {
            return new MultiPartInspector(inRequest);
        }
        return new ClassicInspector(inRequest);
    }

    /** Checks whether the request to inspect contains the specified parameter.
     *
     * @param inName String name of the parameter to look up
     * @return boolean <code>true</code> if the wrapped request contains the specified parameter */
    public boolean containsParameter(final String inName) {
        return this.inspector.containsParameter(inName);
    }

    /** Returns the specified parameter's value from the wrapped request.
     *
     * @param inName String name of the parameter to return the value
     * @return String the parameter's value or <code>null</code>, if the parameter doesn't exist in the request */
    public String getParameterValue(final String inName) {
        return this.inspector.getParameterValue(inName);
    }

    /** Extracts all parameters except the specified out of the servlet request and adds them to the context.
     *
     * @param inContext org.hip.kernel.servlet.Context The context set for the clients session
     * @param inExcept String the name of the parameter to ignore */
    public void addParametersToContext(final Context inContext, final String inExcept) {
        this.inspector.addParametersToContext(inContext, inExcept);
    }

    /** Convenience method: checks the passed request for a parameter with the specified name.
     *
     * @param inRequest HttpServletRequest
     * @param inName String parameter name.
     * @return boolean */
    public static boolean containsParameter(final HttpServletRequest inRequest, final String inName) {
        for (final Enumeration<?> lParameters = inRequest.getParameterNames(); lParameters.hasMoreElements();) {
            if (lParameters.nextElement().equals(inName)) {
                return true;
            }
        }
        return false;
    }

    // --- inner classes ---

    /** Interface for inspecting the servlet request. */
    private interface IInspector {
        /** Parse the wrapped request.
         *
         * @throws RequestException */
        void parse() throws RequestException;

        /** Checks whether the request to inspect contains the specified parameter.
         *
         * @param inName String name of the parameter to look up
         * @return boolean <code>true</code> if the wrapped request contains the specified parameter */
        boolean containsParameter(String inName);

        /** Returns the specified parameter's value from the wrapped request.
         *
         * @param inName String
         * @return String the parameter's value or <code>null</code>, if the parameter doesn't exist in the request */
        String getParameterValue(String inName);

        /** @param inContext {@link Context}
         * @param inExcept String */
        void addParametersToContext(Context inContext, String inExcept);
    }

    private static class ClassicInspector implements IInspector { // NOPMD by lbenno
        private final transient HttpServletRequest request;
        private transient Map<String, String[]> parameters = new ConcurrentHashMap<String, String[]>(); // NOPMD

        ClassicInspector(final HttpServletRequest inRequest) {
            this.request = inRequest;
        }

        @Override
        public void parse() { // NOPMD by lbenno
            this.parameters = this.request.getParameterMap();
        }

        @Override
        public boolean containsParameter(final String inName) { // NOPMD by lbenno
            return this.parameters.containsKey(inName);
        }

        @Override
        public String getParameterValue(final String inName) { // NOPMD by lbenno
            final String[] outValue = this.parameters.get(inName);
            return outValue == null ? null : outValue[0];
        }

        @Override
        public void addParametersToContext(final Context inContext, final String inExcept) { // NOPMD by lbenno
            final NameValueList lList = new DefaultNameValueList();
            for (final String lKey : this.parameters.keySet()) {
                if (lKey.equals(inExcept)) {
                    continue;
                }

                final String[] lValues = this.parameters.get(lKey);
                lList.add(new DefaultNameValue(lList, lKey, lValues.length == 1 ? lValues[0] : lValues));
            }
            inContext.setParameters(lList);
        }
    }

    private static class MultiPartInspector implements IInspector { // NOPMD by lbenno
        private final transient HttpServletRequest request;
        private final transient Map<String, DiskFileItem> uploadParameters = new ConcurrentHashMap<>(); // NOPMD
        private final transient Map<String, Collection<String>> formParameters = new ConcurrentHashMap<>(); // NOPMD

        MultiPartInspector(final HttpServletRequest inRequest) {
            this.request = inRequest;
        }

        private long getSize() {
            long size = DFLT_SIZE;
            try {
                size = Long.parseLong(VSys.getVSysProperties().getProperty(KEY_MAX_SIZE));
            } catch (final IOException exc) { // NOPMD by lbenno
                // intentionally left empty
            }
            return size * MEGA;
        }

        @Override
        public void parse() throws RequestException { // NOPMD by lbenno
            final DiskFileItemFactory factory = DiskFileItemFactory.builder().get();
            final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> upload = new JakartaServletFileUpload<>(
                    factory);
            upload.setFileSizeMax(getSize());
            try {
                final List<DiskFileItem> items = upload.parseRequest(this.request);
                for (final DiskFileItem item : items) {
                    final String fieldName = item.getFieldName();
                    if (item.isFormField()) {
                        Collection<String> values = this.formParameters.get(fieldName);
                        if (values == null) {
                            values = new ArrayList<>();
                        }
                        values.add(item.getString(Charset.forName(AbstractRequestHandler.ENCODING)));
                        this.formParameters.put(fieldName, values);
                    }
                    else {
                        this.uploadParameters.put(fieldName, item);
                    }
                }
            } catch (final IOException exc) {
                throw new RequestException(exc);
            }
        }

        @Override
        public boolean containsParameter(final String inName) { // NOPMD by lbenno
            return this.formParameters.containsKey(inName);
        }

        @Override
        public String getParameterValue(final String inName) { // NOPMD by lbenno
            final Collection<String> outValue = this.formParameters.get(inName);
            if (outValue == null) {
                return null;
            }
            return outValue.iterator().next();
        }

        @Override
        public void addParametersToContext(final Context inContext, final String inExcept) { // NOPMD by lbenno
            final NameValueList lParameters = new DefaultNameValueList();
            for (final String lKey : this.formParameters.keySet()) {
                if (lKey.equals(inExcept)) {
                    continue;
                }

                final Collection<String> lValues = this.formParameters.get(lKey);
                lParameters.add(new DefaultNameValue(lParameters, lKey, lValues.size() == 1 ? lValues.iterator().next()
                        : lValues.toArray(new String[lValues.size()])));
            }
            inContext.setParameters(lParameters);

            final NameValueList lFileItems = new DefaultNameValueList();
            for (final String lKey : this.uploadParameters.keySet()) {
                lFileItems.add(new DefaultNameValue(lParameters, lKey, new org.hip.kernel.servlet.impl.FileItem(
                        this.uploadParameters.get(lKey))));
            }
            inContext.setFileItems(lFileItems);
        }
    }

}
