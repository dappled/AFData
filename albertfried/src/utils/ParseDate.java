package utils;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * My standard date format is MM/dd/yyyy
 * @author Zhenghong Dong
 */
public class ParseDate {
	public static String		yesterday = getPreviousWorkingDay( standardFromDate( new Date() ) );
	// today should be the next day of yesterday.. this is for test only, if we want to run some test on Monday for last friday's report, which should be 
	// generated last Saturday, today is then last Saturday which is one day after yesterday but not real today.
	public static String		today = getNextDay( yesterday );
	
	/**
	 * Given string like yyyyMMdd, return localDate format
	 * @param date the string date
	 * @return the localDate
	 * @throws Exception
	 */
	public static LocalDate stringToDate(final String date) {
		final DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyyMMdd" );
		return formatter.parseLocalDate( date );
	}

	/**
	 * Return today's date in string format MM/DD/YYYY like 10/24/2013
	 * @return today's date in string format
	 * @throws Exception
	 */
	public static String todayString() {
		String ret = LocalDate.now().toString();
		ret = ret.replaceAll( "-", "" );
		return ParseDate.standardFromyyyyMMdd( ret );
	}

	/**
	 * Convert date string from yyyyMMdd to MM/dd/yyyy
	 * @param date String in yyyyMMdd format
	 * @return date String in MM/dd/yyyy format
	 * @throws Exception
	 */
	public static String standardFromyyyyMMdd(final String date) {
		try {
			return ParseDate.standardFromMMddyyyy( date.substring( 4, 8 ) + date.substring( 0, 4 ) );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MM/dd/yyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert things like "JAN 16 2013" to "01/16/2013"
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String standardFromStringMonth(final String date) {
		try {
			final String[] list = date.split( " " );
			return ParseDate.fillDigitalString( ParseDate.getMonth( list[ 0 ] ) ) + "/" + list[ 1 ].trim() + "/"
					+ list[ 2 ].trim();
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MM/dd/yyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert date string from yyyy-MM-dd to MM/dd/yyyy
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String standardFromyyyyBMMBdddd(final String date) {
		try {
			return ParseDate.standardFromyyyyMMdd( date.replace( "-", "" ) );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MM/dd/yyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert date string from MMDDYYYY to MM/dd/yyyy
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String standardFromMMddyyyy(final String date) {
		try {
			return date.substring( 0, 2 ) + "/" + date.substring( 2, 4 ) + "/" + date.substring( 4, 8 );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MM/dd/yyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert sql.Date date to MM/dd/yyyy
	 * @param date
	 * @return
	 */
	public static String standardFromSQLDate(final java.sql.Date date) {
		if (date == null) return null;
		else return standardFromyyyyBMMBdddd( date.toString() );
	}

	/**
	 * Convert standard date format MM/dd/yyyy to MMddyyyy, which will be used to find tradesummary file
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String MMddyyyyFromStandard(final String date) {
		try {
			return date.replace( "/", "" );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MMddyyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert standard date format MM/dd/yyyy to yyyyMMdd, which will be used to find trde file
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String yyyyMMddFromStandard(String date) {
		try {
			date = date.replace( "/", "" );
			return date.substring( 4, 8 ) + date.substring( 0, 4 );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to yyyyMMdd from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Once again I try to bypass email system to send mismatch report... The email system doesn't
	 * let me send email with subject containing certain date format like MM/dd/yyyy MMddyyyy for certain days(like 11/15, 11/19...)
	 * @param date
	 * @return
	 */
	public static String MMddFromStandard(final String date) {
		try {
			return date.replace( "/", "" ).substring( 0, 4 );
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MMdd from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Simply convert month string to digital form
	 * @param month String
	 * @return month number
	 * @throws Exception
	 */
	public static int getMonth(final String month) {
		final DateTimeFormatter format = DateTimeFormat.forPattern( "MMM" );
		final DateTime instance = format.parseDateTime( month );

		return instance.getMonthOfYear();
	}

	/** change x to 0x */
	private static String fillDigitalString(final int month) {
		final NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits( 2 );
		return format.format( month );
	}

	/**
	 * Given a Java calendar, convert it to String format MM/dd/yyyy
	 * @param date
	 * @return
	 */
	public static String standardFromDate(final Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( date );

		return String.format( "%d/%d/%d", cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DATE ),
				cal.get( Calendar.YEAR ) );
	}
	
	/**
	 * Return the next day of a certain day in format MM/dd/yyyy
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getNextDay(final String date) {
		Calendar cal =Calendar.getInstance();
		cal.setTime( new Date(date) );
		
		cal.add( Calendar.DAY_OF_MONTH, +1 );
		return String.format( "%d/%d/%d", cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DATE ),
				cal.get( Calendar.YEAR ) );
	}

	/**
	 * Get previous working day in format MM/dd/yyyy
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getPreviousWorkingDay(final String date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( new Date(date) );

		int dayOfWeek;
		do {
			cal.add( Calendar.DAY_OF_MONTH, -1 );
			dayOfWeek = cal.get( Calendar.DAY_OF_WEEK );
		} while (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY || ParseDate.isHoliday( cal ));

		return String.format( "%d/%d/%d", cal.get( Calendar.MONTH ) + 1, cal.get( Calendar.DATE ),
				cal.get( Calendar.YEAR ) );
	}

	private static boolean isHoliday(final Calendar cal) {
		final int year = cal.get( Calendar.YEAR );
		final int month = cal.get( Calendar.MONTH ) + 1;
		final int dayOfMonth = cal.get( Calendar.DAY_OF_MONTH );

		if ((month == 12 && dayOfMonth == 25) || (month == 11 && dayOfMonth == 28 && year == 2013)) return true;

		// more checks

		return false;
	}
}
