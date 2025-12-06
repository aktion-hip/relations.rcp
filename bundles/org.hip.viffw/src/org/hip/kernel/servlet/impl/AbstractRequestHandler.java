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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.hip.kernel.exc.DefaultErrorHandler;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.exc.DefaultRuntimeExceptionHandler;
import org.hip.kernel.exc.VError;
import org.hip.kernel.exc.VException;
import org.hip.kernel.exc.VRuntimeException;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.RequestException;
import org.hip.kernel.servlet.RequestHandler;
import org.hip.kernel.servlet.Task;
import org.hip.kernel.servlet.TaskManager;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.TransformationError;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Base class of all request handler. All servlet requests are handled from this request handler. The request
 * type-parameter is required for every request. It holds the name of the task to run. Application specific request
 * handler which subclass this base class, have to specify an taskmanager.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.servlet.RequestHandler
 * @see org.hip.kernel.servlet.TaskManager */

@SuppressWarnings("serial")
public abstract class AbstractRequestHandler extends HttpServlet implements RequestHandler {
    // constants
    public static final String ENCODING = "UTF-8";
    public final static String RESPONSE_CONTENT_TYPE = "text/html; charset=UTF-8";
    private final static String BODY_PARAMETER = "body";
    private final static int MEGA_BYTES = 1048576;

    // instance attributes
    private TaskManager taskManager;

    /** Constructor of AbstractRequestHandler. Initializes the task manager. */
    public AbstractRequestHandler() {
        super();
        initialize();
    }

    /** Checks various parameters of the request and, if successful, handles it, i.e. executes the requested task.
     *
     * @param inRequest {@link HttpServletRequest}
     * @param inResponse {@link HttpServletResponse}
     * @param inContext org.hip.kernel.servlet.Context
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException */
    private void checkAndHandleRequest(final HttpServletRequest inRequest, final HttpServletResponse inResponse,
            final Context inContext) throws ServletException, IOException, RequestException {
        try {
            final ServletRequestHelper lHelper = new ServletRequestHelper(inRequest);

            // check if the requestType is defined, if not, we run the default task
            final String lRequestType = lHelper.containsParameter(AbstractContext.REQUEST_TYPE) ? lHelper
                    .getParameterValue(AbstractContext.REQUEST_TYPE) : getDefaultRequestType();

            inContext.set(AbstractContext.REQUEST_TYPE, lRequestType);
            lHelper.addParametersToContext(inContext, AbstractContext.REQUEST_TYPE);
            if (inContext.getParameterValue(BODY_PARAMETER).length() == 0) {
                setStartupRequestType(inContext);
            }

            if ("memoryInfo".equals(lRequestType)) {
                showMemoryInfo(inContext);
                return;
            }

            if (requestTypeCheck(lRequestType)) {
                subRequestTypeHandler(inContext, inResponse);
                return;
            }

            // Set the servlet path and the requested URL to the context.
            inContext.setServletPath(inRequest.getServletPath());
            inContext.setRequestURL(inRequest.getRequestURL());

            // Set the clients host and address to the context
            String lRemoteInfo = inRequest.getRemoteAddr();
            inContext.setRemoteAddr(lRemoteInfo == null ? "N.N." : lRemoteInfo);
            lRemoteInfo = inRequest.getRemoteHost();
            inContext.setRemoteHost(lRemoteInfo == null ? "N.N." : lRemoteInfo);

            // /////////Create task for requestType an run task///////////////
            final Task lTask = this.taskManager.create(lRequestType);
            lTask.setContext(inContext);
            lTask.run();
        } catch (final Exception exc) { // NOPMD
            this.handle(getContext(inRequest), exc);
        }
    }

    /** We set the default task to be displayed in the body frame if nothing is specified. Subclasses may override and
     * provide a different request type for the startup.
     *
     * @param inContext Context */
    protected void setStartupRequestType(final Context inContext) {
        inContext.setParameter(BODY_PARAMETER, AbstractTaskManager.DEFAULT_TASK_NAME);
    }

    /** By default, we run the master task and set the default task to be displayed in the body frame. Subclasses may
     * override and provide a different request type or throw a <code>RequestException</code>.
     *
     * @param inContext Context
     * @return String the default request type.
     * @throws RequestException */
    protected String getDefaultRequestType() throws RequestException {
        return AbstractTaskManager.MASTER_TASK_NAME;
    }

    /** Handles the request and creates the appropriate response.
     *
     * @param inRequest javax.servlet.http.HttpServletRequest
     * @param inResponse javax.servlet.http.HttpServletResponse
     * @exception javax.servlet.ServletException
     * @exception java.io.IOException */
    @Override
    public void doGet(final HttpServletRequest inRequest, final HttpServletResponse inResponse)
            throws ServletException, IOException {

        final Context lContext = this.getContext(inRequest);
        lContext.set(AbstractContext.RESPONSE_KEY, inResponse);

        try {
            if (inRequest.getCharacterEncoding() == null) {
                inRequest.setCharacterEncoding(ENCODING);
            }
            inResponse.setCharacterEncoding(ENCODING);

            // authorisation check and process the request
            checkAndHandleRequest(inRequest, inResponse, lContext);

            // send the view to the client
            writeToResponse(inResponse, lContext, getSessionID(inRequest, lContext));
        } catch (final RequestException exc) {
            this.error(inRequest, inResponse, exc.getMessage());
        } catch (final TransformationError exc) {
            this.error(inRequest, inResponse, exc.getMessage());
        }
    }

    /** Subclasses may override
     *
     * @param inResponse HttpServletResponse
     * @param inContext
     * @throws IOException
     * @throws RequestException */
    protected void writeToResponse(final HttpServletResponse inResponse, final Context inContext,
            final String inSessionID) throws RequestException, IOException {
        final HtmlView lView = inContext.getView();
        inResponse.setContentType(RESPONSE_CONTENT_TYPE);
        lView.renderToWriter(inResponse.getWriter(), inSessionID);
    }

    /** Handles the request of non-standard MIME type.
     *
     * @param inRequest javax.servlet.http.HttpServletRequest
     * @param inResponse javax.servlet.http.HttpServletResponse
     * @exception javax.servlet.ServletException
     * @exception java.io.IOException */
    public void doGetMIME(final HttpServletRequest inRequest, final HttpServletResponse inResponse)
            throws ServletException, IOException {
        try {
            checkAndHandleRequest(inRequest, inResponse, getContext(inRequest));
        } catch (final RequestException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }
    }

    /** Calls doGet() to handle the request.
     *
     * @param inRequest javax.servlet.http.HttpServletRequest
     * @param inResponse javax.servlet.http.HttpServletResponse
     * @exception javax.servlet.ServletException
     * @exception java.io.IOException
     * @see org.hip.kernel.servlet.AbstractRequestHandler#doGet */
    @Override
    public void doPost(final HttpServletRequest inRequest, final HttpServletResponse inResponse)
            throws ServletException, IOException {
        this.doGet(inRequest, inResponse);
    }

    /** Overrides the super class's init() method. Used to set application specific parameters to VSys.
     *
     * @see javax.servlet.GenericServlet */
    @Override
    public void init() throws ServletException {
        super.init();
        ServletContainer.getInstance().setServerInfo(getServletContext().getServerInfo());
        VSys.setContextPath(getServletContext().getRealPath("/"));
        VSys.setSysName(getSysName());
    }

    /** Subclasses have to provide the name of the application's properties file, e.g. <code>vif</code> for
     * <code>vif.properties</code>
     *
     * @return java.lang.String */
    protected abstract String getSysName();

    /** This method starts an ErrorTask to send an error message to the client. The parameter inMessage will be displayed
     * in this view.
     *
     * @param inResponse javax.servlet.http.HttpServletResponse
     * @param inMessage java.lang.String */
    public void error(final HttpServletRequest inRequest, final HttpServletResponse inResponse, final String inMessage) {
        try {
            final Task lTask = this.taskManager.getErrorTask();
            final Context lContext = this.getContext(inRequest);
            lContext.set(AbstractContext.ERROR_MESSAGE_KEY, inMessage);

            lTask.setContext(lContext);
            lTask.run();

            // send the view to the client
            try {
                inResponse.reset();
            } catch (final IllegalStateException exc) {
                DefaultExceptionWriter.printOut(this, new VException(inMessage), true);
            }
            final HtmlView lView = (HtmlView) lContext.get(AbstractContext.VIEW);
            lView.renderToWriter(inResponse.getWriter(), getSessionID(inRequest, lContext));
        } catch (final IOException | VException exc) {
            this.handle(getContext(inRequest), exc);
        }
    }

    /** Returns the context object out of the actual session. If the session is just created a new context object will be
     * created and added to the session object.
     *
     * @return org.hip.kernel.servlets.Context */
    protected Context getContext(final HttpServletRequest inRequest) {
        final HttpSession lSession = inRequest.getSession(true);
        Context outContext = null;
        final boolean lNewSession = lSession.isNew();

        if (lNewSession && lSession.getAttribute(AbstractContext.CONTEXT) == null) {
            outContext = this.loadContextObject();
            lSession.setAttribute(AbstractContext.CONTEXT, outContext);
        }
        else {
            outContext = (Context) lSession.getAttribute(AbstractContext.CONTEXT);
        }

        // The session object will be set to the context
        outContext.set(AbstractContext.SESSION_NAME, lSession);
        // Set flag in context if session is new or not
        outContext.set(AbstractContext.NEW_SESSION_KEY, Boolean.valueOf(lNewSession));

        return outContext;
    }

    /** Subclasses, which implement this method, return the name of the specialized application-context.
     *
     * @return java.lang.String */
    protected abstract String getContextClassName();

    /** Subclasses which implement this method, have to return an instance of the applications specific TaskManager.
     *
     * @return an instance of the taskmanager
     * @see org.hip.kernel.servlet.TaskManager */
    protected abstract TaskManager getTaskManager();

    /** Handling of errors and exceptions.
     *
     * @param inContext org.hip.kernel.servlet.Context
     * @param inThrowable java.lang.Throwable */
    protected void handle(final Context inContext, final Throwable inThrowable) {
        if (inThrowable instanceof VException) {
            DefaultExceptionHandler.instance().handle(inThrowable);
        }
        else if (inThrowable instanceof VRuntimeException) {
            DefaultRuntimeExceptionHandler.instance().handle(inThrowable);
        }
        else if (inThrowable instanceof VError) {
            DefaultErrorHandler.instance().handle(inThrowable);
        }
        else {
            DefaultErrorHandler.instance().handle(inThrowable);
        }
    }

    /** Sets the application specific taskmanager. */
    private void initialize() {
        this.taskManager = this.getTaskManager();
    }

    /** Returns a new instance of the application specific context.
     *
     * @return org.hip.kernel.servlets.Context */
    private Context loadContextObject() {
        Context outContext = null;

        try {
            // create a context object through reflection
            final String lContextClassName = this.getContextClassName();
            final Class<?> lContextClass = Class.forName(lContextClassName);
            outContext = (Context) lContextClass.getDeclaredConstructor().newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            outContext = new DefaultContextImpl();
        }

        return outContext;
    }

    /** @param inResponse javax.servlet.http.HttpServletResponse */
    private void showMemoryInfo(final Context inContext) {
        final HtmlView lView = new AbstractHtmlView() {
            @Override
            public void renderToStream(final ServletOutputStream inStream, final String inSessionID) { // NOPMD
                try {
                    renderToWriter(new PrintWriter(new OutputStreamWriter(inStream, "")), inSessionID);
                }
                catch (final IOException | RequestException exc) {
                    DefaultExceptionWriter.printOut(this, exc, true);
                }
            }

            @Override
            public void renderToWriter(final PrintWriter inWriter, final String inSessionID) throws RequestException { // NOPMD
                inWriter.println("<h2>Memory</h2>");
                inWriter.println("<b>Total memory:</b> " + Runtime.getRuntime().totalMemory() / MEGA_BYTES
                        + " MB <br/>");
                inWriter.println("<b>Free memory:</b> " + Runtime.getRuntime().freeMemory() / MEGA_BYTES + " MB");
            }

            @Override
            protected String getXMLName() { // NOPMD
                return null;
            }
        };

        final AbstractHtmlPage lPage = new DefaultHtmlPage();
        lPage.setHeadHtml("<style type='text/css'><!-- body {font-family: Verdana, Arial, geneva, helvetica, sans-serif;} //--></style>\n");
        lPage.add(lView);

        inContext.set(AbstractContext.VIEW, lPage);
    }

    /** Hook for subclasses. This method can be overwritten in subclasses to check for request types which have to be
     * processed in a special way, i.e. by an implementation of subRequestTypeHandler()
     *
     * @param inRequestType java.lang.String
     * @return boolean true, if subRequestTypeHandler() has to be processed. */
    protected boolean requestTypeCheck(final String inRequestType) {
        return false;
    }

    /** Hook for subclasses. This method can be overwritten in subclasses to process a request type in a special way.
     *
     * @param inContext org.hip.kernel.servlet.Context
     * @param inResponse javax.servlet.http.HttpServletResponse
     * @throws java.lang.Exception */
    protected void subRequestTypeHandler(final Context inContext, final HttpServletResponse inResponse) // NOPMD
            throws Exception { // NOPMD
        // hook for subclasses
    }

    /** Gets session ID to allow cookieless session tracking, i.e. by URL rewriting.
     *
     * @param inRequest HttpServletRequest
     * @param inContext Context
     * @return String */
    private String getSessionID(final HttpServletRequest inRequest, final Context inContext) {
        String outSessionID = "";
        if (!inRequest.isRequestedSessionIdFromCookie()) {
            final HttpSession lSession = (HttpSession) inContext.get(AbstractContext.SESSION_NAME);
            outSessionID = lSession.getId();
        }
        return outSessionID;
    }
}
