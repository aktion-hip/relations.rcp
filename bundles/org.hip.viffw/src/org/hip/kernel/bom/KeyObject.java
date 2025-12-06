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

package org.hip.kernel.bom;

import org.hip.kernel.bom.impl.CriteriaStackFactory;
import org.hip.kernel.bom.impl.KeyCriterionImpl.LevelReturnFormatter;
import org.hip.kernel.util.SortedList;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** KeyObjects are used to find DomainObjects. They are a special form of a SortedList list. They hold list items (i.e.
 * select criteria) in the order they have added to the KeyObject.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.util.SortedList */
public interface KeyObject extends SortedList {
    /** Binary Operator enum for the KeyObject. */
    public enum BinaryBooleanOperator {
        AND {
            @Override
            public String render() { // NOPMD
                return "AND";
            };

            @Override
            public String getPrefixOperand() { // NOPMD
                return "&";
            }
        },
        OR {
            @Override
            public String render() { // NOPMD
                return "OR";
            };

            @Override
            public String getPrefixOperand() { // NOPMD
                return "|";
            }
        },
        XOR {
            @Override
            public String render() { // NOPMD
                return "XOR";
            };

            @Override
            public String getPrefixOperand() { // NOPMD
                return "|";
            }
        };

        /** @return String */
        abstract public String render();

        /** @return String */
        abstract public String getPrefixOperand();
    }

    /** @param schemaName java.lang.String */
    String getSchemaName();

    /** @return boolean */
    boolean isPrimaryKey();

    /** @param inSchemaName java.lang.String */
    void setSchemaName(String inSchemaName);

    /** Sets name and value of a key object.
     *
     * @param inName java.lang.String Column/field name
     * @param inValue java.lang.Object Filtering value
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException */
    void setValue(String inName, Object inValue) throws VInvalidNameException, VInvalidValueException;

    /** Sets name and value of a key object plus the comparison operator.
     *
     * @param inName java.lang.String Column/field name
     * @param inValue java.lang.Object Filtering value
     * @param inComparison java.lang.String e.g. '>='
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException */
    void setValue(String inName, Object inValue, String inComparison) throws VInvalidNameException,
            VInvalidValueException;

    /** @param inName java.lang.String Column/field name
     * @param inValue java.lang.Object Filtering value
     * @param inComparison java.lang.String e.g. '>='
     * @param inLinkType boolean the type this key is appended to the preceding, true for AND, false for OR
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException
     * @deprecated use setValue(String inName, Object inValue, String inComparison, BinaryBooleanOperator
     *             inBinaryOperator) instead */
    @Deprecated
    void setValue(String inName, Object inValue, String inComparison, boolean inLinkType) throws VInvalidNameException,
            VInvalidValueException;

    /** Sets name and value of a key object plus comparison operator and link type.
     *
     * @param inName String Column/field name
     * @param inValue Object Filtering value
     * @param inComparison String e.g. '>='
     * @param inBinaryOperator BinaryBooleanOperator the boolean operator used on this and the preceding key, e.g. 'AND'
     * @throws VInvalidNameException
     * @throws VInvalidValueException */
    void setValue(String inName, Object inValue, String inComparison, BinaryBooleanOperator inBinaryOperator)
            throws VInvalidNameException, VInvalidValueException;

    /** @param inName java.lang.String Column/field name
     * @param inValue java.lang.Object Filtering value
     * @param inComparison java.lang.String e.g. '>='
     * @param inLinkType boolean the type this key is appended to the preceding, true for AND, false for OR
     * @param inColumnModifier org.hip.kernel.bom.ColumnModifier e.g. UCASE()
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException
     * @deprecated use setValue(String inName, Object inValue, String inComparison, BinaryBooleanOperator
     *             inBinaryOperator, ColumnModifier inColumnModifier) instead */
    @Deprecated
    void setValue(String inName, Object inValue, String inComparison, boolean inLinkType,
            ColumnModifier inColumnModifier) throws VInvalidNameException, VInvalidValueException;

    /** Sets name and value of a key object plus comparison operator, link type and column modifier.
     *
     * @param inName String Column/field name
     * @param inValue Object Filtering value
     * @param inComparison String e.g. '>='
     * @param inBinaryOperator BinaryBooleanOperator the boolean operator used on this and the preceding key, e.g. 'AND'
     * @param inColumnModifier ColumnModifier e.g. UCASE()
     * @throws VInvalidNameException
     * @throws VInvalidValueException */
    void setValue(String inName, Object inValue, String inComparison, BinaryBooleanOperator inBinaryOperator,
            ColumnModifier inColumnModifier) throws VInvalidNameException, VInvalidValueException;

    /** Sets a KeyObject as group in an enclosing KeyObject. This method can be used to create SQL expressions like<br/>
     * KEY1 AND (KEY2)<br/>
     * e.g. date >= 20030801 AND (name LIKE 'VI%' OR name LIKE 'vi%')
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException */
    void setValue(KeyObject inKey) throws VInvalidNameException, VInvalidValueException;

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inLinkType boolean the type this key is appended to the preceding, true for AND, false for OR
     * @throws org.hip.kernel.util.VInvalidNameException
     * @throws org.hip.kernel.util.VInvalidValueException
     * @deprecated use setValue(KeyObject inKey, BinaryBooleanOperator inBinaryOperator) instead. */
    @Deprecated
    void setValue(KeyObject inKey, boolean inLinkType) throws VInvalidNameException, VInvalidValueException;

    /** Sets a KeyObject as group in an enclosing KeyObject. This method can be used to create SQL expressions like<br/>
     * KEY1 AND/OR (KEY2)<br/>
     * e.g. date >= 20030801 OR (name LIKE 'VI%' OR name LIKE 'vi%')
     *
     * @param inKey KeyObject
     * @param inBinaryOperator BinaryBooleanOperator
     * @throws VInvalidNameException
     * @throws VInvalidValueException */
    void setValue(KeyObject inKey, BinaryBooleanOperator inBinaryOperator) throws VInvalidNameException,
            VInvalidValueException;

    /** Renders the key to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuffer the rendered SQL part.
     * @deprecated Usae render2() instead */
    @Deprecated
    StringBuffer render(GeneralDomainObjectHome inDomainObjectHome);

    /** Renders the key to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuilder the rendered SQL part. */
    StringBuilder render2(GeneralDomainObjectHome inDomainObjectHome);

    /** Sets the render strategy of all contained <code>KeyCriterion</code> objects.
     *
     * @param inStrategy ICriteriumRenderStrategy */
    void setCriteriumRenderer(ICriteriumRenderStrategy inStrategy);

    /** Sets the factory that creates the stack instances defining how the sequence of criteria and operators is
     * rendered.
     *
     * @param inFactory CriteriaStackFactory */
    void setCriteriaStackFactory(CriteriaStackFactory inFactory);

    /** Sets the strategy defining how the criterium value is retrieved.
     *
     * @param inGetValueStrategy IGetValueStrategy */
    void setGetValueStrategy(IGetValueStrategy inGetValueStrategy);

    /** Sets the formatter object for special treatment when the recursion returns the rendered <code>KeyObject</code>.
     *
     * @param inFormatter LevelReturnFormatter */
    void setLevelReturnFormatter(LevelReturnFormatter inFormatter);

    /** @param inDistinct boolean */
    void setDistinct(boolean inDistinct);

    /** Should the retrieved entries be distinct?
     *
     * @return boolean */
    boolean isDistinct();

}
