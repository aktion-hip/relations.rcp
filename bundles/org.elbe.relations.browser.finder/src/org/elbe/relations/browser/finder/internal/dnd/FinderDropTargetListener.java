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

import java.sql.SQLException;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.elbe.relations.browser.finder.internal.FinderPane.GalleryItemAdapter;
import org.elbe.relations.dnd.DropDataHelper;
import org.elbe.relations.dnd.DropDataHelper.IDropHandler;
import org.elbe.relations.models.IAssociationsModel;
import org.elbe.relations.models.PeripheralAssociationsModel;
import org.elbe.relations.services.IBrowserManager;
import org.hip.kernel.exc.VException;

import jakarta.inject.Inject;

/**
 * Drop target adapter to enable item drops on finder items.
 *
 * @author Luthiger
 */
public class FinderDropTargetListener extends DropTargetAdapter {
    private static final Color COLOR_BACK_DRAG_OVER = new Color(Display.getCurrent(), 203, 231, 229);

    private final Gallery gallery;
    private final boolean isCenter;

    private GalleryItemAdapter startItem = null;
    private GalleryItemAdapter currentItem = null;
    private Color startItemColor;

    @Inject
    private Logger log;

    @Inject
    private IEclipseContext context;

    @Inject
    private IBrowserManager browserManager;

    /**
     * FinderDropTargetListener constructor, must not be called by clients
     * directly!
     */
    public FinderDropTargetListener(final Gallery gallery, final boolean isCenter) {
        this.gallery = gallery;
        this.isCenter = isCenter;
    }

    /** Factory method to create instances of <code>FinderDropTargetListener</code>s.
     *
     * @param gallery {@link Gallery}
     * @param isCenter boolean <code>true</code> if the item is displayed on the center pane
     * @param context {@link IEclipseContext}
     * @return {@link FinderDropTargetListener} */
    public static FinderDropTargetListener create(final Gallery gallery, final boolean isCenter,
            final IEclipseContext context) {
        final FinderDropTargetListener listener = new FinderDropTargetListener(gallery, isCenter);
        ContextInjectionFactory.inject(listener, context);
        return listener;
    }

    /** @return {@link GalleryItemAdapter} my be <code>null</code> */
    private GalleryItemAdapter getItemUnderCursor(final int x, final int y) {
        final Point point = this.gallery.toControl(new Point(x, y));
        return (GalleryItemAdapter) this.gallery.getItem(point);
    }

    @Override
    public void dragOver(final DropTargetEvent event) {
        final GalleryItemAdapter item = getItemUnderCursor(event.x, event.y);
        if (item != null) {
            event.detail = DND.DROP_COPY;
            // this marks the start of the drag movement
            if (this.startItem == null) {
                this.startItem = item;
                this.startItemColor = getCurrentBGColor();
                this.currentItem = item;
            } else if (!this.currentItem.isEqual(item)) {
                changeBGColor(COLOR_BACK_DRAG_OVER, item, this.currentItem);
                this.currentItem = item;
            }
        }
    }

    @Override
    public void dragLeave(final DropTargetEvent event) {
        if (this.startItem != null) {
            changeBGColor(this.startItemColor, this.startItem, null);
            // make first selected
            this.gallery.setSelection(new GalleryItem[] { this.gallery.getSelection()[0] });

        }
        event.detail = DND.DROP_NONE;
        this.startItem = null;
    }

    @Override
    public void drop(final DropTargetEvent event) {
        final IDropHandler handler = DropDataHelper.getDropHandler(event);
        if (handler != null) {
            try {
                final IAssociationsModel model = getModel(event.x, event.y);
                if (model != null) {
                    handler.handleDrop(event.data, model, this.context);
                    if (this.startItem != null) {
                        this.gallery.setSelection(new GalleryItem[] { this.startItem });
                    }
                }
            }
            catch (final Exception exc) {
                this.log.error(exc, exc.getMessage());
            }
        }
        this.startItem = null;
    }

    /** @return {@link IAssociationsModel} may be <code>null</code> */
    private IAssociationsModel getModel(final int x, final int y) throws SQLException, VException {
        if (this.isCenter) {
            return this.browserManager.getCenterModel();
        }
        final GalleryItemAdapter item = getItemUnderCursor(x, y);
        if (item == null) {
            return null;
        }
        return PeripheralAssociationsModel.createExternalAssociationsModel(item.getRelationsItem(), this.context);
    }

    /** @return Color of current selected item */
    private Color changeBGColor(final Color color, final GalleryItem selected, final GalleryItem deselected) {
        final ListItemRenderer renderer = (ListItemRenderer) this.gallery.getItemRenderer();
        final Color oldBG = renderer.getSelectionBackgroundColor();
        renderer.setSelectionBackgroundColor(color);
        this.gallery.setSelection(new GalleryItem[] { selected });
        this.gallery.redraw(selected);
        if (deselected != null) {
            this.gallery.redraw(deselected);
        }
        return oldBG;
    }

    private Color getCurrentBGColor() {
        final ListItemRenderer renderer = (ListItemRenderer) this.gallery.getItemRenderer();
        return renderer.getSelectionBackgroundColor();
    }

}
