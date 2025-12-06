/**
	This package is part of the framework used for the application VIF.
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

import java.util.StringTokenizer;

import org.hip.kernel.bom.KeyCriterion;
import org.hip.kernel.bom.KeyObject;

/** This class offers various static methods with debugging functionality
 *
 * @author: Benno Luthiger */
public final class Debug {

    private Debug() {
        // prevent instantiation
    }

    /** @return java.lang.String <DomainObjectName Key=inKey /> */
    public static String classBOMMarkupString(final String inBOMName, final NameValueList inKey) {
        final StringBuilder lKeys = new StringBuilder(100).append('<');
        lKeys.append(inBOMName).append(">\n\t<Keys>\n");

        for (final NameValue lNameValue : inKey.getNameValues2()) {
            lKeys.append("\t\t<Key ").append(lNameValue.getName()).append('=');
            lKeys.append((lNameValue.getValue() == null ? "null" : lNameValue.getValue().toString())).append(" />\n");
        }
        lKeys.append("\t</Keys>\n</").append(inBOMName).append('>');

        return new String(lKeys);
    }

    /** @return java.lang.String <DomainObjectName Key=inKey /> */
    public static String classBOMMarkupString(final String inBOMName, final KeyObject inKey) {
        final StringBuilder lKeys = new StringBuilder(100).append('<');
        lKeys.append(inBOMName).append(">\n\t<Keys>\n");

        for (final SortableItem lCriterion : inKey.getItems2()) {
            lKeys.append("\t\t<Key ").append(((KeyCriterion) lCriterion).getName()).append('=');
            lKeys.append(
                    (((KeyCriterion) lCriterion).getValue() == null ? "null" : ((KeyCriterion) lCriterion).getValue()
                            .toString())).append(" />\n");
        }
        lKeys.append("\t</Keys>\n</").append(inBOMName).append('>');

        return new String(lKeys);
    }

    /** @return java.lang.String <Classname inMessage /> */
    public static String classMarkupString(final Object inObject, final String inMessage) {
        return "< " + inObject.getClass().getName() + " " + inMessage + " />";
    }

    /** @return java.lang.String <Classname inAttributes> <inMarkups> </Classname> */
    public static String classMarkupString(final Object inObject, final String inAttributes, final String inMarkups) {
        final String lName = inObject.getClass().getName();
        final StringBuilder outMarkup = new StringBuilder(100).append('<').append(lName).append(' ')
                .append(inAttributes).append(">\n"); // NOPMD by lbenno 
        final StringTokenizer lTokens = new StringTokenizer(inMarkups, "\n");
        while (lTokens.hasMoreTokens()) {
            outMarkup.append('\t').append(lTokens.nextToken()).append('\n');
        }
        outMarkup.append("</").append(lName).append('>');
        return new String(outMarkup);
    }

    /** @return java.lang.String <MetaModelObjectName attName attType/> */
    public static String classMetaModelMarkupString(final Object inObject, final Object[][] inDef) { // NOPMD by lbenno
        final StringBuilder lDefs = new StringBuilder(100);
        for (int i = 0; i < inDef.length; i++) {
            lDefs.append("\t<Attribute name=\"").append(inDef[i][0].toString()).append("\" type=\"")
                    .append(inDef[i][1].toString()).append("\"/>\n");
        }

        return "<" + inObject.getClass().getName() + ">\n" + new String(lDefs) + "</" + inObject.getClass().getName()
                + ">";
    }

    /** @return java.lang.String <MetaModelObjectName attName attType/> */
    public static String classMetaModelMarkupString(final Object inObject, final String[][] inDef) { // NOPMD by lbenno
        final StringBuilder lDefs = new StringBuilder(100);
        for (int i = 0; i <= inDef.length; i++) {
            lDefs.append("\t<Attribute name=\"").append(inDef[i][0]).append("\" type=\"").append(inDef[i][1])
            .append("\"/>\n");
        }

        return "<" + inObject.getClass().getName() + ">\n" + new String(lDefs) + "</" + inObject.getClass().getName()
                + ">";
    }

    /** @return java.lang.String <Classname>inMessage</Classname> */
    public static String classMultilineMarkupString(final Object inObject, final String inMessage) {
        final String lObjectName = inObject.getClass().getName();
        return "<" + lObjectName + ">\n" + inMessage + "</" + lObjectName + ">";
    }

    /** @return java.lang.String Classname [inMessage] */
    public static String classTaggedString(final Object inObject, final String inMessage) {
        return inObject.getClass().getName() + " [" + inMessage + "]";
    }
}
