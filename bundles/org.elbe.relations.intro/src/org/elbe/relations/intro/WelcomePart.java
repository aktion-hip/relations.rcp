/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/** The welcome page: displays the (localized) welcome HTML page in an embedded browser. Links to the Relations help
 * and the 'Start working' link are handled by the application. If no embedded browser is available, a plain fallback
 * is displayed.
 *
 * @author lbenno */
public class WelcomePart {

    @Inject
    private Logger log;

    @Inject
    private MPart part;

    @Inject
    private EPartService partService;

    @Inject
    private TranslationService translation;

    @Inject
    @Optional
    @Named(TranslationService.LOCALE)
    private Locale locale;

    @PostConstruct
    public void createControls(final Composite parent) {
        parent.setLayout(new FillLayout());
        Browser browser = null;
        try {
            browser = new Browser(parent, SWT.NONE);
        } catch (final SWTError exc) {
            this.log.error(exc, translate("welcome.error.browser")); //$NON-NLS-1$
            disposeChildren(parent);
            createFallback(parent);
            return;
        }

        final String pageUrl = getPageUrl();
        if (pageUrl == null) {
            this.log.error(translate("welcome.error.content")); //$NON-NLS-1$
            browser.dispose();
            createFallback(parent);
            return;
        }
        browser.addLocationListener(LocationListener.changingAdapter(this::handleLocation));
        browser.setUrl(pageUrl);
    }

    private void handleLocation(final LocationEvent event) {
        final String location = event.location;
        if (location == null) {
            return;
        }
        if (location.startsWith(WelcomeConstants.SCHEME)) {
            event.doit = false;
            handleWelcomeLink(location.substring(WelcomeConstants.SCHEME.length()), event.display);
        } else if (location.startsWith("http://") || location.startsWith("https://")) { //$NON-NLS-1$ //$NON-NLS-2$
            // the welcome page never navigates away
            event.doit = false;
            Program.launch(location);
        }
    }

    private void handleWelcomeLink(final String link, final org.eclipse.swt.widgets.Display display) {
        if (link.startsWith(WelcomeConstants.ACTION_HELP)) {
            HelpLauncher.showTopic(getTopic(link));
        } else if (link.startsWith(WelcomeConstants.ACTION_CLOSE)) {
            // don't dispose the browser while it is processing the location event
            display.asyncExec(() -> this.partService.hidePart(this.part, true));
        }
    }

    private static String getTopic(final String link) {
        final int pos = link.indexOf(WelcomeConstants.PARAM_TOPIC);
        if (pos < 0) {
            return null;
        }
        String topic = URLDecoder.decode(link.substring(pos + WelcomeConstants.PARAM_TOPIC.length()),
                StandardCharsets.UTF_8);
        final int end = topic.indexOf('&');
        if (end >= 0) {
            topic = topic.substring(0, end);
        }
        return topic;
    }

    /** @return String the file URL of the welcome page in the actual language (english as fallback) or
     *         <code>null</code> if the page can't be found */
    private String getPageUrl() {
        final Bundle bundle = FrameworkUtil.getBundle(WelcomePart.class);
        final URL dirEntry = FileLocator.find(bundle, IPath.fromPortableString(WelcomeConstants.WELCOME_DIR), null);
        if (dirEntry == null) {
            return null;
        }
        try {
            // converting the directory extracts all the page's resources (css, images) too
            final File dir = new File(FileLocator.toFileURL(dirEntry).getPath());
            final String language = (this.locale == null ? Locale.getDefault() : this.locale).getLanguage();
            File page = new File(dir,
                    WelcomeConstants.WELCOME_PAGE + "_" + language + WelcomeConstants.WELCOME_PAGE_EXT); //$NON-NLS-1$
            if (language.isEmpty() || !page.isFile()) {
                page = new File(dir, WelcomeConstants.WELCOME_PAGE + WelcomeConstants.WELCOME_PAGE_EXT);
            }
            return page.isFile() ? page.toURI().toString() : null;
        } catch (final IOException exc) {
            this.log.error(exc, exc.getMessage());
            return null;
        }
    }

    private void createFallback(final Composite parent) {
        final Composite fallback = new Composite(parent, SWT.NONE);
        fallback.setLayout(new GridLayout(1, false));

        final Label text = new Label(fallback, SWT.WRAP);
        text.setText(translate("welcome.fallback.text")); //$NON-NLS-1$
        final GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
        data.widthHint = 500;
        text.setLayoutData(data);

        final Link help = new Link(fallback, SWT.NONE);
        help.setText(translate("welcome.fallback.help")); //$NON-NLS-1$
        help.addListener(SWT.Selection, event -> HelpLauncher.showTopic(WelcomeConstants.TOPIC_GETTING_STARTED));
        parent.layout(true, true);
    }

    private static void disposeChildren(final Composite parent) {
        for (final Control child : parent.getChildren()) {
            child.dispose();
        }
    }

    private String translate(final String key) {
        return this.translation.translate("%" + key, WelcomeConstants.CONTRIBUTOR_URI); //$NON-NLS-1$
    }

}
