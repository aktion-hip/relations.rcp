/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2025, Benno Luthiger

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
package org.hip.kernel.servlet.impl; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.MIMEFile;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.NameValue;
import org.hip.kernel.util.NameValueList;

/** Baseclass of all contexts.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.Context */
@SuppressWarnings("serial")
public abstract class AbstractContext implements Context, Serializable { // NOPMD
    // constants
    /** Keyname for the session */
    public static final String SESSION_NAME = "Session";
    /** Keyname for the response */
    public static final String RESPONSE_KEY = "Response";
    /** Keyname of the request type */
    public static final String REQUEST_TYPE = "requestType";
    /** Keyname of the context, i.e. key for context object in session */
    public static final String CONTEXT = "context";
    /** Keyname of the userId */
    public static final String USER_ID_KEY = "userId";
    /** Keyname of the password */
    public static final String USER_PASSWORD_KEY = "pwd";
    /** Keyname of a view/page shown in the browser */
    public static final String VIEW = "view";
    /** Keyname of a MIME file provided through the browser */
    public static final String MIME_FILE = "mimeFile";
    /** Keyname of query result view */
    public static final String QUERY_RESULT_VIEW_KEY = "queryResultView";
    /** Keyname of the language */
    public static final String LANGUAGE_KEY = "language";
    /** Keyname of newSession-property */
    public static final String NEW_SESSION_KEY = "newSession";
    /** Keyname of userAuthorized-property */
    public static final String USER_AUTHORIZED_KEY = "userAuthorized";
    /** Keyname of error message */
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    /** Keyname of sub application path */
    private static final String SERVLET_PATH = "servletPath";
    /** Keyname of remote address field */
    private static final String REMOTE_ADDR = "remoteAddr";
    /** Keyname of remote host field */
    private static final String REMOTE_HOST = "remoteHost";
    /** Keyname of request URL */
    private static final String REQUEST_URL = "requestURL";

    // instance attributes
    private Map<String, Object> props;
    private Map<String, Object> params;
    private Map<String, FileItem> files;

    /** Returns the value with key <code>inName</code> if set in the context. Returns null if not found.
     *
     * @param String inName - Key of a value
     * @return value as Object */
    @Override
    public Object get(final String inName) {
        return properties().get(inName);
    }

    /** @return java.lang.String */
    @Override
    public String getLanguage() {
        return get(LANGUAGE_KEY) == null ? VSys.dftLanguage : get(LANGUAGE_KEY).toString();
    }

    /** Returns an enumeration of the parameternames set in the context. (Parameternames from the request to the servlet)
     *
     * @return java.util.Enumeration<String> Enumeration of parameters */
    @Override
    public Enumeration<String> getParameterNames() {
        return new Vector<String>(getParameterNames2()).elements(); // NOPMD
    }

    /** Returns a Collection view of the parameter names contained in this context.
     *
     * @return Set<String> parameter names */
    @Override
    public Set<String> getParameterNames2() {
        return this.parameters().keySet();
    }

    /** Returns the parameter with key <code>inName</code> as string-value. (Parameter from the request to the servlet)
     *
     * @param inName java.lang.String Name of parameter
     * @return java.lang.String String value of a request parameter. */
    @Override
    public String getParameterValue(final String inName) {
        if (this.parameters().containsKey(inName)) {
            return this.parameters().get(inName).toString();
        }
        else {
            return "";
        }
    }

    /** Returns the file item with the specified name or <code>null</code>.
     *
     * @param inName String Name of parameter.
     * @return FileItem */
    @Override
    public FileItem getFileItem(final String inName) {
        return this.fileItems().get(inName);
    }

    /** Returns an array of parameter values if found in context. Returns an empty String array (length: 0) if no
     * parameters of the specified name are found in the context.
     *
     * @param inName java.lang.String Name of parameter
     * @return java.lang.String[] Array of Parameters */
    @Override
    public String[] getParameterValueArray(final String inName) {
        String[] outValues = null;

        if (this.parameters().containsKey(inName)) {
            if (this.parameters().get(inName) instanceof String[]) {
                outValues = (String[]) parameters().get(inName);
            }
            else {
                outValues = new String[1];
                outValues[0] = (String) parameters().get(inName);
            }
        }
        else {
            outValues = new String[0];
        }
        return outValues;
    }

    /** @return java.lang.String */
    @Override
    public String getUserID() {
        return get(USER_ID_KEY) == null ? "" : get(USER_ID_KEY).toString();
    }

    /** @return org.hip.kernel.servlet.HtmlView */
    @Override
    public HtmlView getView() {
        final Object lView = get(VIEW);
        return lView == null ? null : (HtmlView) lView;
    }

    /** @return {@link MIMEFile} */
    @Override
    public MIMEFile getMIMEFile() {
        final Object lFile = get(MIME_FILE);
        return lFile == null ? null : (MIMEFile) lFile;
    }

    /** Returns true if context contains parameter with name: <code>inParamaterName</code>
     *
     * @return boolean -true if parameter set.
     * @param inParameterName java.lang.String Name of parameter */
    @Override
    public boolean hasParameter(final String inParameterName) {
        return this.parameters().containsKey(inParameterName);
    }

    /** Returns parameters.
     *
     * @return java.util.Map Table of parameters */
    private Map<String, Object> parameters() {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        return this.params;
    }

    /** @return Map<String, FileItem> */
    private Map<String, FileItem> fileItems() {
        if (this.files == null) {
            this.files = new HashMap<>();
        }
        return this.files;
    }

    /** Returns properties.
     *
     * @return java.util.Map Table of properties */
    private Map<String, Object> properties() {
        if (this.props == null) {
            this.props = new HashMap<>();
        }
        return this.props;
    }

    /** Sets property <code>inValue</code> in context with key or name <code>inName</code>.
     *
     * @param inName java.lang.String key or name of property
     * @param inValue java.lang.Object value of property */
    @Override
    public void set(final String inName, final Object inValue) {
        if (inValue == null) {
            properties().remove(inName);
        }
        else {
            properties().put(inName, inValue);
        }
    }

    /** @param inLanguage java.lang.String */
    @Override
    public void setLanguage(final String inLanguage) {
        set(LANGUAGE_KEY, inLanguage);
    }

    /** Sets a Name-Value-List as parameters of this context.
     *
     * @param inList org.hip.kernel.util.NameValueList */
    @Override
    public void setParameters(final NameValueList inList) {
        this.parameters().clear();
        for (final NameValue lNameValue : inList.getNameValues2()) {
            parameters().put(lNameValue.getName(), lNameValue.getValue());
        }
    }

    /** Sets a Name-Value-List as file items of this context.
     *
     * @param inList {@link NameValueList} */
    @Override
    public void setFileItems(final NameValueList inList) {
        this.fileItems().clear();
        for (final NameValue lNameValue : inList.getNameValues2()) {
            fileItems().put(lNameValue.getName(), (FileItem) lNameValue.getValue());
        }
    }

    /** Sets the specified parameter with the specified value to the context.
     *
     * @param inName java.lang.String
     * @param inValue java.lang.String */
    @Override
    public void setParameter(final String inName, final String inValue) {
        VSys.assertNotNull(Assert.ERROR, this, "setParameter", inName);
        VSys.assertNotNull(Assert.ERROR, this, "setParameter", inValue);

        parameters().put(inName, inValue);
    }

    /** @param inUserID java.lang.String */
    @Override
    public void setUserID(final String inUserID) {
        set(USER_ID_KEY, inUserID);
    }

    /** @param inView org.hip.kernel.servlet.HtmlView */
    @Override
    public void setView(final HtmlView inView) {
        set(VIEW, inView);
    }

    /** @param inFile {@link MIMEFile} */
    @Override
    public void setMIMEFile(final MIMEFile inFile) {
        set(MIME_FILE, inFile);
    }

    /** Returns string with names and values of all parameters in the context.
     *
     * @return java.lang.String */
    @Override
    public String toString() {
        final StringBuilder lBuffer = new StringBuilder();
        for (final String name : properties().keySet()) {
            lBuffer.append(name).append('=').append(properties().get(name).toString()).append(", ");
        }
        return lBuffer.toString();
    }

    /** The path to the sub application in the same webapp of the servlet engine.
     *
     * @return String */
    @Override
    public String getServletPath() {
        final Object lPath = get(SERVLET_PATH);
        return lPath == null ? "" : (String) lPath;
    }

    /** Sets the path of the sub application to the context.
     *
     * @param inPath String */
    @Override
    public void setServletPath(final String inPath) {
        set(SERVLET_PATH, inPath);
    }

    /** Sets the Internet Protocol (IP) address of the client that sent the request.
     *
     * @param inRemoteAddr String containing the IP address */
    @Override
    public void setRemoteAddr(final String inRemoteAddr) {
        set(REMOTE_ADDR, inRemoteAddr);
    }

    /** Sets the fully qualified name of the client that sent the request.
     *
     * @param inRemoteHost String containing the fully qualified name */
    @Override
    public void setRemoteHost(final String inRemoteHost) {
        set(REMOTE_HOST, inRemoteHost);
    }

    /** @return String containing the IP address of the client that sent the request. */
    @Override
    public String getRemoteAddr() {
        return (String) get(REMOTE_ADDR);
    }

    /** @return String containing the fully qualified name of the client. */
    @Override
    public String getRemoteHost() {
        return (String) get(REMOTE_HOST);
    }

    /** Sets the URL the client used to make the request, i.e. <code>http://localhost:8080/vifapp/forum</code>.
     *
     * @param StringBuffer inRequestURL */
    @Override
    public void setRequestURL(final StringBuffer inRequestURL) {
        set(REQUEST_URL, inRequestURL);
    }

    /** @return StringBuffer Reconstructs the URL the client used to make the request, i.e.
     *         <code>http://localhost:8080/vifapp/forum</code>. */
    @Override
    public StringBuffer getRequestURL() {
        return (StringBuffer) get(REQUEST_URL);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        this.props.remove(SESSION_NAME);
        this.props.remove(RESPONSE_KEY);
        this.props.remove(VIEW);
        out.writeObject(this.props);
        out.writeObject(this.params);
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        this.props = (Map<String, Object>) inStream.readObject();
        this.params = (Map<String, Object>) inStream.readObject();
    }

    @Override
    public void dispose() { // NOPMD
        this.props.clear();
        this.params.clear();
        this.props = null; // NOPMD
        this.params = null; // NOPMD
    }
}
