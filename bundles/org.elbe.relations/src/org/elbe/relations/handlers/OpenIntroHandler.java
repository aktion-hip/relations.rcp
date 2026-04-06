/*
 * Copyright (c) 2025, Benno Luthiger
 */
package org.elbe.relations.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;

/**
 * Handler for the welcome page.
 *
 * @author lbenno
 */
public class OpenIntroHandler {
    // The command ID you will use in Application.e4xmi
    public static final String COMMAND_ID = "org.elbe.relations.openIntro";

    @Execute
    public void execute(@Optional @UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) final String event) {
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }

        try {
            final IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
            introManager.showIntro(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), false);

        } catch (final IllegalStateException e) {
            // This catch block should now be unnecessary if the event fires correctly,
            // but keeps the application stable.
            e.printStackTrace();
        }
    }

}
