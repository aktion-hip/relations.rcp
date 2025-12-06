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

import org.hip.kernel.bom.GettingException;
import org.hip.kernel.bom.PropertySet;
import org.hip.kernel.bom.impl.DomainObjectImpl;
import org.hip.kernel.bom.impl.PropertyImpl;
import org.hip.kernel.bom.model.MetaModelObject;
import org.hip.kernel.bom.model.ModelObject;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.hip.kernel.util.VInvalidNameException;
import org.hip.kernel.util.VInvalidValueException;

/** This is the base abstract implementation of the ModelObject interface.
 *
 * @author Benno Luthiger
 * @see org.hip.kernel.bom.model.ModelObject */
@SuppressWarnings("serial")
abstract public class AbstractModelObject extends DomainObjectImpl implements ModelObject {
    /** AbstractModelObject default constructor. */
    public AbstractModelObject() {
        this((Object[][]) null);
    }

    /** AbstractModelObject constructor. This constructor fills the property set with initial values. The array of the
     * objects contains the names in the first column and the values in the second.
     *
     * @param inInitialValues java.lang.Object[][] */
    public AbstractModelObject(final Object[][] inInitialValues) { // NOPMD by lbenno
        super();

        if (inInitialValues != null) {
            final PropertySet lSet = propertySet();
            for (int i = 0; i < inInitialValues.length; i++) {
                try {
                    lSet.setValue((String) inInitialValues[i][0], inInitialValues[i][1]);
                } catch (final VInvalidNameException | VInvalidValueException exc) {
                    DefaultExceptionWriter.printOut(this, exc, true);
                } // try catch
            } // for
        } // if
    }

    /** AbstractModelObject constructor with XML. not implemented yet
     *
     * @param inXMLString java.lang.String */
    public AbstractModelObject(final String inXMLString) { // NOPMD by lbenno
        super();
    }

    /** This Method returns the class name of the home.
     *
     * @return java.lang.String */
    @Override
    public String getHomeClassName() {
        return "org.hip.kernel.bom.model.ModelHome";
    }

    /** Returns the descriptor for the given modelObjectType. Actually, the system supports the following types:
     * <UL>
     * <LI>ObjectDefDef</LI>
     * <LI>PropertySetDefDef</LI>
     * <LI>PropertyDefDef</LI>
     * <LI>MappingDefDef</LI>
     * <LI>TypeDefDef</LI>
     * </UL>
     *
     * @return org.hip.kernel.bom.model.MetaModelObject */
    @Override
    abstract public MetaModelObject getMetaModelObject();

    /** Subclasses of ModelObject must provide their own initialization.
     *
     * @param inSet org.hip.kernel.bom.PropertySet */
    @Override
    public final void initializePropertySet(final PropertySet inSet) {

        try {
            final MetaModelObject lMetaModelObject = getMetaModelObject();
            for (final String lName : lMetaModelObject.getPropertyNames2()) {
                inSet.add(new PropertyImpl(inSet, lName, null, (String) lMetaModelObject.get(lName)));
            }
        } catch (final VInvalidValueException exc) { // NOPMD by lbenno
            // intentionally left blank
            // as we initialize with null
        } catch (final GettingException exc) { // NOPMD by lbenno
            // intentionally left blank
        }
    }
}
