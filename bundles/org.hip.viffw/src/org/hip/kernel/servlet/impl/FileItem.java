/**
	This package is part of the application VIF.
	Copyright (C) 2010-2015, Benno Luthiger

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
package org.hip.kernel.servlet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.fileupload2.core.DiskFileItem;

/** Wrapper class for the file upload's <code>FileItem</code> object.
 *
 * @author Luthiger Created: 22.09.2010
 * @see org.apache.commons.fileupload2.core.FileItem */
public class FileItem {
    private final transient org.apache.commons.fileupload2.core.FileItem<DiskFileItem> item;

    /** FileItem constructor.
     *
     * @param inItem {@link org.apache.commons.fileupload.FileItem} */
    public FileItem(final org.apache.commons.fileupload2.core.FileItem<DiskFileItem> inItem) {
        this.item = inItem;
    }

    public InputStream getInputStream() throws IOException { // NOPMD
        return this.item.getInputStream();
    }

    public String getContentType() { // NOPMD
        return this.item.getContentType();
    }

    public String getName() { // NOPMD
        return this.item.getName();
    }

    public boolean isInMemory() { // NOPMD
        return this.item.isInMemory();
    }

    public long getSize() { // NOPMD
        return this.item.getSize();
    }

    public byte[] get() { // NOPMD
        return this.item.get();
    }

    public String getFieldName() { // NOPMD
        return this.item.getFieldName();
    }

    public void delete() throws IOException { // NOPMD
        this.item.delete();
    }

    public void write(final Path file) throws IOException { // NOPMD
        this.item.write(file);
    }

}
