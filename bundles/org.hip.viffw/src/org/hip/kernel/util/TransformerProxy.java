/**
 This package is part of the framework used for the application VIF.
 Copyright (C) 2002-2014, Benno Luthiger

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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hip.kernel.exc.VError;
import org.hip.kernel.servlet.ISourceCreatorStrategy;
import org.hip.kernel.servlet.impl.FullyQualifiedNameStrategy;
import org.hip.kernel.sys.VObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Proxy object for xsl transformation. An instance of this class holds the information needed to process a
 * transformation.
 *
 * Created on 25.10.2002
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class TransformerProxy extends VObject implements Serializable {
    // constants
    private static final String WARNINGS_ELEMENT = "warnings";
    private static final String WARNING_ELEMENT = "warning";
    private static final String MESSAGES_ELEMENT = "messages";
    private static final String MESSAGE_ELEMENT = "message";
    private static final String SESSION_ID = "global.sessionID";

    // Cache for XSL templates.
    protected static Map<String, Templates> cTemplates = new ConcurrentHashMap<String, Templates>(199); // NOPMD

    protected XMLRepresentation xmlToTransform;
    protected String xslFileName;
    protected Map<String, Object> stylesheetParameters;
    protected ISourceCreatorStrategy sourceStrategy;

    /** Constructor for TransformerProxy.
     *
     * @param inXSLName String Name of XSLT file.
     * @param inXML XMLRepresentation XML to transform
     * @param inStylesheetParameters Map<String, Object> the parameters for the XSLT stylesheet, may be
     *            <code>null</code>. */
    public TransformerProxy(final String inXSLName, final XMLRepresentation inXML,
            final Map<String, Object> inStylesheetParameters) {
        this(new FullyQualifiedNameStrategy(inXSLName), inXML, inStylesheetParameters);
        xslFileName = inXSLName;
    }

    /** Constructor for TransformerProxy.
     *
     * @param inStrategy {@link ISourceCreatorStrategy}
     * @param inXML {@link XMLRepresentation} XML to transform
     * @param inStylesheetParameters Map&lt;String, Object> */
    public TransformerProxy(final ISourceCreatorStrategy inStrategy, final XMLRepresentation inXML,
            final Map<String, Object> inStylesheetParameters) {
        super();
        sourceStrategy = inStrategy;
        xmlToTransform = inXML;
        stylesheetParameters = inStylesheetParameters;
    }

    /** Synchronized access to the template cache.
     *
     * @return Transformer
     * @throws XSLProcessingException */
    private Transformer getTransformer() throws XSLProcessingException {
        // pre: stylesheet has to be defined
        if (sourceStrategy == null || "".equals(sourceStrategy.getResourceId())) { // NOPMD by lbenno
            throw new VError("Missing stylesheet for transformation.");
        }

        synchronized (this) {
            Transformer outTransformer;
            Templates lTemplates;
            final String lResourceId = sourceStrategy.getResourceId();
            try {
                lTemplates = cTemplates.get(lResourceId);
                if (lTemplates == null) {
                    lTemplates = TransformerFactory.newInstance().newTemplates(sourceStrategy.createSource());
                    cTemplates.put(lResourceId, lTemplates);
                }
                outTransformer = lTemplates.newTransformer();
            } catch (final TransformerConfigurationException exc) {
                throw new XSLProcessingException("Configuration exception while precompiling the XSL file "
                        + exc.toString(), exc);
            } catch (final IOException exc) {
                throw new XSLProcessingException("Configuration exception while precompiling the XSL file "
                        + exc.toString(), exc);
            }
            return outTransformer;
        }
    }

    /** Performs the transformation to the specified output stream.<br/>
     * <b>Note:</b> Use this method to output bit streams.<br/>
     * To render a view with the correct encodings set, use <code>renderToWriter(PrintWriter, String)</code> instead.
     *
     * @param java.io.OutputStream
     * @param inSessionID java.lang.String
     * @throws org.hip.kernel.util.XSLProcessingException */
    public void renderToStream(final OutputStream inStream, final String inSessionID) throws XSLProcessingException {
        final Source lXMLSource = new DOMSource(xmlToTransform.reveal());
        final StreamResult lResult = new StreamResult(inStream);
        try {
            final Transformer lTransformer = getTransformer();
            if (inSessionID.length() != 0) {
                lTransformer.setParameter(SESSION_ID, ";jsessionid=" + inSessionID);
            }
            setStylesheetParameters(lTransformer, stylesheetParameters);
            lTransformer.transform(lXMLSource, lResult);
        } catch (final TransformerException exc) {
            throw new XSLProcessingException("Transformer exception while performing the XSL transformation "
                    + exc.toString(), exc);
        }
    }

    /** Performs the transformation to the specified print writer.
     *
     * @param inWriter PrintWriter
     * @param inSessionID String
     * @throws XSLProcessingException */
    public void renderToWriter(final PrintWriter inWriter, final String inSessionID) throws XSLProcessingException {
        final Source lXMLSource = new DOMSource(xmlToTransform.reveal());
        final StreamResult lResult = new StreamResult(inWriter);
        try {
            final Transformer lTransformer = getTransformer();
            if (inSessionID.length() != 0) {
                lTransformer.setParameter(SESSION_ID, ";jsessionid=" + inSessionID);
            }
            setStylesheetParameters(lTransformer, stylesheetParameters);
            lTransformer.transform(lXMLSource, lResult);
        } catch (final TransformerException exc) {
            throw new XSLProcessingException("Transformer exception while performing the XSL transformation "
                    + exc.toString(), exc);
        }
    }

    /** Includes an error message to the XML. The message is included in the form <warnings>
     * <warning>inErrorMessage</warning> </warnings>
     *
     * @param inErrorMessage java.lang.String */
    public void includeErrorMessage(final String inErrorMessage) {
        includeInXML(WARNINGS_ELEMENT, WARNING_ELEMENT, inErrorMessage);
    }

    /** Includes a message to the XML.
     *
     * @param inGroupingTag java.lang.String
     * @param inElementTag java.lang.String
     * @param inMessage java.lang.String */
    private void includeInXML(final String inGroupingTag, final String inElementTag, final String inMessage) {
        final Document lXML = xmlToTransform.reveal();

        Node lWarnings = null;
        final NodeList lNodeList = lXML.getElementsByTagName(inGroupingTag);
        if (lNodeList.getLength() > 0) {
            lWarnings = lNodeList.item(0);
        }
        else {
            lWarnings = lXML.createElement(inGroupingTag);
            lXML.getDocumentElement().appendChild(lWarnings);
        }
        final Element lWarning = lXML.createElement(inElementTag);
        lWarning.appendChild(lXML.createTextNode(inMessage));
        lWarnings.appendChild(lWarning);

        xmlToTransform = new XMLRepresentation(lXML);
    }

    /** Includes a message to the XML. The message is included in the form <messages> <message>inMessage</message>
     * </messages>
     *
     * @param inMessage java.lang.String */
    public void includeMessage(final String inMessage) {
        includeInXML(MESSAGES_ELEMENT, MESSAGE_ELEMENT, inMessage);
    }

    /** Removes the messages from the XML. */
    public void clearMessages() {
        clearFromXML(MESSAGES_ELEMENT);
    }

    /** Removes the error messages from the XML. */
    public void clearErrorMessages() {
        clearFromXML(WARNINGS_ELEMENT);
    }

    /** Removes the node with the specified tag from the XML.
     *
     * @param inGroupingTag java.lang.String */
    private void clearFromXML(final String inGroupingTag) {
        final Document lXML = xmlToTransform.reveal();
        final NodeList lNodeList = lXML.getElementsByTagName(inGroupingTag);
        if (lNodeList.getLength() > 0) {
            final Node lMessages = lNodeList.item(0);
            lXML.getDocumentElement().removeChild(lMessages);
            xmlToTransform = new XMLRepresentation(lXML);
        }
    }

    /** Sets the stylesheet parameters to the specified <code>Transformer</code>.
     *
     * @param inTransformer Transformer
     * @param inStylesheetParameters HashMap<String, Object> */
    protected void setStylesheetParameters(final Transformer inTransformer,
            final Map<String, Object> inStylesheetParameters) {
        if (inStylesheetParameters == null) {
            return;
        }

        for (final String lKey : inStylesheetParameters.keySet()) {
            inTransformer.setParameter(lKey, inStylesheetParameters.get(lKey));
        }
    }

}
