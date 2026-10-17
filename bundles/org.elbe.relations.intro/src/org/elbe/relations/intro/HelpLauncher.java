/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.server.WebappManager;
import org.eclipse.swt.program.Program;

/** Opens the Relations help in the system browser, the same way <i>Help &gt; Help Contents</i> does.
 *
 * @author lbenno */
@SuppressWarnings("restriction")
public final class HelpLauncher {

    private HelpLauncher() {
        // prevent instantiation
    }

    /** Opens the help on the specified topic.
     *
     * @param topic String the help topic, e.g. <code>/org.elbe.relations.help/html/gettingstarted/index.html</code>,
     *            may be <code>null</code> or empty to open the help start page */
    public static void showTopic(final String topic) {
        BaseHelpSystem.ensureWebappRunning();
        final StringBuilder helpURL = new StringBuilder("http://") //$NON-NLS-1$
                .append(WebappManager.getHost()).append(":") //$NON-NLS-1$
                .append(WebappManager.getPort()).append("/help/index.jsp"); //$NON-NLS-1$
        if (topic != null && !topic.isEmpty()) {
            helpURL.append("?topic=").append(topic); //$NON-NLS-1$
        }
        Program.launch(helpURL.toString());
    }

}
