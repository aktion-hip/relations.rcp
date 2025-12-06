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

package org.hip.kernel.bom.impl;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

import org.hip.kernel.bom.AbstractSerializer;
import org.hip.kernel.bom.DomainObjectIterator;
import org.hip.kernel.bom.DomainObjectVisitor;
import org.hip.kernel.bom.GeneralDomainObject;
import org.hip.kernel.bom.Property;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.SortedArray;
import org.hip.kernel.sys.VSys;

/** This class is a Serializer for XML objects. Includes a filter for XML data.
 *
 * @author Benno Luthiger
 * @see java.io.Serializable */
@SuppressWarnings("serial")
public class XMLSerializer extends AbstractSerializer implements Serializable {

    // Instance variables
    private final DecimalFormat decimalFormat = new DecimalFormat();
    private transient XMLCharacterFilter xmlCharFilter;
    protected Locale locale = VSys.dftLocale;

    /** XMLSerializer default constructor. */
    public XMLSerializer() {
        super();
    }

    /** XMLSerializer constructor for inputed inLevel.
     *
     * @param inLevel int */
    public XMLSerializer(final int inLevel) {
        super(inLevel);
    }

    /** XMLSerializer for inputed inLevel filtering the characters in inFilter.
     *
     * @param inLevel int
     * @param inFilter org.hip.kernel.bom.impl.XMLCharacterFilter */
    public XMLSerializer(final int inLevel, final XMLCharacterFilter inFilter) {
        super(inLevel);
        xmlCharFilter = inFilter;
    }

    /** XMLSerializer filtering the characters in inFilter.
     *
     * @param inFilter org.hip.kernel.bom.impl.XMLCharacterFilter */
    public XMLSerializer(final XMLCharacterFilter inFilter) {
        super();
        xmlCharFilter = inFilter;
    }

    /** Setter for filter to be used for serializing.
     *
     * @param inFilter XMLCharacterFilter or <code>null</code>. */
    public void setFilter(final XMLCharacterFilter inFilter) {
        xmlCharFilter = inFilter;
    }

    /** Emits a comment line.
     *
     * @param inComment java.lang.String */
    @Override
    protected void emitComment(final String inComment) {
        emit_nl();
        synchronized (this) {
            emit("<!--" + inComment + "-->");
        }
    }

    /** Emits an end tag.
     *
     * @param inContent java.lang.String */
    @Override
    protected synchronized void emitEndTag(final String inContent) { // NOPMD by lbenno
        getBuffer2().append("</" + inContent + ">");
    }

    /** Emits a start tag.
     *
     * @param inContent java.lang.String */
    @Override
    protected void emitStartTag(final String inContent) {
        synchronized (this) {
            emit("<" + inContent + ">");
        }
    }

    /** Filters text if a XMLCharacterFilter is set and appends text.
     *
     * @param inObject java.lang.String */
    @Override
    protected void emitText(final Object inObject) {
        if (inObject == null) {
            synchronized (this) {
                getBuffer2().append(getNullString());
            }
        } else {
            synchronized (this) {
                if (xmlCharFilter == null) {
                    getBuffer2().append(inObject);
                } else {
                    getBuffer2().append(xmlCharFilter.filter(inObject.toString()));
                }
            }
        }
    }

    /** Emits the sequence ending the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void endDomainObject(final GeneralDomainObject inObject) {
        emit_nl();
        emit_indent();
        emitEndTag(inObject.getObjectName());
    }

    /** Emits the sequence ending the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void endIterator(final DomainObjectIterator inIterator) {
        emit_nl();
        emit_indent();
        emitEndTag("ObjectList");
    }

    /** Sequence ending the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void endProperty(final Property inProperty) {
        emitEndTag(inProperty.getName());
    }

    /** Sequence ending the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void endPropertySet(final PropertySet inSet) {
        emit_nl();
        emit_indent();
        emitEndTag(DomainObjectVisitor.PROPERTY_SET_TAG);
    }

    /** Emits the sequence ending the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void endSortedArray(final SortedArray inSortedArray) {
        emit_nl();
        emit_indent();
        emitEndTag("ObjectList");
    }

    @Override
    public void start() { // NOPMD by lbenno
    }

    /** Sequence starting the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    protected void startDomainObject(final GeneralDomainObject inObject) {
        emit_nl();
        emitStartTag(inObject.getObjectName());
    }

    /** Sequence starting the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    protected void startIterator(final DomainObjectIterator inIterator) {
        emit_nl();
        emitStartTag("ObjectList");
    }

    /** Sequence starting the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    protected void startProperty(final Property inProperty) {
        emit_nl();
        emitStartTag(inProperty.getName());
        final Object lValue = inProperty.getValue();

        if (lValue == null) {
            return;
        }

        final String lFormatPattern = inProperty.getFormatPattern();
        if ("none".equals(lFormatPattern)) {
            emitText(inProperty.getValue());
            return;
        }

        if (lValue instanceof Timestamp) {
            final DateFormat lDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            final Calendar lCalendar = lDateFormat.getCalendar();
            lCalendar.setTime((Timestamp) lValue);
            emitText(lDateFormat.format(lCalendar.getTime()));
            return;
        }

        if (lValue instanceof Date) {
            final DateFormat lDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            final Calendar lCalendar = lDateFormat.getCalendar();
            lCalendar.setTime((Date) lValue);
            emitText(lDateFormat.format(lCalendar.getTime()));
            return;
        }

        if (lValue instanceof Time) {
            final DateFormat lTimeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
            final Calendar lCalendar = lTimeFormat.getCalendar();
            lCalendar.setTime((Time) lValue);
            emitText(lTimeFormat.format(lCalendar.getTime()));
            return;
        }

        if (lValue instanceof Number) {
            if (lFormatPattern == null) {
                emitText(NumberFormat.getNumberInstance().format(lValue));
                return;
            } else {
                if (" ".equals(lFormatPattern) && ((Number) lValue).intValue() == 0) { // NOPMD by lbenno
                    return;
                }

                decimalFormat.applyPattern(lFormatPattern);
                emitText(decimalFormat.format(lValue));
                return;
            }
        }

        emitText(emitPropertyValue(inProperty));
    }

    /** Subclasses can overwrite this method if they want the property value emitted in a special way.
     *
     * @param inProperty org.hip.kernel.bom.Property
     * @return java.lang.Object */
    protected Object emitPropertyValue(final Property inProperty) {
        return inProperty.getValue();
    }

    /** Sequence starting the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    protected void startPropertySet(final PropertySet inSet) {
        emit_nl();
        emitStartTag(DomainObjectVisitor.PROPERTY_SET_TAG);
    }

    /** Sequence starting the visit of a SortedArray
     *
     * @param inSortedArray org.hip.kernel.bom.SortedArray */
    @Override
    protected void startSortedArray(final SortedArray inSortedArray) {
        emit_nl();
        emitStartTag("SortedArray");
    }

    /** Set the Locale the date values can be formatted with.
     *
     * @param inLocale java.util.Locale */
    public void setLocale(final Locale inLocale) {
        if (inLocale != null) {
            locale = inLocale;
        }
    }

    /** @return {@link Locale} */
    public Locale getLocale() {
        return locale;
    }

}