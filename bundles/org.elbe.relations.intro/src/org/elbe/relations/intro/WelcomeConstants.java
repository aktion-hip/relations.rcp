/*
 * Copyright (c) 2026, Benno Luthiger
 */
package org.elbe.relations.intro;

/** Constants used by the Relations welcome page.
 *
 * @author lbenno */
public final class WelcomeConstants {
    /** The symbolic name of this bundle, also used as preference node. */
    public static final String BUNDLE_ID = "org.elbe.relations.intro"; //$NON-NLS-1$
    /** The contributor URI used to translate the bundle's <code>%keys</code>. */
    public static final String CONTRIBUTOR_URI = "platform:/plugin/" + BUNDLE_ID; //$NON-NLS-1$

    /** The id of the welcome part descriptor (see <code>Welcome.e4xmi</code>). */
    public static final String PART_ID = "org.elbe.relations.intro.part.welcome"; //$NON-NLS-1$
    /** The id of the <i>Help &gt; Welcome</i> command (see <code>Relations.e4xmi</code>). The handler is activated
     * programmatically, because the menu item shares this id, i.e. a model import would be ambiguous. */
    public static final String COMMAND_WELCOME = "org.eclipse.ui.help.quickStartAction"; //$NON-NLS-1$
    /** The part stack of the Relations browsers, i.e. the stack the welcome page is shown in. */
    public static final String STACK_ID = "relations.views"; //$NON-NLS-1$

    /** Preference key: <code>true</code> once the welcome page has been shown automatically. */
    public static final String PREF_WELCOME_SHOWN = "welcome.shown"; //$NON-NLS-1$

    /** The bundle folder containing the welcome page resources. */
    public static final String WELCOME_DIR = "welcome"; //$NON-NLS-1$
    /** The name of the (english) welcome page; translations are named <code>welcome_&lt;lang&gt;.html</code>. */
    public static final String WELCOME_PAGE = "welcome"; //$NON-NLS-1$
    public static final String WELCOME_PAGE_EXT = ".html"; //$NON-NLS-1$

    /** The URL scheme the welcome page uses for links handled by the application. */
    public static final String SCHEME = "relations-welcome://"; //$NON-NLS-1$
    public static final String ACTION_HELP = "help"; //$NON-NLS-1$
    public static final String ACTION_CLOSE = "close"; //$NON-NLS-1$
    public static final String PARAM_TOPIC = "topic="; //$NON-NLS-1$

    /** The help topic 'Getting started with Relations'. */
    public static final String TOPIC_GETTING_STARTED = "/org.elbe.relations.help/html/gettingstarted/index.html"; //$NON-NLS-1$

    private WelcomeConstants() {
        // prevent instantiation
    }
}
