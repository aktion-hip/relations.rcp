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
package org.elbe.relations.internal.preferences;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.internal.actions.RelationsPreferences;
import org.elbe.relations.internal.services.PeerTransferRegistry;
import org.elbe.relations.services.IPeerTransferProvider;

import jakarta.inject.Inject;

/** The preference page showing this installation's peer identity and configuring the port
 * used for incoming peer connections.
 *
 * @author lbenno */
@SuppressWarnings("restriction")
public class PeerConfigPrefPage extends AbstractPreferencePage {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private Text port;

    @Inject
    private IEclipseContext context;

    @Override
    protected Control createContents(final Composite parent) {
        final Composite outComposite = new Composite(parent, SWT.NONE);
        final int columns = 2;
        setLayout(outComposite, columns);
        outComposite.setFont(parent.getFont());

        final Optional<IPeerTransferProvider> provider = this.context.get(PeerTransferRegistry.class)
                .getActiveProvider();
        createLabel(outComposite, RelationsMessages.getString("PeerConfigPrefPage.lbl.identity")); //$NON-NLS-1$
        final Text identity = new Text(outComposite, SWT.READ_ONLY | SWT.SINGLE);
        identity.setLayoutData(createGridData());
        identity.setText(getPeerId(provider));
        if (provider.isPresent()) {
            final Label description = new Label(outComposite, SWT.WRAP);
            description.setText(provider.get().getDescription());
            description.setLayoutData(GridDataFactory.fillDefaults().span(columns, 1).grab(true, false)
                    .hint(convertWidthInCharsToPixels(60), SWT.DEFAULT).create());
        }
        createSeparator(outComposite, columns);

        this.port = createLabelText(outComposite, RelationsMessages.getString("PeerConfigPrefPage.lbl.port")); //$NON-NLS-1$
        this.port.addModifyListener(event -> validatePort());
        this.port.setText(String.valueOf(RelationsPreferences.getPreferences()
                .getInt(RelationsConstants.PREFS_PEER_PORT, RelationsConstants.DFT_PEER_PORT)));
        return outComposite;
    }

    private String getPeerId(final Optional<IPeerTransferProvider> provider) {
        if (provider.isEmpty()) {
            return RelationsMessages.getString("PeerConfigPrefPage.msg.no.provider"); //$NON-NLS-1$
        }
        try {
            return provider.get().getPeerId();
        }
        catch (final IOException exc) {
            this.context.get(Logger.class).error(exc, "Unable to read the peer identity!"); //$NON-NLS-1$
            return RelationsMessages.getString("PeerConfigPrefPage.msg.no.identity"); //$NON-NLS-1$
        }
    }

    private void validatePort() {
        if (parsePort() < 0) {
            setErrorMessage(String.format(RelationsMessages.getString("PeerConfigPrefPage.msg.port.invalid"), //$NON-NLS-1$
                    MIN_PORT, MAX_PORT));
            setValid(false);
        } else {
            setErrorMessage(null);
            setValid(true);
        }
    }

    /** @return int the entered port, or <code>-1</code> if the entry is not a valid port */
    private int parsePort() {
        try {
            final int value = Integer.parseInt(this.port.getText().trim());
            return value >= MIN_PORT && value <= MAX_PORT ? value : -1;
        }
        catch (final NumberFormatException exc) {
            return -1;
        }
    }

    @Override
    protected void performDefaults() {
        this.port.setText(String.valueOf(RelationsConstants.DFT_PEER_PORT));
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        return savePreferences();
    }

    @Override
    protected void performApply() {
        savePreferences();
    }

    private boolean savePreferences() {
        if (this.port != null && parsePort() > 0) {
            final IEclipsePreferences store = RelationsPreferences.getPreferences();
            store.putInt(RelationsConstants.PREFS_PEER_PORT, parsePort());
        }
        return true;
    }

}
