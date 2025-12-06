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
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.ColumnDefDef;
import org.hip.kernel.bom.model.GroupingDef;
import org.hip.kernel.bom.model.GroupingDefDef;
import org.hip.kernel.bom.model.JoinDef;
import org.hip.kernel.bom.model.JoinDefDef;
import org.hip.kernel.bom.model.JoinedObjectDef;
import org.hip.kernel.bom.model.ModelObject;
import org.hip.kernel.bom.model.NestedDef;
import org.hip.kernel.bom.model.NestedDefDef;
import org.hip.kernel.bom.model.ObjectDescDef;
import org.hip.kernel.bom.model.PlaceholderDefDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.DefaultNameValueList;
import org.hip.kernel.util.NameValueList;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** SAX-Parser for JoinedObjectDef
 *
 * @author Benno Luthiger */
public class JoinedObjectDefGenerator extends DefaultHandler { // NOPMD by lbenno

    // Class variables
    private static JoinedObjectDefGenerator singleton;

    // Instance variables
    private transient XMLReader xmlParser;
    private transient JoinedObjectDef objectDef;
    private transient JoinDef currentJoinDef;
    private transient ColumnDefState columnDefState;
    private transient NestedDef nestedDef;
    private transient GroupingDef groupingDef;

    /** Inner class to track the states of columnDef. */
    private class ColumnDefState {
        public final static int COLUMN_DEF = 1;
        public final static int JOIN_CONDITION = 2;
        public final static int OBJECT_NESTED = 3;
        public final static int RESULT_GROUPING = 4;

        private transient final Stack<Integer> states;

        /** ColumnDefState default constructor. */
        public ColumnDefState() {
            super();
            states = new Stack<Integer>();
        }

        /** Sets <code>to column defs</code> */
        public void setToColumnDefs() {
            states.push(Integer.valueOf(COLUMN_DEF));
        }

        /** Sets <code>to join condition</code> */
        public void setToJoinCondition() { // NOPMD by lbenno
            states.push(Integer.valueOf(JOIN_CONDITION));
        }

        /** Sets <code>to object nested</code> */
        public void setToObjectNested() {
            states.push(Integer.valueOf(OBJECT_NESTED));
        }

        /** Sets <code>to result grouping</code> */
        public void setToResultGrouping() {
            states.push(Integer.valueOf(RESULT_GROUPING));
        }

        /** Pops the topmost state. */
        public void pop() {
            states.pop();
        }

        /** @return int get actual state. */
        public int getState() {
            if (states.empty()) {
                return 0;
            }
            return states.peek().intValue();
        }
    }

    /** JoinedObjectDefGenerator default constructor. */
    private JoinedObjectDefGenerator() {
        super();
    }

    /** Creates a JoinedObjectDef from a definition string.
     *
     * @return org.hip.kernel.bom.model.JoinedObjectDef
     * @param inDefinitionString java.lang.String */
    public JoinedObjectDef createJoinedObjectDef(final String inDefinitionString) throws SAXException {

        try {
            final StringReader lReader = new StringReader(inDefinitionString);
            final InputSource lInputSource = new InputSource(lReader);
            parser().parse(lInputSource);
        } catch (final IOException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }

        return objectDef;
    }

    /** @return org.hip.kernel.bom.model.impl.JoinedObjectDefGenerator */
    public synchronized static JoinedObjectDefGenerator getSingleton() { // NOPMD by lbenno 
        if (singleton == null) {
            singleton = new JoinedObjectDefGenerator();
        }

        return singleton;
    }

    /** @return org.xml.sax.XMLReader */
    private XMLReader parser() {

        if (xmlParser == null) {
            try {
                xmlParser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                xmlParser.setContentHandler(this);
                xmlParser.setErrorHandler(this);
            } catch (final SAXException | ParserConfigurationException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
        return xmlParser;
    }

    /** Handles the end of the <columnDef> tag.
     *
     * @param inName java.lang.String */
    private void end_columnDef(final String inName) { // NOPMD by lbenno
    }

    /** Handles the end of the <hidden> tag.
     *
     * @param inName java.lang.String */
    private void end_hidden(final String inName) { // NOPMD by lbenno
    }

    /** Handles the end of the <joinCondition> tag.
     *
     * @param inName java.lang.String */
    private void end_joinCondition(final String inName) { // NOPMD by lbenno
        columnDefState.pop();
    }

    /** Handles the end of the <objectNested> tag.
     *
     * @param inName java.lang.String */
    private void end_objectNested(final String inName) { // NOPMD by lbenno
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "end_objectNested", currentJoinDef) == Assert.FAILURE) {
            return;
        }
        if (VSys.assertNotNull(this, "end_objectNested", nestedDef) == Assert.FAILURE) {
            return;
        }

        currentJoinDef.setNestedQuery(nestedDef.getNestedQuery());
        columnDefState.pop();
    }

    /** Handles the end of the <resultGrouping> tag.
     *
     * @param inName java.lang.String */
    private void end_resultGrouping(final String inName) { // NOPMD
        columnDefState.pop();
    }

    /** Handles the end of the <objectPlaceholder> tag.
     *
     * @param inName java.lang.String */
    private void end_objectPlaceholder(final String inName) { // NOPMD by lbenno
    }

    /** Handles the end of the <joinDef> tag.
     *
     * @param inName java.lang.String */
    private void end_joinDef(final String inName) { // NOPMD by lbenno
        // Pre
        if (VSys.assertNotNull(this, "end_joinDef", currentJoinDef) == Assert.FAILURE) {
            return;
        }

        final JoinDef lJoinDef = currentJoinDef.getParentJoinDef();
        if (lJoinDef != null) {
            lJoinDef.setChildJoinDef(currentJoinDef); // set current as child in parent
            currentJoinDef.setParentJoinDef(null); // set parent of current to null
        }
        currentJoinDef = lJoinDef;
    }

    /** Handles the end of the <joinedObjectDef> tag.
     *
     * @param inName java.lang.String */
    private void end_objectDef(final String inName) { // NOPMD
    }

    /** Handles the end of the <objectDesc> tag.
     *
     * @param inName java.lang.String */
    private void end_objectDesc(final String inName) { // NOPMD
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
    public void endElement(final String inUri, final String inLocalName, String inRawName) throws SAXException { // NOPMD
        // by
        // lbenno

        if (inRawName != null) {
            // make sure the string is internalized to compare references
            inRawName = inRawName.intern();
        }

        // tag <joinedObjectDef>
        if (inRawName == ModelObject.joinedObjectDef) {
            end_objectDef(inRawName);
            return;
        }
        // tag <columnDef>
        if (inRawName == ModelObject.columnDef) {
            end_columnDef(inRawName);
            return;
        }
        // tag <hidden>
        if (inRawName == ModelObject.hidden) {
            end_hidden(inRawName);
            return;
        }
        // tag <joinDef>
        if (inRawName == ModelObject.joinDef) {
            end_joinDef(inRawName);
            return;
        }
        // tag <objectDesc>
        if (inRawName == ModelObject.objectDesc) {
            end_objectDesc(inRawName);
            return;
        }
        // tag <joinCondition>
        if (inRawName == ModelObject.joinCondition) {
            end_joinCondition(inRawName);
            return;
        }
        // tag <objectNested>
        if (inRawName == ModelObject.objectNested) {
            end_objectNested(inRawName);
            return;
        }
        // tag <resultGrouping >
        if (inRawName == ModelObject.resultGrouping) {
            end_resultGrouping(inRawName);
            return;
        }
        // tag <objectPlaceholder>
        if (inRawName == ModelObject.objectPlaceholder) {
            end_objectPlaceholder(inRawName);
            return;
        }
    }

    /** Handles the start of the <columnDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_columnDef(final String inName, final Attributes inAttributes) { // NOPMD
        final DefaultNameValueList lList = createAttributesList(inAttributes);
        try {
            switch (columnDefState.getState()) {
            case ColumnDefState.COLUMN_DEF:
                objectDef.addColumnDef(lList);
                break;
            case ColumnDefState.JOIN_CONDITION:
                currentJoinDef.addColumnDef(retrieveColumnName(lList));
                break;
            case ColumnDefState.OBJECT_NESTED:
                nestedDef.addColumnDef(lList);
                break;
            case ColumnDefState.RESULT_GROUPING:
                groupingDef.addColumnDef(lList);
                break;
            default:
                objectDef.addColumnDef(lList);
                break;
            }
        } catch (final BOMException | VInvalidNameException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    private String retrieveColumnName(final NameValueList inList) throws VInvalidNameException { // NOPMD by lbenno
        String outColumnName = objectDef.getColumnName(inList);
        if (inList.hasValue(ColumnDefDef.nestedObject)) {
            final String lTableName = (String) inList.getValue(ColumnDefDef.nestedObject);
            outColumnName = lTableName + outColumnName.substring(outColumnName.indexOf('.'));
        }
        return outColumnName;
    }

    /** Handles the start of the <hidden> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_hidden(final String inName, final Attributes inAttributes) { // NOPMD by lbenno
        final DefaultNameValueList lList = createAttributesList(inAttributes);
        objectDef.addHidden(lList);
    }

    private DefaultNameValueList createAttributesList(final Attributes inAttributes) {
        final DefaultNameValueList outList = new DefaultNameValueList();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                outList.setValue(inAttributes.getQName(i), inAttributes.getValue(i));
            } catch (final VInvalidNameException | VInvalidValueException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
        return outList;
    }

    /** Handles the start of the <joinCondition> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_joinCondition(final String inName, final Attributes inAttributes) { // NOPMD

        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_joinCondition", currentJoinDef) == Assert.FAILURE) {
            return;
        }

        columnDefState.setToJoinCondition();
        currentJoinDef.addJoinCondition(inAttributes.getValue(0));
    }

    /** Handles the start of the <joinDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_joinDef(final String inName, final Attributes inAttributes) { // NOPMD by lbenno

        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_joinDef", objectDef) == Assert.FAILURE) {
            return;
        }

        final String lJoinType = (inAttributes == null) ? null : inAttributes.getValue(JoinDefDef.joinType); // NOPMD

        final JoinDef lJoinDef = new JoinDefImpl();
        try {
            lJoinDef.set(JoinDefDef.joinType, lJoinType);
        } catch (final SettingException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }

        if (currentJoinDef == null) {
            objectDef.setJoinDef(lJoinDef);
        }
        else {
            lJoinDef.setParentJoinDef(currentJoinDef);
        }
        currentJoinDef = lJoinDef;
    }

    /** Handles the start of the <joinedObjectDef> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    synchronized private void start_objectDef(final String inName, final Attributes inAttributes) { // NOPMD

        this.objectDef = new JoinedObjectDefImpl();
        for (int i = 0; i < inAttributes.getLength(); i++) {
            try {
                objectDef.set(inAttributes.getQName(i), inAttributes.getValue(i).intern());
            } catch (final SettingException exc) {
                DefaultExceptionWriter.printOut(this, exc, true);
            }
        }
    }

    /** Handles the start of the <objectDesc> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_objectDesc(final String inName, final Attributes inAttributes) { // NOPMD by lbenno

        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_objectDesc", currentJoinDef) == Assert.FAILURE) {
            return;
        }

        final String lObjectClassName = (inAttributes == null) ? null : inAttributes // NOPMD
                .getValue(ObjectDescDef.objectClassName);
        currentJoinDef.setTableName(objectDef.getTableName(lObjectClassName));
    }

    /** Handles the start of the <objectNested> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_objectNested(final String inName, final Attributes inAttributes) { // NOPMD
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_objectNested", currentJoinDef) == Assert.FAILURE) {
            return;
        }

        final String lNestingName = (inAttributes != null) ? inAttributes.getValue(NestedDefDef.name) : null; // NOPMD
        nestedDef = new NestedDefImpl(lNestingName);
        objectDef.addNestedDef(nestedDef);
        columnDefState.setToObjectNested();
    }

    /** Handles the start of the <resultGrouping> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_resultGrouping(final String inName, final Attributes inAttributes) { // NOPMD by lbenno
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_resultGrouping", nestedDef) == Assert.FAILURE) {
            return;
        }

        final String lModifierType = (inAttributes != null) ? inAttributes.getValue(GroupingDefDef.modifier) : null; // NOPMD
        groupingDef = new GroupingDefImpl(lModifierType);
        nestedDef.addGroupingDef(groupingDef);
        columnDefState.setToResultGrouping();
    }

    /** Handles the start of the <objectPlaceholder> tag.
     *
     * @param inName java.lang.String
     * @param inAttributes org.xml.sax.Attributes */
    private void start_objectPlaceholder(final String inName, final Attributes inAttributes) { // NOPMD by lbenno
        // There must be something wrong in the definition
        if (VSys.assertNotNull(this, "start_objectPlaceholder", currentJoinDef) == Assert.FAILURE) {
            return;
        }

        final String lPlaceholderName = (inAttributes != null) ? inAttributes.getValue(PlaceholderDefDef.name) : null; // NOPMD
        currentJoinDef.addPlaceholderDef(new PlaceholderDefImpl(lPlaceholderName));
    }

    /** @exception org.xml.sax.SAXException */
    @Override
    public void startDocument() throws SAXException {
        columnDefState = new ColumnDefState();
        columnDefState.setToColumnDefs();
        currentJoinDef = null; // NOPMD
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
    public void startElement(final String inUri, final String inLocalName, String inRawName, // NOPMD by lbenno
            final Attributes inAttributes) throws SAXException {

        // make sure the string is internalized to compare references
        if (inRawName != null) {
            inRawName = inRawName.intern();
        }

        // tag <joinedObjectDef>
        if (inRawName == ModelObject.joinedObjectDef) {
            start_objectDef(inRawName, inAttributes);
            return;
        }
        // tag <columnDef>
        if (inRawName == ModelObject.columnDef) {
            start_columnDef(inRawName, inAttributes);
            return;
        }
        // tag <hidden>
        if (inRawName == ModelObject.hidden) {
            start_hidden(inRawName, inAttributes);
            return;
        }
        // tag <joinDef>
        if (inRawName == ModelObject.joinDef) {
            start_joinDef(inRawName, inAttributes);
            return;
        }
        // tag <objectDesc>
        if (inRawName == ModelObject.objectDesc) {
            start_objectDesc(inRawName, inAttributes);
            return;
        }
        // tag <joinCondition>
        if (inRawName == ModelObject.joinCondition) {
            start_joinCondition(inRawName, inAttributes);
            return;
        }
        // tag <objectNested>
        if (inRawName == ModelObject.objectNested) {
            start_objectNested(inRawName, inAttributes);
            return;
        }
        // tag <resultGrouping >
        if (inRawName == ModelObject.resultGrouping) {
            start_resultGrouping(inRawName, inAttributes);
            return;
        }
        // tag <objectPlaceholder>
        if (inRawName == ModelObject.objectPlaceholder) {
            start_objectPlaceholder(inRawName, inAttributes);
            return;
        }
    }
}