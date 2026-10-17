/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.internal.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.elbe.relations.RelationsMessages;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/** Displays the startup progress (progress bar and status message) on the splash screen.
 * <p>
 * The E4Application doesn't evaluate the splash handlers of the 3.x workbench, therefore, this class attaches itself
 * directly to the splash window of the launcher (the same way the 3.x workbench does). The progress bar and the
 * message are placed in the areas defined by the product properties <code>startupProgressRect</code> and
 * <code>startupMessageRect</code>.
 * </p>
 * <p>
 * The progress display is removed when the application signals that it is running, i.e. together with the splash
 * screen. If no splash screen is available, all methods are no-ops.
 * </p>
 *
 * @author lbenno */
public final class SplashProgress {
    /** Message keys of the startup phases, in the order they are passed. */
    public static final String STEP_PREPARE = "splash.progress.prepare"; //$NON-NLS-1$
    public static final String STEP_DATABASE = "splash.progress.database"; //$NON-NLS-1$
    public static final String STEP_DATA = "splash.progress.data"; //$NON-NLS-1$
    public static final String STEP_INDEX = "splash.progress.index"; //$NON-NLS-1$
    public static final String STEP_BROWSERS = "splash.progress.browsers"; //$NON-NLS-1$
    public static final String STEP_UI = "splash.progress.ui"; //$NON-NLS-1$
    private static final int STEPS = 6;

    private static final String PROP_SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle"; //$NON-NLS-1$
    private static final String PROP_SPLASH_LOCATION = "osgi.splashLocation"; //$NON-NLS-1$
    private static final String SPLASH_IMAGE = "splash.png"; //$NON-NLS-1$

    private static final String PRODUCT_PROGRESS_RECT = "startupProgressRect"; //$NON-NLS-1$
    private static final String PRODUCT_MESSAGE_RECT = "startupMessageRect"; //$NON-NLS-1$
    private static final String PRODUCT_FOREGROUND = "startupForegroundColor"; //$NON-NLS-1$
    // fallbacks: the values of the Relations product
    private static final Rectangle DFT_PROGRESS_RECT = new Rectangle(2, 286, 450, 12);
    private static final Rectangle DFT_MESSAGE_RECT = new Rectangle(15, 263, 450, 20);
    private static final int DFT_FOREGROUND = 0xFFFFFF;

    private final Logger log;
    private Shell splash;
    private Image background;
    private ProgressBar progressBar;
    private Label message;
    private ServiceRegistration<StartupMonitor> registration;
    private int position;

    private SplashProgress(final Shell splash, final Logger log) {
        this.splash = splash;
        this.log = log;
    }

    /** Creates the progress display on the splash screen.
     *
     * @param display {@link Display} the application's display, may be <code>null</code>
     * @param log {@link Logger}
     * @return {@link SplashProgress} never <code>null</code>, a no-op instance if there is no splash screen */
    public static SplashProgress create(final Display display, final Logger log) {
        final SplashProgress progress = new SplashProgress(getSplashShell(display, log), log);
        progress.init();
        return progress;
    }

    private static Shell getSplashShell(final Display display, final Logger log) {
        final String handle = System.getProperty(PROP_SPLASH_HANDLE);
        if (handle == null || display == null || display.isDisposed()) {
            return null;
        }
        try {
            return Shell.internal_new(display, Long.parseLong(handle));
        } catch (RuntimeException | SWTError exc) {
            log.warn(exc, "Unable to access the splash screen."); //$NON-NLS-1$
            return null;
        }
    }

    private void init() {
        if (this.splash == null) {
            return;
        }
        registerStartupMonitor();
        try {
            this.background = loadSplashImage(this.splash.getDisplay());
            if (this.background != null) {
                // the launcher paints the splash image natively, SWT has to know it to draw transparent labels
                this.splash.setBackgroundImage(this.background);
            }
            this.splash.setBackgroundMode(SWT.INHERIT_FORCE);

            final IProduct product = Platform.getProduct();
            final Color foreground = new Color(getForeground(product));

            this.progressBar = new ProgressBar(this.splash, SWT.HORIZONTAL | SWT.SMOOTH);
            this.progressBar.setMinimum(0);
            this.progressBar.setMaximum(STEPS);
            this.progressBar.setBounds(getRectangle(product, PRODUCT_PROGRESS_RECT, DFT_PROGRESS_RECT));

            this.message = new Label(this.splash, SWT.NONE);
            this.message.setForeground(foreground);
            this.message.setBounds(getRectangle(product, PRODUCT_MESSAGE_RECT, DFT_MESSAGE_RECT));
        } catch (RuntimeException | SWTError exc) {
            this.log.warn(exc, "Unable to display the startup progress on the splash screen."); //$NON-NLS-1$
            disposeControls();
        }
    }

    private void registerStartupMonitor() {
        final Bundle bundle = FrameworkUtil.getBundle(SplashProgress.class);
        final BundleContext context = bundle == null ? null : bundle.getBundleContext();
        if (context == null) {
            return;
        }
        final Dictionary<String, Object> properties = new Hashtable<>();
        // the highest ranking ensures that the progress is removed before the launcher closes the splash window
        properties.put(Constants.SERVICE_RANKING, Integer.valueOf(Integer.MAX_VALUE));
        this.registration = context.registerService(StartupMonitor.class, new StartupMonitor() {
            @Override
            public void update() {
                // nothing to do
            }

            @Override
            public void applicationRunning() {
                final Display display = SplashProgress.this.splash == null ? null
                        : SplashProgress.this.splash.getDisplay();
                if (display == null || display.isDisposed() || Display.getCurrent() == display) {
                    dispose();
                } else {
                    display.syncExec(SplashProgress.this::dispose);
                }
            }
        }, properties);
    }

    /** Advances the progress bar to the next startup phase and displays the phase's message.
     *
     * @param messageKey String the key of the phase's message, one of the <code>STEP_*</code> constants */
    public void step(final String messageKey) {
        if (!isActive()) {
            return;
        }
        this.position = Math.min(this.position + 1, STEPS);
        this.progressBar.setSelection(this.position);
        this.message.setText(RelationsMessages.getString(messageKey));
        // there is no event loop during startup: repaint explicitly
        this.message.update();
        this.progressBar.update();
        this.splash.update();
    }

    /** Runs the specified runnable (e.g. a dialog) with the splash screen hidden.
     *
     * @param runnable {@link Runnable} */
    public void runHidden(final Runnable runnable) {
        final boolean hide = this.splash != null && !this.splash.isDisposed() && this.splash.isVisible();
        if (hide) {
            this.splash.setVisible(false);
        }
        try {
            runnable.run();
        } finally {
            if (hide && !this.splash.isDisposed()) {
                this.splash.setVisible(true);
            }
        }
    }

    /** Removes the progress display and releases the splash screen. Can be called more than once. */
    public void dispose() {
        if (this.registration != null) {
            try {
                this.registration.unregister();
            } catch (final IllegalStateException exc) {
                // already unregistered
            }
            this.registration = null;
        }
        disposeControls();
        if (this.splash != null && !this.splash.isDisposed()) {
            this.splash.dispose();
        }
        this.splash = null;
        if (this.background != null && !this.background.isDisposed()) {
            this.background.dispose();
        }
        this.background = null;
    }

    private void disposeControls() {
        if (this.progressBar != null && !this.progressBar.isDisposed()) {
            this.progressBar.dispose();
        }
        this.progressBar = null;
        if (this.message != null && !this.message.isDisposed()) {
            this.message.dispose();
        }
        this.message = null;
    }

    private boolean isActive() {
        return this.splash != null && !this.splash.isDisposed() && this.progressBar != null
                && !this.progressBar.isDisposed();
    }

    private Image loadSplashImage(final Display display) {
        try (InputStream input = openSplashImage()) {
            return input == null ? null : new Image(display, input);
        } catch (IOException | RuntimeException | SWTError exc) {
            this.log.warn(exc, "Unable to load the splash image."); //$NON-NLS-1$
            return null;
        }
    }

    private static InputStream openSplashImage() throws IOException {
        final String location = System.getProperty(PROP_SPLASH_LOCATION);
        if (location != null && Files.isReadable(Path.of(location))) {
            return Files.newInputStream(Path.of(location));
        }
        final Bundle bundle = FrameworkUtil.getBundle(SplashProgress.class);
        final URL entry = bundle == null ? null : bundle.getEntry(SPLASH_IMAGE);
        return entry == null ? null : entry.openStream();
    }

    private static Rectangle getRectangle(final IProduct product, final String property, final Rectangle fallback) {
        final String value = product == null ? null : product.getProperty(property);
        if (value == null) {
            return fallback;
        }
        final String[] parts = value.split(","); //$NON-NLS-1$
        if (parts.length != 4) {
            return fallback;
        }
        try {
            return new Rectangle(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim()));
        } catch (final NumberFormatException exc) {
            return fallback;
        }
    }

    private static RGB getForeground(final IProduct product) {
        int rgb = DFT_FOREGROUND;
        final String value = product == null ? null : product.getProperty(PRODUCT_FOREGROUND);
        if (value != null) {
            try {
                rgb = Integer.parseInt(value.trim(), 16);
            } catch (final NumberFormatException exc) {
                // use default
            }
        }
        return new RGB((rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, rgb & 0xFF);
    }

}
