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

import org.hip.kernel.bom.KeyObject.BinaryBooleanOperator;
import org.hip.kernel.bom.impl.CriteriaStackFactory;
import org.hip.kernel.bom.impl.KeyCriterionImpl.LevelReturnFormatter;
import org.hip.kernel.util.SortableItem;

/** Interface for select criterion in SQL WHERE clause. With this object it is possible to create statements as
 *
 * <pre>
 * ... WHERE MODIFIER(field_name) COMPARISON VALUE LINK MODIFIER(field_name) COMPARISON VALUE
 * </pre>
 *
 * e.g.
 *
 * <pre>
 * ... WHERE UCASE(name) LIKE 'VI%' AND YEAR(mutaion_date) >= 2001
 * </pre>
 *
 * Created on 20.09.2002
 *
 * @author Benno Luthiger */
public interface KeyCriterion extends SortableItem {
    String NAME_FOR_KEY = "__keyObject(VIF)";

    /** Returns the name of the field used for the criterion.
     *
     * @param java.lang.String */
    String getName();

    /** Returns the value to match in the criterion.
     *
     * @param java.lang.Object */
    @Override
    Object getValue();

    /** Returns the comparison operator for the criterion.
     *
     * @param java.lang.String */
    String getComparison();

    /** Returns the binary boolean operator this criterion is using in the SQL WHERE clause.
     *
     * @param BinaryBooleanOperator */
    BinaryBooleanOperator getBinaryBooleanOperator();

    /** Returns the function to modify the field value in the criterion.
     *
     * @param java.lang.String */
    ColumnModifier getColumnModifier();

    /** Position of item in the owing list.
     *
     * @return int */
    int getPosition();

    /** Sets the position of the criterion in the owing list.
     *
     * @param inPosition int */
    void setPosition(int inPosition);

    /** Renders the criterion to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuffer the rendered SQL part.
     * @deprecated Use render() instead */
    @Deprecated
    StringBuffer render(GeneralDomainObjectHome inDomainObjectHome);

    /** Renders the criterion to a valid part in an SQL statement.
     *
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return StringBuffer the rendered SQL part. */
    StringBuilder render2(GeneralDomainObjectHome inDomainObjectHome);

    /** Sets the render strategy of this criterion and all contained <code>KeyObject</code>s.
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

}
