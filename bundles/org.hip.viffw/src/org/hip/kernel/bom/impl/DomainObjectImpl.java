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

package org.hip.kernel.bom.impl; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectHome;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.ReadOnlyDomainObject;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.ObjectDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.bom.model.TypeDef;
import org.hip.kernel.exc.VException;
import org.hip.kernel.util.Debug;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class implements the interface DomainObject and ReadOnlyDomainObject.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.DomainObject
 * @see org.hip.kernel.bom.ReadOnlyDomainObject */
@SuppressWarnings("serial")
abstract public class DomainObjectImpl extends AbstractSemanticObject implements DomainObject, ReadOnlyDomainObject { // NOPMD
    private static final Logger LOG = LoggerFactory.getLogger(DomainObjectImpl.class);

    // Class variables
    private static final String MODE_NEW = "new".intern();
    private static final String MODE_CHANGE = "change".intern();

    // Instance variables
    private GeneralDomainObjectHome home;

    private boolean isLoaded;
    private String mode;
    private KeyObject initialKey;

    /** Sets the specified visitor. This method implements the visitor pattern.
     *
     * @param inVisitor org.hip.kernel.bom.DomainObjectVisitor */
    @Override
    public void accept(final DomainObjectVisitor inVisitor) {
        inVisitor.visitDomainObject(this);
    }

    /** This method will be called after the loadFromResultSet. It's a hook for subclasses. */
    protected void afterLoad() { // NOPMD by lbenno
        // intentionally left empty
    }

    /** Returns a vector of SQL delete strings. Each table to update gets a delete string.
     *
     * @return java.lang.Vector<String>
     * @see org.hip.kernel.bom.DBAdapterSimple#createDeleteString(String, DomainObject) */
    private List<String> createDeleteString() {
        final List<String> outDeletes = new ArrayList<String>();
        for (final String lTableName : getObjectDef().getTableNames2()) {
            outDeletes.add(((DomainObjectHome) getHome()).createDeleteString(lTableName, this));
        }
        return outDeletes;
    }

    /** Returns a vector of SQL insert strings.
     *
     * @return List<String>
     * @see org.hip.kernel.bom.DBAdapterSimple#createInsertString(String, DomainObject) */
    private List<String> createInsertString() {
        final List<String> outInserts = new ArrayList<String>();
        for (final String lTableName : getObjectDef().getTableNames2()) {
            outInserts.add(((DomainObjectHome) getHome()).createInsertString(lTableName, this));
        }
        return outInserts;
    }

    /** Returns a vector of SQL strings for prepared updates.
     *
     * @return List<String>
     * @see org.hip.kernel.bom.DBAdapterSimple#createPreparedUpdateString(String, DomainObject) */
    protected List<String> createPreparedUpdateString() {
        final List<String> outUpdates = new ArrayList<String>();
        for (final String lTableName : getObjectDef().getTableNames2()) {
            outUpdates.add(((DomainObjectHome) getHome()).createPreparedUpdateString(lTableName, this));
        }
        return outUpdates;
    }

    /** Returns a vector of SQL update strings. Each table to update gets an update string.
     *
     * @return List<String>
     * @see org.hip.kernel.bom.DBAdapterSimple#createUpdateString(String, DomainObject) */
    private List<String> createUpdateString() {
        final List<String> outUpdates = new ArrayList<String>();
        for (final String lTableName : getObjectDef().getTableNames2()) {
            outUpdates.add(((DomainObjectHome) getHome()).createUpdateString(lTableName, this));
        }
        return outUpdates;
    }

    /** DomainObjects are equal if their class (i.e. classname) is equal and they have the same key
     *
     * @return boolean */
    @Override
    public boolean equals(final Object inObject) {
        if (inObject == null) {
            return false;
        }
        if (getClass() != inObject.getClass()) {
            return false;
        }

        if (getKey() == null) {
            return ((DomainObject) inObject).getKey() == null;
        } else {
            return getKey().equals(((DomainObject) inObject).getKey());
        }
    }

    /** Returns the home of this class.
     *
     * @return org.hip.kernel.bom.GeneralDomainObjectHome */
    @Override
    public final GeneralDomainObjectHome getHome() {
        synchronized (this) {
            if (home == null) {
                home = (GeneralDomainObjectHome) HomeManagerImpl.getSingleton().getHome(getHomeClassName());
            }
        }
        return home;
    }

    /** Get a <code>KeyObject</code> initialized with the specified value. */
    private KeyObject getKey(final Long keyValue) {
        KeyObject outKey = null;

        final KeyDef lKeyDef = getObjectDef().getPrimaryKeyDef();
        if (lKeyDef == null) {
            return outKey;
        }

        outKey = new KeyObjectImpl();
        for (final String lKeyName : lKeyDef.getKeyNames2()) {
            try {
                outKey.setValue(lKeyName, keyValue);
            } catch (final VInvalidNameException | VInvalidValueException exc) {
                LOG.error("Error encountered while setting the key!", exc);
            }
        }
        return outKey;
    }

    /** This method returns an instance of a key object. Null will be returned if no key def could be found.
     *
     * @return org.hip.kernel.bom.KeyObject */
    @Override
    public KeyObject getKey() {
        KeyObject outKey = null;

        final KeyDef lKeyDef = getObjectDef().getPrimaryKeyDef();
        if (lKeyDef == null) {
            return outKey;
        }

        outKey = new KeyObjectImpl();
        for (final String lKeyName : lKeyDef.getKeyNames2()) {
            try {
                outKey.setValue(lKeyName, this.get(lKeyName));
            } catch (final GettingException | VInvalidNameException | VInvalidValueException exc) {
                LOG.error("Error encountered while setting the key!", exc);
            }
        }
        return outKey;
    }

    /** This method returns an instance of a key object. Null will be returned if no key def could be found. The initial
     * key object is returned if demanded or if this domain objects mode is changed, else the actual key is returned.
     *
     * @param inInitial boolean
     * @return org.hip.kernel.bom.KeyObject */
    @Override
    public KeyObject getKey(final boolean inInitial) {
        if (inInitial || MODE_CHANGE.equals(mode)) {
            return initialKey;
        } else {
            return getKey();
        }
    }

    /** Returns the object definition for this object.
     *
     * @return org.hip.kernel.bom.model.ObjectDef */
    @Override
    public ObjectDef getObjectDef() {
        synchronized (this) {
            return getHome().getObjectDef();
        }
    }

    /** Returns the name of this object.
     *
     * @return java.lang.String */
    @Override
    public String getObjectName() {
        try {
            return (String) getObjectDef().get(ObjectDefDef.objectName);
        } catch (final GettingException exc) {
            LOG.error("Error encountered while getting the object name!", exc);
            return "";
        }
    }

    /** @return org.hip.kernel.bom.PropertySet */
    private PropertySet getPropertySet() {
        return this.propertySet();
    }

    /** Returns the changed properties of this domain object.
     *
     * @return java.util.Iterator<Property> */
    @Override
    public Iterator<Property> getChangedProperties() {
        return getPropertySet().getChangedProperties2().iterator();
    }

    /** Returns a hash code value for the domain object.
     *
     * @return int */
    @Override
    public int hashCode() {
        return getKey() == null ? 1 : getKey().hashCode();
    }

    /** Initialize the DomainObject and mark it as new. */
    protected void initializeForNew() {
        mode = DomainObjectImpl.MODE_NEW;
    }

    /** Initializing of the domain object's PropertySet with the help of the ModelObject (i.e. the domain object's
     * ObjectDef). It takes all the ObjectDefs PropertyDefs and puts it to the domain object's PropertySet.
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    public void initializePropertySet(final PropertySet inSet) {
        final ObjectDef lObjectDef = getHome().getObjectDef();
        for (final PropertyDef lPropertyDef : lObjectDef.getPropertyDefs2()) {
            inSet.add(lPropertyDef.create(inSet));
        }
    }

    /** Returns true if some properties have been changed.
     *
     * @return boolean */
    @Override
    public boolean isChanged() {
        // Quite straightforward implementation
        return getPropertySet().getChangedProperties2().size() != 0;
    }

    /** DomainObjects do not allow a dynamic add. Only those properties are supported which are defined in the
     * definition string.
     *
     * @return boolean */
    @Override
    public boolean isDynamicAddAllowed() {
        return false;
    }

    /** This method fills the DomainObject with data from the ResultSet.
     *
     * @param inResult java.sql.ResultSet */
    protected void loadFromResultSet(final ResultSet inResult) { // NOPMD by lbenno
        String lName = "";
        String lType = "";

        try {
            final GeneralDomainObjectHome lHome = getHome();
            final ResultSetMetaData lMetaData = inResult.getMetaData();
            for (int i = 1; i <= lMetaData.getColumnCount(); i++) {
                final String lColumnName = lMetaData.getColumnLabel(i);
                final PropertyDef lProperty = lHome.getPropertyDefFor(lColumnName);
                if (lProperty == null) {
                    // we assume a calculated/modified column and, therefore, a numerical value.
                    this.set(lColumnName, inResult.getLong(i));
                } else {
                    try {
                        lName = (String) lProperty.get(PropertyDefDef.propertyName);
                        lType = ((String) lProperty.get(PropertyDefDef.valueType)).intern();

                        if (lType == TypeDef.String) {
                            this.set(lName, inResult.getString(i));
                        } else if (lType == TypeDef.LongVarchar) {
                            this.set(lName, inResult.getAsciiStream(i));
                        } else if (lType == TypeDef.Date) {
                            this.set(lName, inResult.getDate(i));
                        } else if (lType == TypeDef.Timestamp) {
                            Timestamp lValue = null;
                            try {
                                lValue = inResult.getTimestamp(i);
                            } catch (final SQLException exc) { // NOPMD
                                // intentionally left empty (because of MySQL zeroDateTimeBehavior)
                            }
                            this.set(lName, lValue);
                        } else if (lType == TypeDef.Integer) {
                            this.set(lName, Integer.valueOf(inResult.getInt(i)));
                        } else if (lType == TypeDef.Long) {
                            this.set(lName, Long.valueOf(inResult.getLong(i)));
                        } else if (lType == TypeDef.BigInteger) {
                            final java.math.BigDecimal lValue = inResult.getBigDecimal(i);
                            this.set(lName, lValue == null ? null : lValue.toBigInteger());
                        } else if (lType == TypeDef.BigDecimal) {
                            this.set(lName, inResult.getBigDecimal(i));
                        } else if (lType == TypeDef.Number) {
                            this.set(lName, inResult.getBigDecimal(i));
                        } else if (lType == TypeDef.Binary) {
                            this.set(lName, inResult.getBlob(i));
                        }
                    } catch (final GettingException | SettingException | SQLException exc) {
                        LOG.error("Error encountered while loading the model instance!", exc);
                    }
                } // if
            } // for
            initialKey = getKey();

            // Let subclasses do additional things
            this.isLoaded = true;
            getPropertySet().notifyInit(true);
            afterLoad();
        } catch (final SettingException | SQLException exc) {
            LOG.error("Error encountered while loading the model instance!", exc);
        }
    }

    /** Initializes the DomainObject again, i.e. sets its properties to its initial value. */
    public void reinitialize() {

        // set key properties as initial
        initialKey = getKey();

        // set properties as initial
        for (final Property lProperty : propertySet().getChangedProperties2()) {
            final PropertyDef lPropertyDef = lProperty.getPropertyDef();

            try {
                final Property lNewProperty = lPropertyDef.create(propertySet());
                lNewProperty.setValue(lProperty.getValue());
                propertySet().add(lNewProperty);

            } catch (final VInvalidValueException exc) {
                LOG.error("Error encountered while initializing the model instance!", exc);
            }
        }
    }

    /** Use this method to release a this DomainObject. Released objects can act as cache and, therefore, instead of
     * creating a new instance of a DomainObject from scratch, can improve performance. */
    @Override
    public void release() {
        getHome().release(this);
    }

    @Override
    public String toString() { // NOPMD by lbenno
        if (getKey() == null) {
            return Debug.classBOMMarkupString(getHome().getObjectClassName(), getPropertySet());
        } else {
            return Debug.classBOMMarkupString(getHome().getObjectClassName(), getKey());
        }
    }

    /** Insert the new DomainObject in the table of the DB. The default implementation is without commit (for safety
     * reasons).
     *
     * @return Long The auto-generated value of the new entry or 0, if there's no autoincrement column.
     * @exception java.sql.SQLException */
    @Override
    public Long insert() throws SQLException, VException {
        return insert(false);
    }

    /** Insert the new DomainObject in the table of the DB.
     *
     * @param inCommit boolean Trigger a COMMIT after the insert?
     * @return Long The auto-generated value of the new entry or 0, if there's no auto-increment column.
     * @exception java.sql.SQLException */
    @Override
    public Long insert(final boolean inCommit) throws SQLException, VException {
        Collection<Long> autoKeys = Collections.emptyList();
        Long outKey = Long.valueOf(0L);
        try (InsertStatement statement = new InsertStatement(getHome())) {
            statement.setInserts(this.createInsertString());
            autoKeys = statement.executeInsert();
            if (!autoKeys.isEmpty()) {
                outKey = autoKeys.iterator().next();
            }
            if (inCommit) {
                statement.commit();
                initialKey = getKey(outKey);
            }
        }
        if (!autoKeys.isEmpty()) {
            initKeyValue(outKey);
        }
        return outKey;
    }

    private void initKeyValue(final Long inValue) throws VException {
        final Collection<String> lAccepted = Arrays.asList(TypeDef.Number, TypeDef.BigInteger, TypeDef.Integer,
                TypeDef.Long);
        final ObjectDef lObjectDef = getObjectDef();
        final KeyDef lKeyDef = lObjectDef.getPrimaryKeyDef();
        for (final String lKeyName : lKeyDef.getKeyNames2()) {
            if (lAccepted.contains(lObjectDef.getPropertyDef(lKeyName).getValueType())) {
                set(lKeyName, inValue);
            }
        }
    }

    /** Updates the DomainObject. The default implementation is without commit (for safety reasons).
     *
     * @exception java.sql.SQLException */
    @Override
    public void update() throws SQLException {
        update(false);
    }

    /** Updates the DomainObject.
     *
     * @param inCommit boolean Trigger a COMMIT after the insert?
     * @exception java.sql.SQLException */
    @Override
    public void update(final boolean inCommit) throws SQLException {
        if (this.isChanged()) {
            try (UpdateStatement statement = new UpdateStatement()) {
                statement.setUpdates(this.createUpdateString());
                statement.executeUpdate();
                if (inCommit) {
                    statement.commit();
                    reinitialize();
                }
            }
        }
    }

    /** Deletes the DomainObject from the table of the DB. The default implementation is without commit (for safety
     * reasons).
     *
     * @exception java.sql.SQLException */
    @Override
    public void delete() throws SQLException {
        delete(false);
    }

    /** Deletes the DomainObject from the table of the DB.
     *
     * @param inCommit boolean
     * @exception java.sql.SQLException */
    @Override
    public void delete(final boolean inCommit) throws SQLException {

        try (UpdateStatement statement = new UpdateStatement()) {
            statement.setUpdates(this.createDeleteString());
            statement.executeUpdate();
            if (inCommit) {
                statement.commit();
            }
        }
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(initialKey);
        out.writeObject(mode);
        out.writeBoolean(isLoaded);
    }

    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        initialKey = (KeyObject) inStream.readObject();
        mode = (String) inStream.readObject();
        isLoaded = inStream.readBoolean();
    }

}