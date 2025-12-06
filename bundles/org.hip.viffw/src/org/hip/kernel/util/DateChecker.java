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
package org.hip.kernel.util;

/** Helper class which can be used to read date fields from a form (i.e Context) and checks whether the inputed data is
 * valid.
 *
 * @author: Benno Luthiger */
public interface DateChecker {
    // class variables to signal exceptions and failures
    int DATE_OK = 0;
    int FAILURE_NO_PARAMETERS_FOUND = 1;
    int FAILURE_EMPTY_FIELDS_FOUND = 2;
    int FAILURE_NO_NUMERIC_VALUES = 3;
    int FAILURE_INVALID_DATE = 4;
    int FAILURE_WRONG_DATE_ORDER = 5;

    // constants for form values
    String FROM_DAY_KEY = "vonTag";
    String FROM_MONTH_KEY = "vonMonat";
    String FROM_YEAR_KEY = "vonJahr";
    String TO_DAY_KEY = "bisTag";
    String TO_MONTH_KEY = "bisMonat";
    String TO_YEAR_KEY = "bisJahr";

    /** Returns the checked end date.
     *
     * @return java.util.Calendar */
    java.util.Calendar getEndDate();

    /** Returns the status after checking the date-fields.
     *
     * @return int */
    int getMode();

    /** Returns the checked start date.
     *
     * @return java.util.Calendar */
    java.util.Calendar getStartDate();

    /** Reads the Data from the context and forms a checked date out of them.
     *
     * @param param org.hip.kernel.servlet.Context */
    void readFormDateFields(org.hip.kernel.servlet.Context inContext);
}
