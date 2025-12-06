/**
	This package is part of the application VIF.
	Copyright (C) 2009-2015, Benno Luthiger

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hip.kernel.sys;

/** Constants for the <code>VSys</code> class.
 *
 * @author Luthiger Created 21.10.2009 */
public final class VSysConstants {
    // Application i18n
    public final static String KEY_DFT_COUNTRY = "org.hip.vif.dftCountry";
    public final static String KEY_DFT_LANGUAGE = "org.hip.vif.dftLanguage";
    public static final String KEY_DFT_CONTENT_LANGUAGE = "org.hip.vif.content.language ";
    public static final String KEY_DFT_DATE_PATTERN = "org.hip.vif.datePattern ";

    // Application resources
    public final static String LOGFILE_PATH = "org.hip.vif.logPath";

    // Database connection configuration
    public static final String KEY_DB_DRIVER = "org.hip.vif.db.driver";
    public static final String KEY_DB_URL = "org.hip.vif.db.url";
    public static final String KEY_DB_USER = "org.hip.vif.db.userId";
    public static final String KEY_DB_PASSWD = "org.hip.vif.db.password";

    // Application XML an XSL configuration
    public final static String KEY_SAX_PARSER_FACTORY = "javax.xml.parsers.SAXParserFactory";
    public final static String KEY_DOCUMENT_BUILDER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory";
    public final static String KEY_XSL_PROCESSOR_FACTORY = "javax.xml.transform.TransformerFactory";

    private VSysConstants() {
        // prevent instantiation
    }

}
