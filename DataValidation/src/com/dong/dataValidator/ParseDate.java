package com.dong.dataValidator;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ParseDate {
	
	/**
	 * Given string like YYYYMMDD, return localDate format
	 * @param date the string date
	 * @return the localDate 
	 * @throws Exception
	 */
	public static LocalDate stringToDate(final String date) throws Exception {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		return formatter.parseLocalDate(date);
	}
	
	/**
	 * Return today's date in string format MMDDYYYY like 10242013	
	 * @return today's date in string format
	 * @throws Exception
	 */
	public static String todayString() throws Exception {
		String ret = LocalDate.now().toString();
		ret = ret.replaceAll( "-", "" );
		return MMDDYYYY( ret );
	}
	
	
	/**
	 * Convert date string from YYYYMMDD to MMDDYYYY
	 * @param date String in YYYYMMDD format
	 * @return date String in MMDDYYYY foramt
	 * @throws Exception
	 */
	public static String MMDDYYYY(String date) throws Exception {
		return date.substring( 4, 8 ) + date.substring( 0, 4 );
	}

	/**
	 * Convert things like "JAN 16 2013" to "01/16/2013"
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String MMSlashDDSlashYYYYFromStringMonth(String date) throws Exception {
		String[] list = date.split( " " );
		return fillDigitalString( getMonth( list[0] ) ) + "/" + list[1].trim() + "/" + list[2].trim(); 
	}
	
	/**
	 * Convert date string from MMDDYYYY to MM/DD/YYYY
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String MMSlashDDSlashYYYY(String date) throws Exception {
		return date.substring( 0, 2 ) + "/" + date.substring( 2, 4 ) + "/" + date.substring( 4, 8 );
	}
	
	/**
	 * Simply convert month string to digitial form
	 * @param month String
	 * @return month number
	 * @throws Exception
	 */
	public static int getMonth(String month) throws Exception {
	    DateTimeFormatter format = DateTimeFormat.forPattern("MMM");
	    DateTime instance        = format.parseDateTime(month);  

	    return instance.getMonthOfYear();
	}
	
	/** change x to 0x */
	private static String fillDigitalString(int month) {
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits(2);
		return format.format( month );
	}
	
	public static String getPreviousWorkingDay(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		int dayOfWeek;
		do {
			cal.add(Calendar.DAY_OF_MONTH, -1);
			dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		} while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY || isHoliday(cal));
		
		return String.format( "%d%d%d", cal.get( Calendar.YEAR ), cal.get( Calendar.MONTH )+1, cal.get( Calendar.DATE ) );
	}

	private static boolean isHoliday(Calendar cal) {
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH) + 1;
	    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

	    if ((month == 12 && dayOfMonth == 25) || (month == 11 && dayOfMonth == 28 && year == 2013)) {
	        return true;
	    }

	    // more checks

	    return false;
	}
}
