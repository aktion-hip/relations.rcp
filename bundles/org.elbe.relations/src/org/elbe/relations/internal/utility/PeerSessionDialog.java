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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.IPeerSession;

/** Displays an open peer transfer session: the address to connect to, the nature of the
 * session and the state of its transfers. Closing the dialog ends the session.
 *
 * @author lbenno */
public class PeerSessionDialog extends Dialog {
    private static final int TEXT_WIDTH = 480;

    private final IPeerSession session;
    private Combo addresses;
    private Label status;

    /** PeerSessionDialog constructor.
     *
     * @param parent {@link Shell}
     * @param session {@link IPeerSession} the open session */
    public PeerSessionDialog(final Shell parent, final IPeerSession session) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.session = session;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        parent.getShell().setText(RelationsMessages.getString("PeerSessionDialog.dlg.title")); //$NON-NLS-1$
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(3)
                .extendedMargins(12, 3, 5, 3).create());

        createWrappingLabel(composite, 3).setText(RelationsMessages.getString("PeerSessionDialog.msg.live")); //$NON-NLS-1$

        new Label(composite, SWT.NONE).setText(RelationsMessages.getString("PeerSessionDialog.lbl.address")); //$NON-NLS-1$
        this.addresses = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
        this.addresses.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        final Button copy = new Button(composite, SWT.PUSH);
        copy.setText(RelationsMessages.getString("PeerSessionDialog.btn.copy.lbl")); //$NON-NLS-1$
        copy.addSelectionListener(widgetSelectedAdapter(e -> copyAddress()));

        final List<String> reachable = this.session.getAddresses();
        if (reachable.isEmpty()) {
            this.addresses.setEnabled(false);
            copy.setEnabled(false);
            createWrappingLabel(composite, 3).setText(
                    RelationsMessages.getString("PeerSessionDialog.msg.no.address")); //$NON-NLS-1$
        } else {
            this.addresses.setItems(reachable.toArray(new String[0]));
            this.addresses.select(0);
            createWrappingLabel(composite, 3).setText(
                    RelationsMessages.getString("PeerSessionDialog.msg.address.scope")); //$NON-NLS-1$
        }

        createWrappingLabel(composite, 3).setText(this.session.isDiscoverable()
                ? RelationsMessages.getString("PeerSessionDialog.msg.discoverable") //$NON-NLS-1$
                : RelationsMessages.getString("PeerSessionDialog.msg.not.discoverable")); //$NON-NLS-1$

        final Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
        this.status = createWrappingLabel(composite, 3);
        this.status.setText(RelationsMessages.getString("PeerSessionDialog.status.waiting")); //$NON-NLS-1$
        return composite;
    }

    private Label createWrappingLabel(final Composite parent, final int span) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(GridDataFactory.fillDefaults().span(span, 1).grab(true, false)
                .hint(TEXT_WIDTH, SWT.DEFAULT).create());
        return label;
    }

    /** Copies the selected address, which includes the peer identity, to the clipboard. */
    private void copyAddress() {
        final String address = this.addresses.getText();
        if (address.isEmpty()) {
            return;
        }
        final Clipboard clipboard = new Clipboard(getShell().getDisplay());
        try {
            clipboard.setContents(new Object[] { address }, new Transfer[] { TextTransfer.getInstance() });
        }
        finally {
            clipboard.dispose();
        }
    }

    /** Displays the state of the session's transfers. Must be called on the UI thread; has no
     * effect once the dialog is closed.
     *
     * @param message String */
    public void setStatus(final String message) {
        if (this.status == null || this.status.isDisposed()) {
            return;
        }
        this.status.setText(message);
        this.status.getParent().layout(true);
    }

    /** @return boolean <code>true</code> while the dialog is displayed */
    public boolean isOpen() {
        return getShell() != null && !getShell().isDisposed();
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                RelationsMessages.getString("PeerSessionDialog.btn.close.lbl"), true); //$NON-NLS-1$
    }

}
