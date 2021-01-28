package com.runescape.utility.world;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 5, 2015
 */
public class DateCalculation {

	/**
	 * Gets the date instance in toronto time
	 */
	public static Date getDate() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Toronto"));
		return new Date();
	}

	/**
	 * Gets the week number we're on
	 *
	 * @return The week number we're on
	 */
	public static int getWeekNumber() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		return cal.get(Calendar.WEEK_OF_MONTH);
	}

	/**
	 * Gets the name of the month we're on
	 */
	@SuppressWarnings("deprecation")
	public static String getMonthName() {
		return DateFormatSymbols.getInstance().getMonths()[getDate().getMonth()];
	}

	/**
	 * Gets the name of a number
	 *
	 * @param number
	 * 		The number
	 */
	public static String getNumberName(int number) {
		String[] names = { "Zero", "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eigth", "Ninth" };
		return names[number];
	}
}
