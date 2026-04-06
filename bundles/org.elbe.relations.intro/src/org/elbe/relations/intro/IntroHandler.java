package org.elbe.relations.intro;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.internal.intro.IntroMessages;

/**
 *
 * @author Prakash G.R.
 *
 * @since 3.7
 */
public class IntroHandler extends AbstractHandler {

    private final Workbench workbench;
    private final IntroDescriptor introDescriptor;

    public IntroHandler() {
        this.workbench = (Workbench) PlatformUI.getWorkbench();
        this.introDescriptor = this.workbench.getIntroDescriptor();
    }

    @Override
    public Object execute(final ExecutionEvent event) {

        if (this.introDescriptor == null) {
            MessageDialog.openWarning(HandlerUtil.getActiveShell(event), IntroMessages.Intro_missing_product_title,
                    IntroMessages.Intro_missing_product_message);
        } else {
            final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
            this.workbench.getIntroManager().showIntro(window, false);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {

        boolean enabled = false;
        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            enabled = window.getPages().length > 0;
        }
        return enabled;
    }

}
