/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

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
package org.hip.kernel.code;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** Home of <code>CodeList</code>s.
 *
 * @author: Benno Luthiger */
public final class CodeListHome {

    // instance attributes
    private transient final Map<String, HashMap<String, CodeList>> codeListHashtablesPerLanguage;

    // class attributes
    private static CodeListHome theCodeHome;

    /** CodeListHome singleton constructor. */
    private CodeListHome() {
        codeListHashtablesPerLanguage = new HashMap<String, HashMap<String, CodeList>>(5);
    }

    /** Returns an instance of the CodeList the specified CodeClass belongs to.
     *
     * @return org.hip.kernel.code.CodeList
     * @param inCodeClass java.lang.Class
     * @param inLanguage java.lang.String
     * @exception org.hip.kernel.code.CodeListNotFoundException The exception description. */
    public CodeList getCodeList(final Class<?> inCodeClass, final String inLanguage) throws CodeListNotFoundException {
        try {
            final String lCodeID = inCodeClass.getField("CODEID").get("").toString();
            final URL lUrl = inCodeClass.getResource("/" + lCodeID + CodeListFactory.FILE_EXTENSION);
            return getCodeList(lCodeID, inLanguage, lUrl);
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException exc) {
            throw new CodeListNotFoundException(exc);
        }
    }

    /** Returns the code list instance with the specified id.
     *
     * @param inCodeID String
     * @param inLanguage String
     * @param inUrl {@link URL}
     * @return {@link CodeList}
     * @throws CodeListNotFoundException */
    public CodeList getCodeList(final String inCodeID, final String inLanguage, final URL inUrl)
            throws CodeListNotFoundException {
        CodeList outCodeList;
        Map<String, CodeList> lTableOfCodeLists;
        lTableOfCodeLists = codeListHashtablesPerLanguage.get(inLanguage);

        if (lTableOfCodeLists == null) {
            lTableOfCodeLists = new HashMap<String, CodeList>(11);
            codeListHashtablesPerLanguage.put(inLanguage, (HashMap<String, CodeList>) lTableOfCodeLists);
        }

        outCodeList = lTableOfCodeLists.get(inCodeID);
        if (outCodeList == null) {
            outCodeList = new CodeListFactory().createCodeList(inCodeID, inLanguage, inUrl);
            if (outCodeList != null) {
                lTableOfCodeLists.put(inCodeID, outCodeList);
            }
        }

        return outCodeList;
    }

    /** Get the prepared CodeList for given CodeID, or prepare one, if it doesn't exists yet.
     *
     * @param String codeID
     * @param String inLanguage as defined by java.util.Locale
     * @return the CodeList which belongs to the codeID */
    public CodeList getCodeList(final String inCodeID, final String inLanguage) throws CodeListNotFoundException {
        return getCodeList(inCodeID, inLanguage, null);
    }

    /** Return the singleton object.
     *
     * @return the CodeListHome */
    public synchronized static CodeListHome instance() { // NOPMD by lbenno
        if (theCodeHome == null) {
            theCodeHome = new CodeListHome();
        }
        return theCodeHome;
    }

}
