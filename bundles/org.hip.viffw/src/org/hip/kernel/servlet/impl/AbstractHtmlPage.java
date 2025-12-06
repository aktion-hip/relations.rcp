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
package org.hip.kernel.servlet.impl; // NOPMD

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.hip.kernel.exc.VError;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.HtmlView;
import org.hip.kernel.servlet.IPage;
import org.hip.kernel.servlet.RequestException;

import jakarta.servlet.ServletOutputStream;

/** <p>
 * Baseclass of all html pages. A html page is a container of html views. You can add different html-views or pages (->
 * composite-pattern) to this container.
 * </p>
 *
 * <p>
 * The HTML representation of this page can be showed in a browser. It builds a html-body arround the
 * HTML-representations of the views added to this page.
 * </p>
 *
 * <p>
 * You can set links to Stylesheets (CSS) used in this page.
 * </p>
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
abstract public class AbstractHtmlPage extends AbstractHtmlView implements IPage { // NOPMD
    // class attributes
    protected static final String HTML_BEGIN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
    protected static final String HTML_END = "</html>";

    protected static final String HEAD_BEGIN = "<head>\n<meta http-equiv=\"content-type\" content=\"text/html;charset=utf8\" />\n";
    protected static final String HEAD_TITLE = "<title>VIF</title>\n";
    protected static final String HEAD_END = "</head>\n";

    protected static final String BODY_BEGIN_1 = "<body bgcolor='#FFFFFF' text='#505050' onLoad=";
    protected static final String BODY_BEGIN_2 = ">";
    protected static final String BODY_END = "</body>";

    // instance attributes
    private transient List<HtmlView> views;
    private transient String errorMessage = "";
    private transient String statusMessage = "";
    private transient String onLoad = "";
    private transient String htmlHead = "";
    private transient CssLinkList cssLinkList;
    private transient ScriptLinkList scriptLinkList;

    /** AbstractHtmlPage default constructor. */
    public AbstractHtmlPage() {
        this(null);
    }

    /** AbstractHtmlPage constructor with specified context.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    public AbstractHtmlPage(final Context inContext) {
        super(inContext);
    }

    /** Adds a new html-view to this page.
     *
     * @param inView org.hip.kernel.servlet.HtmlView */
    @Override
    public void add(final HtmlView inView) {
        this.getViews().add(inView);
    }

    /** Returns begin of this page as html-string. Contains stylesheet-include and onLoad-script.
     *
     * @return java.lang.String */
    protected String createBegin() {
        final StringBuilder outBegin = new StringBuilder(150).append(HTML_BEGIN);
        outBegin.append(HEAD_BEGIN).append(getTitle()).append(getHtmlHead()).append(cssLinks()).append(scriptLinks())
        .append(HEAD_END).append(BODY_BEGIN_1).append('"').append(getOnLoad()).append('"').append(BODY_BEGIN_2);
        return new String(outBegin);
    }

    /** Returns end of this page as html-string.
     *
     * @return java.lang.String
     * @param inString java.lang.String */
    protected String createEnd() {
        return BODY_END + HTML_END;
    }

    /** Returns a list of CssLink.
     *
     * @return CssLinkList */
    protected CssLinkList cssLinks() {
        if (this.cssLinkList == null) {
            this.cssLinkList = new CssLinkList();
        }
        return this.cssLinkList;
    }

    /** Returns a list of ScriptLink.
     *
     * @return ScriptLinkList */
    protected ScriptLinkList scriptLinks() {
        if (this.scriptLinkList == null) {
            this.scriptLinkList = new ScriptLinkList();
        }
        return this.scriptLinkList;
    }

    /** Returns additional html code for this page's html head.
     *
     * @return java.lang.String */
    protected String getHtmlHead() {
        return this.htmlHead;
    }

    /** Sets the style definitions for the page.
     *
     * @param inCssStyle java.lang.String
     * @deprecated Use method setHeadHtml(String) instead. */
    @Deprecated
    public void setCssStyle(final String inCssStyle) {
        this.htmlHead = inCssStyle;
    }

    /** The html code passed will be added to the <head/> part of the page's html.
     *
     * @param inHtml java.lang.String The html code to be written in the page's html. */
    public void setHeadHtml(final String inHtml) {
        this.htmlHead = inHtml;
    }

    /** Returns the title of the page.
     *
     * <pre>
     * <title>getTitle()</title>
     * </pre>
     *
     * @return java.lang.String */
    protected String getTitle() {
        return HEAD_TITLE;
    }

    /** Returns vector of views (lazy initializing).
     *
     * @return java.util.List */
    protected List<HtmlView> getViews() {
        if (this.views == null) {
            this.views = new ArrayList<HtmlView>(3);
        }
        return this.views;
    }

    /** Returns a list of CssLink.
     *
     * @return CssLinkList */
    public CssLinkList getCssLinks() {
        return cssLinks();
    }

    /** Returns a list of ScriptLink.
     *
     * @return ScriptLinkList */
    public ScriptLinkList getScriptLinks() {
        return scriptLinks();
    }

    /** Returns the error message set to this page. Returns an empty string if not set.
     *
     * @return java.lang.String */
    public String getErrorMessage() {
        return this.errorMessage.trim();
    }

    /** Returns script set for the onLoad-call in the body of this pages html representation.
     *
     * @return java.lang.String */
    public String getOnLoad() {
        return this.onLoad;
    }

    /** Returns the status message set to this page. Returns an empty string if not set.
     *
     * @return java.lang.String */
    public String getStatusMessage() {
        return this.statusMessage.trim();
    }

    private boolean hasMessages() {
        return !"".equals(getErrorMessage()) || !"".equals(getStatusMessage());
    }

    /** Add the message information to all views. It's up the the views how to handle this information.
     *
     * @return boolean true, if the messages have been included
     * @param inStatus java.lang.String
     * @param inError java.lang.String */
    private boolean includeMessagesToElements(final String inStatus, final String inError) {
        // pre: inStatus and inError not empty
        if ("".equals(inStatus + inError)) {
            return true;
        }

        boolean outCouldInclude = false;
        for (final HtmlView lView : getViews()) {
            if (lView.getTransformer() != null) {
                outCouldInclude = true;
                if (!"".equals(inStatus)) { // NOPMD
                    lView.getTransformer().includeMessage(inStatus);
                }
                if (!"".equals(inError)) { // NOPMD
                    lView.getTransformer().includeErrorMessage(inError);
                }
            }
        }
        return outCouldInclude;
    }

    /** Writes html-representation of all views in this page to the servlet's response writer.
     *
     * @param inWriter PrintWriter
     * @param inSessionID String
     * @throws RequestException */
    private void renderElementsToWriter(final PrintWriter inWriter, final String inSessionID) throws RequestException {
        for (final HtmlView lView : getViews()) {
            lView.renderToWriter(inWriter, inSessionID);
        }
    }

    /** Writes the view as html-String to the passed servlet output stream.<br/>
     * <b>Note:</b> Use this method to output bit streams.<br/>
     * To render the view with the correct encodings set, use <code>renderToWriter(PrintWriter, String)</code> instead.
     *
     * @param inStream jakarta.servlet.ServletOutputStream
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.servlet.RequestException */
    @Override
    public void renderToStream(final ServletOutputStream inStream, final String inSessionID) throws RequestException {
        try {
            renderElementsToWriter(new PrintWriter(new OutputStreamWriter(inStream, "")), inSessionID);
        } catch (final UnsupportedEncodingException exc) {
            throw new VError(
                    "Something went wrong in AbstractHtmlPage:renderToStream(), while writting the html-page to the servlet-outputstream"
                            + exc);
        }
    }

    /** Writes the view as html-String to the passed servlet's response writer.<br />
     * The error-message will be added to the XML before rendering and then cleared.<br />
     * Using the servlet's writer to stream the view, you can use
     * <code>ServletResponse.setCharacterEncoding(ENCODING)</code> to set the view's encoding correctly.
     *
     * @param inWriter PrintWriter
     * @param inSessionID String
     * @throws RequestException */
    @Override
    public void renderToWriter(final PrintWriter inWriter, final String inSessionID) throws RequestException {
        inWriter.println(createBegin());
        boolean lHandledMessages = true;
        if (hasMessages()) {
            lHandledMessages = includeMessagesToElements(getStatusMessage(), getErrorMessage());
        }

        this.renderElementsToWriter(inWriter, inSessionID);

        if (!lHandledMessages) {
            // either there are no messages or they havn't written to the output stream yet
            // (because there was no XSL to handle them) so add them to the HTML view now
            if (!"".equals(getStatusMessage())) { // NOPMD
                inWriter.println(
                        "<b><font face='Arial' color='#0000cc' size='3'>" + getStatusMessage() + "</font></b>");
            }
            if (!"".equals(getErrorMessage())) { // NOPMD
                inWriter.println("<b><font face='Arial' color='#cc0000' size='3'>" + getErrorMessage() + "</font></b>");
            }
        }
        // clear messages
        this.errorMessage = "";
        this.statusMessage = "";

        inWriter.println(createEnd());
    }

    /** Sets a CssLink for this html page. Previous set links will be overwritten.
     *
     * @param inCssLink CssLink */
    public void setCssLink(final CssLink inCssLink) {

        // pre: parameter not null
        if (inCssLink == null) {
            return;
        }
        cssLinks().addCssLink(inCssLink);
    }

    /** Sets a list of CssLink for this html page. Previous set links will be overwritten.
     *
     * @param inCssLinks CssLinkList */
    public void setCssLinks(final CssLinkList inCssLinks) {

        // pre: parameter not null
        if (inCssLinks == null) {
            return;
        }
        this.cssLinkList = inCssLinks;
    }

    /** Sets a javascript link to this html page.
     *
     * @param inScriptLink ScriptLink */
    public void setScriptLink(final ScriptLink inScriptLink) {

        // pre: parameter not null
        if (inScriptLink == null) {
            return;
        }
        scriptLinks().addScriptLink(inScriptLink);
    }

    /** Sets a list of CssLink for this html page. Previous set links will be overwritten.
     *
     * @param inScriptLinks ScriptLinkList */
    public void setScriptLinks(final ScriptLinkList inScriptLinks) {

        // pre: parameter not null
        if (inScriptLinks == null) {
            return;
        }
        this.scriptLinkList = inScriptLinks;
    }

    /** Sets ErrorMessage which will be added to the content of this page. After sending the html-representation of this
     * page to the client, including the error-message, the error-message will be cleared and has to be set again if
     * needed.
     *
     * @param inErrorMessage java.lang.String */
    @Override
    public void setErrorMessage(final String inErrorMessage) {

        // pre: parameter not null
        if (inErrorMessage == null) {
            return;
        }

        this.errorMessage = inErrorMessage;
    }

    /** Sets a command in the onLoad-tag of this pages body.
     *
     * @param inOnLoadCmd java.lang.String */
    public void setOnLoad(final String inOnLoadCmd) {

        // pre: parameter not null
        if (inOnLoadCmd == null) {
            return;
        }

        this.onLoad = inOnLoadCmd;
    }

    /** Sets status-message which will be added to the content of this page. After sending the html-representation of
     * this page to the client, including the status-message, the status-message will be cleared and has to be set again
     * if needed.
     *
     * @param inMessage java.lang.String */
    @Override
    public void setStatusMessage(final String inMessage) {

        // pre: parameter not null
        if (inMessage == null) {
            return;
        }

        this.statusMessage = inMessage;
    }

    /** Clears all status messages from the views. */
    @Override
    public void clearStatusMessage() {
        for (final HtmlView lView : getViews()) {
            if (lView.getTransformer() != null) {
                lView.getTransformer().clearMessages();
            }
        }
    }

    /** Clears all error messages from the views. */
    @Override
    public void clearErrorMessage() {
        for (final HtmlView lView : getViews()) {
            if (lView.getTransformer() != null) {
                lView.getTransformer().clearErrorMessages();
            }
        }
    }

    @Override
    public int hashCode() { // NOPMD
        final int prime = 31; // NOPMD
        int result = 1;
        result = prime * result + (this.views == null ? 0 : this.views.hashCode());
        return result;
    }

    /** Two pages are equal if all views they contain are equal. */
    @Override
    public boolean equals(final Object inObject) { // NOPMD
        if (this == inObject) {
            return true;
        }
        if (inObject == null) {
            return false;
        }
        if (getClass() != inObject.getClass()) {
            return false;
        }
        final AbstractHtmlPage lOther = (AbstractHtmlPage) inObject;
        if (this.views == null) {
            if (lOther.views != null) {
                return false;
            }
        } else {
            final List<HtmlView> lOtherViews = lOther.views;
            if (lOtherViews == null) {
                return false;
            }

            if (this.views.size() != lOtherViews.size()) {
                return false;
            }

            int i = 0; // NOPMD
            for (final HtmlView lView : this.views) {
                if (!lView.equals(lOtherViews.get(i++))) { // NOPMD
                    return false;
                }
            }
        }
        return true;
    }

}
