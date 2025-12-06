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

package org.hip.kernel.bom.impl; // NOPMD

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.hip.kernel.bom.BOMException;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderItem;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.model.JoinDef;
import org.hip.kernel.bom.model.JoinOperation;
import org.hip.kernel.bom.model.JoinedObjectDef;
import org.hip.kernel.bom.model.JoinedObjectDefDef;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.ListJoiner;
import org.hip.kernel.util.SortableItem;
import org.hip.kernel.util.VNameValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This abstract class provides generic functionality for joined database adapters.
 *
 * Created on 07.09.2002
 *
 * @author Benno Luthiger */
public abstract class AbstractDBAdapterJoin extends VObject {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDBAdapterJoin.class);

    private final static String SQL_SELECT = "SELECT ";
    private static final String SQL_DISTINCT = "DISTINCT ";
    protected final static String SQL_WHERE = " WHERE ";
    private final static String SQL_GROUP_BY = " GROUP BY ";
    private final static String SQL_HAVING = " HAVING ";
    private final static String SQL_ORDER_BY = " ORDER BY ";
    private final static String SQL_LIMIT = " LIMIT {0} OFFSET {1}";

    private static final String[][] JOIN_TYPES = {
        { "EQUI_JOIN", " INNER JOIN " },
        { "LEFT_JOIN", " LEFT JOIN " },
        { "LEFT_JOIN_OUTER", " LEFT OUTER JOIN " },
        { "RIGHT_JOIN", " RIGHT JOIN " },
        { "RIGHT_JOIN_OUTER", " RIGHT OUTER JOIN " }
    };

    private transient final JoinedObjectDef objectDef;
    private transient JoinDef joinDef;
    private transient String countSQL;
    private transient String selectSQL;

    /** Constructor for AbstractDBAdapterJoin. */
    public AbstractDBAdapterJoin(final JoinedObjectDef inObjectDef) {
        super();
        objectDef = inObjectDef;
    }

    /** Returns the SQL statement counting the entries in the join.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createCountAllSQL() throws BOMException {
        final String out = getCountSQL(false);
        LOG.debug("createCountAllSQL {}", out);
        return out;
    }

    /** Returns the SQL SELECT statement doing the JOIN.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createSelectAllSQL() throws BOMException {
        final String outSQL = getSelectSQL(false);
        LOG.debug("createSelectAllSQL {}", outSQL);
        return outSQL;
    }

    /** Returns the SQL SELECT statement ordered by the specified object.
     *
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createSelectSQL(final OrderObject inOrder, final GeneralDomainObjectHome inDomainObjectHome)
            throws BOMException {
        final String outSQL = getSelectSQL(false) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);
        LOG.debug("createSelectSQL {}", outSQL); // NOPMD
        return outSQL;
    }

    /** @param inKey {@link KeyObject}
     * @param inLimit {@link LimitObject}
     * @param inDomainObjectHome {@link DomainObjectHome}
     * @return String the SQL select statement
     * @throws BOMException */
    public String createSelectSQL(final KeyObject inKey, final LimitObject inLimit,
            final GeneralDomainObjectHome inDomainObjectHome) throws BOMException {
        final String outSQL = getSelectSQL(inKey.isDistinct()) +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createLimitBy(SQL_LIMIT, inLimit);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    /** @param inDistinct boolean
     * @return String the SQL select statement
     * @throws BOMException */
    protected String getSelectSQL(final boolean inDistinct) throws BOMException {
        if (selectSQL == null) {
            selectSQL = createSelectSQL(inDistinct);
        }
        return selectSQL;
    }

    /** @param inDistinct boolean
     * @return String the SQL count statement
     * @throws BOMException */
    protected String getCountSQL(final boolean inDistinct) throws BOMException {
        if (countSQL == null) {
            countSQL = createCountSQL(inDistinct);
        }
        return countSQL;
    }

    private String createSelectSQL(final boolean inDistinct) throws BOMException {
        final StringBuilder out = new StringBuilder(SQL_SELECT);
        out.append(inDistinct ? SQL_DISTINCT : "").append(getFields()).append(' ').append(getFromSQL());
        return new String(out);
    }

    private String createCountSQL(final boolean inDistinct) throws BOMException {
        final StringBuilder out = new StringBuilder(SQL_SELECT);
        out.append(inDistinct ? SQL_DISTINCT : "").append(getCountFields()).append(' ').append(getFromSQL());
        return new String(out);
    }

    /** @return String the fields for the SQL statement */
    protected String getFields() {
        final ListJoiner outSQL = new ListJoiner();
        for (final String lColumn : getColumns()) {
            outSQL.addEntry(lColumn);
        }
        return outSQL.joinSpaced(",");
    }

    /** @return String */
    protected String getCountFields() {
        final StringBuilder outSQL = new StringBuilder();
        final Iterator<String> lColumns = getColumns().iterator();
        if (lColumns.hasNext()) {
            outSQL.append("COUNT(").append(lColumns.next()).append(')');
        }
        return new String(outSQL);
    }

    /** @return Collection<String> */
    @SuppressWarnings("unchecked")
    private Collection<String> getColumns() {
        try {
            Collection<String> outColumns = (Collection<String>) objectDef.get(JoinedObjectDefDef.columnDefs);
            if (outColumns == null) {
                outColumns = new ArrayList<String>();
                objectDef.set(JoinedObjectDefDef.columnDefs, outColumns);
            }
            return outColumns;
        } catch (final VNameValueException exc) {
            DefaultExceptionWriter.printOut(this, exc, true);
            return new ArrayList<String>();
        }
    }

    /** @return {@link JoinDef}
     * @throws BOMException */
    protected JoinDef getJoinDef() throws BOMException {
        if (joinDef == null) {
            joinDef = objectDef.getJoinDef();
        }
        return joinDef;
    }

    /** Returns the type associated with the specified key in the specified array of types.
     *
     * @param inTypes java.lang.String[][] Array with types in first column and keys in second column.
     * @param inTypeKey java.lang.String
     * @return java.lang.String */
    protected String findType(final String[][] inTypes, final String inTypeKey) throws BOMException {
        if ("NO_JOIN".equals(inTypeKey)) {
            return "";
        }

        String outType = "";
        for (int i = inTypes.length - 1; i >= 0; i--) {
            if (inTypeKey.equals(inTypes[i][0])) {
                outType = inTypes[i][1];
                break;
            }
        }
        if (outType.isEmpty()) {
            throw new BOMException("No correct type defined.");
        }
        return outType;
    }

    /** Creates the WHERE or HAVING part of the SQL select string.
     *
     * @param inSQLPart SQL WHERE or HAVING beginning the clause.
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @param boolean inOrdered If true, an ORDER BY part is added.
     * @return java.lang.String */
    protected String createSQLWhere(final String inSQLPart, final KeyObject inKey,
            final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inKey not null
        if (inKey == null) {
            return "";
        }
        if (inKey.getItems2().size() == 0) {
            return "";
        }

        final StringBuilder outSQL = new StringBuilder(inSQLPart);
        outSQL.append(inKey.render2(inDomainObjectHome));
        return new String(outSQL);
    }

    /** Creates the ORDER BY part of the SQL select string.
     *
     * @param inSQLPart SQL ORDER BY or GROUP BY beginning the clause.
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String */
    protected String createOrderBy(final String inSQLPart, final OrderObject inOrder,
            final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inOrder not null
        if (VSys.assertNotNull(this, "createOrderBy", inOrder) == Assert.FAILURE) {
            return "";
        }
        if (inOrder.getItems2().size() == 0) {
            return "";
        }

        final ListJoiner outSQL = new ListJoiner();
        for (final SortableItem lItem : inOrder.getItems2()) {
            outSQL.addEntry(inDomainObjectHome.getColumnNameFor(((OrderItem) lItem).getColumnName()),
                    "%1$s%2$s",
                    ((OrderItem) lItem).isDescending() ? " DESC" : "");
        }
        return inSQLPart + outSQL.joinSpaced(",");
    }

    /** Creates the limitation part of the SQL select.
     *
     * @param inLimitPart String LIMIT # OFFSET #
     * @param inLimit LimitObject
     * @return String */
    private String createLimitBy(final String inLimitPart, final LimitObject inLimit) {
        // Pre: inLimit not null
        if (VSys.assertNotNull(this, "createLimitBy", inLimit) == Assert.FAILURE) {
            return "";
        }

        return MessageFormat.format(inLimitPart, inLimit.getArguments());
    }

    /** Resets the SQL cache for that it can be recreated again. */
    public void reset() {
        selectSQL = null; // NOPMD
    }

    /** Renders the from part of the SQL JOIN statement. Subclasses may override.
     *
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    protected String getFromSQL() throws BOMException {
        final StringBuilder outSQL = new StringBuilder();
        String lLeftmostTable = "";
        boolean lFirst = true;
        for (final Iterator<?> lOperations = getJoinDef().getJoinOperations().iterator(); lOperations.hasNext();) {
            final JoinOperation lOperation = (JoinOperation) lOperations.next();
            if (lFirst) {
                lLeftmostTable = lOperation.getLeftTableName();
            }
            lFirst = false;
            final String lJoinType = lOperation.getJoinType();
            if (!JoinDef.NO_JOIN_TYPE.equals(lJoinType)) {
                outSQL.append(findType(getJoinTypes(), lOperation.getJoinType()));
                outSQL.append((lOperation.getRightTableName().equals("") ? lOperation.getLeftTableName() : lOperation
                        .getRightTableName())).append(" ON ");
                outSQL.append(lOperation.renderSQL());
            }
        }
        return "FROM " + lLeftmostTable + new String(outSQL);
    }

    /** Subclasses may override (to provide their join types).
     *
     * @return String[][] [DTD-JoinType][vendor specific JoinType] */
    protected String[][] getJoinTypes() {
        return JOIN_TYPES; // NOPMD
    }

    /** Returns the SQL SELECT statement matching the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createSelectSQL(final KeyObject inKey, final GeneralDomainObjectHome inDomainObjectHome)
            throws BOMException {
        final String outSQL = getSelectSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inDomainObjectHome);
        LOG.debug("createSelectSQL {}", outSQL);
        return outSQL;
    }

    /** Returns the SQL SELECT statement matching the specified key ordered by the specified object.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createSelectSQL(final KeyObject inKey, final OrderObject inOrder,
            final GeneralDomainObjectHome inDomainObjectHome) throws BOMException {
        final String outSQL = getSelectSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);
        LOG.debug("createSelectSQL {}", outSQL);
        return outSQL;
    }

    /** @param inKey KeyObject
     * @param inOrder OrderObject
     * @param inHaving HavingObject
     * @return String
     * @throws BOMException
     * @see org.hip.kernel.bom.DBAdapterJoin#createSelectSQL */
    public String createSelectSQL(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GeneralDomainObjectHome inDomainObjectHome) throws BOMException {
        final String outSQL = getSelectSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);
        LOG.debug("createSelectSQL {}", outSQL);
        return outSQL;
    }

    /** @param inKey KeyObject
     * @param inOrder OrderObject
     * @param inHaving HavingObject
     * @param inGroupBy GroupByObject
     * @param inDomainObjectHome GeneralDomainObjectHome
     * @return String
     * @throws BOMException */
    public String createSelectSQL(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving,
            final GroupByObject inGroupBy, final GeneralDomainObjectHome inDomainObjectHome) throws BOMException {
        final String outSQL = getSelectSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inDomainObjectHome) +
                createOrderBy(SQL_GROUP_BY, inGroupBy, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);
        LOG.debug("createSelectSQL {}", outSQL);
        return outSQL;
    }

    /** Returns the SQL statement counting the entries in the join matching the specified key.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createCountSQL(final KeyObject inKey, final GeneralDomainObjectHome inHome) throws BOMException {
        final String outSQL = getCountSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inHome);
        LOG.debug("createCountSQL {}", outSQL);
        return outSQL;
    }

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inHaving HavingObject
     * @param inGroupBy GroupByObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String
     * @throws org.hip.kernel.bom.BOMException */
    public String createCountSQL(final KeyObject inKey, final HavingObject inHaving, final GroupByObject inGroupBy,
            final GeneralDomainObjectHome inHome) throws BOMException {
        final String outSQL = getCountSQL(inKey.isDistinct()) +
                createSQLWhere(getWhereAdder(), inKey, inHome) +
                createOrderBy(SQL_GROUP_BY, inGroupBy, inHome) +
                createSQLWhere(SQL_HAVING, inHaving, inHome);
        LOG.debug("createCountSQL {}", outSQL);
        return outSQL;
    }

    /** Different DBs add the SQL WHERE clause using different terms. Hook for subclasses.
     *
     * @return String */
    protected abstract String getWhereAdder();
}