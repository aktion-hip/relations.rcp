/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

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
package org.hip.kernel.bom.model.impl; // NOPMD by lbenno

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.IMappingDefCreator;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.bom.model.KeyDefDef;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.ModelObject;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.RelationshipDef;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** This class is used to generate ObjectDefs
 *
 * @author Benno Luthiger */
public class ObjectDefGenerator extends DefaultHandler {

    // Class variables
    private static ObjectDefGenerator singleton;

    // Instance variables
    private transient ObjectDef objectDef;
    private transient KeyDef currentKeyDef;
    private transient PropertyDef currentPropertyDef;
    private transient XMLReader xmpParser;
    private transient int keyItemIndex;

    private transient IMappingDefCreator mappingDefCreator = new DefaultMappingDefCreator();

    /** ObjectDefGenerator default constructor. */
    private ObjectDefGenerator() {
        super();
    }

    /** Creates a ObjectDef from a definition string.
     *
     * @param inDefinitionString java.lang.String
     * @return org.hip.kernel.bom.model.ObjectDef
     * @throws SAXException */
    public ObjectDef createObjectDef(final String inDefinitionString) throws SAXException {
        try {
            final StringReader lReader = new StringReader(inDefinitionString);
            final InputSource lInputSource = new InputSource(lReader);
            parser().parse(lInputSource);
        } catch (final IOException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }
        return objectDef;
    }

    /** Creates a ObjectDef from a definition string using the specified <code>IMappingDefCreator</code>.
     *
     * @param inDefinitionString String
     * @param inMappingDefCreator IMappingDefCreator
     * @return ObjectDef
     * @throws SAXException */
    public ObjectDef createObjectDef(final String inDefinitionString, final IMappingDefCreator inMappingDefCreator)
            throws SAXException {
        final IMappingDefCreator lOldCreator = mappingDefCreator;
        if (inMappingDefCreator != null) {
            mappingDefCreator = inMappingDefCreator;
        }
        try {
            createObjectDef(inDefinitionString);
        } finally {
            mappingDefCreator = lOldCreator;
        }
        return objectDef;
    }

    // ---

    /** Handles the end of the <keyDef> tag.
     *
     * @param inName java.lang.String */
    private void end_keyDef(final String inName) { // NOPMD
    }

    /** Handles the end of the </keyItemDef> tag.
     *
     * @param name java.lang.String */
    private void end_keyItemDef(final String inName) { // NOPMD
        this.keyItemIndex = 0;
    }

    /** Handles the end of the <mappingDef> tag.
     *
     * @param inName java.lang.String */
    private void end_mappingDef(final String inName) { // NOPMD
    }

    /** Handles the end of the <objectDef> tag.
     *
     * @param inName java.lang.String */
    private void end_objectDef(final String inName) { // NOPMD
    }

    /** Handles the end of the <propertyDef> tag.
     *
     * @param inName java.lang.String */
    private void end_propertyDef(final String inName) { // NOPMD
        currentPropertyDef = null; // NOPMD by lbenno
    }

    /** @exception org.xml.sax.SAXException */
    @Override
    public void endDocument() throws SAXException { // NOPMD
    }

    /** Receive notification of the end of an element.
     *
     * @param inUri java.lang.String The Namespace URI.
     * @param inLocalName java.lang.String The local name.
     * @param inRawName java.lang.String The qualified (prefixed) name.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement */
    @Override
    public void endElement(final String inUri, final String inLocalName, String inRawName) throws SAXException {

        if (inRawName != null) {
            // make sure the string is internalized to compare references
            inRawName = inRawName.intern();
        }

        // tag <objectDef>
        if (inRawName == ModelObject.objectDef) {
            end_objectDef(inRawName);
            return;
        }
        // tag <propertyDef>
        if (inRawName == ModelObject.propertyDef) {
            end_propertyDef(inRawName);
            return;
        }
        // tag <mappingDef>
        if (inRawName == ModelObject.mappingDef) {
            end_mappingDef(inRawName);
            return;
        }
        // tag <keyDef>
        if (inRawName == ModelObject.keyDef) {
            end_keyDef(inRawName);
            return;
        }
        // tag <keyItemDef>
        if (inRawName == ModelObject.keyItemDef) {
            end_keyItemDef(inRawName);
            return;
        }
    }

    /** @return org.hip.kernel.bom.model.impl.ObjectDefGenerator */
    public synchronized static ObjectDefGenerator getSingleton() { // NOPMD by lbenno
        if (singleton == null) {
            singleton = new ObjectDefGenerator();
        }

        return singleton;
    }

    /** @return org.xml.sax.XMLReader */
    private XMLReader parser() {

        if (xmpParser == null) {
            try {
                xmpParser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                xmpParser.setContentHandler(this);
                xmpParser.setErrorHandler(this);
            } catch (final SAXException | ParserConfigurationException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }

        }
        return xmpParser;
    }

    /** This method handles the keyDef tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_keyDef(final String inName, final Attributes inAttributes) { // NOPMD
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_keyDef", objectDef) == Assert.FAILURE) {
            return;
        }

        final String lType = (inAttributes == null) ? null : inAttributes.getValue(KeyDefDef.keyType); // NOPMD

        if (lType == KeyDef.TYPE_PRIMARY_KEY || lType == null) {
            currentKeyDef = new PrimaryKeyDefImpl();
        }

        ((ObjectDefImpl) objectDef).setPrimaryKeyDef(currentKeyDef);
    }

    /** This method handles the keyItemDef tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_keyItemDef(final String inName, final Attributes inAttributes) { // NOPMD
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_keyItemDef", currentKeyDef) == Assert.FAILURE) {
            return;
        }
        this.currentKeyDef.addKeyNameAt(inAttributes.getValue(KeyDef.keyPropertyName).intern(), this.keyItemIndex++);
    }

    /** This method handles the mappingDef tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_mappingDef(final String inName, final Attributes inAttributes) { // NOPMD

        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_mappingDef", currentPropertyDef) == Assert.FAILURE) {
            return;
        }

        // Create new Mapping
        final MappingDef lMappingDef = mappingDefCreator.createMappingDef();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                lMappingDef.set(inAttributes.getQName(i), inAttributes.getValue(i));
            } catch (final SettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        } // for
        currentPropertyDef.setMappingDef(lMappingDef);
    }

    /** Handles the start of the <objectDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    synchronized private void start_objectDef(final String inName, final Attributes inAttributes) { // NOPMD

        this.objectDef = new ObjectDefImpl();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                objectDef.set(inAttributes.getQName(i), inAttributes.getValue(i).intern());
            } catch (final SettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
    }

    /** Handles the start of the <propertyDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_propertyDef(final String inName, final Attributes inAttributes) { // NOPMD

        currentPropertyDef = new PropertyDefImpl();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                currentPropertyDef.set(inAttributes.getQName(i), inAttributes.getValue(i).intern());
            } catch (final SettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
        objectDef.addPropertyDef(currentPropertyDef);
    }

    /** This method handles the <relationshipDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_relationshipDef(final String inName, final Attributes inAttributes) { // NOPMD

        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_relationshipDef", currentPropertyDef) == Assert.FAILURE) {
            return;
        }

        // Create new Mapping
        final RelationshipDef lRelationshipDef = new RelationshipDefImpl();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                lRelationshipDef.set(inAttributes.getQName(i), inAttributes.getValue(i).intern());
            } catch (final SettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }

        } // for
        currentPropertyDef.setRelationshipDef(lRelationshipDef);
    }

    /** @exception org.xml.sax.SAXException */
    @Override
    public void startDocument() throws SAXException { // NOPMD
    }

    /** Receive notification of the start of an element.
     *
     * @param inUri java.lang.String The Namespace URI.
     * @param inLocalName java.lang.String The local name.
     * @param inRawName java.lang.String The qualified (prefixed) name.
     * @param inAttributes org.xml.sax.Attributes The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement */
    @Override
    public void startElement(final String inUri, final String inLocalName, String inRawName,
            final Attributes inAttributes) throws SAXException {

        // make sure the string is internalized to compare references
        if (inRawName != null) {
            inRawName = inRawName.intern();
        }

        // tag <objectDef>
        if (inRawName == ModelObject.objectDef) {
            start_objectDef(inRawName, inAttributes);
            return;
        }
        // tag <propertyDef>
        if (inRawName == ModelObject.propertyDef) {
            start_propertyDef(inRawName, inAttributes);
            return;
        }
        // tag <mappingDef>
        if (inRawName == ModelObject.mappingDef) {
            start_mappingDef(inRawName, inAttributes);
            return;
        }
        // tag <keyDef>
        if (inRawName == ModelObject.keyDef) {
            start_keyDef(inRawName, inAttributes);
            return;
        }
        // tag <keyItemDef>
        if (inRawName == ModelObject.keyItemDef) {
            start_keyItemDef(inRawName, inAttributes);
            return;
        }
        // tag <relationshipDef>
        if (inRawName == ModelObject.relationshipDef) {
            start_relationshipDef(inRawName, inAttributes);
            return;
        }
    }

    // ---

    private class DefaultMappingDefCreator implements IMappingDefCreator { // NOPMD by lbenno
        @Override
        public MappingDef createMappingDef() { // NOPMD by lbenno
            return new MappingDefImpl();
        }
    }

}
