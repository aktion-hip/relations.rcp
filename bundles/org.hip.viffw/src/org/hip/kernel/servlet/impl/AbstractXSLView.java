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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.impl.XMLCharacterFilter;
import org.hip.kernel.bom.impl.XMLSerializer;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.exc.VError;
import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.ISourceCreatorStrategy;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.TransformerProxy;
import org.hip.kernel.util.XMLRepresentation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** Base class of all HTML views created using XSL stylesheets.
 * <p/>
 * Basically subclasses have to implement the <code>getXSLName()</code> method, which returns the name of the XSL-file
 * located in a sub-directory of this application's webroot-directory.
 * <p/>
 *
 * The html-representation of this view will be created through a XSLT processor. The processor needs as input an XSL
 * containing the representation/definitions and a XML containing the data.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
abstract public class AbstractXSLView extends AbstractHtmlView {
    private final Map<String, Object> stylesheetParameters = new ConcurrentHashMap<String, Object>(); // NOPMD

    /** AbstractXSLView constructor with specified context.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    protected AbstractXSLView(final Context inContext) {
        super(inContext);
    }

    private String getRelativeXSLName() {
        return DOCS_ROOT + getSubAppPath() + this.getLanguage() + "/" + getXMLName();
    }

    /** Returns an instance of a document using the DocumentBuilder.
     *
     * @return org.w3c.dom.Document */
    protected Document getXSLDocument() {
        Document outDocument = null;
        try {
            final FileInputStream lStream = getXSLStream();
            outDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(lStream);
            lStream.close();
        } catch (final IOException exc) {
            throw new VError("Problems encountered with xsl file for documents!", exc);
        } catch (final SAXException exc) {
            throw new VError("Problems encountered while parsing the xsl file for documents!", exc);
        } catch (final ParserConfigurationException exc) {
            throw new VError("Problems encountered with parser configuration!", exc);
        }
        return outDocument;
    }

    /** Returns a FileInputStream which accesses the xsl-file
     *
     * @return java.io.FileInputStream */
    private FileInputStream getXSLStream() {
        FileInputStream outStream = null;
        try {
            final String lFilename = getRelativeXSLName();
            outStream = new FileInputStream(lFilename);
        } catch (final IOException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }
        return outStream;
    }

    /** Prepares the transformation of this view and sets it to this view.
     * <p/>
     * A XSLT Transformer processes the transformation out of this view's XSL and the XML Document passed as parameter.
     * The XSLT Transformer used for transformation is specified externaly in the application's properties file.
     *
     * @param inXML org.hip.kernel.util.XMLRepresentation */
    protected void prepareTransformation(final XMLRepresentation inXML) {
        setTransformer(new TransformerProxy(getSourceStrategy(), inXML, stylesheetParameters));
    }

    /** Subclasses may provide their strategies to find the XSL files.
     *
     * @return ISourceCreatorStrategy */
    protected ISourceCreatorStrategy getSourceStrategy() {
        return new FullyQualifiedNameStrategy(getRelativeXSLName());
    }

    /** Traces the XML, can be used for testing purposes.
     *
     * @param inXML org.w3c.dom.Document */
    protected void traceXML(final Document inXML) {
        try {
            final Transformer lSerializer = TransformerFactory.newInstance().newTransformer();
            lSerializer.setOutputProperty(OutputKeys.INDENT, "yes");
            lSerializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            lSerializer.transform(new DOMSource(inXML), new StreamResult(VSys.out));
        } catch (final TransformerException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }
    }

    /** Sets the parameter for the XSL stylesheet.
     *
     * @param inName String
     * @param inValue Object
     * @see Transformer#setParameter(String, Object) */
    protected void setStylesheetParameter(final String inName, final Object inValue) {
        stylesheetParameters.put(inName, inValue);
    }

    /** Returns the data of the specified domain object serialized. By default, an XML serializer is used.
     *
     * @param inDomainObject GeneralDomainObject
     * @return java.lang.String */
    protected String getSerialized(final GeneralDomainObject inDomainObject) {
        final XMLSerializer lSerializer = new XMLSerializer();
        lSerializer.setFilter(XMLCharacterFilter.DEFAULT_FILTER);
        inDomainObject.accept(lSerializer);
        return lSerializer.toString();
    }

    /** Returns the data of the specified domain object serialized using the specified serializer.
     *
     * @param inDomainObject GeneralDomainObject
     * @param inSerializerName java.lang.String Class name of the serializer to use.
     * @return java.lang.String
     * @throws BOMException */
    protected String getSerialized(final GeneralDomainObject inDomainObject, final String inSerializerName)
            throws BOMException {
        try {
            final Class<?> lClass = Class.forName(inSerializerName);
            final XMLSerializer lSerializer = (XMLSerializer) lClass.newInstance();
            lSerializer.setLocale(getLocale());
            lSerializer.setFilter(XMLCharacterFilter.DEFAULT_FILTER);
            inDomainObject.accept(lSerializer);
            return lSerializer.toString();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException exc) {
            throw new BOMException(exc);
        }
    }

}
