/**
	This package is part of the framework used for the application VIF.
	Copyright (C) 2006, Benno Luthiger

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

package org.hip.kernel.bom.directory; // NOPMD by lbenno

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.hip.kernel.bom.DomainObject;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObjectHome;
import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.KeyObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.ReadOnlyDomainObject;
import org.hip.kernel.bom.impl.AbstractSemanticObject;
import org.hip.kernel.bom.impl.HomeManagerImpl;
import org.hip.kernel.bom.impl.KeyObjectImpl;
import org.hip.kernel.bom.model.KeyDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.bom.model.ObjectDefDef;
import org.hip.kernel.bom.model.PropertyDef;
import org.hip.kernel.bom.model.PropertyDefDef;
import org.hip.kernel.exc.DefaultExceptionHandler;
import org.hip.kernel.exc.VException;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** Generic domain object (aka model) for entries stored on and retrieved from a LDAP server.
 *
 * @author Luthiger Created on 06.07.2007 */
@SuppressWarnings("serial")
public abstract class LDAPObject extends AbstractSemanticObject implements DomainObject, ReadOnlyDomainObject { // NOPMD
    /** Mode type enum */
    private enum ModeType {
        MODE_NEW, MODE_CHANGE
    };

    private GeneralDomainObjectHome home;

    private KeyObject initialKey;
    private boolean isLoaded;

    private ModeType mode;

    @Override
    protected void initializePropertySet(final PropertySet inSet) { // NOPMD by lbenno
        final ObjectDef lObjectDef = getHome().getObjectDef();
        for (final PropertyDef lPropertyDef : lObjectDef.getPropertyDefs2()) {
            inSet.add(lPropertyDef.create(inSet));
        }
    }

    @Override
    protected boolean isDynamicAddAllowed() { // NOPMD by lbenno
        return false;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#delete() */
    @Override
    public void delete() throws SQLException { // NOPMD by lbenno
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#delete(boolean) */
    @Override
    public void delete(final boolean inCommit) throws SQLException { // NOPMD by lbenno
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#getChangedProperties() */
    @Override
    public Iterator<Property> getChangedProperties() { // NOPMD by lbenno
        // intentionally left empty
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#insert() */
    @Override
    public Long insert() throws SQLException { // NOPMD by lbenno
        // intentionally left empty
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#insert(boolean) */
    @Override
    public Long insert(final boolean inCommit) throws SQLException { // NOPMD by lbenno
        // intentionally left empty
        return null;
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#update() */
    @Override
    public void update() throws SQLException { // NOPMD by lbenno
        // intentionally left empty
    }

    /** No implementation provided.
     *
     * @see org.hip.kernel.bom.DomainObject#update(boolean) */
    @Override
    public void update(final boolean inCommit) throws SQLException { // NOPMD by lbenno
        // intentionally left empty
    }

    @Override
    public void accept(final DomainObjectVisitor inVisitor) { // NOPMD by lbenno
        inVisitor.visitDomainObject(this);
    }

    /** Returns the home of this class.
     *
     * @return org.hip.kernel.bom.GeneralDomainObjectHome */
    @Override
    public final synchronized GeneralDomainObjectHome getHome() { // NOPMD by lbenno
        if (home == null) {
            home = (GeneralDomainObjectHome) HomeManagerImpl.getSingleton().getHome(getHomeClassName());
        }
        return home;
    }

    @Override
    public KeyObject getKey() { // NOPMD by lbenno
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
                DefaultExceptionHandler.instance().handle(exc);
            }
        }
        return outKey;
    }

    @Override
    public KeyObject getKey(final boolean inInitial) { // NOPMD by lbenno
        if (inInitial || ModeType.MODE_CHANGE.equals(mode)) {
            return initialKey;
        }
        else {
            return getKey();
        }
    }

    @Override
    public ObjectDef getObjectDef() { // NOPMD by lbenno
        return getHome().getObjectDef();
    }

    @Override
    public String getObjectName() { // NOPMD by lbenno
        try {
            return (String) getObjectDef().get(ObjectDefDef.objectName);
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
            return "";
        }
    }

    @Override
    public boolean isChanged() { // NOPMD by lbenno
        return false;
    }

    @Override
    public void release() { // NOPMD by lbenno
        getHome().release(this);
    }

    /** Package friendly.
     *
     * @param inResult {@link SearchResult} */
    protected void loadFromResultSet(final SearchResult inResult) {
        final GeneralDomainObjectHome lHome = getHome();
        final Attributes lAttributes = inResult.getAttributes();
        final NamingEnumeration<String> lIDs = lAttributes.getIDs();
        try {
            while (lIDs.hasMore()) {
                final String lID = lIDs.next();
                final PropertyDef lProperty = lHome.getPropertyDefFor(lID);
                if (lProperty != null) {
                    final String lName = (String) lProperty.get(PropertyDefDef.propertyName);
                    if (lProperty.getPropertyType() == PropertyDefDef.propertyTypeComposite) {
                        this.set(lName, getCollection(lAttributes.get(lID)));
                    }
                    else {
                        this.set(lName, lAttributes.get(lID).get());
                    }
                }
            }
            initialKey = getKey();

            // Let subclasses do additional things
            isLoaded = true;
            getPropertySet().notifyInit(true);
            afterLoad();
        } catch (final VException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        } catch (final NamingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    private Collection<Object> getCollection(final Attribute inAttribute) throws NamingException {
        final Collection<Object> outCollection = new ArrayList<Object>();
        for (final NamingEnumeration<?> lEnum = inAttribute.getAll(); lEnum.hasMore();) {
            outCollection.add(lEnum.next());
        }
        return outCollection;
    }

    /** @return org.hip.kernel.bom.PropertySet */
    private PropertySet getPropertySet() {
        return this.propertySet();
    }

    /** This method will be called after the loadFromResultSet. It's a hook for subclasses. */
    protected void afterLoad() { // NOPMD by lbenno
    }

    /** Friendly method called by <code>LDAPObjectHome</code>. */
    protected void initializeForNew() {
        mode = ModeType.MODE_NEW;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(initialKey);
        out.writeObject(mode);
        out.writeBoolean(isLoaded);
    }

    private void readObject(final ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        initialKey = (KeyObject) inStream.readObject();
        mode = (ModeType) inStream.readObject();
        isLoaded = inStream.readBoolean();
    }

}
