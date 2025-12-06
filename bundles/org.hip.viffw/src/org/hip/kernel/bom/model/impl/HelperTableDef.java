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
package org.hip.kernel.bom.model.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hip.kernel.bom.model.MappingDef;
import org.hip.kernel.bom.model.ObjectDef;
import org.hip.kernel.sys.Assert;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.util.Debug;

/** This helper class provides a special view on the mapping definitions. It is more like an index then a real
 * definition. It creates a list of tableNames and associated mappingDefs out of an objectDef.
 *
 * @author Benno Luthiger */
@SuppressWarnings("serial")
public final class HelperTableDef implements Serializable {

    // Instance variables
    private final String tableName;
    private Map<String, MappingDef> mappings;

    /** HelperTableDef singleton constructor. */
    private HelperTableDef(final String inTableName) {
        super();
        tableName = inTableName;
    }

    /** This method adds the specified MappingDef to the collection of MappingDefs
     *
     * @param inMappingDef org.hip.kernel.bom.model.MappingDef */
    public void add(final MappingDef inMappingDef) {
        // Pre: mappingDef not null
        if (VSys.assertNotNull(this, "add", inMappingDef) == Assert.FAILURE) {
            return;
        }
        mappingDefs().put(inMappingDef.getColumnName(), inMappingDef);
    }

    /** This method creates a new list HelperTableDefs based on the specified ObjectDef. Each HelperTableDef in the
     * collection can return the tableName and the associated mappingDefs.
     *
     * @return {@link Map}&lt;String, HelperTableDef>
     * @param inObjectDef org.hip.kernel.bom.model.ObjectDef */
    public synchronized static Map<String, HelperTableDef> createTableDefs(final ObjectDef inObjectDef) { // NOPMD by lbenno 
        final Map<String, HelperTableDef> outTable = new ConcurrentHashMap<String, HelperTableDef>(7); // NOPMD

        // Pre: inObjectDef not null
        if (VSys.assertNotNull(HelperTableDef.class, "createTableDef", inObjectDef) == Assert.FAILURE) {
            return outTable;
        }

        // Get mapping defs out of the specified objectDef
        for (final MappingDef lMappingDef : inObjectDef.getMappingDefs2()) {
            final String lTableName = lMappingDef.getTableName();
            HelperTableDef lTableDef = outTable.get(lTableName);
            if (lTableDef == null) {
                lTableDef = new HelperTableDef(lTableName);
                outTable.put(lTableName, lTableDef);
            }
            lTableDef.add(lMappingDef);
        }

        return outTable;
    }

    /** Returns Collection view of all <code>MappingDef</code>s contained in this <code>HelperTableDef</code> instance.
     *
     * @return Collection<MappingDef> */
    public Collection<MappingDef> getMappingDefs2() {
        return mappingDefs().values();
    }

    /** Returns the table name.
     *
     * @return java.lang.String */
    public String getTableName() {
        return tableName;
    }

    /** @return java.util.Hashtable<String, MappingDef> */
    private Map<String, MappingDef> mappingDefs() {
        if (mappings == null) {
            mappings = new HashMap<String, MappingDef>(53);
        }
        return mappings;
    }

    @Override
    public String toString() { // NOPMD by lbenno
        return Debug.classMarkupString(this, "tableName=\"" + (tableName == null ? "null" : tableName) + "\"");
    }
}
