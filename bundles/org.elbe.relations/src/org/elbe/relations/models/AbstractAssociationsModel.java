/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2025, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.bom.BOMException;
import org.elbe.relations.data.bom.BOMHelper;
import org.elbe.relations.data.bom.IItem;
import org.elbe.relations.data.bom.ILightWeightItem;
import org.elbe.relations.data.bom.RelationHome;
import org.elbe.relations.data.utility.IItemVisitor;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.models.ItemWithIcon;
import org.elbe.relations.internal.utility.RelatedItemHelper;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/** Abstract associations class providing generic functionality for associations models.
 *
 * @author Benno Luthiger Created on 09.05.2006
 * @see org.elbe.relations.models.IAssociationsModel */
@SuppressWarnings("restriction")
public abstract class AbstractAssociationsModel implements IAssociationsModel {
    private ItemAdapter focusItem;

    protected List<ItemAdapter> related;
    protected Collection<UniqueID> uniqueIDs;
    protected Collection<UniqueID> added;
    protected Collection<UniqueID> removed;

    @Inject
    private IDataService data;

    @Inject
    private IEclipseContext context;

    @Inject
    private Logger log;

    /** Sets the item which is selected (i.e. has the focus).
     *
     * @param inItem {@link ItemAdapter} */
    public void setFocusedItem(final ItemAdapter inItem) {
        this.focusItem = inItem;
    }

    protected ItemAdapter getFocusedItem() {
        return this.focusItem;
    }

    /** This method should be called in the constructor. Subclasses may extend or override.
     *
     * @param inItem ItemAdapter the focus (i.e. central) item.
     * @throws VException
     * @throws SQLException */
    protected void initialize(final ItemAdapter inItem) throws VException,
    SQLException {
        this.related = new ArrayList<ItemAdapter>();
        this.uniqueIDs = new ArrayList<UniqueID>();

        // Add the item's ID for that the item is filtered.
        this.uniqueIDs.add(inItem.getUniqueID());
        this.added = new HashSet<UniqueID>();
        this.removed = new HashSet<UniqueID>();

        processResult(inItem, RelatedItemHelper.getRelatedTerms(inItem));
        processResult(inItem, RelatedItemHelper.getRelatedTexts(inItem));
        processResult(inItem, RelatedItemHelper.getRelatedPersons(inItem));
    }

    private void processResult(final ItemAdapter inSource,
            final Collection<ItemWithIcon> inItems) throws VException,
    SQLException {
        for (final ItemWithIcon lItemIcon : inItems) {
            final IItem lItem = lItemIcon.getItem();

            // create and configure relation
            final IRelation lRelation = createRelation(lItem, inSource);

            // create and configure (adapted) item
            final ItemAdapter lAdapted = new ItemAdapter(lItem,
                    lItemIcon.getIcon(), this.context);
            lAdapted.addTarget(lRelation);

            this.related.add(lAdapted);
            this.uniqueIDs.add(new UniqueID(lItem.getItemType(), lItem.getID()));
        }
    }

    /** Creates and configures the relation. Subclasses may override.
     *
     * @param inItem {@link IItem}
     * @param inSource ItemAdapter
     * @return IRelation
     * @throws VException */
    protected IRelation createRelation(final IItem inItem,
            final ItemAdapter inSource) throws VException {
        return null;
    }

    /** Hook for subclasses. */
    protected abstract void afterSave() throws VException, SQLException;

    /** Returns the associated items as array of IItem.
     *
     * @return Object[] Array of <code>ItemAdapter</code>. */
    @Override
    public Object[] getElements() {
        return this.related.toArray();
    }

    /** Filters the specified item against the associated items.
     *
     * @param inItem ILightWeightItem
     * @return <code>true</code> if element is included in the filtered set, and <code>false</code> if excluded */
    @Override
    public boolean select(final ILightWeightItem inItem) {
        if (this.uniqueIDs.contains(new UniqueID(inItem.getItemType(), inItem
                .getID()))) {
            return false;
        }
        return true;
    }

    /** Add new associations.
     *
     * @param inAssociations Object[] Array of <code>ILightWeightItem</code>s. */
    @Override
    public void addAssociations(final Object[] inAssociations) {
        for (int i = 0; i < inAssociations.length; i++) {
            final LightWeightAdapter lItem = new LightWeightAdapter(
                    (ILightWeightModel) inAssociations[i]);
            final UniqueID lID = lItem.getUniqueID();
            this.related.add(new ItemAdapter(lItem, this.context));
            handleUniqueAdd(lID);
        }
    }

    /** Add new associations.
     *
     * @param inAssociations UniqueID[] */
    @Override
    public void addAssociations(final UniqueID[] inAssociations) {
        try {
            for (int i = 0; i < inAssociations.length; i++) {
                this.related.add(adapt(this.data.retrieveItem(inAssociations[i])));
                handleUniqueAdd(inAssociations[i]);
            }
        } catch (final BOMException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    private ItemAdapter adapt(final IItemModel inItem) {
        if (inItem instanceof ItemAdapter) {
            return (ItemAdapter) inItem;
        }
        return new ItemAdapter(inItem, this.context);
    }

    /** Add to unique IDs and checks whether the item to add has been removed before.
     *
     * @param inID UniqueID */
    private void handleUniqueAdd(final UniqueID inID) {
        this.uniqueIDs.add(inID);
        if (this.removed.contains(inID)) {
            this.removed.remove(inID);
        } else {
            this.added.add(inID);
        }
    }

    /** Removes the specified associations.
     *
     * @param inObjects Object[] */
    @Override
    public void removeAssociations(final Object[] inObjects) {
        for (int i = 0; i < inObjects.length; i++) {
            final ItemAdapter lItem = (ItemAdapter) inObjects[i];
            this.related.remove(lItem);
            handleUniqueRemove(lItem.getUniqueID());
        }
    }

    @Override
    public void removeAssociations(final UniqueID[] inAssociations) {
        try {
            for (int i = 0; i < inAssociations.length; i++) {
                this.related.remove(adapt(this.data.retrieveItem(inAssociations[i])));
                handleUniqueRemove(inAssociations[i]);
            }
        } catch (final BOMException exc) {
            this.log.error(exc, exc.getMessage());
        }
    }

    /** Removes the specified relation from this model.
     *
     * @param relation IRelation */
    @Override
    public void removeRelation(final IRelation relation) {
        final ItemAdapter lItem = new ItemAdapter(relation.getTargetItem(), null, this.context);
        this.related.remove(lItem);
        handleUniqueRemove(lItem.getUniqueID());

        // save the change in the database
        try {
            BOMHelper.getRelationHome().deleteRelation(relation.getRelationID());
            afterSave();
        } catch (BOMException | VException | SQLException exc) {
            this.log.error(exc, exc.getMessage());
        }

    }

    /** Removes from unique IDs and checks whether the item to remove has been added before.
     *
     * @param inID UniqueID */
    private void handleUniqueRemove(final UniqueID inID) {
        this.uniqueIDs.remove(inID);
        if (this.added.contains(inID)) {
            this.added.remove(inID);
        } else {
            this.removed.add(inID);
        }
    }

    /** Store changes made during display of dialog.
     *
     * @throws BOMException */
    @Override
    public void saveChanges() throws BOMException {
        final RelationHome home = BOMHelper.getRelationHome();
        // first add: process added
        for (final UniqueID id : this.added) {
            home.newRelation(this.focusItem, new UniqueIDWrapper(id));
        }

        // then remove: process removed
        final int type = this.focusItem.getItemType();
        try {
            final long itemID = this.focusItem.getID();
            for (final UniqueID id : this.removed) {
                home.deleteRelation(type, itemID, id.itemType, id.itemID);
            }
            afterSave();
        } catch (VException | SQLException exc) {
            throw new BOMException(exc.getMessage());
        }

    }

    /** @see IAssociationsModel#undoChanges() */
    @Override
    public void undoChanges() throws BOMException {
        // intentionally left empty, subclasses may override.
    }

    /** @return ItemAdapter the central item. */
    public ItemAdapter getCenter() {
        return this.focusItem;
    }

    /** Checks whether the specified ids exist already in this item's associations.
     *
     * @param ids UniqueID[]
     * @return boolean <code>true</code> if all ids are associated, <code>false</code> if at least one item is not
     *         associated yet. */
    @Override
    public boolean isAssociated(final UniqueID[] ids) {
        for (int i = 0; i < ids.length; i++) {
            if (!isAssociated(ids[i])) {
                return false;
            }
        }
        return true;
    }

    /** Checks whether the specified id exists already in this item's associations.
     *
     * @param id UniqueID
     * @return boolean <code>true</code> if the specified ID is an association. */
    @Override
    public boolean isAssociated(final UniqueID id) {
        return this.uniqueIDs.contains(id);
    }

    // --- private classes ---

    class UniqueIDWrapper implements IItem {
        private final UniqueID id;

        public UniqueIDWrapper(final UniqueID inID) {
            this.id = inID;
        }

        @Override
        public long getID() throws VException {
            return this.id.itemID;
        }

        @Override
        public int getItemType() {
            return this.id.itemType;
        }

        @Override
        public String getTitle() throws VException {
            return ""; //$NON-NLS-1$
        }

        @Override
        public void visit(final IItemVisitor inVisitor) throws VException {
            // intentionally left empty
        }

        @Override
        public ILightWeightItem getLightWeight() throws BOMException {
            return null;
        }

        @Override
        public void saveTitleText(final String inTitle, final String inText)
                throws BOMException {
            throw new BOMException(
                    RelationsMessages
                    .getString("AbstractAssociationsModel.error.msg")); //$NON-NLS-1$
        }

        @Override
        public String getCreated() throws VException {
            return null;
        }

        @Override
        public void accept(final DomainObjectVisitor inVisitor) {
            // intentionally left empty
        }
    }

    @Override
    public int hashCode() {
        final int lPrime = 31;
        int outResult = 1;
        outResult = lPrime * outResult
                + (this.focusItem == null ? 0 : this.focusItem.hashCode());
        outResult = lPrime * outResult
                + (this.related == null ? 0 : this.related.hashCode());
        return outResult;
    }

    @Override
    public boolean equals(final Object inObj) {
        if (this == inObj) {
            return true;
        }
        if (inObj == null) {
            return false;
        }
        if (getClass() != inObj.getClass()) {
            return false;
        }
        final AbstractAssociationsModel lOther = (AbstractAssociationsModel) inObj;
        if (this.focusItem == null) {
            if (lOther.focusItem != null) {
                return false;
            }
        } else if (!this.focusItem.equals(lOther.focusItem)) {
            return false;
        }
        if (this.related == null) {
            if (lOther.related != null) {
                return false;
            }
        } else if (!this.related.equals(lOther.related)) {
            return false;
        }
        return true;
    }

}
