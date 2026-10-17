/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2026, Benno Luthiger
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
package org.elbe.relations.internal.utility;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.elbe.relations.RelationsMessages;

/** Dialog to start the export to a peer: lets the user choose the scope and states what a
 * transfer session exposes before it is opened.
 *
 * @author lbenno */
public class ExportToPeerDialog extends Dialog {
    private static final int TEXT_WIDTH = 420;

    private final boolean hasEvents;
    private boolean incrementalFlag;
    private Label sharedLogNote;

    /** ExportToPeerDialog constructor.
     *
     * @param hasEvents boolean <code>true</code> if the event store contains entries */
    public ExportToPeerDialog(final boolean hasEvents) {
        super(Display.getDefault().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.hasEvents = hasEvents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.getShell().setText(RelationsMessages.getString("ExportToPeerDialog.dlg.title")); //$NON-NLS-1$
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
                .extendedMargins(12, 3, 5, 3).create());

        final Label synchType = new Label(composite, SWT.NONE);
        synchType.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.BEGINNING, SWT.TOP).create());
        synchType.setText(RelationsMessages.getString("ExportToPeerDialog.scope.lbl")); //$NON-NLS-1$

        final Composite group = new Composite(composite, SWT.NONE);
        group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        group.setLayout(new RowLayout(SWT.VERTICAL));

        final Button btnIncremental = new Button(group, SWT.RADIO);
        btnIncremental.setText(RelationsMessages.getString("ExportToPeerDialog.btn.incr.lbl")); //$NON-NLS-1$
        btnIncremental.addSelectionListener(widgetSelectedAdapter(e -> setIncremental(true)));

        final Button btnFull = new Button(group, SWT.RADIO);
        btnFull.setText(RelationsMessages.getString("ExportToPeerDialog.btn.full.lbl")); //$NON-NLS-1$
        btnFull.addSelectionListener(widgetSelectedAdapter(e -> setIncremental(false)));

        this.sharedLogNote = createWrappingLabel(composite);

        // what a session exposes; stated before the session is opened
        final Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false).create());
        createWrappingLabel(composite).setText(RelationsMessages.getString("ExportToPeerDialog.msg.exposure")); //$NON-NLS-1$

        if (this.hasEvents) {
            btnIncremental.setSelection(true);
            setIncremental(true);
        } else {
            btnFull.setSelection(true);
            btnIncremental.setSelection(false);
            btnIncremental.setEnabled(false);
            setIncremental(false);
        }
        return composite;
    }

    private Label createWrappingLabel(final Composite parent) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, false)
                .hint(TEXT_WIDTH, SWT.DEFAULT).create());
        return label;
    }

    private void setIncremental(final boolean incremental) {
        this.incrementalFlag = incremental;
        this.sharedLogNote.setText(incremental
                ? RelationsMessages.getString("ExportToPeerDialog.msg.shared.log") //$NON-NLS-1$
                : ""); //$NON-NLS-1$
        this.sharedLogNote.getParent().layout(true);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                RelationsMessages.getString("ExportToPeerDialog.btn.open.lbl"), true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /** @return boolean <code>true</code> in case of <i>incremental</i> export is selected,
     *         <code>false</code> in case of full export */
    public boolean isIncremental() {
        return this.incrementalFlag;
    }

}
