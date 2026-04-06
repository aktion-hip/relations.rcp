/*
 * Copyright (c) 2025, Benno Luthiger
 */
package org.elbe.relations.utility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import jakarta.annotation.PostConstruct;

/**
 * @author lbenno
 */
public class IntroDisplayStartup {

    @PostConstruct
    public void createControls(final Composite parent) {
        parent.setLayout(new FillLayout());
        final Label label = new Label(parent, SWT.CENTER);
        label.setText("Welcome to your e4 Application!");
        // Add your branding, quick links, etc.
    }
}
