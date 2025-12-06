/**
 This package is part of the framework used for the application VIF.
 Copyright (C) 2001, Benno Luthiger

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
package org.hip.kernel.bom;

import java.io.Serializable;

/** An abstract serializer offering basic functionality for concrete serializer classes.
 *
 * @author Benno Luthiger
 * @see java.io.Serializable */
@SuppressWarnings("serial")
abstract public class AbstractSerializer extends AbstractDomainObjectVisitor implements Serializable { // NOPMD
    private final static int TAB = 4;

    // Instance variables
    private StringBuilder buffer = new StringBuilder(1024); // NOPMD by lbenno
    private String nl = null; // NOPMD by lbenno
    private String indent = "                                                "; // NOPMD
    private String nullString = "";

    /** AbstractSerializer default constructor. */
    public AbstractSerializer() {
        super();
    }

    /** AbstractSerializer initializes inputed level.
     *
     * @param inLevel int */
    public AbstractSerializer(final int inLevel) {
        super(inLevel);
    }

    /** Clears the buffer. */
    public void clear() {
        buffer = new StringBuilder(1024);
    }

    /** Emits a string.
     *
     * @param inWhat java.lang.String */
    protected final void emit(final String inWhat) {
        synchronized (buffer) {
            buffer.append(indent()).append(inWhat);
        }
    }

    /** Emits line indentation. */
    protected final synchronized void emit_indent() { // NOPMD by lbenno
        buffer.append(indent());
    }

    /** Emits a new line.
     *
     * @param java.lang.String */
    protected final synchronized void emit_nl() { // NOPMD by lbenno
        buffer.append(nl());
    }

    /** Emits a comment line.
     *
     * @param inComment java.lang.String */
    protected void emitComment(final String inComment) {
        synchronized (this) {
            emit_nl();
            emit("<!--" + inComment + "-->");
        }
    }

    /** Emits an end tag.
     *
     * @param inContent java.lang.String */
    protected void emitEndTag(final String inContent) {
        synchronized (this) {
            buffer.append("</" + inContent + ">");
        }
    }

    /** Emits a start tag.
     *
     * @param inContent java.lang.String */
    protected void emitStartTag(final String inContent) {
        synchronized (buffer) {
            emit("<" + inContent + ">");
        }
    }

    /** Emits an object.
     *
     * @param inObject java.lang.Object */
    protected void emitText(final Object inObject) {
        synchronized (buffer) {
            buffer.append((inObject == null) ? this.getNullString() : inObject);
        }
    }

    /** Sequence ending the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    abstract protected void endDomainObject(GeneralDomainObject inObject);

    /** Sequence ending the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */
    @Override
    abstract protected void endIterator(org.hip.kernel.bom.DomainObjectIterator inIterator);

    /** Sequence ending the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */
    @Override
    abstract protected void endProperty(org.hip.kernel.bom.Property inProperty);

    /** Sequence ending the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    abstract protected void endPropertySet(org.hip.kernel.bom.PropertySet inSet);

    /** Returns the buffer.
     *
     * @return java.lang.StringBuffer
     * @deprecated Use getBuffer2() instead */
    @Deprecated
    protected StringBuffer getBuffer() {
        return new StringBuffer(getBuffer2().toString());
    }

    /** Returns the buffer.
     *
     * @return {@link StringBuilder} */
    protected StringBuilder getBuffer2() {
        if (buffer == null) {
            buffer = new StringBuilder(1024);
        }
        return buffer;
    }

    /** @return java.lang.String */
    public final String getNullString() {
        return nullString;
    }

    /** Returns line indentation.
     *
     * @return java.lang.String */
    private String indent() {
        final int indentLen = getLevel() * TAB;
        if (indent.length() <= indentLen) {
            indent += indent;
        }

        return indent.substring(0, indentLen);
    }

    /** Returns a new line.
     *
     * @return java.lang.String */
    protected String nl() { // NOPMD
        if (nl == null) {
            nl = System.getProperty("line.separator");
        }
        return nl;
    }

    /** @param inNullString java.lang.String */
    public final void setNullString(final String inNullString) {
        nullString = (inNullString == null) ? nullString : inNullString;
    }

    /** Starts serialization. Hook for sublasses */
    @Override
    public void start() { // NOPMD
        // intentionally left empty
    }

    /** Sequence starting the visit of a DomainObject
     *
     * @param inObject org.hip.kernel.bom.GeneralDomainObject */
    @Override
    abstract protected void startDomainObject(GeneralDomainObject inObject);

    /** Sequence starting the visit of a DomainObjectIterator
     *
     * @param inIterator org.hip.kernel.bom.DomainObjectIterator */

    @Override
    abstract protected void startIterator(org.hip.kernel.bom.DomainObjectIterator inIterator);

    /** Sequence starting the visit of a Property
     *
     * @param inProperty org.hip.kernel.bom.Property */

    @Override
    abstract protected void startProperty(org.hip.kernel.bom.Property inProperty);

    /** Sequence starting the visit of a PropertySet
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    abstract protected void startPropertySet(org.hip.kernel.bom.PropertySet inSet);

    /** @return java.lang.String */
    @Override
    public String toString() {
        return new String(buffer);
    }
}
