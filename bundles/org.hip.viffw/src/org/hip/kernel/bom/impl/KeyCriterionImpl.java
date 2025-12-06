/**
 This package is part of the framework used for the application VIF.
 Copyright (C) 2001, Benno Luthiger

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
package org.hip.kernel.bom.impl;

import java.io.Serializable;

import org.hip.kernel.bom.ColumnModifier;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.ICriteriumRenderStrategy;
import org.hip.kernel.bom.IGetValueStrategy;
import org.hip.kernel.bom.KeyCriterion;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.KeyObject.BinaryBooleanOperator;
import org.hip.kernel.util.AbstractNameValueList;
import org.hip.kernel.util.NameValue;
import org.hip.kernel.util.SortedList;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** Parameter object to create the criteria in SQL WHERE statements. With this object it is possible to create statements
 * as
 *
 * <pre>
 * ... WHERE MODIFIER(field_name) COMPARISON VALUE LINK MODIFIER(field_name) COMPARISON VALUE
 * </pre>
 *
 * e.g.
 *
 * <pre>
 * ... WHERE UCASE(name) LIKE 'VI%' AND YEAR(mutation_date) >= 2001
 * </pre>
 *
 * Created on 10.09.2002
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public class KeyCriterionImpl extends AbstractNameValueList implements KeyCriterion, Comparable<KeyCriterion> { // NOPMD
    public final static boolean LINK_OPERATOR_DFT = true;
    public final static String COMPARISON_OPERATOR_DFT = "=";
    public final static ColumnModifier COLUMN_MODIFIER_DFT = new DefaultColumnModifier();
    public static LevelReturnFormatter LEVEL_STRAIGHT = null; // NOPMD

    private final static String NAME_KEY = "keyName";
    private final static String VALUE_KEY = "keyValue";
    private final static String BINARY_BOOLEAN_OPERATOR_KEY = "binaryBooleanOperator";
    private final static String COMPARISON_OPERATOR_KEY = "comparisonOperator";
    private final static String COLUMN_MODIFIER_KEY = "columnModifier";

    // instance variables
    private SortedList owingList;
    private int position;
    private final KeyObjectTraversal traversal;
    private final KeyObjectRecursion recursion;
    private ICriteriumRenderStrategy renderStrategy;
    private IGetValueStrategy getValueStrategy = new CriteriumValueStrategy();
    private LevelReturnFormatter levelFormatter = new LevelReturnEnclosed();

    static {
        LEVEL_STRAIGHT = new LevelReturnFormatter() {
            @Override
            public StringBuilder format(final StringBuilder inRenderedKey) { // NOPMD
                return inRenderedKey;
            }
        };
    }

    /** Constructor for KeyCriterionImpl.
     *
     * @param inName java.lang.String Name of the field used for the criterion.
     * @param inValue java.lang.Object Value to match in the criterion.
     * @param inComparison java.lang.String Comparison operator for the criterion.
     * @param inLinkType boolean Type of link if concatenation multiple criteria. True (default) links with AND, false
     *            with OR.
     * @param inColumnModifier org.hip.kernel.bom.ColumnModifier Function to modify field value in the criterion.
     * @throws VInvalidValueException
     * @deprecated use constructor with BinaryBooleanOperator instead of boolean inLinkType */
    @Deprecated
    public KeyCriterionImpl(final String inName, final Object inValue, final String inComparison,
            final boolean inLinkType, final ColumnModifier inColumnModifier) throws VInvalidValueException {
        this(inName, inValue, inComparison, inLinkType ? BinaryBooleanOperator.AND : BinaryBooleanOperator.OR,
                inColumnModifier);
    }

    /** Constructor for KeyCriterionImpl.
     *
     * @param inName String Name of the field used for the criterion.
     * @param inValue Object Value to match in the criterion.
     * @param inComparison String Comparison operator for the criterion.
     * @param inBinaryOperator BinaryBooleanOperator the binary operator used for the criterion operands.
     * @param inColumnModifier ColumnModifier Function to modify field value in the criterion.
     * @throws VInvalidValueException */
    public KeyCriterionImpl(final String inName, final Object inValue, final String inComparison,
            final BinaryBooleanOperator inBinaryOperator, final ColumnModifier inColumnModifier)
                    throws VInvalidValueException {
        super();
        traversal = new KeyObjectTraversal() {
            @Override
            public void setCriteriumRenderer(final ICriteriumRenderStrategy inStrategy) { // NOPMD
                // nothing to do
            }

            @Override
            public void setCriteriaStackFactory(final CriteriaStackFactory inFactory) { // NOPMD
                // nothing to do
            }

            @Override
            public void setGetValueStrategy(final IGetValueStrategy inStrategy) { // NOPMD
                // nothing to do
            }

            @Override
            public void setLevelReturnFormatter(final LevelReturnFormatter inFormatter) { // NOPMD
                // nothing to do
            }
        };
        recursion = new NoRecursion();
        renderStrategy = new SQLCriteriumRenderer();

        try {
            add(create(NAME_KEY, inName));
            add(create(VALUE_KEY, inValue));
            add(create(COMPARISON_OPERATOR_KEY, inComparison));
            add(create(BINARY_BOOLEAN_OPERATOR_KEY, inBinaryOperator));
            add(create(COLUMN_MODIFIER_KEY, inColumnModifier));
        } catch (final VInvalidNameException exc) {
            throw new VInvalidValueException(); // NOPMD
        }
    }

    /** KeyCriterionImpl constructor to create Keys embedded in other Keys.
     *
     * @param inKey KeyObject the Key to embed
     * @param inLinkType boolean Type of link if concatenation multiple criteria. True (default) links with AND, false
     *            with OR.
     * @throws VInvalidValueException
     * @deprecated use constructor with BinaryBooleanOperator instead of boolean inLinkType */
    @Deprecated
    public KeyCriterionImpl(final KeyObject inKey, final boolean inLinkType) throws VInvalidValueException {
        this(inKey, inLinkType ? BinaryBooleanOperator.AND : BinaryBooleanOperator.OR);
    }

    /** KeyCriterionImpl constructor to create Keys embedded in other Keys.
     *
     * @param inKey KeyObject the Key to embed
     * @param inBinaryOperator BinaryBooleanOperator the binary operator used for the criterion operands.
     * @throws VInvalidValueException */
    public KeyCriterionImpl(final KeyObject inKey, final BinaryBooleanOperator inBinaryOperator)
            throws VInvalidValueException {
        super();
        traversal = new KeyObjectTraversal() {
            @Override
            public void setCriteriumRenderer(final ICriteriumRenderStrategy inStrategy) { // NOPMD
                ((KeyObject) getValue()).setCriteriumRenderer(inStrategy);
            }

            @Override
            public void setCriteriaStackFactory(final CriteriaStackFactory inFactory) { // NOPMD
                ((KeyObject) getValue()).setCriteriaStackFactory(inFactory);
            }

            @Override
            public void setGetValueStrategy(final IGetValueStrategy inStrategy) { // NOPMD
                ((KeyObject) getValue()).setGetValueStrategy(inStrategy);
            }

            @Override
            public void setLevelReturnFormatter(final LevelReturnFormatter inFormatter) { // NOPMD
                ((KeyObject) getValue()).setLevelReturnFormatter(inFormatter);
            }
        };
        recursion = new NormalRecursion();
        renderStrategy = new SQLCriteriumRenderer();

        try {
            add(create(NAME_KEY, NAME_FOR_KEY));
            add(create(VALUE_KEY, inKey));
            add(create(BINARY_BOOLEAN_OPERATOR_KEY, inBinaryOperator));
        } catch (final VInvalidNameException exc) {
            throw new VInvalidValueException(); // NOPMD
        }
    }

    @Override
    public String getName() { // NOPMD
        return (String) get(NAME_KEY).getValue();
    }

    @Override
    public Object getValue() { // NOPMD
        return get(VALUE_KEY).getValue();
    }

    @Override
    public BinaryBooleanOperator getBinaryBooleanOperator() { // NOPMD
        return (BinaryBooleanOperator) get(BINARY_BOOLEAN_OPERATOR_KEY).getValue();
    }

    @Override
    public String getComparison() { // NOPMD
        return (String) get(COMPARISON_OPERATOR_KEY).getValue();
    }

    @Override
    public ColumnModifier getColumnModifier() { // NOPMD
        return (ColumnModifier) get(COLUMN_MODIFIER_KEY).getValue();
    }

    /** This method creates a KeyItemImpl (NameValue) and initializes it with the specified value.
     *
     * @return org.hip.kernel.util.NameValue
     * @param inName java.lang.String
     * @param inValue java.lang.Object
     * @exception org.hip.kernel.util.VInvalidNameException
     * @exception org.hip.kernel.util.VInvalidValueException */
    @Override
    protected NameValue create(final String inName, final Object inValue) throws VInvalidValueException,
    VInvalidNameException {
        final NameValue outKeyItem = new KeyItemImpl(this, inName);
        outKeyItem.setValue(inValue);
        return outKeyItem;
    }

    /** @return boolean */
    @Override
    protected boolean dynamicAddAllowed() {
        return false;
    }

    /** Position of item.
     *
     * @return int */
    @Override
    public int getPosition() {
        return position;
    }

    /** Sets the position of this KeyCriterion in the KeyObject.
     *
     * @param inPosition int */
    @Override
    public void setPosition(final int inPosition) {
        position = inPosition;
    }

    /** @see java.lang.Comparable#compareTo(Object) */
    @Override
    public int compareTo(final KeyCriterion inKeyCriterion) {
        return position - inKeyCriterion.getPosition();
    }

    /** Returns the list the instance is member from.
     *
     * @return org.hip.kernel.util.SortedList */
    @Override
    public SortedList getOwingList() {
        return owingList;
    }

    /** @param inOwingList org.hip.kernel.util.SortedList */
    @Override
    public void setOwingList(final SortedList inOwingList) {
        owingList = inOwingList;
    }

    /** Renders the criterion to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuffer the rendered SQL part. */
    @Override
    public StringBuffer render(final GeneralDomainObjectHome inDomainObjectHome) {
        return new StringBuffer(render2(inDomainObjectHome));
    }

    @Override
    public StringBuilder render2(final GeneralDomainObjectHome inDomainObjectHome) { // NOPMD
        return recursion.run(inDomainObjectHome);
    }

    @Override
    public void setCriteriumRenderer(final ICriteriumRenderStrategy inStrategy) { // NOPMD
        renderStrategy = inStrategy;
        traversal.setCriteriumRenderer(inStrategy);
    }

    @Override
    public void setCriteriaStackFactory(final CriteriaStackFactory inFactory) { // NOPMD
        traversal.setCriteriaStackFactory(inFactory);
    }

    @Override
    public void setGetValueStrategy(final IGetValueStrategy inGetValueStrategy) { // NOPMD
        getValueStrategy = inGetValueStrategy;
        traversal.setGetValueStrategy(inGetValueStrategy);
    }

    @Override
    public void setLevelReturnFormatter(final LevelReturnFormatter inFormatter) { // NOPMD
        levelFormatter = inFormatter;
        traversal.setLevelReturnFormatter(inFormatter);
    }

    // --- inner classes ---

    private class SQLCriteriumRenderer extends AbstractCriteriumRenderer implements Serializable { // NOPMD
        @Override
        public StringBuffer render() { // NOPMD
            return new StringBuffer(render2());
        }

        @Override
        public StringBuilder render2() { // NOPMD
            final StringBuilder outSQL = new StringBuilder(operand1);
            outSQL.append(' ');
            if (comparison.length() > 0) {
                outSQL.append(comparison).append(' ');
            }
            outSQL.append(operand2);
            return outSQL;
        }
    }

    private interface KeyObjectTraversal extends Serializable { // NOPMD
        void setCriteriumRenderer(ICriteriumRenderStrategy inStrategy); // NOPMD

        void setCriteriaStackFactory(CriteriaStackFactory inFactory); // NOPMD

        void setGetValueStrategy(IGetValueStrategy inStrategy); // NOPMD

        void setLevelReturnFormatter(LevelReturnFormatter inFormatter); // NOPMD
    }

    private interface KeyObjectRecursion extends Serializable { // NOPMD
        StringBuilder run(GeneralDomainObjectHome inDomainObjectHome); // NOPMD
    }

    private class NoRecursion implements KeyObjectRecursion { // NOPMD
        @Override
        public StringBuilder run(final GeneralDomainObjectHome inDomainObjectHome) { // NOPMD
            renderStrategy
            .setOperand1(getColumnModifier().modifyColumn(inDomainObjectHome.getColumnNameFor(getName())));
            renderStrategy.setOperand2(getValueStrategy.getValue(KeyCriterionImpl.this));
            renderStrategy.setComparison(getComparison());
            return renderStrategy.render2();
        }
    }

    private class NormalRecursion implements KeyObjectRecursion { // NOPMD
        @Override
        public StringBuilder run(final GeneralDomainObjectHome inDomainObjectHome) { // NOPMD
            return levelFormatter.format(((KeyObject) getValue()).render2(inDomainObjectHome));
        }
    }

    /** Interface to define how interwoven levels of keys are formatted.
     *
     * @author Luthiger Created on 25.08.2007 */
    public interface LevelReturnFormatter extends Serializable {
        StringBuilder format(StringBuilder inRenderedKey); // NOPMD
    }

    /** Implementation of <code>LevelReturnFormatter</code> to enclose interwoven levels in parenthesis.
     *
     * @see LevelReturnFormatter
     * @author Luthiger Created on 25.08.2007 */
    public class LevelReturnEnclosed implements LevelReturnFormatter { // NOPMD
        @Override
        public StringBuilder format(final StringBuilder inRenderedKey) { // NOPMD
            final StringBuilder out = new StringBuilder(30).append('(');
            out.append(inRenderedKey).append(')');
            return out;
        }
    }

}
