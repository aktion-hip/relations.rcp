/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;
import org.osgi.service.prefs.BackingStoreException;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/** Addon showing the welcome page on the first start of the application.
 * <p>
 * A welcome page that was open when the application was closed is removed from the persisted model before rendering,
 * i.e. the welcome page is never restored.
 * </p>
 *
 * @author lbenno */
public class WelcomeAddon {

    @Inject
    private Logger log;

    @PostConstruct
    void removePersistedWelcome(final MApplication application, final EModelService modelService) {
        WelcomeOpener.removeAll(application, modelService);
    }

    @Inject
    @Optional
    void appStartupComplete(@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) final Event event,
            final MApplication application, final EModelService modelService, final EPartService partService,
            final EHandlerService handlerService, final UISynchronize sync) {
        // activated after the model handlers have been processed, so that this handler wins
        handlerService.activateHandler(WelcomeConstants.COMMAND_WELCOME, new OpenWelcomeHandler());
        if (!markFirstStart()) {
            return;
        }
        // defer, so that the welcome page is shown after the active browser has been restored
        sync.asyncExec(() -> WelcomeOpener.open(application, modelService, partService));
    }

    /** Checks whether this is the first start and marks the welcome page as shown. The flag is flushed before the page
     * is shown, so that the page is not shown again even if the application terminates abnormally.
     *
     * @return boolean <code>true</code> if the welcome page has not been shown yet */
    private boolean markFirstStart() {
        final IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(WelcomeConstants.BUNDLE_ID);
        if (preferences.getBoolean(WelcomeConstants.PREF_WELCOME_SHOWN, false)) {
            return false;
        }
        preferences.putBoolean(WelcomeConstants.PREF_WELCOME_SHOWN, true);
        try {
            preferences.flush();
        } catch (final BackingStoreException exc) {
            this.log.error(exc, exc.getMessage());
        }
        return true;
    }

}
