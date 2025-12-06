/**
	This package is part of the persistency layer of the application VIF.
	Copyright (C) 2001-2014, Benno Luthiger

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
package org.hip.kernel.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** An abstract adapter super class for classes that adapt to a Vector.
 *
 * @author Benno Luthiger
 * @see java.util.List
 * @see java.util.Vector */
public abstract class VectorAdapter implements List<Object> { // NOPMD by lbenno
    private transient final List<Object> collection;

    /** Constructor for VectorAdapter. */
    protected VectorAdapter() {
        super();
        collection = new ArrayList<Object>();
    }

    /** Add method with reduced visibility. Only subclasses can access this method and, therefore, add elements to the
     * collection.
     *
     * @param inObject java.lang.Object The element to add. */
    protected boolean addElement(final Object inObject) {
        return collection.add(inObject);
    }

    /** @see Collection#add(Object) */
    @Override
    public boolean add(final Object inObject) {
        return false;
    }

    /** Returns the number of elements in this collection.
     *
     * @return int */
    @Override
    public int size() {
        return collection.size();
    }

    /** @see Collection#isEmpty() */
    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    /** @see Collection#contains(Object) */
    @Override
    public boolean contains(final Object arg0) {
        return collection.contains(arg0);
    }

    /** @see Collection#iterator() */
    @Override
    public Iterator<Object> iterator() {
        return collection.iterator();
    }

    /** @see Collection#toArray() */
    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    /** @see Collection#toArray(Object[]) */
    @Override
    @SuppressWarnings("hiding")
    public <Object> Object[] toArray(final Object[] arg0) { // NOPMD by lbenno 
        return collection.toArray(arg0);
    }

    /** @see Collection#remove(Object) */
    @Override
    public boolean remove(final Object arg0) {
        return collection.remove(arg0);
    }

    /** @see Collection#containsAll(Collection) */
    @Override
    public boolean containsAll(final Collection<?> arg0) {
        return collection.containsAll(arg0);
    }

    /** @see Collection#addAll(Collection) */
    @Override
    public boolean addAll(final Collection<?> arg0) {
        return false;
    }

    /** @see List#addAll(int, Collection) */
    @Override
    public boolean addAll(final int arg0, final Collection<?> arg1) {
        return false;
    }

    /** @see Collection#removeAll(Collection) */
    @Override
    public boolean removeAll(final Collection<?> arg0) {
        return collection.removeAll(arg0);
    }

    /** @see Collection#retainAll(Collection) */
    @Override
    public boolean retainAll(final Collection<?> arg0) {
        return collection.retainAll(arg0);
    }

    /** @see Collection#clear() */
    @Override
    public void clear() {
        collection.clear();
    }

    /** @see List#get(int) */
    @Override
    public Object get(final int arg0) {
        return collection.get(arg0);
    }

    /** @see List#set(int, Object) */
    @Override
    public Object set(final int arg0, final Object arg1) { // NOPMD by lbenno
        return null;
    }

    /** @see List#add(int, Object) */
    @Override
    public void add(final int arg0, final Object arg1) { // NOPMD by lbenno
    }

    /** @see List#remove(int) */
    @Override
    public Object remove(final int arg0) {
        return collection.remove(arg0);
    }

    /** @see List#indexOf(Object) */
    @Override
    public int indexOf(final Object arg0) {
        return collection.indexOf(arg0);
    }

    /** @see List#lastIndexOf(Object) */
    @Override
    public int lastIndexOf(final Object arg0) {
        return collection.lastIndexOf(arg0);
    }

    /** @see List#listIterator() */
    @Override
    public ListIterator<Object> listIterator() {
        return collection.listIterator();
    }

    /** @see List#listIterator(int) */
    @Override
    public ListIterator<Object> listIterator(final int arg0) {
        return collection.listIterator(arg0);
    }

    /** @see List#subList(int, int) */
    @Override
    public List<Object> subList(final int arg0, final int arg1) {
        return collection.subList(arg0, arg1);
    }
}
