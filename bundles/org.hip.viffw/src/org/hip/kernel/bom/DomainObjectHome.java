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

package org.hip.kernel.bom;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.NamingException;

import org.hip.kernel.bom.impl.ModifierStrategy;
import org.hip.kernel.exc.VException;

/** This interface defines the responsibilities of "Homes". Conceptually, this Home and all subclasses are similar the
 * EJBHome.
 *
 * @author Benno Luthiger */
public interface DomainObjectHome extends GeneralDomainObjectHome { // NOPMD

    /** @return Hashtable<String, String>
     * @deprecated Use tableNames2() instead */
    @Deprecated
    Hashtable<String, String> tableNames(); // NOPMD

    /** @return Map<String, String> */
    Map<String, String> tableNames2();

    /** This method creates a new instance of a DomainObject
     *
     * @return org.hip.kernel.bom.DomainObject */
    DomainObject create() throws BOMException;

    /** Use this method to find a DomainObject by its inKey.
     *
     * @return org.hip.kernel.bom.DomainObject
     * @param inKey org.hip.kernel.bom.KeyObject */
    DomainObject findByKey(KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException;

    /** Returns an empty DomainObject. An empty object is initialized but does not contain any values. The object goes
     * back into this state after releasing.
     *
     * @return org.hip.kernel.bom.DomainObject */
    DomainObject newInstance() throws BOMException;

    /** Use this method to delete an entry from the table. (Contrary of insert.)
     *
     * @param inKey org.hip.kernel.bom.KeyObject */
    void remove(KeyObject inKey) throws BOMNotFoundException, BOMInvalidKeyException;

    /** This method is a limited version of a delete. It takes as argument a KeyObject.
     *
     * @param inKey org.hip.kernel.bom.KeyObject
     * @param inCommit boolean true if the statement has to bo commited after execution.
     * @exception java.sql.SQLException The exception description. */
    void delete(KeyObject inKey, boolean inCommit) throws SQLException;

    /** Returns the SQL string to delete an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createDeleteString(String inTableName, DomainObject inDomainObject);

    /** Returns the SQL string to insert a new entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createInsertString(String inTableName, DomainObject inDomainObject);

    /** Returns the SQL string to update an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createUpdateString(String inTableName, DomainObject inDomainObject);

    /** Returns the SQL Update string to update entries having the specified characteristics to the specified values.
     *
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String UPDATE table SET field=value WHERE field=value; */
    String createUpdateString(KeyObject inChange, KeyObject inWhere);

    /** Returns the SQL string to prepare an update of an entry.
     *
     * @param inTableName java.lang.String
     * @param inSemanticObject org.hip.kernel.bom.DomainObject */
    String createPreparedUpdateString(String inTableName, DomainObject inDomainObject);

    /** Returns a Vector of prepared SQL strings to insert a new entry.
     *
     * @param java.util.Vector<Object>
     * @deprecated Use createPreparedInserts2() instead */
    @Deprecated
    Vector<String> createPreparedInserts(); // NOPMD

    /** Returns a List of prepared SQL strings to insert a new entry.
     *
     * @param List<Object> */
    List<String> createPreparedInserts2();

    /** Returns a Vector of prepared SQL strings to update entries.
     *
     * @param java.util.Vector<Object>
     * @deprecated Use createPreparedUpdates2() instead */
    @Deprecated
    Vector<String> createPreparedUpdates(); // NOPMD

    /** Returns a List of prepared SQL strings to update entries.
     *
     * @param List<Object> */
    List<String> createPreparedUpdates2();

    /** Returns the maximum value of the specified column corresponding to this home.
     *
     * @param inColumnName java.lang.String
     * @return java.math.BigDecimal
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    BigDecimal getMax(String inColumnName) throws SQLException, BOMException;

    /** Returns a collection of calculated values according to the modifiers strategy passed.
     *
     * @param inStrategy ModifierStrategy
     * @return Collection containing <code>BigDecimal</code>s or <code>Timestamp</code>s.
     * @throws SQLException */
    Collection<Object> getModified(ModifierStrategy inStrategy) throws SQLException;

    /** Returns the maximum value of the specified column corresponding to this home and the specified key.
     *
     * @param inColumnName java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return java.math.BigDecimal
     * @throws java.sql.SQLException
     * @throws org.hip.kernel.bom.BOMException */
    BigDecimal getMax(String inColumnName, KeyObject inKey) throws SQLException, BOMException;

    /** Returns a collection of calculated values according to the modifiers strategy passed.
     *
     * @param inStrategy ModifierStrategy
     * @param inKey org.hip.kernel.bom.KeyObject
     * @return Collection containing <code>BigDecimal</code>s or <code>Timestamp</code>s.
     * @throws SQLException */
    Collection<Object> getModified(ModifierStrategy inStrategy, KeyObject inKey) throws SQLException;

    /** Returns a SQL command usable for a prepared update.
     *
     * @param inChange KeyObject
     * @param inWhere KeyObject
     * @return String */
    String createPreparedUpdate(KeyObject inChange, KeyObject inWhere);

    /** Creates the prepared select SQL string to fetch all domain objects with the specified key managed by this home.
     *
     * @return java.lang.String
     * @param inKey org.hip.kernel.bom.KeyObject */
    String createPreparedSelectString(KeyObject inKey);

    /** Checks whether the database table structure corresponds to the domain object definition.
     *
     * @param inSchemaPattern String table schema (may be <code>null</code>)
     * @return boolean <code>true</code> if the table structure in the database corresponds to the structur according to
     *         the home's object definition.
     * @throws SQLException
     * @throws VException */
    boolean checkStructure(String inSchemaPattern) throws SQLException, NamingException, VException;

}