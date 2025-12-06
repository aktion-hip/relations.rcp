package org.hip.kernel.stext;

/*
 This package is part of the structured text framework used for the application VIF.
 Copyright (C) 2003, Benno Luthiger

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hip.kernel.sys.VObject;

/** This class converts inline formats according StructuredText rules to HTML code.
 *
 * @author: Benno Luthiger */
public class InlineStructuredText2HTML {
    private static InlineStructuredText2HTML singleton;

    private static Collection<StructureFactory> cFactories = new ArrayList<StructureFactory>();

    static {
        singleton = new InlineStructuredText2HTML();
        cFactories.add(singleton.new FactoryEmphasized());
        cFactories.add(singleton.new FactoryStrong());
        cFactories.add(singleton.new FactoryUnderlined());
        cFactories.add(singleton.new FactoryLiteral());
    }

    // --------- start inner classes -------------
    // --------- inner class StructureFactory

    /** Interface for StructuredText factory classes */
    public interface StructureFactory {
        /** Returns the Pattern of this factory.
         *
         * @return Pattern */
        Pattern getPattern();

        /** Returns the replacement of structured text formats with plain HTML.
         *
         * @param inMatcher Matcher
         * @return java.lang.String */
        String createHTMLReplacement(Matcher inMatcher);

        /** Returns the replacement of structured text formats with escaped HTML.
         *
         * @param inMatcher Matcher
         * @return java.lang.String */
        String createEscapedHTMLReplacement(Matcher inMatcher);

        /** Returns the replacement ignoring structured text formats.
         *
         * @param inMatcher Matcher
         * @return java.lang.String */
        String createIgnoringReplacement(Matcher inMatcher);
    }

    /** Generic funtionality for StructuredText factory classes */
    private abstract class AbstractStructureFactory extends VObject { // NOPMD by lbenno
        private static final String TAG_START_NORMAL = "<";
        private static final String TAG_END_NORMAL = ">";
        private static final String TAG_START_ESCAPED = "&lt;";
        private static final String TAG_END_ESCAPED = "&gt;";

        /** @return String */
        abstract protected String getTagContent();

        public String createHTMLReplacement(final Matcher inMatcher) { // NOPMD by lbenno
            final StringBuilder outReplacement = new StringBuilder();
            outReplacement.append(inMatcher.group(1)).append(TAG_START_NORMAL).append(getTagContent())
                    .append(TAG_END_NORMAL).append(inMatcher.group(2)).append(TAG_START_NORMAL).append('/')
                    .append(getTagContent()).append(TAG_END_NORMAL).append(inMatcher.group(3));
            return new String(outReplacement);
        }

        /** @param inMatcher {@link Matcher}
         * @return String */
        public String createEscapedHTMLReplacement(final Matcher inMatcher) {
            final StringBuilder outReplacement = new StringBuilder();
            outReplacement.append(inMatcher.group(1)).append(TAG_START_ESCAPED).append(getTagContent())
                    .append(TAG_END_ESCAPED).append(inMatcher.group(2)).append(TAG_START_ESCAPED).append('/')
                    .append(getTagContent()).append(TAG_END_ESCAPED).append(inMatcher.group(3));
            return new String(outReplacement);
        }

        /** @param inMatcher {@link Matcher}
         * @return String */
        public String createIgnoringReplacement(final Matcher inMatcher) {
            final StringBuffer outReplacement = new StringBuffer();
            outReplacement.append(inMatcher.group(1)).append(inMatcher.group(2)).append(inMatcher.group(3));
            return new String(outReplacement);
        }
    }

    /** Factory class for emphasized inline StructuredText format. */
    private class FactoryEmphasized extends AbstractStructureFactory implements StructureFactory { // NOPMD by lbenno
        private final static String TAG_CONTENT = "em";
        private final transient Pattern pattern = Pattern
                .compile("(^|[\\s\\(\\[\\{])\\*\\b([\\p{L}\\p{Digit}\\p{Punct}\\s]+?)\\*(\\s|\\p{Punct}|$)");

        @Override
        public Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }

        @Override
        protected String getTagContent() { // NOPMD by lbenno
            return TAG_CONTENT;
        }
    }

    /** Factory class for strong inline StructuredText format. */
    private class FactoryStrong extends AbstractStructureFactory implements StructureFactory { // NOPMD by lbenno
        private final static String TAG_CONTENT = "strong";
        private final Pattern pattern = Pattern
                .compile("(^|[\\s\\(\\[\\{])\\*\\*\\b([\\p{L}\\p{Digit}\\p{Punct}\\s&&[^\\*]]+?)\\*\\*(\\s|\\p{Punct}|$)");

        @Override
        public Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }

        @Override
        protected String getTagContent() { // NOPMD by lbenno
            return TAG_CONTENT;
        }
    }

    /** Factory class for underlined inline StructuredText format. */
    private class FactoryUnderlined extends AbstractStructureFactory implements StructureFactory { // NOPMD by lbenno
        private final static String TAG_CONTENT = "u";
        private final transient Pattern pattern = Pattern
                .compile("(^|[\\s\\(\\[\\{])_([\\p{L}\\p{Digit}\\p{Punct}\\s&&[^_<>]]+?)_(\\s|\\p{Punct}|$)");

        @Override
        public Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }

        @Override
        protected String getTagContent() { // NOPMD by lbenno
            return TAG_CONTENT;
        }
    }

    /** Factory class for literal inline StructuredText format. */
    private class FactoryLiteral extends AbstractStructureFactory implements StructureFactory { // NOPMD by lbenno
        private final static String TAG_CONTENT = "code";
        private final transient Pattern pattern = Pattern
                .compile("(\\W+|^)'([\\p{L}\\p{Digit}\\p{Punct}\\s]+?)'(\\s|\\p{Punct}|$)");

        @Override
        public Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }

        @Override
        protected String getTagContent() { // NOPMD by lbenno
            return TAG_CONTENT;
        }
    }

    // --------- inner class StructuredText converters

    /** Generic functionality for StructuredText converters */
    private abstract class AbstractConverter extends VObject { // NOPMD by lbenno

        /** @param inFactory {@link StructureFactory}
         * @param inMatcher {@link Matcher}
         * @return String */
        abstract protected String getReplacement(StructureFactory inFactory, Matcher inMatcher);

        /** @param inStructuredTextString String
         * @return {@link StringBuffer} */
        public StringBuffer convert(final String inStructuredTextString) {
            StringBuffer outHtml = new StringBuffer(inStructuredTextString);
            for (final StructureFactory lFactory : cFactories) {
                final Matcher lMatcher = lFactory.getPattern().matcher(outHtml);
                final StringBuffer lReplaced = new StringBuffer();
                boolean lFound = lMatcher.find();
                while (lFound) {
                    lMatcher.appendReplacement(lReplaced, getReplacement(lFactory, lMatcher));
                    lFound = lMatcher.find();
                }
                lMatcher.appendTail(lReplaced);
                outHtml = lReplaced;
            }
            return outHtml;
        }
    }

    /** Inner class to convert text inline formated to plain HTML code. */
    private class Converter2HTML extends AbstractConverter { // NOPMD by lbenno

        @Override
        protected String getReplacement(final StructureFactory inFactory, final Matcher inMatcher) { // NOPMD by lbenno
            return inFactory.createHTMLReplacement(inMatcher);
        }
    }

    /** Inner class to convert text inline formated to escaped HTML code. */
    private class Converter2EscapedHTML extends AbstractConverter {
        public Converter2EscapedHTML() { // NOPMD by lbenno
            super();
        }

        @Override
        protected String getReplacement(final StructureFactory inFactory, final Matcher inMatcher) { // NOPMD by lbenno
            return inFactory.createEscapedHTMLReplacement(inMatcher);
        }
    }

    /** Inner class to ignore the inline formates in the text. */
    private class ConverterIgnoring extends AbstractConverter { // NOPMD by lbenno

        @Override
        protected String getReplacement(final StructureFactory inFactory, final Matcher inMatcher) { // NOPMD by lbenno
            return inFactory.createIgnoringReplacement(inMatcher);
        }
    }

    // --------- end inner classes -------------

    /** Singleton constructor for InlineStructuredText2HTML. */
    private InlineStructuredText2HTML() {
        super();
    }

    /** Returns singleton instance of inline format converter.
     *
     * @return InlineStructuredText2HTML */
    public static synchronized InlineStructuredText2HTML getSingleton() { // NOPMD
        if (singleton == null) {
            singleton = new InlineStructuredText2HTML();
        }

        return singleton;
    }

    /** Returns the specified text with inline format converted to HTML.
     *
     * @param inStructuredTextString java.lang.String
     * @return java.lang.StringBuffer */
    public StringBuffer convertToHTML(final String inStructuredTextString) {
        return new Converter2HTML().convert(inStructuredTextString);
    }

    /** Returns the specified text with inline format converted to escaped HTML.
     *
     * @param inStructuredTextString java.lang.String
     * @return java.lang.StringBuffer */
    public StringBuffer convertToEscapedHTML(final String inStructuredTextString) {
        return new Converter2EscapedHTML().convert(inStructuredTextString);
    }

    /** Returns the specified text with inline format ignored.
     *
     * @param inStructuredTextString java.lang.String
     * @return java.lang.StringBuffer */
    public StringBuffer convertIgnoring(final String inStructuredTextString) {
        return new ConverterIgnoring().convert(inStructuredTextString);
    }
}
