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

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.IPeerConnectionApproval;

/** Asks the user, for every connecting device, whether it may receive the export. The
 * device's identity is the one established by the encrypted handshake. Declining is the
 * default, and a connection arriving after the session window has closed is declined.
 *
 * @author lbenno */
public class PeerApprovalPrompt implements IPeerConnectionApproval {
    private static final int SEND = 0;

    private final Display display;
    private final Supplier<Shell> parent;

    /** PeerApprovalPrompt constructor.
     *
     * @param display {@link Display}
     * @param parent {@link Supplier}&lt;{@link Shell}> the shell of the session display, or
     *            <code>null</code> once it is closed */
    public PeerApprovalPrompt(final Display display, final Supplier<Shell> parent) {
        this.display = display;
        this.parent = parent;
    }

    @Override
    public CompletableFuture<Boolean> approve(final String peerId) {
        final CompletableFuture<Boolean> answer = new CompletableFuture<>();
        if (this.display.isDisposed()) {
            answer.complete(false);
            return answer;
        }
        this.display.asyncExec(() -> {
            final Shell shell = this.parent.get();
            if (shell == null || shell.isDisposed()) {
                answer.complete(false);
                return;
            }
            final MessageDialog dialog = new MessageDialog(shell,
                    RelationsMessages.getString("PeerApprovalPrompt.dlg.title"), //$NON-NLS-1$
                    null,
                    String.format(RelationsMessages.getString("PeerApprovalPrompt.dlg.msg"), peerId), //$NON-NLS-1$
                    MessageDialog.QUESTION, 1,
                    RelationsMessages.getString("PeerApprovalPrompt.btn.send"), //$NON-NLS-1$
                    RelationsMessages.getString("PeerApprovalPrompt.btn.decline")); //$NON-NLS-1$
            answer.complete(dialog.open() == SEND);
        });
        return answer;
    }

}
