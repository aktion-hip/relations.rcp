/*
 * Copyright (c) 2025 ETH Zurich
 */
package org.elbe.relations.internal.utility;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.splash.AbstractSplashHandler;
import org.eclipse.ui.splash.BasicSplashHandler;

/** @author lbenno */
public class RelationsSplashHandler extends AbstractSplashHandler {
    private ProgressBar progressBar;
    private static final int PROGRESS_STEPS = 20;

    @Override
    public void init(Shell splash) {
        super.init(splash);

        createUI(splash);
        splash.layout();
    }

    private void createUI(Shell splash) {
        splash.setLayout(null); // Allows absolute positioning

        Rectangle splashBounds = splash.getBounds();

        int barWidth = 300;
        int barHeight = 15;

        int barX = (splashBounds.width - barWidth) / 2;
        int barY = splashBounds.height - 50;

        progressBar = new ProgressBar(splash, SWT.NONE);
        progressBar.setMinimum(0);
        progressBar.setMaximum(PROGRESS_STEPS);
        progressBar.setBounds(barX, barY, barWidth, barHeight);

        Label versionLabel = new Label(splash, SWT.NONE);
        versionLabel.setBounds(10, splashBounds.height - 20, 200, 15);
        versionLabel.setText("Starting…");
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void updateSplashShell(Display display) {
        // Called repeatedly during startup → automatically drives the progress bar
        if (!progressBar.isDisposed()) {
            int selection = progressBar.getSelection();
            if (selection < PROGRESS_STEPS) {
                progressBar.setSelection(selection + 1);
            }
        }
    }

}
