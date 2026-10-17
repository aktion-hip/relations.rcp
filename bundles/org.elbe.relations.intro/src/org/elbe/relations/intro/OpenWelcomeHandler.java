/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/** Handler for <i>Help &gt; Welcome</i>: shows the welcome page.
 *
 * @author lbenno */
public class OpenWelcomeHandler {

    @Execute
    public void execute(final MApplication application, final EModelService modelService,
            final EPartService partService) {
        WelcomeOpener.open(application, modelService, partService);
    }

}
