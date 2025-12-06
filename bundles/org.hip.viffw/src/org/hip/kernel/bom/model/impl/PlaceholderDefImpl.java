/**
 This package is part of the domain object framework used for the application VIF.
 Copyright (C) 2004-2014, Benno Luthiger

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

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.SettingException;
import org.hip.kernel.bom.model.MetaModelHome;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.PlaceholderDef;
import org.hip.kernel.bom.model.PlaceholderDefDef;
import org.hip.kernel.exc.DefaultExceptionHandler;

/** Class implementing a placeholder in an joined object def.
 *
 * @author Benno Luthiger Created on Nov 7, 2004 */
@SuppressWarnings("serial")
public class PlaceholderDefImpl extends AbstractModelObject implements PlaceholderDef {
    private int position;

    /** PlaceholderDefImpl constructor with the specified name/alias.
     *
     * @param inName String */
    public PlaceholderDefImpl(final String inName) {
        super();
        try {
            set(PlaceholderDefDef.name, inName);
        } catch (final SettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
    }

    /** @return MetaModelObject
     * @see org.hip.kernel.bom.model.ModelObject#getMetaModelObject() */
    @Override
    public MetaModelObject getMetaModelObject() {
        return MetaModelHome.singleton.getPlaceholderDefDef();
    }

    /** Returns the nestings name, i.e. the alias of the placeholder.
     *
     * @return String */
    @Override
    public String getName() {
        try {
            return (String) get(PlaceholderDefDef.name);
        } catch (final GettingException exc) {
            DefaultExceptionHandler.instance().handle(exc);
        }
        return "";
    }

    /** @param position The position to set. */
    @Override
    public void setPosition(final int inPosition) {
        position = inPosition;
    }

    /** @return Returns the position. */
    @Override
    public int getPosition() {
        return position;
    }
}
