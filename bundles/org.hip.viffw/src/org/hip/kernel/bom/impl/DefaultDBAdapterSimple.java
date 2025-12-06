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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hip.kernel.bom.DBAdapterSimple;
import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.GroupByObject;
import org.hip.kernel.bom.HavingObject;
import org.hip.kernel.bom.IGetValueStrategy;
import org.hip.kernel.bom.IValueForSQL;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.LimitObject;
import org.hip.kernel.bom.OrderItem;
import org.hip.kernel.bom.OrderObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.StatisticsHome;
import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VObject;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.ListJoiner;
import org.hip.kernel.util.SortableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Default implementation for simple database adapters. It seems that no subclassing is needed. Else, the factory method
 * <code>DBAdapterType#getSimpleDBAdapter(ObjectDef)</code> has to be adjusted.
 *
 * Created on 31.08.2002
 *
 * @author Benno Luthiger
 * @see DBAdapterType#getSimpleDBAdapter(ObjectDef) */
public class DefaultDBAdapterSimple extends VObject implements DBAdapterSimple {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDBAdapterSimple.class);

    private final static String SQL_SELECT = "SELECT ";
    private static final String SQL_DISTINCT = "DISTINCT ";
    private final static String SQL_FROM = " FROM ";
    private final static String SQL_WHERE = " WHERE ";
    private final static String SQL_GROUP_BY = " GROUP BY ";
    private final static String SQL_HAVING = " HAVING ";
    private final static String SQL_ORDER_BY = " ORDER BY ";
    private final static String SQL_LIMIT = " LIMIT {0} OFFSET {1}";

    private transient final ObjectDef objectDef;
    private transient String columnList;
    private transient String tableNameString;

    /** Constructor for DefaultDBAdapterSimple. */
    public DefaultDBAdapterSimple(final ObjectDef inObjectDef) {
        super();
        objectDef = inObjectDef;
    }

    /** Returns the SQL string to insert a new entry.
     *
     * @param inTableName java.lang.String
     * @param inDomainObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createInsertString(final String inTableName, final DomainObject inDomainObject) {
        final StringBuilder outSQL = new StringBuilder("INSERT INTO ");
        outSQL.append(inTableName);

        final ListJoiner lColumns = new ListJoiner();
        final ListJoiner lValues = new ListJoiner();
        for (final MappingDef lMapping : objectDef.getMappingDefsForTable2(inTableName)) {
            // First we get the value
            final String lPropertyName = lMapping.getPropertyDef().getName();
            Object lValue = null;
            try {
                lValue = inDomainObject.get(lPropertyName);
            } catch (final GettingException exc) {
                DefaultExceptionHandler.instance().handle(exc);
            }

            // No further processing if the value is null or empty
            if (lValue == null || lValue.toString().length() == 0) {
                continue;
            }

            lColumns.addEntry(lMapping.getColumnName());
            final IValueForSQL lSQLValue = createValueForSQL(lValue);
            lValues.addEntry(lSQLValue.getValueAsString(), "%2$s%1$s%3$s", lSQLValue.getDelimiter(),
                    lSQLValue.getDelimiter());

        }

        outSQL.append("( ").append(lColumns.joinSpaced(",")).append(" ) VALUES (")
        .append(lValues.joinSpaced(",")).append(" )");

        final String out = new String(outSQL);
        LOG.debug("createInsertString {}", outSQL);
        return out;
    }

    /** Returns a Vector of prepared SQL strings to insert a new entry.
     *
     * @return java.util.Vector<String>
     * @deprecated */
    @Deprecated
    @Override
    public Vector<String> createPreparedInserts() { // NOPMD by lbenno
        return new Vector<String>(createPreparedInserts2()); // NOPMD by lbenno
    }

    @Override
    public List<String> createPreparedInserts2() { // NOPMD by lbenno
        final List<String> outPreparedInserts = new ArrayList<String>();
        for (final String lTableName : objectDef.getTableNames2()) {
            outPreparedInserts.add(getPreparedInsertString(lTableName));
        }
        return outPreparedInserts;
    }

    private String getPreparedInsertString(final String inTableName) {
        final StringBuilder outSQL = new StringBuilder("INSERT INTO ");
        outSQL.append(inTableName);

        final ListJoiner lColumns = new ListJoiner();
        final ListJoiner lValues = new ListJoiner();
        for (final MappingDef lMapping : objectDef.getMappingDefsForTable2(inTableName)) {
            lColumns.addEntry(lMapping.getColumnName());
            lValues.addEntry("?");
        }
        outSQL.append("( ").append(lColumns.joinSpaced(",")).append(" ) VALUES (")
                .append(lValues.joinSpaced(",")).append(" )");

        final String out = new String(outSQL);
        LOG.debug("getPreparedInsertString {}", outSQL);
        return out;
    }

    /** Returns the SQL string to delete an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createDeleteString(final String inTableName, final DomainObject inDomainObject) {
        String outSQL = "DELETE FROM " + inTableName;
        outSQL += createSQLWhere(SQL_WHERE, inDomainObject.getKey(), inDomainObject.getHome()); // NOPMD by lbenno

        LOG.debug("createDeleteString {}", outSQL);
        return outSQL;
    }

    /** Returns the SQL string to update an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createUpdateString(final String inTableName, final DomainObject inDomainObject) {
        return getUpdateString(false, inTableName, inDomainObject);
    }

    @Override
    public String createUpdateString(final DomainObjectHome inHome, final KeyObject inChange, final KeyObject inWhere) { // NOPMD
        return createUpdate(inHome, inChange, inWhere, new CriteriumValueStrategy());
    }

    /** Returns the SQL string to prepare an update of an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    @Override
    public String createPreparedUpdateString(final String inTableName, final DomainObject inDomainObject) {
        return getUpdateString(true, inTableName, inDomainObject);
    }

    private String getUpdateString(final boolean inPrepared, final String inTableName, final DomainObject inDomainObject) {
        final StringBuilder outSQL = new StringBuilder(1024);
        outSQL.append("UPDATE ").append(inTableName).append(" SET ");

        // We want to get all changed
        final ListJoiner lColumns = new ListJoiner();
        for (final Iterator<Property> lChanged = inDomainObject.getChangedProperties(); lChanged.hasNext();) {
            final Property lProperty = lChanged.next();
            final MappingDef lMapping = objectDef.getMappingDef(lProperty.getName());
            if (!lMapping.getTableName().equals(inTableName)) {
                continue;
            }

            String lEqualed = "?";
            if (!inPrepared) {
                final IValueForSQL lSQLValue = createValueForSQL(lProperty.getValue());
                lEqualed = lSQLValue.getDelimiter() + lSQLValue.getValueAsString() + lSQLValue.getDelimiter();
            }
            lColumns.addEntry(lMapping.getColumnName(), "%1$s = %2$s", lEqualed);
        }
        outSQL.append(lColumns.joinSpaced(","));

        if (inPrepared) {
            outSQL.append(createSQLPreparedWhere(SQL_WHERE, inDomainObject.getKey(false), inDomainObject.getHome()));
        }
        else {
            outSQL.append(createSQLWhere(SQL_WHERE, inDomainObject.getKey(true), inDomainObject.getHome()));
        }
        return new String(outSQL);
    }

    /** Returns a Vector of prepared SQL strings to update entries.
     *
     * @return java.util.Vector<String>
     * @deprecated */
    @Deprecated
    @Override
    public Vector<String> createPreparedUpdates() { // NOPMD by lbenno
        return new Vector<String>(createPreparedUpdates2()); // NOPMD by lbenno
    }

    @Override
    public List<String> createPreparedUpdates2() { // NOPMD by lbenno
        final List<String> outPreparedUpdates = new ArrayList<String>();
        for (final String lTableName : objectDef.getTableNames2()) {
            outPreparedUpdates.add(getPreparedUpdateString(lTableName));
        }
        return outPreparedUpdates;
    }

    /** Returns a SQL command usable for a prepared update.
     *
     * @param inHome DomainObjectHome
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String */
    @Override
    public String createPreparedUpdate(final DomainObjectHome inHome, final KeyObject inChange, final KeyObject inWhere) {
        return createUpdate(inHome, inChange, inWhere, new PreparedValueStrategy());
    }

    private String createUpdate(final DomainObjectHome inHome, final KeyObject inChange, final KeyObject inWhere, // NOPMD
                                                                                                                  // by
                                                                                                                  // lbenno
            final IGetValueStrategy inGetValueStrategy) {
        final String lTableName = objectDef.getTableNames2().toArray(new String[1])[0];
        final StringBuilder outSQL = new StringBuilder("UPDATE " + lTableName + " SET ");
        inChange.setGetValueStrategy(inGetValueStrategy);
        inChange.setCriteriaStackFactory(new CriteriaStackFactory(CriteriaStackFactory.StackType.FLAT_JOIN, ", "));
        outSQL.append(inChange.render2(inHome)).append(SQL_WHERE);
        inWhere.setGetValueStrategy(inGetValueStrategy);
        outSQL.append(inWhere.render2(inHome));
        return new String(outSQL);
    }

    private String getPreparedUpdateString(final String inTableName) {
        final StringBuilder outSQL = new StringBuilder(1024).append("UPDATE ").append(inTableName).append(" SET ");

        ListJoiner lColumnNames = new ListJoiner();
        for (final MappingDef lMapping : objectDef.getMappingDefsForTable2(inTableName)) {
            final String lPropertyName = lMapping.getPropertyDef().getName();
            if (isKeyPropertyName(lPropertyName)) {
                continue;
            }
            lColumnNames.addEntry(lMapping.getColumnName(), "%1$s = ?");
        }
        outSQL.append(lColumnNames.joinSpaced(",")); // NOPMD by lbenno

        // now the primary key
        outSQL.append(SQL_WHERE);
        lColumnNames = new ListJoiner();
        for (final MappingDef lMapping : objectDef.getMappingDefsForTable2(inTableName)) {
            final String lPropertyName = lMapping.getPropertyDef().getName();
            if (isKeyPropertyName(lPropertyName)) {
                lColumnNames.addEntry(lMapping.getColumnName(), "%1$s = ?");
            }
        }
        outSQL.append(lColumnNames.joinSpaced(" AND"));
        return new String(outSQL);
    }

    /** Returns true if passed columnName is part of the primary key.
     *
     * @return boolean
     * @param inPropertyName java.lang.String the name of the property, i.e. the column. */
    private boolean isKeyPropertyName(final String inPropertyName) {
        for (final String lKeyName : objectDef.getPrimaryKeyDef().getKeyNames2()) {
            if (inPropertyName.equals(lKeyName)) {
                return true;
            }
        }
        return false;
    }

    /** Creates the select SQL string counting all table entries corresponding to the specified home.
     *
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome;
     * @return java.lang.String */
    @Override
    public String createCountAllString(final DomainObjectHome inDomainObjectHome) {
        String outSQL = getSelect(false) + createKeyCountColumnList(inDomainObjectHome);
        outSQL += SQL_FROM + getTableNameString(); // NOPMD by lbenno

        LOG.debug("createCountAllString {}", outSQL);
        return outSQL;
    }

    private String getSelect(final boolean inDistinct) {
        final StringBuilder out = new StringBuilder(SQL_SELECT);
        out.append(inDistinct ? SQL_DISTINCT : "");
        return new String(out);
    }

    /** Creates the select SQL string counting all table entries with the specified key corresponding to the specified
     * home.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome;
     * @return java.lang.String */
    @Override
    public String createCountString(final KeyObject inKey, final DomainObjectHome inDomainObjectHome) {
        final StringBuilder outSQL = new StringBuilder(1024).append(getSelect(inKey.isDistinct()))
                .append(createKeyCountColumnList(inDomainObjectHome)).append(SQL_FROM).append(getTableNameString())
                .append(createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome));

        final String out = new String(outSQL);
        LOG.debug("createCountString {}", outSQL);
        return out;
    }

    /** @param inKey org.hip.kernel.bom.KeyObject
     * @param inHaving HavingObject
     * @param inGroupBy GroupByObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome;
     * @return java.lang.String */
    @Override
    public String createCountString(final KeyObject inKey, final HavingObject inHaving, final GroupByObject inGroupBy,
            final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createCountAllString(inDomainObjectHome) +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createOrderBy(SQL_GROUP_BY, inGroupBy, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome);

        LOG.debug("createCountString {}", outSQL);
        return outSQL;
    }

    /** Creates the select SQL string returning the calculated value(s) according to the modify strategy.
     *
     * @param inStrategy ModifierStrategy
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome
     * @return String */
    @Override
    public String createModifiedString(final ModifierStrategy inStrategy, final DomainObjectHome inDomainObjectHome) {
        final StringBuilder outSQL = new StringBuilder("SELECT ")
                .append(inStrategy.createModifiedSQL(inDomainObjectHome));
        outSQL.append(SQL_FROM).append(getTableNameString());

        final String out = new String(outSQL);
        LOG.debug("createModifiedString {}", outSQL);
        return out;
    }

    /** Creates the select SQL string returning the calculated value(s) according to the modify strategy.
     *
     * @param inStrategy ModifierStrategy
     * @param inKey org.hip.kernel.bom.KeyObject for the sub selection.
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome
     * @return String */
    @Override
    public String createModifiedString(final ModifierStrategy inStrategy, final KeyObject inKey,
            final DomainObjectHome inDomainObjectHome) {
        final StringBuilder outSQL = new StringBuilder(createModifiedString(inStrategy, inDomainObjectHome));
        outSQL.append(createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome));

        final String out = new String(outSQL);
        LOG.debug("createModifiedString {}", outSQL);
        return out;
    }

    /** Creates select SQL string to fetch all domain objects managed by the specified home.
     *
     * @return java.lang.String */
    @Override
    public String createSelectAllString() {
        final StringBuilder outSQL = new StringBuilder(getSelect(false));
        outSQL.append(createColumnList()).append(SQL_FROM).append(getTableNameString());
        return new String(outSQL);
    }

    /** Creates the select SQL string to fetch all domain objects with the specified key managed by the specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    @Override
    public String createSelectString(final KeyObject inKey, final DomainObjectHome inDomainObjectHome) {
        final StringBuilder outSQL = new StringBuilder(1024).append(getSelect(inKey.isDistinct()))
                .append(createColumnList()).append(SQL_FROM).append(getTableNameString())
                .append(createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome));

        final String out = new String(outSQL);
        LOG.debug("createSelectString {}", outSQL); // NOPMD by lbenno
        return out;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final StatisticsHome inDomainObjectHome) { // NOPMD by
        // lbenno
        final StringBuilder outSQL = new StringBuilder(getSelect(inKey.isDistinct()));
        outSQL.append(createColumnList()).append(SQL_FROM).append(getTableNameString())
        .append(createSQLWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome));

        final String out = new String(outSQL);
        LOG.debug("createSelectString {}", outSQL);
        return out;
    }

    /** Creates the select SQL string to fetch all domain objects with the specified key that meet the specified having
     * criterion managed by the specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inHaving org.hip.kernel.bom.HavingObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    @Override
    public String createSelectString(final KeyObject inKey, final HavingObject inHaving,
            final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final HavingObject inHaving, // NOPMD by lbenno
            final StatisticsHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, (GeneralDomainObjectHome) inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, // NOPMD by lbenno
            final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, // NOPMD by lbenno
            final StatisticsHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, (GeneralDomainObjectHome) inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving, // NOPMD
            // by
            // lbenno
            final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving, // NOPMD
            // by
            // lbenno
            final StatisticsHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, (GeneralDomainObjectHome) inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, (GeneralDomainObjectHome) inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving, // NOPMD
            // by
            // lbenno
            final GroupByObject inGroupBy, final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createOrderBy(SQL_GROUP_BY, inGroupBy, inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final OrderObject inOrder, final HavingObject inHaving, // NOPMD
            final GroupByObject inGroupBy, final StatisticsHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome) +
                createOrderBy(SQL_GROUP_BY, inGroupBy, (GeneralDomainObjectHome) inDomainObjectHome) +
                createSQLWhere(SQL_HAVING, inHaving, (GeneralDomainObjectHome) inDomainObjectHome) +
                createOrderBy(SQL_ORDER_BY, inOrder, (GeneralDomainObjectHome) inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final OrderObject inOrder, final DomainObjectHome inDomainObjectHome) { // NOPMD by
        // lbenno
        final String outSQL = createSelectAllString() +
                createOrderBy(SQL_ORDER_BY, inOrder, inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final OrderObject inOrder, final StatisticsHome inDomainObjectHome) { // NOPMD by
        // lbenno
        final String outSQL = createSelectAllString() +
                createOrderBy(SQL_ORDER_BY, inOrder, (GeneralDomainObjectHome) inDomainObjectHome);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createSelectString(final KeyObject inKey, final LimitObject inLimit, // NOPMD by lbenno
            final DomainObjectHome inDomainObjectHome) {
        final String outSQL = createSelectAllString() +
                createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome) +
                createLimitBy(SQL_LIMIT, inLimit);

        LOG.debug("createSelectString {}", outSQL);
        return outSQL;
    }

    /** Creates the prepared select SQL string to fetch all domain objects with the specified key managed by the
     * specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    @Override
    public String createPreparedSelectString(final KeyObject inKey, final DomainObjectHome inDomainObjectHome) {
        String outSQL = createSelectAllString();
        outSQL += createSQLPreparedWhere(SQL_WHERE, inKey, inDomainObjectHome); // NOPMD by lbenno

        LOG.debug("createPreparedSelectString {}", outSQL);
        return outSQL;
    }

    @Override
    public String createPreparedSelectString(final KeyObject inKey, final StatisticsHome inDomainObjectHome) { // NOPMD
        final StringBuilder outSQL = new StringBuilder(1024).append(getSelect(inKey.isDistinct()))
                .append(createColumnList()).append(SQL_FROM).append(getTableNameString())
                .append(createSQLPreparedWhere(SQL_WHERE, inKey, (GeneralDomainObjectHome) inDomainObjectHome));

        final String out = new String(outSQL);
        LOG.debug("createPreparedSelectString {}", outSQL);
        return out;
    }

    /** Creates select SQL string to delete all domain objects with the specified key managed by the specified home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.DomainObjectHome; */
    @Override
    public String createDeleteString(final KeyObject inKey, final DomainObjectHome inDomainObjectHome) {
        String outSQL = "DELETE" + SQL_FROM + getTableNameString();
        outSQL += createSQLWhere(SQL_WHERE, inKey, inDomainObjectHome); // NOPMD by lbenno

        LOG.debug("createDeleteString {}", outSQL);
        return outSQL;
    }

    /** Creates the WHERE or HAVING part of the SQL select string.
     *
     * @param inSqlPart String WHERE or HAVING to be set at the beginning of the clause.
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String */
    private String createSQLWhere(final String inSqlPart, final KeyObject inKey,
            final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inKey not null
        if (inKey == null) {
            return "";
        }
        if (inKey.getItems2().size() == 0) {
            return "";
        }

        final StringBuilder outSQL = new StringBuilder(inSqlPart);
        outSQL.append(inKey.render2(inDomainObjectHome));
        return new String(outSQL);
    }

    /** Creates the WHERE or HAVING part of a prepared SQL select string.
     *
     * @param inSqlPart String WHERE or HAVING to be set at the beginning of the clause.
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String */
    private String createSQLPreparedWhere(final String inSqlPart, final KeyObject inKey,
            final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inKey not null
        if (inKey == null) {
            return "";
        }
        if (inKey.getItems2().size() == 0) {
            return "";
        }

        final StringBuilder outSQL = new StringBuilder(inSqlPart);
        inKey.setGetValueStrategy(new PreparedValueStrategy());
        outSQL.append(inKey.render2(inDomainObjectHome));
        return new String(outSQL);
    }

    /** Creates the ORDER BY or GROUP BY part of the SQL select string.
     *
     * @param inSqlPart String ORDER BY or GROUP BY to be set at the beginning of the clause.
     * @param inOrder org.hip.kernel.bom.OrderObject
     * @param inDomainObjectHome org.hip.kernel.bom.GeneralDomainObjectHome;
     * @return java.lang.String */
    private String createOrderBy(final String inSqlPart, final OrderObject inOrder,
            final GeneralDomainObjectHome inDomainObjectHome) {
        // Pre: inOrder not null
        if (VSys.assertNotNull(this, "createOrderBy", inOrder) == Assert.FAILURE) {
            return "";
        }
        if (inOrder.getItems2().size() == 0) {
            return "";
        }

        final ListJoiner lJoiner = new ListJoiner();
        for (final SortableItem lItem : inOrder.getItems2()) {
            lJoiner.addEntry(inDomainObjectHome.getColumnNameFor(((OrderItem) lItem).getColumnName()),
                    "%1$s %2$s",
                    ((OrderItem) lItem).isDescending() ? "DESC" : "");
        }
        return inSqlPart + lJoiner.joinSpaced(",");
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

    /** This method looks for all key columns of the table mapped to the DomainObject managed by the specified home and
     * creates a list COUNT(keyField). Instead of sending SELECT COUNT(*) FROM ... this SQL-sequence can be used to
     * count all entries in a table with better performance.
     *
     * @return java.lang.String */
    @Override
    public String createKeyCountColumnList(final DomainObjectHome inDomainObjectHome) {
        final ListJoiner lList = new ListJoiner();
        for (final String lKeyName : objectDef.getPrimaryKeyDef().getKeyNames2()) {
            lList.addEntry(inDomainObjectHome.getColumnNameFor(lKeyName), "COUNT(%1$s)");
        }
        return lList.joinSpaced(",");
    }

    /** This method creates a list of all columns of the table mapped to the DomainObject managed by this home. This
     * method can be used to create the SQL string.
     *
     * @return java.lang.String */
    private String createColumnList() {
        if (columnList == null) {
            final ListJoiner lColumns = new ListJoiner();
            for (final PropertyDef lProperty : objectDef.getPropertyDefs2()) {
                final MappingDef lMapping = lProperty.getMappingDef();
                lColumns.addEntry(lMapping.getColumnName(), "%2$s.%1$s", lMapping.getTableName());
            }
            columnList = lColumns.joinSpaced(",");
        }
        return columnList;
    }

    /** Generates the list of table names used for the SQL FROM clause.
     *
     * @return String */
    private String getTableNameString() {
        if (tableNameString == null) {
            final ListJoiner lTableNames = new ListJoiner();

            for (final String lTableName : objectDef.getTableNames2()) {
                lTableNames.addEntry(lTableName);
            }
            tableNameString = lTableNames.joinSpaced(",");
        }
        return tableNameString;
    }

    /** Factory method to create instances of <code>IValueForSQL</code>. The default implementation returns an instance
     * of <code>ValueForSQL</code>.
     *
     * @param inValue Object
     * @return IValueForSQL */
    protected IValueForSQL createValueForSQL(final Object inValue) {
        return new ValueForSQL(inValue);
    }

}
