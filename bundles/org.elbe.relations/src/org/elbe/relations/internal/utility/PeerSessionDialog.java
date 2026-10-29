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

import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.IPeerSession;

/** Displays an open peer transfer session: the address to connect to (as QR code to scan and
 * as text to copy), the nature of the session and the state of its transfers. Closing the
 * dialog ends the session.
 *
 * @author lbenno */
public class PeerSessionDialog extends Dialog {
    private static final int TEXT_WIDTH = 480;
    /** Pixels per QR module at 100% zoom; a typical session address then gives a code of about 250 px. */
    private static final int QR_MODULE_PIXELS = 5;

    private final IPeerSession session;
    private Text address;
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

        final Optional<String> chosen = PeerAddresses.choose(this.session.getAddresses());
        chosen.ifPresent(reachable -> createQrCode(composite, reachable));

        new Label(composite, SWT.NONE).setText(RelationsMessages.getString("PeerSessionDialog.lbl.address")); //$NON-NLS-1$
        this.address = new Text(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        this.address.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        final Button copy = new Button(composite, SWT.PUSH);
        copy.setText(RelationsMessages.getString("PeerSessionDialog.btn.copy.lbl")); //$NON-NLS-1$
        copy.addSelectionListener(widgetSelectedAdapter(e -> copyAddress()));

        if (chosen.isEmpty()) {
            this.address.setEnabled(false);
            copy.setEnabled(false);
            createWrappingLabel(composite, 3).setText(
                    RelationsMessages.getString("PeerSessionDialog.msg.no.address")); //$NON-NLS-1$
        } else {
            this.address.setText(chosen.get());
            createWrappingLabel(composite, 3).setText(
                    RelationsMessages.getString("PeerSessionDialog.msg.address.scope")); //$NON-NLS-1$
        }

        if (!this.session.isDiscoverable()) {
            createWrappingLabel(composite, 3).setText(
                    RelationsMessages.getString("PeerSessionDialog.msg.not.discoverable")); //$NON-NLS-1$
        }

        final Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(GridDataFactory.fillDefaults().span(3, 1).grab(true, false).create());
        this.status = createWrappingLabel(composite, 3);
        this.status.setText(RelationsMessages.getString("PeerSessionDialog.status.waiting")); //$NON-NLS-1$
        return composite;
    }

    /** Displays the address as QR code for a phone to scan. The image is rendered anew for each
     * monitor zoom, so that the modules stay sharp instead of being resampled. */
    private void createQrCode(final Composite parent, final String reachable) {
        final ImageData base;
        try {
            base = QrCodeImage.render(reachable, QR_MODULE_PIXELS);
        }
        catch (final IllegalArgumentException exc) {
            // the address can't be encoded; the text below still allows copying it
            return;
        }
        createWrappingLabel(parent, 3).setText(RelationsMessages.getString("PeerSessionDialog.msg.scan")); //$NON-NLS-1$

        final ImageDataProvider provider = zoom -> zoom == 100 ? base
                : QrCodeImage.render(reachable, Math.round(QR_MODULE_PIXELS * zoom / 100f));
        final Image image = new Image(parent.getDisplay(), provider);
        final Label code = new Label(parent, SWT.NONE);
        code.setImage(image);
        code.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).align(SWT.CENTER, SWT.CENTER)
                .grab(true, false).indent(0, 5).create());
        code.addDisposeListener(e -> image.dispose());
    }

    private Label createWrappingLabel(final Composite parent, final int span) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(GridDataFactory.fillDefaults().span(span, 1).grab(true, false)
                .hint(TEXT_WIDTH, SWT.DEFAULT).create());
        return label;
    }

    /** Copies the selected address, which includes the peer identity, to the clipboard. */
    private void copyAddress() {
        final String text = this.address.getText();
        if (text.isEmpty()) {
            return;
        }
        final Clipboard clipboard = new Clipboard(getShell().getDisplay());
        try {
            clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
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
