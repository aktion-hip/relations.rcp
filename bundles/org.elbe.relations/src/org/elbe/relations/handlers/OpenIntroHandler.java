/*
 * Copyright (c) 2025, Benno Luthiger
 */
package org.elbe.relations.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.swt.program.Program;

/**
 * Handler for the welcome page.
 *
 * @author lbenno
 */
public class OpenIntroHandler {
    // The command ID you will use in Application.e4xmi
    public static final String COMMAND_ID = "org.elbe.relations.openIntro";

    @SuppressWarnings("restriction")
    @Execute
    public void execute() throws Exception {
        BaseHelpSystem.ensureWebappRunning();
        final String introURL = "http://" //$NON-NLS-1$
                + WebappManager.getHost() + ":" //$NON-NLS-1$
                + WebappManager.getPort() + "/help/topic/org.eclipse.platform.welcome"; //$NON-NLS-1$
        Program.launch(introURL);
    }
}
