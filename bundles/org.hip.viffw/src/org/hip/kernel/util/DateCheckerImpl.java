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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.hip.kernel.servlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper class which can be used to read date fields from a form (i.e Context) and checks whether the inputed data is
 * valid.
 *
 * @author: Benno Luthiger */
public class DateCheckerImpl implements DateChecker { // NOPMD by lbenno
    private static final Logger LOG = LoggerFactory.getLogger(DateCheckerImpl.class);

    // checked dates
    private transient Calendar startDate;
    private transient Calendar endDate;

    // instance variables
    private transient int mode;
    private transient final boolean singleDate;
    private transient final boolean ignoreEmpty;

    private transient String fromDayKey = "";
    private transient String fromMonthKey = "";
    private transient String fromYearKey = "";
    private transient String toDayKey = "";
    private transient String toMonthKey = "";
    private transient String toYearKey = "";

    // constants for date-validation
    private static final int MONTH_LENGTH[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private static final int LEAP_MONTH_LENGTH[] = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    /** DateChecker default constructor. */
    public DateCheckerImpl() {
        this(false, false, DateChecker.FROM_DAY_KEY,
                DateChecker.FROM_MONTH_KEY,
                DateChecker.FROM_YEAR_KEY,
                DateChecker.TO_DAY_KEY,
                DateChecker.TO_MONTH_KEY,
                DateChecker.TO_YEAR_KEY);
    }

    /** DateChecker constructor for a single date check.
     *
     * @param inSingleDate boolean true if there's only one date to check
     * @param inIgnoreEmpty boolean true if empty date fields have to be ignored */
    public DateCheckerImpl(final boolean inSingleDate, final boolean inIgnoreEmpty) {
        this(inSingleDate, inIgnoreEmpty, DateChecker.FROM_DAY_KEY,
                DateChecker.FROM_MONTH_KEY,
                DateChecker.FROM_YEAR_KEY,
                DateChecker.TO_DAY_KEY,
                DateChecker.TO_MONTH_KEY,
                DateChecker.TO_YEAR_KEY);
    }

    /** DateChecker constructor for a date check with specified field names for form date fields.
     *
     * @param inSingleDate boolean true if there's only one date to check
     * @param inIgnoreEmpty boolean true if empty date fields have to be ignored
     * @param inFromDay java.lang.String
     * @param inFromMonth java.lang.String
     * @param inFromYear java.lang.String
     * @param inToDay java.lang.String
     * @param inToMonth java.lang.String
     * @param inToYear java.lang.String */
    public DateCheckerImpl(final boolean inSingleDate, final boolean inIgnoreEmpty, final String inFromDay,
            final String inFromMonth, final String inFromYear, final String inToDay, final String inToMonth,
            final String inToYear) {
        super();

        mode = DateChecker.DATE_OK;
        singleDate = inSingleDate;
        ignoreEmpty = inIgnoreEmpty;

        fromDayKey = inFromDay;
        fromMonthKey = inFromMonth;
        fromYearKey = inFromYear;
        toDayKey = inToDay;
        toMonthKey = inToMonth;
        toYearKey = inToYear;
    }

    /** Check if passed integer > 0 and < daysOfMonth.
     *
     * @return boolean
     * @param inDay int
     * @param inMonth int
     * @param inYear int */

    private boolean checkDay(final int inDay, final int inMonth, final int inYear) {

        final GregorianCalendar lCalendar = new GregorianCalendar();
        int lMonthLength = 0;
        // use calendar to test if it is a leap year.
        if (lCalendar.isLeapYear(inYear)) {
            lMonthLength = LEAP_MONTH_LENGTH[inMonth]; // index 0 -> Days of January
        }
        else {
            lMonthLength = MONTH_LENGTH[inMonth]; // index 0 -> Days of January
        }
        return inDay > 0 && inDay <= lMonthLength;
    }

    /** Creates a Calendar date of year-, month- und day-Strings. The parameters will be checked and the mode will bet
     * set accordingliy if the they are wrong.
     *
     * @return java.util.Calendar
     * @param inYear java.lang.String
     * @param inMonth java.lang.String
     * @param inDay java.lang.String */
    private Calendar getCheckedDateOf(final String inYear, final String inMonth, final String inDay) {
        Calendar lDate = new GregorianCalendar();

        getCheckedTimeStampOf(inYear, inMonth, inDay);
        if (mode == DateChecker.DATE_OK) {
            lDate = new GregorianCalendar(getInt(inYear), getInt(inMonth) - 1, getInt(inDay));
        }
        return lDate;
    }

    /** Creates SQL-TimeStamp of year-, month- und day-Strings. The parameters will be checked and the mode will bet set
     * accordingly if the they are wrong.
     *
     * Checks: year > 1900 month > 0 and month <= 12 day > 0 and day <= max. days of month
     *
     * @return java.sql.TimeStamp
     * @param inYear java.lang.String
     * @param inMonth java.lang.String
     * @param inDay java.lang.String */
    private Timestamp getCheckedTimeStampOf(final String inYear, final String inMonth, final String inDay) {

        int lYear;
        int lMonth;
        int lDay;
        Timestamp outTimeStamp = null;

        if (inYear == null || inYear.equals("") ||
                inMonth == null || inMonth.equals("") ||
                inDay == null || inDay.equals("")) {
            mode = DateChecker.FAILURE_EMPTY_FIELDS_FOUND;
        }
        else {
            try {
                lYear = Integer.parseInt(inYear);
                lMonth = Integer.parseInt(inMonth);
                lMonth -= 1; // correct for creating java.sql.Date
                lDay = Integer.parseInt(inDay);

                // Check parameter if they are correct for creating a date
                if (!(lYear > 1800) || !(lMonth >= 0) || !(lMonth < 12) || !(checkDay(lDay, lMonth, lYear))) { // NOPMD by lbenno 
                    mode = DateChecker.FAILURE_INVALID_DATE;
                }
                else {
                    outTimeStamp = new Timestamp(new GregorianCalendar(lYear, lMonth, lDay).getTime().getTime());
                } // if - else
            } // try
            catch (final NumberFormatException ex) {
                mode = DateChecker.FAILURE_NO_NUMERIC_VALUES;
            } // try - catch
        }
        return outTimeStamp;
    }

    @Override
    public Calendar getEndDate() { // NOPMD by lbenno 
        return endDate;
    }

    /** @return int
     * @param inValue java.lang.String */

    private int getInt(final String inValue) {
        return Integer.valueOf(inValue);
    }

    @Override
    public int getMode() { // NOPMD by lbenno
        return mode;
    }

    @Override
    public Calendar getStartDate() { // NOPMD by lbenno
        return startDate;
    }

    private boolean hasFromDateParameters(final Context inContext) {
        return inContext.hasParameter(fromDayKey) &&
                inContext.hasParameter(fromMonthKey) &&
                inContext.hasParameter(fromYearKey);
    }

    private boolean hasToDateParameters(final Context inContext) {
        return inContext.hasParameter(toDayKey) &&
                inContext.hasParameter(toMonthKey) &&
                inContext.hasParameter(toYearKey);
    }

    @Override
    public void readFormDateFields(final Context inContext) { // NOPMD by lbenno
        String lDay;
        String lMonth;
        String lYear;

        // test input fields for date FROM
        if (hasFromDateParameters(inContext)) {
            lDay = inContext.getParameterValue(fromDayKey);
            lMonth = inContext.getParameterValue(fromMonthKey);
            lYear = inContext.getParameterValue(fromYearKey);
            if (!"".equals(lDay) && !"".equals(lMonth) && !"".equals(lYear)) { // NOPMD by lbenno 
                // all fields are filled with input
                startDate = getCheckedDateOf(lYear, lMonth, lDay);
            }
            else {
                // at least one field is empty
                if (ignoreEmpty) {
                    // if all fields are empty, it's ok
                    if ("".equals(lDay) && "".equals(lMonth) && "".equals(lYear)) { // NOPMD by lbenno 
                        // all fields are empty
                        mode = DateChecker.DATE_OK;
                        startDate = null; // NOPMD by lbenno
                    }
                    else {
                        // at least one field has an input
                        mode = DateChecker.FAILURE_EMPTY_FIELDS_FOUND;
                    }
                }
                else {
                    mode = DateChecker.FAILURE_EMPTY_FIELDS_FOUND;
                } // if
            } // if - else
        }
        else {
            LOG.error("Failure: Names of date fields in form don't correspond with the names in code.");
            mode = DateChecker.FAILURE_NO_PARAMETERS_FOUND;
        } // if - else

        if (!singleDate && mode == DateChecker.DATE_OK) {
            // test input fields for date TO
            if (hasToDateParameters(inContext)) {
                lDay = inContext.getParameterValue(toDayKey);
                lMonth = inContext.getParameterValue(toMonthKey);
                lYear = inContext.getParameterValue(toYearKey);
                if (!"".equals(lDay) && !"".equals(lMonth) && !"".equals(lYear)) { // NOPMD by lbenno 
                    // all fields are filled with input
                    endDate = getCheckedDateOf(lYear, lMonth, lDay);
                    if (endDate.before(startDate)) {
                        mode = DateChecker.FAILURE_WRONG_DATE_ORDER;
                    }
                }
                else {
                    // at least one field is empty
                    if (ignoreEmpty) {
                        // if all fields are empty, it's ok
                        if ("".equals(lDay) && "".equals(lMonth) && "".equals(lYear)) { // NOPMD by lbenno 
                            // all fields are empty
                            mode = DateChecker.DATE_OK;
                            endDate = null; // NOPMD by lbenno
                        }
                        else {
                            // at least one field has an input
                            mode = DateChecker.FAILURE_EMPTY_FIELDS_FOUND;
                        }
                    }
                    else {
                        mode = DateChecker.FAILURE_EMPTY_FIELDS_FOUND;
                    } // if
                } // if - else
            }
            else {
                LOG.error("Failure: Names of date fields in form don't correspond with the names in code.");
                mode = DateChecker.FAILURE_NO_PARAMETERS_FOUND;
            } // if - else
        } // if
        else {
            endDate = new GregorianCalendar();
        }
    }

    @Override
    public String toString() { // NOPMD by lbenno 
        final String lMessage = "StartDate=\"" +
                ((startDate == null) ? "null" : startDate.getTime().toString()) +
                "\" EndDate=\"" +
                ((endDate == null) ? "null" : endDate.getTime().toString()) + "\"";
        return Debug.classMarkupString(this, lMessage);
    }
}
