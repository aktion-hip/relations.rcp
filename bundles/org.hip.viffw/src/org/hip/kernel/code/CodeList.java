/**
	This package is part of the framework used for the application VIF.
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
package org.hip.kernel.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.util.ListJoiner;

/** A CodeList consists of various items associating a code with a label in the specified language.
 *
 * @author: Benno Luthiger */
public class CodeList {
    // instance attributes
    private transient final List<CodeListItem> codeListElements = new ArrayList<CodeListItem>();
    private transient Map<String, String> codeLabelsKeyValue;
    private transient Set<String> codeLabels;
    private transient String[] codeLabelsUnsorted;
    private transient String[] codeElementID;
    private transient String[] codeElementIDUnsorted;
    private final String codeID;
    private final String language;

    private final static String TAG_CODE_LIST = "codeList";
    private final static String TAG_CODE_LIST_ITEM = "codeListItem";

    /** CodeList constructor, should only be called by CodeListHome. Creates an instance of CodeList with the specified
     * codeID for the specified language. */
    CodeList(final String inCodeID, final String inLanguage) {
        codeID = inCodeID;
        language = inLanguage;

        prepareArrays();
    }

    /** @param inElement org.hip.kernel.code.CodeListItem */
    protected void add(final CodeListItem inElement) {
        codeListElements.add(inElement);
    }

    /** Does a Code with the specified ElementID exist?
     *
     * @param inElementID java.lang.String the short label to translate
     * @return true, if exists
     * @exception CodeNotFoundException: If no appropriate ElementID can be found */
    public boolean existElementID(final String inElementID) throws CodeNotFoundException {
        final Object lItem = codeLabelsKeyValue.get(inElementID);

        if (lItem == null) {
            throw new CodeNotFoundException(getCodeID() + "-Element not found for '" + inElementID + "'");
        }
        return true;
    }

    /** @return java.lang.String */
    public String getCodeID() {
        return codeID;
    }

    /** Translate a label to the appropriate ElementID.
     *
     * @param inLabel java.lang.String the label to translate
     * @return java.lang.String the appropriate ElementID
     * @exception CodeNotFoundException: If no appropriate ElementID can be found */
    public String getElementIDByLabel(final String inLabel) throws CodeNotFoundException {
        Iterator<CodeListItem> lItems = getElementIdCollection().iterator();
        while (lItems.hasNext()) {
            final CodeListItem lItem = lItems.next();
            if (lItem.getLabel().equals(inLabel)) {
                return lItem.getElementID();
            }
        }

        lItems = getElementIdCollection().iterator();
        while (lItems.hasNext()) {
            final CodeListItem lItem = lItems.next();

            final StringTokenizer lElementLabelTokens = new StringTokenizer(lItem.getLabel());
            final StringTokenizer lInputLabelTokens = new StringTokenizer(inLabel); // must be new every time !!!

            if (tokensAreEqual(lElementLabelTokens, lInputLabelTokens))
            {
                return lItem.getElementID(); // all tokens equal
            }
        }

        throw new CodeNotFoundException(getCodeID() + "-Element not found for '" + inLabel + "'");
    }

    /** Returns the collection of code list elements.
     *
     * @return {@link List} **/
    public List<CodeListItem> getElementIdCollection() {
        return codeListElements;
    }

    /** Get a sorted array of all short labels (ElementIDs) to this codeID The order of the ElementIDs is in a way that
     * the associated labels appear in alphabetically sorted order.
     *
     * @return java.lang.String[] the sorted array of short labels */
    public String[] getElementIDs() {
        final String[] outIDs = new String[codeElementID.length];
        System.arraycopy(codeElementID, 0, outIDs, 0, codeElementID.length);
        return outIDs;
    }

    /** Get an unsorted array of all short labels (ElementIDs) to this codeID
     *
     * @return java.lang.String[] the unsorted array of short labels */
    public String[] getElementIDsUnsorted() {
        final String[] outIDs = new String[codeElementIDUnsorted.length];
        System.arraycopy(codeElementIDUnsorted, 0, outIDs, 0, codeElementIDUnsorted.length);
        return outIDs;
    }

    /** Get a label for the specified ElementID
     *
     * @return java.lang.String the label or null, if not found
     * @param strElementID */
    public String getLabel(final String inElementID) {

        if (inElementID == null) {
            return null;
        }

        return codeLabelsKeyValue.get(inElementID);
    }

    /** Get an alphabetically sorted array of all labels to this codeID.
     *
     * @return java.lang.String[] the sorted array of labels */
    public String[] getLabels() {
        final String[] outLabels = new String[codeLabels.size()];
        int i = 0; // NOPMD by lbenno
        for (final Iterator<String> lLabels = codeLabels.iterator(); lLabels.hasNext();) {
            outLabels[i++] = lLabels.next();
        }
        return outLabels;
    }

    /** Get an unsorted array of all labels to this codeID.
     *
     * @return java.lang.String[] the unsorted array of labels */
    public String[] getLabelsUnsorted() {
        final String[] outLabels = new String[codeLabelsUnsorted.length];
        System.arraycopy(codeLabelsUnsorted, 0, outLabels, 0, codeLabelsUnsorted.length);
        return outLabels;
    }

    /** @return java.lang.String */
    public String getLanguage() {
        return language;
    }

    /** Checks whether the specified ElementID is in the set of selected Codes.
     *
     * @param inElementID java.lang.String The ElementID to check
     * @param inSelectedCodes org.hip.kernel.code.AbstractCode[] Array of Codes which are selected
     * @return boolean */
    private boolean isSelected(final String inElementID, final AbstractCode... inSelectedCodes) {

        for (int i = 0; i < inSelectedCodes.length; i++) {
            if (inElementID.equals(inSelectedCodes[i].getElementID())) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether the specified ElementID is in the set of selected Codes.
     *
     * @param inElementID java.lang.String The ElementID to check
     * @param inSelectedCodes Collection<AbstractCode> Collection of Codes which are selected
     * @return boolean */
    private boolean isSelected(final String inElementID, final Collection<? extends AbstractCode> inSelectedCodes) {
        for (final AbstractCode lCode : inSelectedCodes) {
            if (inElementID.equals(lCode.getElementID())) {
                return true;
            }
        }
        return false;
    }

    /** Prepares the code list arrays. */
    protected final void prepareArrays() {

        // pre
        if (codeListElements.isEmpty()) {
            return;
        }

        try {
            codeLabelsKeyValue = new Hashtable<String, String>(codeListElements.size() + 1); // in case, there are 0
            codeElementID = new String[codeListElements.size()];
            codeElementIDUnsorted = new String[codeListElements.size()];
            codeLabelsUnsorted = new String[codeListElements.size()];
            codeLabels = new TreeSet<String>();

            // fill elementID and label arrays
            int i = 0; // NOPMD by lbenno
            for (final CodeListItem lItem : codeListElements) {
                codeLabelsKeyValue.put(lItem.getElementID(), lItem.getLabel());
                codeLabels.add(lItem.getLabel());
                codeLabelsUnsorted[i] = lItem.getLabel();
                codeElementIDUnsorted[i++] = lItem.getElementID(); // this.getElementIDByLabel(codeLabelsUnsorted[i]);
            }

            // fill sorted ElementIDs according to the order of sorted labels
            i = 0;
            for (final String lLabel : codeLabels) {
                codeElementID[i++] = getElementIDByLabel(lLabel);
            }
        } catch (final CodeNotFoundException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
        }
    }

    /** @return int */
    public int size() {
        return codeListElements.size();
    }

    /** Compare two Tokenizer
     *
     * @return boolean true if all tokens are equal
     * @param inTokenizer java.util.StringTokenizer
     * @param inTokenizerToCompare java.util.StringTokenizer */
    private boolean tokensAreEqual(final StringTokenizer inTokenizer, final StringTokenizer inTokenizerToCompare) {

        if (inTokenizer.countTokens() != inTokenizerToCompare.countTokens()) {
            return false;
        }

        while (inTokenizer.hasMoreTokens()) {
            final String lToken = inTokenizer.nextToken();
            final String lTokenToCompare = inTokenizerToCompare.nextToken();
            if (!lToken.equals(lTokenToCompare)) {
                return false;
            }
        }

        return true;
    }

    /** Joined list of all <code>CodeLabel</code>.toString()
     *
     * @return java.lang.String String of codeLabels
     * @see org.hip.kernel.code.CodeList.toString() */
    public String toLongString() {
        final ListJoiner outLabels = new ListJoiner();
        for (final String lLabel : codeLabels) {
            outLabels.addEntry(lLabel);
        }
        return outLabels.join(";");
    }

    /** Returns a XML-String of the form <codeList> <codeListItem id="id1" >Item1</codeListItem> <codeListItem id="id2"
     * selected="true">Item2</codeListItem> <codeListItem id="id3" >Item3</codeListItem> <codeListItem id="id4"
     * selected="true">Item4</codeListItem> ... </codeList>
     *
     * @param inSelectedCodes org.hip.kernel.code.AbstractCode[] Array of Codes which are selected
     * @return java.lang.String XML */
    public String toSelectionString(final AbstractCode... inSelectedCodes) {
        final StringBuilder outXML = new StringBuilder(100);

        outXML.append('<').append(TAG_CODE_LIST).append("> \n"); // NOPMD by lbenno
        for (int i = 0; i < codeElementIDUnsorted.length; i++) {
            outXML.append('<').append(TAG_CODE_LIST_ITEM).append(" id=\"").append(codeElementIDUnsorted[i])
                    .append("\" ");
            if (isSelected(codeElementIDUnsorted[i], inSelectedCodes)) {
                outXML.append("selected=\"true\"");
            }
            outXML.append('>').append(getLabel(codeElementIDUnsorted[i])).append("</codeListItem> \n");
        }
        outXML.append("</").append(TAG_CODE_LIST).append("> \n");

        return new String(outXML);
    }

    /** Returns a XML-String of the form <codeList> <codeListItem id="id1" >Item1</codeListItem> <codeListItem id="id2"
     * selected="true">Item2</codeListItem> <codeListItem id="id3" >Item3</codeListItem> <codeListItem id="id4"
     * selected="true">Item4</codeListItem> ... </codeList>
     *
     * @param inSelectedCodes java.util.List Collection of Codes which are selected
     * @return java.lang.String XML */
    public String toSelectionString(final Collection<? extends AbstractCode> inSelectedCodes) {
        final StringBuilder outXML = new StringBuilder(100);

        outXML.append("<" + TAG_CODE_LIST + "> \n");
        for (int i = 0; i < codeElementIDUnsorted.length; i++) {
            outXML.append("<" + TAG_CODE_LIST_ITEM + " id=\"" + codeElementIDUnsorted[i] + "\" ");
            if (isSelected(codeElementIDUnsorted[i], inSelectedCodes)) {
                outXML.append("selected=\"true\"");
            }
            outXML.append(">" + getLabel(codeElementIDUnsorted[i]) + "</codeListItem> \n");
        }
        outXML.append("</" + TAG_CODE_LIST + "> \n");

        return new String(outXML);
    }

    /** Joined list of all codeElementID.toString().
     *
     * @return java.lang.String String of ElementIDs (ShortLabels) */
    @Override
    public String toString() {
        final ListJoiner outIDs = new ListJoiner();
        for (final String lID : codeElementID) {
            outIDs.addEntry(lID);
        }
        return outIDs.join(";");
    }
}
