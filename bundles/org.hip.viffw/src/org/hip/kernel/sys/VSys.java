/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.hip.kernel.sys; // NOPMD by lbenno

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.hip.kernel.bom.HomeManager;
import org.hip.kernel.bom.impl.HomeManagerImpl;
import org.hip.kernel.servlet.impl.ServletContainer;

/** This is a wrapper around java.lang.System.
 *
 * @autor Benno Luthiger
 * @version 1.0
 * @see java.lang.Object */
public class VSys extends VObject { // NOPMD by lbenno

    public static String nl = "\n"; // NOPMD by lbenno

    /** This static attribute provides access to the currently set input stream. Actually this maps to
     * java.langSystem.out. Future releases may extend the default behaviour.
     *
     * @see java.lang.System */
    public final static InputStream in = java.lang.System.in; // NOPMD by lbenno

    /** This static attribute provides access to the currently set standard output stream. Actually this maps to
     * java.lang.System.out Future releases may extend the default behaviour.
     *
     * @see java.lang.System */
    public final static PrintStream out = java.lang.System.out; // NOPMD by lbenno

    /** This static attribute provides access to the currently set standard output stream. Actually this maps to
     * java.lang.System.out Future releases may extend the default behaviour.
     *
     * @see java.lang.System */
    public final static PrintStream err = java.lang.System.err; // NOPMD by lbenno

    /** Access to the current assertLevel. By default this is the default level defined in the org.hip.kernel.sys.Assert
     * class. This can be changed if another policy is required. */
    public final static int assertLevel = Assert.DEFAULT_LEVEL; // NOPMD by lbenno

    /** Access to the HomeManager singleton */
    public final static HomeManager homeManager = HomeManagerImpl.getSingleton(); // NOPMD by lbenno

    private final static String cSep = System.getProperty("file.separator"); // NOPMD by lbenno

    private static String cContextPath = "";
    private final static String CONF_PATH = cSep + "WEB-INF" + cSep + "conf" + cSep;
    private static boolean cUseConfPath = true;

    private static String cSysName = "vif";
    public final static String SYS_FILE_EXT = ".properties";
    public final static Locale dftLocale = Locale.US; // NOPMD by lbenno
    public final static String dftLanguage = dftLocale.getLanguage(); // NOPMD by lbenno
    public final static String dftCountry = dftLocale.getCountry(); // NOPMD by lbenno

    private static boolean doTracing = true;
    private static String cLogPath;

    // Private
    private static Properties cSysProperties;

    /** This method is propagated to Assert.assertNotNull().
     *
     * @return boolean
     * @param inAssertLevel int
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inObject java.lang.Object */
    public static boolean assertNotNull(final int inAssertLevel, final Object inCaller, final String inCallerMethod,
            final Object inObject) {
        // We call the asert with the current assert level
        return Assert.assertNotNull(inAssertLevel, inCaller, inCallerMethod, inObject);
    }

    /** This method is propagated to Assert.assertTrue().
     *
     * @return boolean
     * @param inAssertLevel int
     * @param inCaller java.lang.Object
     * @param inCallingMethod java.lang.String
     * @param inCondition boolean */
    public static boolean assertTrue(final int inAssertLevel, final Object inCaller, final String inCallingMethod,
            final boolean inCondition) {
        // We call the assert with the current assert level
        return Assert.assertTrue(inAssertLevel, inCaller, inCallingMethod, inCondition);
    }

    /** This method is propagated to Assert.assertNotNull(). If it evaluates to false, it will print out an assert
     * failure message or throw a AssertionFailedError.
     *
     * @return boolean
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inObject java.lang.Object */
    public static boolean assertNotNull(final Object inCaller, final String inCallerMethod, final Object inObject) {
        // We call the assert with the current assert level
        return Assert.assertNotNull(VSys.assertLevel, inCaller, inCallerMethod, inObject);
    }

    /** This method is propagated to Assert.assertTrue().
     *
     * @return boolean
     * @param inCaller java.lang.Object
     * @param inCallerMethod java.lang.String
     * @param inCondition boolean */
    public static boolean assertTrue(final Object inCaller, final String inCallerMethod, final boolean inCondition) {
        // We call the assert with the current assert level
        return Assert.assertTrue(VSys.assertLevel, inCaller, inCallerMethod, inCondition);
    }

    /** Returns the path where the messages are logged.
     *
     * @return java.lang.String */
    @Deprecated
    public static String getLogPath() {
        if (cLogPath == null) { // NOPMD by lbenno
            try {
                cLogPath = getVSysProperties().getProperty(VSysConstants.LOGFILE_PATH);
                if (cLogPath == null) { // NOPMD by lbenno
                    cLogPath = "";
                }
                else {
                    cLogPath += File.separator;
                    File lLogDir = new File(cLogPath);
                    if (!lLogDir.exists()) {
                        final String lProperty = ServletContainer.getInstance().getBasePath();
                        if (lProperty == null) {
                            if (log_mkdir(lLogDir)) {
                                cLogPath = lLogDir.getCanonicalPath() + File.separator;
                            }
                        }
                        else {
                            lLogDir = new File(lProperty + File.separator + cLogPath);

                            cLogPath = "";
                            // maybe the directory exists yet
                            if (lLogDir.exists()) {
                                cLogPath = lLogDir.getCanonicalPath() + File.separator;
                            }
                            else {
                                // if not, create it
                                if (log_mkdir(lLogDir)) {
                                    cLogPath = lLogDir.getCanonicalPath() + File.separator;
                                }
                            }
                        }
                    }
                }
            } catch (final IOException ex) {
                // default place of system log is current directory
                cLogPath = "";
            }
        }
        return cLogPath;
    }

    private static boolean log_mkdir(final File inLogDir) { // NOPMD by lbenno
        if (inLogDir.mkdir()) {
            if (inLogDir.exists()) {
                return true;
            }
        }
        return false;
    }

    /** Returns the application specific properties. The application's properties file has to reside in
     * $APPLICATION_CONTEXT/WEB-INF/conf/
     *
     * @return java.util.Properties */
    public synchronized static Properties getVSysProperties() throws IOException { // NOPMD by lbenno
        if (cSysProperties == null) {
            cSysProperties = new Properties();
            final File lDir = new File(cContextPath + (cUseConfPath ? CONF_PATH : ""));
            final File lFile = new File(lDir, cSysName + SYS_FILE_EXT);
            if (lFile.exists()) {
                cSysProperties.load(new FileInputStream(lFile));
            }
            else {
                final InputStream lStream = VSys.class.getResourceAsStream("/" + cSysName + SYS_FILE_EXT);
                if (lStream != null) {
                    cSysProperties.load(lStream);
                }
            }

            // //Read some application specific properties and write them to the system properties
            // Properties lProperties = System.getProperties();
            // putToSystemProperty(VSysConstants.KEY_SAX_PARSER_FACTORY, lProperties);
            // putToSystemProperty(VSysConstants.KEY_DOCUMENT_BUILDER_FACTORY, lProperties);
            // putToSystemProperty(VSysConstants.KEY_XSL_PROCESSOR_FACTORY, lProperties);
            // System.setProperties(lProperties);
        }
        return cSysProperties;
    }

    /** <p>
     * Convenience method to get the specified key's value from the application's properties possibly without
     * initializing the system.
     * </p>
     * <p>
     * This method should only be used in the bundle activation phase.
     * </p>
     *
     * @param inKey String
     * @return String the property value */
    public static String getBundleProperty(final String inKey) {
        // without context path set, we try to retrieve the properties file from the bundle
        if (cContextPath.isEmpty()) {
            try {
                final ResourceBundle lBundle = ResourceBundle.getBundle(cSysName);
                return lBundle == null ? null : (String) lBundle.getObject(inKey);
            } catch (final MissingResourceException exc) {
                return null;
            }
        }
        // else, load the properties file from appContext/WEB-INF/conf/
        try {
            return getVSysProperties().getProperty(inKey);
        } catch (final IOException exc) { // NOPMD by lbenno
            // intentionally left empty
        }
        return null;
    }

    /** @return boolean <code>true</code> if tracing is on. */
    public static boolean isTracing() {
        return doTracing;
    }

    /** Sets the path to context in the servlet container the application is running in. This attribute should be set in
     * the servlet's init() method.
     *
     * @param inContextPath java.lang.String
     * @see javax.servlet.GenericServlet */
    public static void setContextPath(final String inContextPath) {
        if (inContextPath != null) {
            cContextPath = inContextPath;
        }
    }

    /** Returns the servlet context's real path.
     *
     * @return a <code>String</code> specifying the servlet context's real path, or <code>empty</code> if the
     *         translation cannot be performed
     * @see ServletContext.getRealPath() */
    public static String getContextPath() {
        return cContextPath;
    }

    /** Sets the flag that indicates to use the WEB-INF/conf/ path to find the application's system properties.
     *
     * @param inUseConfPath */
    public static void useConfPath(final boolean inUseConfPath) {
        cUseConfPath = inUseConfPath;
    }

    /** Sets the name of the application's properties file. This attribute should be set in the servlet's init() method.
     *
     * @param inSysName java.lang.String
     * @see javax.servlet.GenericServlet */
    public static void setSysName(final String inSysName) {
        cSysName = inSysName;
    }

    /** This method invokes some test against VSys.
     *
     * @param args java.lang.String[] */
    public static void main(final String args[]) { // NOPMD by lbenno
        try {
            VSys.getVSysProperties();
        } catch (final IOException exc) {
            VSys.err.println("The system property file not found. Make sure the System can find a VSys.properties");
            VSys.err.println(exc);
            System.exit(-1); // NOPMD by lbenno
        }
    }

    /** Sets application properties to the specified properties. Properties == null resets/clears the application
     * properties.
     *
     * @param inProperties java.util.Properties */
    public static void setVSysProperties(final Properties inProperties) {
        cSysProperties = inProperties;
    }

    /** This convenience method returns the canonical notation of the path specified in the application's property file.
     * If the file system object doesn't exist, an empty string is returned. The directory path returned is ended with a
     * path separator.
     *
     * @param inPropertyName String
     * @return String The path specified by the property name or an empty string. */
    public static String getVSysCanonicalPath(final String inPropertyName) {
        return getVSysCanonicalPath(inPropertyName, false);
    }

    /** This convenience method returns the canonical notation of the path specified in the application's property file.
     * If <code>inCreate</code> is set to true, the file system object is created if it doesn't exist yet.
     *
     * @param inPropertyName String
     * @param inCreate boolean If true, the directory is created if it doesn't exist yet.
     * @return String The path specified by the property name or an empty string. */
    public static String getVSysCanonicalPath(final String inPropertyName, final boolean inCreate) {
        try {
            return getVSysFile(inPropertyName, inCreate).getCanonicalPath() + File.separator;
        } catch (final Exception exc) { // NOPMD by lbenno
            return "";
        }
    }

    /** Convenience method: Returns the file object of the path specified in the application's property file. If the file
     * system object doesn't exist, <code>null</code> is returned.
     *
     * @param inPropertyName String
     * @return File The File specified by the property name or null. */
    public static File getVSysFile(final String inPropertyName) {
        return getVSysFile(inPropertyName, true);
    }

    /** Convenience method: Returns the file object of the path specified in the application's property file. If
     * <code>inCreate</code> is set to true, the file system object is created if it doesn't exist yet.
     *
     * @param inPropertyName String
     * @param inCreate boolean If true, the directory is created if it doesn't exist yet.
     * @return File The File specified by the property name or null. */
    public static File getVSysFile(final String inPropertyName, final boolean inCreate) {
        try {
            final String outPath = getVSysProperties().getProperty(inPropertyName);
            final File outDir = new File(outPath);
            if (!outDir.exists()) {
                if (inCreate) {
                    outDir.mkdir();
                }
            }
            if (outDir.exists()) {
                return outDir.getCanonicalFile();
            }
            return null;
        } catch (final IOException exc) {
            return null;
        }
    }

    /** This convenience method returns the canonical notation of the path specified in the application's property file.
     * Use this method if the property specifies a path relative to the specified prefix.
     *
     * @param inPropertyName String
     * @param inPrefix String
     * @return String Canonical notation of the specified property of empty string. */
    public static String getVSysCanonicalPath(final String inPropertyName, final String inPrefix) {
        try {
            return getVSysFile(inPropertyName, inPrefix).getCanonicalPath() + File.separator;
        } catch (final Exception exc) { // NOPMD by lbenno
            return "";
        }
    }

    /** Convenience method: Returns the file object of the path specified in the application's property file. Use this
     * method if the property specifies a path relative to the specified prefix.
     *
     * @param inPropertyName String
     * @param inPrefix String
     * @return File */
    public static File getVSysFile(final String inPropertyName, final String inPrefix) {
        try {
            final String outPath = getVSysProperties().getProperty(inPropertyName);
            final File outDir = new File(inPrefix + File.separator + outPath);
            if (outDir.exists()) {
                return outDir.getCanonicalFile();
            }
            return null;
        } catch (final IOException exc) {
            return null;
        }
    }

    /** Method to call during the application's shut down. Closes all handlers attached to the application's loggers.
     *
     * @deprecated */
    @Deprecated
    public static void closeLoggers() {
        // closeVSysLoggers();
    }

}