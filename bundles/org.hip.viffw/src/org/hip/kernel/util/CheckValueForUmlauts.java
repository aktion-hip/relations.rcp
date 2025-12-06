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
package org.hip.kernel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/** This class is a utility class needed to construct sql strings for a select with a LIKE statement. The search has to
 * be tolerant against inputed umlauts.
 *
 * @author Benno Luthiger */
public class CheckValueForUmlauts {
    private transient List<String> searchStrings;

    /** CheckValueForUmlauts constructor checking the specified search string.
     *
     * @param inSearchString java.lang.String */
    public CheckValueForUmlauts(String inSearchString) {
        super();

        inSearchString = inSearchString.toUpperCase(Locale.getDefault());
        final FindFirstUmlaut lFindUmlaut = new FindFirstUmlaut(inSearchString);
        if (lFindUmlaut.hasUmlauts()) {
            String[] lSearchStrings = { "" };
            lSearchStrings = processUmlauts(inSearchString, lSearchStrings);
            searchStrings = new ArrayList<String>(lSearchStrings.length);
            for (int i = 0; i < lSearchStrings.length; i++) {
                searchStrings.add(lSearchStrings[i]);
            }
        }
        else {
            searchStrings = new ArrayList<String>(1);
            searchStrings.add(inSearchString);
        }
    }

    /** Returns an Iterator of search strings containing all variation of umlauts.
     *
     * @return java.util.Iterator */
    public synchronized Iterator<String> elements() { // NOPMD by lbenno 
        return searchStrings.iterator();
    }

    /** Recursive function! Returns an array of Strings containing all variation of umlauts in the search string.
     *
     * @return java.lang.String[]
     * @param inRemaining java.lang.String remaining part of the SearchString to process
     * @param inSearchStrings java.lang.String[] actual variety of SearchStrings */
    private String[] processUmlauts(final String inRemaining, final String... inSearchStrings) {
        String[] outSearchStrings;

        final FindFirstUmlaut lFindUmlaut = new FindFirstUmlaut(inRemaining);
        if (lFindUmlaut.hasUmlauts()) {
            outSearchStrings = new String[inSearchStrings.length * 2];

            // fill first half of new array with AAA + NNN + Ä
            String lAppend = lFindUmlaut.getStarting() + lFindUmlaut.getFoundUmlaut();
            for (int i = 0; i < inSearchStrings.length; i++) {
                outSearchStrings[i] = inSearchStrings[i] + lAppend;
            }

            // fill second half of new array with AAA + NNN + AE
            lAppend = lFindUmlaut.getStarting() + lFindUmlaut.getFoundCorresponding();
            final int lOffset = inSearchStrings.length;
            for (int i = 0; i < inSearchStrings.length; i++) {
                outSearchStrings[i + lOffset] = inSearchStrings[i] + lAppend;
            }

            // make recursion
            outSearchStrings = processUmlauts(lFindUmlaut.getEnding(), outSearchStrings);
        }
        else {
            outSearchStrings = new String[inSearchStrings.length];
            for (int i = 0; i < inSearchStrings.length; i++) {
                outSearchStrings[i] = inSearchStrings[i] + inRemaining;
            }
        }

        return outSearchStrings;
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        return Debug.classMarkupString(this, "searchStrings='" + searchStrings + "'");
    }
}
