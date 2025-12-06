/**
	This package is part of the structured text framework used for the application VIF.
	Copyright (C) 2003-2015, Benno Luthiger

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
package org.hip.kernel.stext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hip.kernel.sys.VObject;

/** This class is responsible to create structured text.
 *
 * @author: Benno Luthiger */
public class StructuredText extends VObject {
    // class variables
    private static StructuredText cSingleton;
    private static Collection<StructuredParagraphFactory> cFactories = new ArrayList<StructuredParagraphFactory>(3);

    // instance variables
    private final transient Collection<StructuredTextParagraph> paragraphs;

    static {
        cSingleton = new StructuredText(new String[] {});
        cFactories.add(cSingleton.new StructuredTextBulletFactory());
        cFactories.add(cSingleton.new StructuredTextNumberedFactory());
        cFactories.add(cSingleton.new StructuredTextIndentedFactory());
    }

    /** Constructor for StructuredText. Creates an instance of StructuredText from the specified paragraphs.
     *
     * @param inRawParagraphs java.lang.String[] */
    public StructuredText(final String... inRawParagraphs) {
        super();
        paragraphs = initialize(inRawParagraphs);
    }

    private Collection<StructuredTextParagraph> initialize(final String... inRawParagraphs) {
        final Collection<StructuredTextParagraph> outParagraphs = new ArrayList<StructuredTextParagraph>();
        StructuredTextParagraph lActualParagraph = null;

        for (int i = 0; i < inRawParagraphs.length; i++) {
            boolean lMatched = false;
            for (final StructuredParagraphFactory lFactory : cFactories) {
                if (lFactory.matches(inRawParagraphs[i])) {
                    final StructuredTextParagraph lParagraph = lFactory.createStructuredParagraph(inRawParagraphs[i]
                            .substring(lFactory.getSubstringStart()));

                    // if paragraph is indented, add indented to the actual paragraph
                    if (lParagraph.getParagraphType() == StructuredTextParagraph.PARAGRAPH_INDENTED) {
                        if (lActualParagraph != null) {
                            lActualParagraph.addIndented(lParagraph.getRawString());
                            lMatched = true;
                            break;
                        }
                    }

                    // if paragraph is of equal type, add to the actual paragraph
                    if (lParagraph.equalsType(lActualParagraph)) {
                        lActualParagraph.add(lParagraph.getRawString());
                        lMatched = true;
                        break;
                    }

                    // else add to the top level of paragraphs and set new paragraph as actual paragraph
                    outParagraphs.add(lParagraph);
                    lActualParagraph = lParagraph;
                    lMatched = true;
                    break;
                }
            }

            // if paragraph didn't match, it is of plain type
            if (!lMatched) {
                final StructuredTextParagraph lParagraph = new StructuredTextPlain(inRawParagraphs[i]);
                outParagraphs.add(lParagraph);
                lActualParagraph = lParagraph;
            }
        }
        return outParagraphs;
    }

    /** Implementation of Visitor Pattern, e.g. to create an HTML string out of this text formatted with structured text
     * rules.
     *
     * @param inSerializer StructuredTextSerializer */
    public void accept(final StructuredTextSerializer inSerializer) {
        inSerializer.visitStructuredText(this);
    }

    /** Returns an Iterator over this StructuredText's paragraphs.
     *
     * @return java.util.Iterator */
    public Iterator<StructuredTextParagraph> getStructuredTextParagraphs() {
        return paragraphs.iterator();
    }

    // ---

    /** Interface for structured text factories. */
    private interface StructuredParagraphFactory {
        /** @param inRawParagraph String
         * @return StructuredTextParagraph */
        StructuredTextParagraph createStructuredParagraph(String inRawParagraph);

        /** @param inRawParagraph String
         * @return boolean */
        boolean matches(String inRawParagraph);

        /** @return int */
        int getSubstringStart();
    }

    /** Base class for the factories. */
    private abstract class AbstractStructuredParagraphFactory implements // NOPMD
            StructuredParagraphFactory {
        private transient int startSubstring;

        /** @return {@link Pattern} */
        protected abstract Pattern getPattern();

        @Override
        public boolean matches(final String inRawParagraph) { // NOPMD by lbenno
            final Matcher lMatcher = getPattern().matcher(inRawParagraph);
            final boolean outFind = lMatcher.find();
            if (outFind) {
                startSubstring = lMatcher.end();
            }
            return outFind;
        }

        @Override
        public int getSubstringStart() { // NOPMD by lbenno
            return startSubstring;
        }
    }

    /** Bullet paragraph. */
    private class StructuredTextBulletFactory extends AbstractStructuredParagraphFactory implements // NOPMD
    StructuredParagraphFactory {
        private final Pattern pattern = Pattern.compile("^\\s*[-*o]\\s+");

        @Override
        public StructuredTextParagraph createStructuredParagraph(final String inRawParagraph) { // NOPMD by lbenno
            return new StructuredTextBullet(inRawParagraph);
        }

        @Override
        protected Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }
    }

    /** Numbered paragraph. */
    private class StructuredTextNumberedFactory extends AbstractStructuredParagraphFactory implements // NOPMD
    StructuredParagraphFactory {
        private final Pattern pattern = Pattern.compile("(^\\s*[\\p{Alnum}]\\.)|(^\\s*[0-9]+\\.)|(^\\s*[0-9]+\\s+)");

        @Override
        public StructuredTextParagraph createStructuredParagraph(final String inRawParagraph) { // NOPMD by lbenno
            return new StructuredTextNumbered(inRawParagraph);
        }

        @Override
        protected Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }
    }

    /** Indented paragraph. */
    private class StructuredTextIndentedFactory extends AbstractStructuredParagraphFactory implements // NOPMD
    StructuredParagraphFactory {
        private final Pattern pattern = Pattern.compile("^\\s+");

        @Override
        public StructuredTextParagraph createStructuredParagraph(final String inRawParagraph) { // NOPMD by lbenno
            return new StructuredTextIndented(inRawParagraph);
        }

        @Override
        protected Pattern getPattern() { // NOPMD by lbenno
            return pattern;
        }
    }
}
