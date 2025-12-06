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

package org.hip.kernel.bom.impl; // NOPMD by lbenno

import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.bom.ColumnModifier;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.ICriteriaStack;
import org.hip.kernel.bom.ICriteriumRenderStrategy;
import org.hip.kernel.bom.IGetValueStrategy;
import org.hip.kernel.bom.KeyCriterion;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.impl.KeyCriterionImpl.LevelReturnFormatter;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.AbstractSortedList;
import org.hip.kernel.util.SortableItem;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidSortCriteriaException;
import org.hip.kernel.util.VInvalidValueException;

/** This is the default implementation of the KeyObject interface. The KeyObject holds KeyCriterion items in the order
 * the have been added to the KeyObject.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.KeyObject */
@SuppressWarnings("serial")
public class KeyObjectImpl extends AbstractSortedList implements KeyObject {
    // Instance variables
    private String schemaName = KeyDef.SCHEMA_PRIMARY_KEY;
    private int position;
    private ICriteriaStack criteriaStack;
    private boolean distinct;

    /** Constructor for KeyObjectImpl. */
    public KeyObjectImpl() {
        super();
        position = 0;
        criteriaStack = new SQLCriteriaStack();
    }

    /** @see org.hip.kernel.util.AbstractSortedList#create(Object, int) */
    @Override
    protected SortableItem create(final Object inValue, final int inPosition) throws VInvalidSortCriteriaException,
    VInvalidValueException {
        try {
            final KeyCriterion outCriterion = (KeyCriterion) inValue;
            outCriterion.setPosition(inPosition);
            outCriterion.setOwingList(this);
            return outCriterion;
        } catch (final ClassCastException exc) {
            throw new VInvalidValueException(exc);
        }
    }

    /** @see org.hip.kernel.util.AbstractSortedList#create(Object, Object) */
    @Override
    protected SortableItem create(final Object inValue, final Object inSortCriteria)
            throws VInvalidSortCriteriaException, VInvalidValueException {
        throw new VInvalidSortCriteriaException("SortCriteria not supported for OrderObject.");
    }

    /** @see org.hip.kernel.bom.KeyObject#getSchemaName() */
    @Override
    public String getSchemaName() {
        return schemaName;
    }

    /** @see org.hip.kernel.bom.KeyObject#isPrimaryKey() */
    @Override
    public boolean isPrimaryKey() {
        return schemaName == KeyDef.SCHEMA_PRIMARY_KEY;
    }

    /** Sets the schema name. By default the schema name is "primaryKey".
     *
     * @param inSchemaName java.lang.String */
    @Override
    public void setSchemaName(final String inSchemaName) {
        // Pre: inSchemaName not null
        if (VSys.assertNotNull(this, "setSchemaName", inSchemaName) == Assert.FAILURE) {
            return;
        }

        // Post: set the schema name
        schemaName = inSchemaName.intern();
    }

    private String createComparison(final Object inValue) {
        if (inValue instanceof SQLRange || inValue instanceof Collection<?>) {
            return "";
        }
        else {
            return KeyCriterionImpl.COMPARISON_OPERATOR_DFT;
        }
    }

    /** @see org.hip.kernel.util.NameValueList#setValue(String, Object) */
    @Override
    public void setValue(final Object inName, final Object inValue) throws VInvalidSortCriteriaException,
    VInvalidValueException {
        try {
            final String lName = (String) inName;
            setValue(lName, inValue, createComparison(inValue), KeyCriterionImpl.LINK_OPERATOR_DFT,
                    KeyCriterionImpl.COLUMN_MODIFIER_DFT);
        } catch (final ClassCastException exc) {
            throw new VInvalidValueException(exc);
        } catch (final VInvalidNameException exc) {
            throw new VInvalidValueException(exc);
        }
    }

    /** @see org.hip.kernel.util.NameValueList#setValue(String, Object) */
    @Override
    public void setValue(final String inName, final Object inValue) throws VInvalidNameException,
    VInvalidValueException {
        setValue(inName, inValue, createComparison(inValue), KeyCriterionImpl.LINK_OPERATOR_DFT,
                KeyCriterionImpl.COLUMN_MODIFIER_DFT);
    }

    /** @see org.hip.kernel.bom.KeyObject#setValue(String, Object, String) */
    @Override
    public void setValue(final String inName, final Object inValue, final String inComparison)
            throws VInvalidNameException, VInvalidValueException {
        setValue(inName, inValue, inComparison, KeyCriterionImpl.LINK_OPERATOR_DFT,
                KeyCriterionImpl.COLUMN_MODIFIER_DFT);
    }

    @Override
    public void setValue(final String inName, final Object inValue, final String inComparison, final boolean inLinkType) // NOPMD
    // by
    // lbenno
            throws VInvalidNameException, VInvalidValueException {
        setValue(inName, inValue, inComparison, inLinkType, KeyCriterionImpl.COLUMN_MODIFIER_DFT);
    }

    @Override
    public void setValue(final String inName, final Object inValue, final String inComparison, // NOPMD by lbenno
            final BinaryBooleanOperator inBinaryOperator) throws VInvalidNameException, VInvalidValueException {
        setValue(inName, inValue, inComparison, inBinaryOperator, KeyCriterionImpl.COLUMN_MODIFIER_DFT);
    }

    @Override
    public void setValue(final String inName, final Object inValue, final String inComparison, // NOPMD by lbenno
            final boolean inLinkType, final ColumnModifier inColumnModifier) throws VInvalidNameException,
            VInvalidValueException {
        this.setValue(inName, inValue, inComparison, inLinkType ? BinaryBooleanOperator.AND : BinaryBooleanOperator.OR,
                inColumnModifier);
    }

    @Override
    public void setValue(final String inName, final Object inValue, final String inComparison, // NOPMD by lbenno
            final BinaryBooleanOperator inBinaryOperator, final ColumnModifier inColumnModifier)
                    throws VInvalidNameException, VInvalidValueException {
        try {
            add(create(new KeyCriterionImpl(inName, inValue, inComparison, inBinaryOperator, inColumnModifier),
                    position++));
        } catch (final VInvalidSortCriteriaException exc) {
            throw new VInvalidValueException(exc);
        }
    }

    /** Sets a KeyObject as group in an enclosing KeyObject. This method can be used to create SQL expressions like<br/>
     * KEY1 AND (KEY2) e.g. date >= 20030801 AND (name LIKE 'VI%' OR name LIKE 'vi%')
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException */
    @Override
    public void setValue(final KeyObject inKey) throws VInvalidNameException, VInvalidValueException {
        setValue(inKey, KeyCriterionImpl.LINK_OPERATOR_DFT);
    }

    @Override
    public void setValue(final KeyObject inKey, final boolean inLinkType) throws VInvalidNameException, // NOPMD by
    // lbenno
    VInvalidValueException {
        this.setValue(inKey, inLinkType ? BinaryBooleanOperator.AND : BinaryBooleanOperator.OR);
    }

    @Override
    public void setValue(final KeyObject inKey, final BinaryBooleanOperator inBinaryOperator) // NOPMD by lbenno
            throws VInvalidNameException, VInvalidValueException {
        try {
            add(create(new KeyCriterionImpl(inKey, inBinaryOperator), position++));
        } catch (final VInvalidSortCriteriaException exc) {
            throw new VInvalidValueException(exc);
        }
    }

    /** Compares all items of KeyObjects.
     *
     * @return boolean
     * @param inObject java.lang.Object */
    @Override
    public boolean equals(final Object inObject) {
        // pre
        if (inObject == null) {
            return false;
        }
        if (!(inObject instanceof KeyObject)) {
            return false;
        }

        final KeyObject lKeyObject = ((KeyObject) inObject);
        if (!lKeyObject.getSchemaName().equals(getSchemaName())) {
            return false;
        }
        if (lKeyObject.size() != size()) {
            return false;
        }

        final Iterator<SortableItem> lThis = getItems();
        final Iterator<SortableItem> lItems = lKeyObject.getItems2().iterator();
        while (lItems.hasNext()) {
            if (!((KeyCriterion) lItems.next()).equals(lThis.next())) {
                return false;
            }
        }

        return true;
    }

    /** Returns a hash code value for the order object.
     *
     * @return int */
    @Override
    public int hashCode() {
        int outHashCode = getSchemaName().hashCode();

        for (final Iterator<SortableItem> lItems = getItems(); lItems.hasNext();) {
            outHashCode ^= ((KeyCriterion) lItems.next()).hashCode();
        }
        return outHashCode;
    }

    /** Renders the key to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuffer the rendered SQL part.
     * @deprecated Use <code>render2()</code> instead */
    @Deprecated
    @Override
    public StringBuffer render(final GeneralDomainObjectHome inDomainObjectHome) {
        return new StringBuffer(render2(inDomainObjectHome));
    }

    @Override
    public StringBuilder render2(final GeneralDomainObjectHome inDomainObjectHome) { // NOPMD by lbenno
        for (final SortableItem lKeyCriterion : getItems2()) {
            criteriaStack.addOperator(((KeyCriterion) lKeyCriterion).getBinaryBooleanOperator());
            criteriaStack.addCriterium(((KeyCriterion) lKeyCriterion).render2(inDomainObjectHome));
        }
        return new StringBuilder(criteriaStack.render());
    }

    @Override
    public void setCriteriumRenderer(final ICriteriumRenderStrategy inStrategy) { // NOPMD by lbenno
        for (final SortableItem lKeyCriterion : getItems2()) {
            ((KeyCriterion) lKeyCriterion).setCriteriumRenderer(inStrategy);
        }
    }

    @Override
    public void setCriteriaStackFactory(final CriteriaStackFactory inFactory) { // NOPMD by lbenno
        criteriaStack = inFactory.getCriteriaStack();
        for (final SortableItem lKeyCriterion : getItems2()) {
            ((KeyCriterion) lKeyCriterion).setCriteriaStackFactory(inFactory);
        }
    }

    @Override
    public void setGetValueStrategy(final IGetValueStrategy inGetValueStrategy) { // NOPMD by lbenno
        for (final SortableItem lKeyCriterion : getItems2()) {
            ((KeyCriterion) lKeyCriterion).setGetValueStrategy(inGetValueStrategy);
        }
    }

    @Override
    public void setLevelReturnFormatter(final LevelReturnFormatter inFormatter) { // NOPMD by lbenno
        for (final SortableItem lKeyCriterion : getItems2()) {
            ((KeyCriterion) lKeyCriterion).setLevelReturnFormatter(inFormatter);
        }
    }

    @Override
    public void setDistinct(final boolean inDistinct) { // NOPMD by lbenno
        distinct = inDistinct;
    }

    @Override
    public boolean isDistinct() { // NOPMD by lbenno
        return distinct;
    }

}