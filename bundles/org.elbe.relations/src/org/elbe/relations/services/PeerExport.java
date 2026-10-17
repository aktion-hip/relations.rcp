/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2026, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.services;

import java.nio.file.Path;

/** The prepared export a peer session offers: the same zipped XML export produced for cloud
 * export.
 *
 * @author lbenno */
public class PeerExport {

    /** The scope of the export. */
    public enum Scope {
        /** The complete data set. */
        FULL,
        /** Only the changes recorded in the change log since the previous export. */
        INCREMENTAL
    }

    private final Scope scope;
    private final Path file;
    private final String name;

    /** PeerExport constructor.
     *
     * @param scope {@link Scope}
     * @param file {@link Path} the file containing the zipped XML export
     * @param name String the file name suggested to the receiver, e.g.
     *            <code>relations_all.zip</code> */
    public PeerExport(final Scope scope, final Path file, final String name) {
        this.scope = scope;
        this.file = file;
        this.name = name;
    }

    public Scope getScope() {
        return this.scope;
    }

    public Path getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

}
