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
package org.hip.kernel.code;

import java.util.Locale;

/** Base class of a code.
 *
 * @author: Benno Luthiger */

public abstract class AbstractCode implements Cloneable {

    // instance attributes
    private String codeID;
    private String elementID;

    /** AbstractCode constructor.
     *
     * @param inCodeID String */
    public AbstractCode(final String inCodeID) {
        codeID = inCodeID;
    }

    /** AbstractCode constructor.
     *
     * @param inCodeID String
     * @param inElementID String */
    public AbstractCode(final String inCodeID, final String inElementID) {
        codeID = inCodeID;
        elementID = inElementID;
    }

    /** Change the content of this code by the specified ElementID.
     *
     * @param inElementID java.lang.String The ShortLabel of the CodeListElement who's ElementID has to be changed
     * @param inLanguage java.lang.String
     * @exception CodeNotFoundException */
    public void changeByElementID(final String inElementID, final String inLanguage) throws CodeNotFoundException {
        try {
            CodeListHome.instance().getCodeList(getCodeID(), inLanguage).existElementID(inElementID);
        } catch (final CodeListNotFoundException exc) {
            throw new CodeNotFoundException(exc);
        }
        elementID = inElementID;
    }

    /** Change the ElementID by label
     *
     * @param strLabel of the CodeListElement who's ElementID has to be changed
     * @param inLanguage java.lang.String
     * @exception CodeNotFoundException */

    public void changeByLabel(final String inLabel, final String inLanguage) throws CodeNotFoundException {
        try {
            elementID = CodeListHome.instance().getCodeList(getCodeID(), inLanguage).getElementIDByLabel(inLabel);
        } catch (final CodeListNotFoundException exc) {
            throw new CodeNotFoundException(exc);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException { // NOPMD by lbenno
        return super.clone();
    }

    /** Compares two CodeListElements (for the same codeID). Implementation of Comparable, i.e. the method returns a
     * negative integer, zero, or a positive integer as this CodeListElement is less than, equal to, or greater than the
     * inputed CodeListElement.
     *
     * @return int
     * @param inObj the object to compare
     * @throws ClassCastException: If inObj is not a CodeListElement */
    public int compareTo(final Object inObj) {
        if (inObj instanceof AbstractCode) {
            final AbstractCode lToCompare = (AbstractCode) inObj;
            final int lCompareToCodeID = getCodeID().compareTo(lToCompare.getCodeID());
            if (lCompareToCodeID == 0) {
                return getElementID().compareTo(lToCompare.getElementID());
            }
            else {
                return lCompareToCodeID;
            }
        }
        else {
            throw new ClassCastException();
        }
    }

    /** Two CodeListElements are equal when they have the same codeID and the same elementID.
     *
     * @return boolean
     * @param inObj the object to compare */
    @Override
    public boolean equals(final Object inObj) {
        if (inObj instanceof AbstractCode) {
            final AbstractCode lElementToCompare = (AbstractCode) inObj;
            return getCodeID().equals(lElementToCompare.getCodeID())
                    && getElementID().equals(lElementToCompare.getElementID());
        }
        return false;
    }

    /** @return String the code's id */
    public String getCodeID() {
        return codeID;
    }

    /** @return String the element's id */
    public String getElementID() {
        return elementID;
    }

    /** Returns array of all possible short labels for this CodeListElement.
     *
     * @return String[] array of short labels */

    public String[] getElementIDs() throws CodeListNotFoundException {
        final CodeList outCodeList = CodeListHome.instance().getCodeList(getCodeID(), Locale.GERMAN.getLanguage());
        return outCodeList.getElementIDs();
    }

    /** Returns the appropriate label for this CodeListElement.
     *
     * @return String the label */
    public String getLabel(final String inLanguage) throws CodeListNotFoundException {
        return CodeListHome.instance().getCodeList(getCodeID(), inLanguage).getLabel(getElementID());
    }

    /** Returns an array of all possible labels for this CodeListElement.
     *
     * @return String[] array of labels */
    public String[] getLabels(final String inLanguage) throws CodeListNotFoundException {
        return CodeListHome.instance().getCodeList(getCodeID(), inLanguage).getLabels();
    }

    /** Proper implementation because of own equals()
     *
     * @return int the code */
    @Override
    public int hashCode() {
        return new String(getCodeID() + getElementID()).hashCode();
    }

    /** @param inCodeID String */
    public void setCodeID(final String inCodeID) {
        codeID = inCodeID;
    }

    /** @param inElementID String */
    public void setElementID(final String inElementID) {
        elementID = inElementID;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return getElementID();
    }
}
