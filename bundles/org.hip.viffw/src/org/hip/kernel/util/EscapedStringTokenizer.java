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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/** This class implements a string tokenizer like the </code>java.util.StringTokenizer</code>. Additionally it's able to
 * parse a string with escaped tokens. For example, you can define the delimiter and a release character which releases
 * the delimiter-character from its rule as delimiter.
 *
 * @author: Benno Luthiger */
public class EscapedStringTokenizer implements Enumeration<String> { // NOPMD by lbenno
    private transient int currentPosition;
    private transient final int maxPosition;
    private transient final char[] string;
    private transient final String delimiters;
    private transient final boolean retTokens;
    private transient char escapeCharacter;
    private transient boolean checkEscapedDelimiter;
    private transient boolean keepEscaped;

    /** Creates a new instance of EscapedStringTokenizer, with default delimiters (space, tab, newline).
     *
     * @param inString The string to be tokenized */
    public EscapedStringTokenizer(final String inString) {
        this(inString, " \t\n\r", false);
    }

    /** Creates a new instance of EscapedStringTokenizer.
     *
     * @param inString The string to be tokenized
     * @param inDelimiter A string containing all delimiters */
    public EscapedStringTokenizer(final String inString, final String inDelimiter) {
        this(inString, inDelimiter, false);
    }

    /** Creates a new instance of EscapedStringTokenizer.
     *
     * @param inString The string to be tokenized
     * @param inDelimiter A string containing all delimiters
     * @param inReturnTokens If true, return delimiters as tokens. Different in <code>StringTokenizer</code> */
    public EscapedStringTokenizer(final String inString, final String inDelimiter, final boolean inReturnTokens) {
        currentPosition = 0;
        string = new char[inString.length()];
        inString.getChars(0, inString.length(), string, 0);
        maxPosition = inString.length() - 1;
        delimiters = inDelimiter;
        retTokens = inReturnTokens;
    }

    /** Creates a new instance of EscapedStringTokenizer.
     *
     * @param inString The string to be tokenized
     * @param inDelimiter A string containing all delimiters
     * @param inReturnTokens If true, return delimiters as tokens. Different in <code>StringTokenizer</code>
     * @param inEscapeCharacter char */
    public EscapedStringTokenizer(final String inString, final String inDelimiter, final boolean inReturnTokens,
            final char inEscapeCharacter) {
        this(inString, inDelimiter, inReturnTokens);
        escapeCharacter = inEscapeCharacter;
        checkEscapedDelimiter = true;
    }

    /** Creates a new instance of EscapedStringTokenizer.
     *
     * @param inString The string to be tokenized
     * @param inDelimiter A string containing all delimiters
     * @param inReturnTokens If true, return delimiters as tokens. Diffrent in <code>StringTokenizer</code>
     * @param inEscapeCharacter char
     * @param inKeepEscapedCharacter If true, escaped characters will be keept escaped. */
    public EscapedStringTokenizer(final String inString, final String inDelimiter, final boolean inReturnTokens,
            final char inEscapeCharacter, final boolean inKeepEscapedCharacter) {
        this(inString, inDelimiter, inReturnTokens);
        escapeCharacter = inEscapeCharacter;
        checkEscapedDelimiter = true;
        keepEscaped = inKeepEscapedCharacter;
    }

    /** Returns true if more elements/tokens are in the string. */
    @Override
    public boolean hasMoreElements() {

        if (currentPosition > maxPosition) {
            return false;
        }
        if (!isDelimiter(currentPosition) || retTokens) {
            return true;
        }

        int lTempPos = currentPosition;
        while (lTempPos <= maxPosition && isDelimiter(lTempPos)) {
            lTempPos++;
        }
        return lTempPos <= maxPosition;
    }

    /** Returns true if character at specified position (inPos) is a delimiter.
     *
     * @return boolean
     * @param inPos int */
    private boolean isDelimiter(final int inPos) {

        if (delimiters.indexOf(string[inPos]) == -1) {
            return false;
        }

        if (inPos == 0 || !checkEscapedDelimiter) {
            return true;
        }

        int lTempPos = currentPosition - 1;
        while (lTempPos >= 0 && string[lTempPos] == escapeCharacter) {
            lTempPos--;
        }

        return (currentPosition - lTempPos + 1) % 2 == 0;
    }

    @Override
    public String nextElement() { // NOPMD by lbenno

        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }

        final StringBuilder outElement = new StringBuilder(100);

        // if delimiters are returned as tokens and character at current position is a delimiter
        if (isDelimiter(currentPosition) && retTokens) {
            currentPosition++;
            return outElement.append(string[currentPosition - 1]).toString();
        }

        // skip the delimiters
        while (currentPosition <= maxPosition && isDelimiter(currentPosition)) {
            currentPosition++;
        }

        boolean lLastWasEscapeChar = false;
        while (currentPosition <= maxPosition && !isDelimiter(currentPosition)) {
            final char lChar = string[currentPosition];
            if (keepEscaped) {
                outElement.append(lChar);
            }
            else {
                if (lChar == escapeCharacter) {
                    if (lLastWasEscapeChar) {
                        lLastWasEscapeChar = false;
                        outElement.append(lChar);
                    }
                    else {
                        lLastWasEscapeChar = true;
                    }
                }
                else {
                    lLastWasEscapeChar = false;
                    outElement.append(lChar);
                }
            }
            currentPosition++;
        }

        return outElement.toString();
    }

    /** @return java.lang.String */
    @Override
    public String toString() {
        String lMessage = "toTokenize='";
        for (int i = 0; i < string.length; i++) {
            lMessage += string[i]; // NOPMD by lbenno
        }
        lMessage += "', delimiters='" + delimiters + "'"; // NOPMD by lbenno
        return Debug.classMarkupString(this, lMessage);
    }
}
