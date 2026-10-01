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
package org.elbe.relations.internal.actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsConstants;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.data.utility.EventStoreChecker;
import org.elbe.relations.db.IDataService;
import org.elbe.relations.internal.backup.XMLExport;
import org.elbe.relations.internal.backup.ZippedXMLExport;
import org.elbe.relations.internal.controls.RelationsStatusLineManager;
import org.elbe.relations.internal.preferences.LanguageService;
import org.elbe.relations.internal.services.PeerTransferRegistry;
import org.elbe.relations.internal.utility.ExportToPeerDialog;
import org.elbe.relations.internal.utility.PeerApprovalPrompt;
import org.elbe.relations.internal.utility.PeerSessionDialog;
import org.elbe.relations.services.IPeerSession;
import org.elbe.relations.services.IPeerTransferListener;
import org.elbe.relations.services.IPeerTransferProvider;
import org.elbe.relations.services.PeerExport;
import org.elbe.relations.services.PeerSessionException;
import org.elbe.relations.services.PeerTransferOutcome;
import org.hip.kernel.exc.VException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class is executing the export to a peer: it prepares the same zipped XML
 * export as the cloud export, opens a transfer session with the installed
 * <code>IPeerTransferProvider</code> and keeps it open until the user closes
 * it. The change log is cleared only after a receiver has acknowledged a
 * complete transfer.
 *
 * @author lbenno
 */
@Component(service = ExportToPeerAction.class)
@SuppressWarnings("restriction")
public class ExportToPeerAction implements ICommand {
    private static final String PATTERN = "yyyy-MM-dd-HHmmss"; //$NON-NLS-1$
    private static final String EXTENSION = ".zip"; //$NON-NLS-1$

    private LanguageService languageService;
    private Logger log;
    private RelationsStatusLineManager statusLine;
    private IDataService dataService;

    private PeerTransferRegistry peerTransferRegistry;

    @Reference
    void bindPeerTransferRegistry(final PeerTransferRegistry peerTransferRegistry) {
        this.peerTransferRegistry = peerTransferRegistry;
    }

    /**
     * Passing relevant objects to the action.
     *
     * @param context
     *            {@link IEclipseContext}
     * @return {@link ExportToPeerAction}
     */
    public ExportToPeerAction initialize(final IEclipseContext context) {
        this.languageService = context.get(LanguageService.class);
        this.log = context.get(Logger.class);
        this.statusLine = context.get(RelationsStatusLineManager.class);
        this.dataService = context.get(IDataService.class);
        return this;
    }

    @Override
    public void execute() {
        final Shell shell = Display.getDefault().getActiveShell();
        final Optional<IPeerTransferProvider> provider = this.peerTransferRegistry.getActiveProvider();
        if (provider.isEmpty()) {
            MessageDialog.openInformation(shell,
                    RelationsMessages.getString("ExportToPeerAction.problem.title"), //$NON-NLS-1$
                    RelationsMessages.getString("ExportToPeerAction.problem.msg")); //$NON-NLS-1$
            return;
        }

        final ExportToPeerDialog dialog = new ExportToPeerDialog(this.dataService.getNumberOfEvents() > 0);
        if (dialog.open() != Window.OK) {
            // declined at the warning: no session, change log untouched
            return;
        }

        final boolean incremental = dialog.isIncremental();
        final String baseName = incremental
                ? String.format("%s%s", RelationsConstants.PEER_EXPORT_DELTA, //$NON-NLS-1$
                        new SimpleDateFormat(PATTERN).format(new Date()))
                        : RelationsConstants.PEER_EXPORT_FULL;
        File tempExport = null;
        try {
            tempExport = File.createTempFile(baseName, EXTENSION);
            if (prepareExport(shell, tempExport, incremental)) {
                runSession(shell, provider.get(), new PeerExport(
                        incremental ? PeerExport.Scope.INCREMENTAL : PeerExport.Scope.FULL,
                                tempExport.toPath(), baseName + EXTENSION));
            } else {
                MessageDialog.openError(shell,
                        RelationsMessages.getString("ExportToPeerAction.err.title"), //$NON-NLS-1$
                        RelationsMessages.getString("ExportToPeerAction.err.export")); //$NON-NLS-1$
            }
        }
        catch (final IOException exc) {
            this.log.error(exc, "Unable to prepare the export to a peer!"); //$NON-NLS-1$
            MessageDialog.openError(shell,
                    RelationsMessages.getString("ExportToPeerAction.err.title"), //$NON-NLS-1$
                    RelationsMessages.getString("ExportToPeerAction.err.export")); //$NON-NLS-1$
        }
        finally {
            if (tempExport != null) {
                try {
                    Files.deleteIfExists(tempExport.toPath());
                }
                catch (final IOException exc) {
                    this.log.error(exc, "Unable to delete \"" + tempExport.toString() + "\"!"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    /** Writes the full or the incremental export to the passed file.
     *
     * @return boolean <code>true</code> if the export has been written completely */
    private boolean prepareExport(final Shell shell, final File tempExport, final boolean incremental) {
        final AtomicReference<Exception> failure = new AtomicReference<>();
        final String fileName = tempExport.getAbsolutePath();
        final int numberOfItems = this.dataService.getNumberOfItems() + this.dataService.getNumberOfRelations();
        try {
            new ProgressMonitorDialog(shell).run(true, false, monitor -> {
                try (XMLExport exporter = incremental
                        ? new ExportToCloudAction.EventStoreExport(fileName, this.languageService.getAppLocale())
                                : new ZippedXMLExport(fileName, this.languageService.getAppLocale(), numberOfItems)) {
                    exporter.export(monitor);
                }
                catch (VException | SQLException | IOException exc) {
                    failure.set(exc);
                }
            });
        }
        catch (InvocationTargetException | InterruptedException exc) {
            failure.set(exc);
        }
        if (failure.get() != null) {
            this.log.error(failure.get(), "Unable to prepare the export to a peer!"); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private void runSession(final Shell shell, final IPeerTransferProvider provider, final PeerExport export) {
        final int port = RelationsPreferences.getPreferences().getInt(RelationsConstants.PREFS_PEER_PORT,
                RelationsConstants.DFT_PEER_PORT);
        final Display display = shell.getDisplay();
        final AtomicReference<PeerSessionDialog> sessionDialog = new AtomicReference<>();
        final SessionListener listener = new SessionListener(display, sessionDialog,
                export.getScope() == PeerExport.Scope.INCREMENTAL);
        final PeerApprovalPrompt approval = new PeerApprovalPrompt(display, () -> {
            final PeerSessionDialog dialog = sessionDialog.get();
            return dialog != null && dialog.isOpen() ? dialog.getShell() : null;
        });

        final IPeerSession session;
        try {
            session = provider.openSession(port, export, approval, listener);
        }
        catch (final PeerSessionException exc) {
            this.log.error(exc, exc.getMessage());
            MessageDialog.openError(shell,
                    RelationsMessages.getString("ExportToPeerAction.err.title"), //$NON-NLS-1$
                    exc.getReason() == PeerSessionException.Reason.PORT_UNAVAILABLE
                    ? String.format(RelationsMessages.getString("ExportToPeerAction.err.port"), port) //$NON-NLS-1$
                            : RelationsMessages.getString("ExportToPeerAction.err.session")); //$NON-NLS-1$
            return;
        }

        try {
            final PeerSessionDialog dialog = new PeerSessionDialog(shell, session);
            sessionDialog.set(dialog);
            dialog.open();
        }
        finally {
            session.close();
        }
        this.statusLine.showStatusLineMessage(listener.getSummary());
    }

    // ---

    /** Observes the transfers of one session: updates the session display, logs failures
     * and clears the change log once a receiver has acknowledged a complete transfer. */
    private class SessionListener implements IPeerTransferListener {
        private final Display display;
        private final AtomicReference<PeerSessionDialog> sessionDialog;
        private final boolean incremental;
        private volatile boolean connected;
        private volatile boolean completed;

        /** @param incremental boolean the scope the user prepared, named when a device asks for the other one */
        SessionListener(final Display display, final AtomicReference<PeerSessionDialog> sessionDialog,
                final boolean incremental) {
            this.display = display;
            this.sessionDialog = sessionDialog;
            this.incremental = incremental;
        }

        @Override
        public void transferStarted(final String peerId) {
            this.connected = true;
            showStatus(String.format(RelationsMessages.getString("ExportToPeerAction.status.connected"), peerId)); //$NON-NLS-1$
        }

        @Override
        public void transferEnded(final String peerId, final PeerTransferOutcome outcome) {
            transferEnded(peerId, outcome, null);
        }

        @Override
        public void transferEnded(final String peerId, final PeerTransferOutcome outcome, final Throwable cause) {
            if (outcome.isComplete()) {
                clearChangeLog();
                this.completed = true;
                ExportToPeerAction.this.log.info(String.format("Export delivered to peer %s.", peerId)); //$NON-NLS-1$
            } else if (outcome != PeerTransferOutcome.DECLINED_BY_USER) {
                final String message = String.format("Transfer to peer %s failed: %s", peerId, outcome); //$NON-NLS-1$
                if (cause == null) {
                    ExportToPeerAction.this.log.error(message);
                } else {
                    ExportToPeerAction.this.log.error(cause, message);
                }
            }
            showStatus(statusOf(peerId, outcome));
        }

        /** Only reached for an acknowledged, digest-verified transfer. */
        private void clearChangeLog() {
            try {
                new EventStoreChecker().clear();
            }
            catch (final SQLException exc) {
                ExportToPeerAction.this.log.error(exc, exc.getMessage());
            }
        }

        private String statusOf(final String peerId, final PeerTransferOutcome outcome) {
            return switch (outcome) {
                case COMPLETED -> String.format(RelationsMessages.getString("ExportToPeerAction.status.completed"), peerId); //$NON-NLS-1$
                case DECLINED_BY_USER -> String.format(RelationsMessages.getString("ExportToPeerAction.status.declined"), peerId); //$NON-NLS-1$
                case INTERRUPTED -> String.format(RelationsMessages.getString("ExportToPeerAction.status.interrupted"), peerId); //$NON-NLS-1$
                case DIGEST_MISMATCH -> failed(peerId, "ExportToPeerAction.reason.digest"); //$NON-NLS-1$
                case STORAGE_ERROR -> failed(peerId, "ExportToPeerAction.reason.storage"); //$NON-NLS-1$
                case REJECTED_BY_RECEIVER -> failed(peerId, "ExportToPeerAction.reason.rejected"); //$NON-NLS-1$
                case PROTOCOL_ERROR -> failed(peerId, "ExportToPeerAction.reason.protocol"); //$NON-NLS-1$
                case VERSION_MISMATCH -> String.format(RelationsMessages.getString("ExportToPeerAction.status.version"), peerId); //$NON-NLS-1$
                case SCOPE_MISMATCH -> String.format(RelationsMessages.getString("ExportToPeerAction.status.scope"), //$NON-NLS-1$
                        peerId, RelationsMessages.getString(this.incremental
                                ? "ExportToPeerAction.scope.incremental" //$NON-NLS-1$
                                        : "ExportToPeerAction.scope.full")); //$NON-NLS-1$
            };
        }

        private String failed(final String peerId, final String reasonKey) {
            return String.format(RelationsMessages.getString("ExportToPeerAction.status.failed"), //$NON-NLS-1$
                    peerId, RelationsMessages.getString(reasonKey));
        }

        private void showStatus(final String message) {
            if (this.display.isDisposed()) {
                return;
            }
            this.display.asyncExec(() -> {
                final PeerSessionDialog dialog = this.sessionDialog.get();
                if (dialog != null) {
                    dialog.setStatus(message);
                }
            });
        }

        /** @return String what the closed session achieved, distinguishing a session no
         *         device connected to from one whose transfers did not complete */
        String getSummary() {
            if (this.completed) {
                return RelationsMessages.getString("ExportToPeerAction.summary.completed"); //$NON-NLS-1$
            }
            return this.connected
                    ? RelationsMessages.getString("ExportToPeerAction.summary.incomplete") //$NON-NLS-1$
                            : RelationsMessages.getString("ExportToPeerAction.summary.no.device"); //$NON-NLS-1$
        }
    }

}
