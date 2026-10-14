/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

/** Helper to show and remove the welcome part.
 *
 * @author lbenno */
public final class WelcomeOpener {

    private WelcomeOpener() {
        // prevent instantiation
    }

    /** Shows the welcome page in front of the browser area. If the welcome page is open already, it is activated.
     *
     * @param application {@link MApplication}
     * @param modelService {@link EModelService}
     * @param partService {@link EPartService} */
    public static void open(final MApplication application, final EModelService modelService,
            final EPartService partService) {
        MPart part = partService.findPart(WelcomeConstants.PART_ID);
        if (part == null) {
            part = partService.createPart(WelcomeConstants.PART_ID);
            if (part == null) {
                return;
            }
            final MUIElement stack = modelService.find(WelcomeConstants.STACK_ID, application);
            if (stack instanceof final MPartStack partStack) {
                partStack.getChildren().add(part);
            }
        }
        partService.showPart(part, PartState.ACTIVATE);
    }

    /** Removes all welcome parts from the (persisted) application model, i.e. welcome pages that have been open when the
     * application was closed.
     *
     * @param application {@link MApplication}
     * @param modelService {@link EModelService} */
    public static void removeAll(final MApplication application, final EModelService modelService) {
        final List<MPart> parts = modelService.findElements(application, WelcomeConstants.PART_ID, MPart.class);
        for (final MPart part : parts) {
            final MElementContainer<MUIElement> parent = part.getParent();
            if (parent == null) {
                continue;
            }
            if (parent.getSelectedElement() == part) {
                parent.setSelectedElement(null);
            }
            parent.getChildren().remove(part);
        }
    }

}
