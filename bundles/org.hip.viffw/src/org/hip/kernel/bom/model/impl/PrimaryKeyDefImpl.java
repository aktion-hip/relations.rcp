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

import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.KeyDefDef;
import org.hip.kernel.bom.model.PrimaryKeyDef;
import org.hip.kernel.exc.DefaultExceptionHandler;

/** Implements the PrimaryKeyDef interface
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.PrimaryKeyDef */
@SuppressWarnings("serial")
public class PrimaryKeyDefImpl extends KeyDefImpl implements PrimaryKeyDef {
    /** PrimaryKeyDefImpl default constructor. */
    public PrimaryKeyDefImpl() {
        super();
    }

    /** PrimaryKeyDef constructor. This constructor fills the property set with initial values. The array of the objects
     * contains the names in the first column and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public PrimaryKeyDefImpl(final java.lang.Object[][] inInitialValues) { // NOPMD by lbenno
        super(inInitialValues);

        // We make sure, the correct keyType and schemaName are set
        setTypeAndSchema();
    }

    /** Instances of this class are primary keys.
     *
     * @return boolean */
    @Override
    public final boolean isPrimaryKeyDef() {
        return true;
    }

    private void setTypeAndSchema() {

        try {
            this.set(KeyDefDef.keyType, KeyDefDef.keyType_PrimaryKey);
            this.set(KeyDefDef.schemaName, KeyDefDef.keyType_PrimaryKey);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

}
