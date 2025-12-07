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
package org.elbe.relations.browser.finder.internal.dnd;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryDragSourceEffect;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;
import org.elbe.relations.data.utility.UniqueID;
import org.elbe.relations.dnd.DropDataHelper;

/**
 * Helper class to handle this browser view's drag and drop.
 *
 * @author Luthiger Created on 17.12.2009
 */
public final class DragAndDropHelper {
    private static final int OPERATIONS = DND.DROP_MOVE | DND.DROP_COPY;

    private DragAndDropHelper() {
        // prevent class instantiation
    }

    /** Create drag source to remove related items from the center item or to relate one related item with another (by
     * dropping the first on the later).
     *
     * @param gallery Gallery the gallery widget.
     * @param isCenter boolean <code>true</code> if this gallery displays the center item, <code>false</code> if it
     *            displays the related items.
     * @return {@link DragSource} */
    public static DragSource createDragSource(final Gallery gallery, final boolean isCenter) {
        final DragSource dragSource = new DragSource(gallery, OPERATIONS);
        dragSource.setTransfer(DropDataHelper.DRAG_TYPES);
        dragSource.addDragListener(new FinderDragSourceAdapter(gallery, isCenter));
        dragSource.setDragSourceEffect(new GalleryDragSourceEffect(gallery));
        return dragSource;
    }

    /** Create drop target to add relations.
     *
     * @param gallery Gallery the gallery widget.
     * @param isCenter boolean <code>true</code> if this gallery displays the center item, <code>false</code> if it
     *            displays the related items.
     * @param context {@link IEclipseContext}
     * @return {@link DropTarget} */
    public static DropTarget createDropTarget(final Gallery gallery, final boolean isCenter,
            final IEclipseContext context) {
        final DropTarget dropTarget = new DropTarget(gallery, OPERATIONS);
        dropTarget.setTransfer(DropDataHelper.DROP_TYPES);
        dropTarget.addDropListener(FinderDropTargetListener.create(gallery, isCenter, context));
        return dropTarget;
    }

    // --- inner classes ---

    private static class FinderDragSourceAdapter extends DragSourceAdapter {
        private final Gallery gallery;
        private final boolean isCenter;

        FinderDragSourceAdapter(final Gallery gallery, final boolean isCenter) {
            this.gallery = gallery;
            this.isCenter = isCenter;
        }

        @Override
        public void dragStart(final DragSourceEvent event) {
            if (this.isCenter) {
                event.doit = false;
                return;
            }
            if (this.gallery.getSelectionCount() == 0) {
                event.doit = false;
                return;
            }
        }

        @Override
        public void dragSetData(final DragSourceEvent event) {
            final GalleryItemAdapter selection = (GalleryItemAdapter) this.gallery
                    .getSelection()[0];
            event.data = new UniqueID[] { selection.getRelationsItem().getUniqueID() };
        }
    }

}
