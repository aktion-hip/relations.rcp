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
package org.hip.kernel.bom.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.bom.model.KeyDefDef;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.util.Debug;

/** Implements the KeyDef interface
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.KeyDef */
@SuppressWarnings("serial")
abstract public class KeyDefImpl extends AbstractModelObject implements KeyDef { // NOPMD by lbenno 
    private static List<String> emptyVector = new ArrayList<String>();

    /** KeyDefImpl default constructor. */
    public KeyDefImpl() {
        super();
    }

    /** MappingDefImpl constructor with initial values. The array of the objects contains the names in the first column
     * and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public KeyDefImpl(final java.lang.Object[][] inInitialValues) { // NOPMD by lbenno 
        super(inInitialValues);
    }

    /** Inserts a Property named inKeyName at the specified position to the PropertySet.
     *
     * @param inKeyName java.lang.String
     * @param inPosition int */
    @Override
    @SuppressWarnings("unchecked")
    public void addKeyNameAt(final String inKeyName, final int inPosition) {
        try {
            List<String> lKeyItems = (List<String>) this.get(KeyDefDef.keyItems);
            if (lKeyItems == null) {
                lKeyItems = new ArrayList<String>();
                this.set(KeyDefDef.keyItems, lKeyItems);
            }
            ((ArrayList<String>) lKeyItems).ensureCapacity(inPosition);
            lKeyItems.add(inPosition, inKeyName);
        } catch (final GettingException | SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        } // try/catch
    }

    /** KeyDefs are equal if all of their keyPropertyNames are equal.
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof KeyDef)) {
            return false;
        }

        try {
            return ((List<?>) get(KeyDefDef.keyItems)).equals(((KeyDef) inObject).get(KeyDefDef.keyItems));
        } catch (final GettingException exc) {
            return false;
        }
    }

    /** Returns the property name for the given sequence number. Numbering starts with 0.
     *
     * @return java.lang.String
     * @param inSequenceNumber int */
    @SuppressWarnings("unchecked")
    @Override
    public String getKeyName(final int inSequenceNumber) {
        String outKeyName = "";

        try {
            final List<String> lKeyItems = (List<String>) this.get(KeyDefDef.keyItems);
            if (lKeyItems != null) {
                outKeyName = lKeyItems.get(inSequenceNumber);
            }
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        } // try/catch

        return outKeyName;
    }

    @Override
    public Iterator<String> getKeyNames() { // NOPMD by lbenno
        return getKeyNames2().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getKeyNames2() { // NOPMD by lbenno
        List<String> outKeyNames = null;
        try {
            outKeyNames = (List<String>) this.get(KeyDefDef.keyItems);
            if (outKeyNames == null) {
                outKeyNames = emptyVector;
            }
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return outKeyNames;
    }

    /** @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getKeyDefDef();
    }

    /** Returns a hash code value for the key def.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outCode = 1;
        try {
            outCode = ((List<?>) get(KeyDefDef.keyItems)).hashCode();
        } catch (final GettingException exc) { // NOPMD by lbenno
            // left blank intentionally
        }
        return outCode;
    }

    /** @return boolean */
    @Override
    public boolean isPrimaryKeyDef() {
        return false;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        final StringBuilder lMessage = new StringBuilder(50);
        for (final Iterator<String> lKeyNames = getKeyNames(); lKeyNames.hasNext();) {
            lMessage.append("keyPropertyName=\"").append(lKeyNames.next()).append("\" ");
        }
        return Debug.classMarkupString(this, new String(lMessage));
    }
}
