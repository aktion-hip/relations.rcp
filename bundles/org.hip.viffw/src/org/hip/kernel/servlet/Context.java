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
package org.hip.kernel.servlet;

import java.util.Enumeration;
import java.util.Set;

import org.hip.kernel.servlet.impl.FileItem;
import org.hip.kernel.util.NameValueList;

/** Context for parameters and properties.
 *
 * The HTTP is a stateless protocol. To keep track of state, the context holds parameters and properties persistent. It
 * uses the session-handling supported by webserver, servletengine und servlet-API.<br/>
 * <br/>
 *
 * The context holds two type of values:<br>
 *
 * <ol>
 * <li>The parameters (and file upload items) passed to the servlet in a request will be hold in the context until next
 * request.</li>
 *
 * <li>The other values set to the context are persistent and available as long as the session is valid (not new).</li>
 * </ol>
 *
 * @author Benno Luthiger */

public interface Context {
    /** Returns the value with key <code>inName</code> if set in the context. Returns null if not found.
     *
     * @param String inName - Key of a value
     * @return value as Object */
    Object get(String inName);

    /** @return java.lang.String */
    String getLanguage();

    /** Returns an enumeration of the paramternames set in the context. (Parameternames from the request to the servlet)
     *
     * @return java.util.Enumeration<String> Enumeration of parameters
     * @deprecated Use {@link Context#getParameterNames2()} instead. */
    @Deprecated
    Enumeration<String> getParameterNames();

    /** Returns a Collection view of the parameter names contained in this context.
     *
     * @return Set<String> parameter names */
    Set<String> getParameterNames2();

    /** Returns the parameter with key <code>inName</code> as string-value. (Parameter from the request to the servlet)
     *
     * @param inName java.lang.String Name of parameter
     * @return java.lang.String Stringvalue of a requestparameter. */
    String getParameterValue(String inName);

    /** Returns the file item with the specified name or <code>null</code>.
     *
     * @param inName String
     * @return {@link FileItem} */
    FileItem getFileItem(String inName);

    /** Returns Array of Parameterstrings if found in context. Returns an empty Stringarray (length: 0) if no parameters
     * are found in the context.
     *
     * @param inName java.lang.String Name of parameter
     * @return java.lang.String[] Array of Parameters */
    String[] getParameterValueArray(String inName);

    /** @return java.lang.String */
    String getUserID();

    /** @return org.hip.kernel.servlet.HtmlView */
    HtmlView getView();

    /** @return {@link MIMEFile} */
    MIMEFile getMIMEFile();

    /** Returns true if context contains parameter with name: <code>inParamaterName</code>
     *
     * @return boolean -true if parameter set.
     * @param inParameterName java.lang.String Name of parameter */
    boolean hasParameter(String inParameterName);

    /** Sets property <code>inValue</code> in context with key or name <code>inName</code>.
     *
     * @param inName java.lang.String key or name of property
     * @param inValue java.lang.Object value of property */
    void set(String inName, Object inValue);

    /** @param inLanguage java.lang.String */
    void setLanguage(String inLanguage);

    /** Sets a Name-Value-List as parameters of this context.
     *
     * @param inList org.hip.kernel.util.NameValueList */
    void setParameters(NameValueList inList);

    /** Sets a Name-Value-List as file items of this context.
     *
     * @param inList {@link NameValueList} */
    void setFileItems(NameValueList inList);

    /** Sets the specified parameter with the specified value to the context.
     *
     * @param inName String
     * @param inValue String */
    void setParameter(String inName, String inValue);

    /** @param inUserID java.lang.String */
    void setUserID(String inUserID);

    /** @param inView org.hip.kernel.servlet.HtmlView */
    void setView(HtmlView inView);

    /** @param inFile {@link MIMEFile} */
    void setMIMEFile(MIMEFile inFile);

    /** Returns string with names and values of all parameters in the context.
     *
     * @return java.lang.String */
    @Override
    String toString();

    /** The path to the sub application in the same webapp of the servlet engine.
     *
     * @return String */
    String getServletPath();

    /** Sets the path of the sub application to the context.
     *
     * @param inPath String */
    void setServletPath(String inPath);

    /** Sets the Internet Protocol (IP) address of the client that sent the request.
     *
     * @param inRemoteAddr String containing the IP address */
    void setRemoteAddr(String inRemoteAddr);

    /** Sets the fully qualified name of the client that sent the request.
     *
     * @param inRemoteHost String containing the fully qualified name */
    void setRemoteHost(String inRemoteHost);

    /** @return String containing the IP address of the client that sent the request. */
    String getRemoteAddr();

    /** @return String containing the fully qualified name of the client. */
    String getRemoteHost();

    /** Sets the URL the client used to make the request, i.e. <code>http://localhost:8080/vifapp/forum</code>.
     *
     * @param StringBuffer inRequestURL */
    void setRequestURL(StringBuffer inRequestURL);

    /** @return StringBuffer Reconstructs the URL the client used to make the request, i.e.
     *         <code>http://localhost:8080/vifapp/forum</code>. */
    StringBuffer getRequestURL();

    /** Release all resources owned by this context. */
    void dispose();
}
