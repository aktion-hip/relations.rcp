/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

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
package org.hip.kernel.servlet.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

import org.hip.kernel.servlet.Context;
import org.hip.kernel.servlet.View;
import org.hip.kernel.sys.VSys;
import org.hip.kernel.sys.VSysConstants;

/** Baseclass of all views
 *
 * @author Benno Luthiger */

@SuppressWarnings("serial")
public abstract class AbstractView implements View, Serializable {
    // instance attributes
    private Context context;

    /** Default constructor. */
    public AbstractView() {
        super();
    }

    /** Constructor sets the context of this view.
     *
     * @param inContext org.hip.kernel.servlet.Context */
    public AbstractView(final Context inContext) {
        super();
        this.setContext(inContext);
    }

    /** Returns context of this view if set or null if not.
     *
     * @return org.hip.kernel.servlet.Context - the context of this view
     * @see org.hip.kernel.servlet.Context
     * @see org.hip.kernel.servlet.View */
    @Override
    public Context getContext() {
        return context;
    }

    /** Returns the language-id set in the context of this view. The language id returned is a ISO2-language-code:
     * <p/>
     *
     * examples;
     * <p/>
     * <ul>
     * <li><b>de</b> for German
     * <li><b>fr</b> for French
     * <li><b>it</b> for Italian
     * </ul>
     * <p/>
     *
     * If no context is set, the default-language of VSys will be returned.
     *
     * @return java.lang.String - ISO2 language-code
     * @see org.hip.kernel.sys.VSys */
    @Override
    public String getLanguage() {

        // pre: Context not null
        if (context == null) {
            return VSys.dftLanguage;
        }

        String outLanguage = (String) context.get(AbstractContext.LANGUAGE_KEY);

        if (outLanguage == null) {
            try {
                outLanguage = VSys.getVSysProperties().getProperty(VSysConstants.KEY_DFT_LANGUAGE);
            } catch (final IOException exc) {
                outLanguage = VSys.dftLanguage;
            }
        }

        return outLanguage;
    }

    /** Returns the locale.
     *
     * @return java.util.Locale */
    @Override
    public Locale getLocale() {
        // pre: Context not null
        if (context == null) {
            return new Locale(VSys.dftLanguage, VSys.dftCountry);
        }

        String lCountry = "";
        try {
            lCountry = VSys.getVSysProperties().getProperty(VSysConstants.KEY_DFT_COUNTRY);
        } catch (final IOException exc) {
            lCountry = VSys.dftCountry;
        }
        return new Locale(getLanguage(), lCountry);
    }

    /** Sets the context of this View
     *
     * @param inContext org.hip.kernel.servlet.Context */
    @Override
    public void setContext(final Context inContext) {
        context = inContext;
    }
}
