/*
 * Copyright (c) 2025, Benno Luthiger
 */
package org.elbe.relations.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.swt.program.Program;

/** Handler to display the Relations help content.
 * 
 * @author lbenno */
public class HelpContentHandler {

    @SuppressWarnings("restriction")
    @Execute
    public void execute() {
        BaseHelpSystem.ensureWebappRunning();
        final String helpURL = "http://" //$NON-NLS-1$
                + WebappManager.getHost() + ":" //$NON-NLS-1$
                + WebappManager.getPort() + "/help/index.jsp"; //$NON-NLS-1$
        Program.launch(helpURL);
    }
}
