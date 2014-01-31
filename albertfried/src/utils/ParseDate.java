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
	// last working day
	public static String	yesterday		= getPreviousWorkingDay( standardFromDate( new Date() ) );
	// today should be the next day of yesterday.. this is for test only, if we want to run some test on Monday for last
	// friday's report, which should be generated last Saturday, today is then last Saturday which is one day after
	// yesterday but not real today.
	// However, we won't never generate report on Monday morning, they should be generated last Saturday.
	public static String	testToday		= getNextWorkingDay( yesterday );
	// today's date, usually used as importDate in database
	public static String	today			= standardFromDate( new Date() );
	// two business days later
	public static String	twoDaysLater	= getNextWorkingDay( getNextWorkingDay( standardFromDate( new Date() ) ) );

	/**
	 * Compare two string date in standard format
	 * @return >0 if date1 is after date2, <0 if date1 is before date2
	 */
	@SuppressWarnings("deprecation")
	public static int compare(String date1, String date2) {
		Date d1 = new Date( date1 );
		Date d2 = new Date( date2 );
		return d1.compareTo( d2 );
	}

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
	public static String standardFromStringMonthTypeOne(final String date) {
		try {
			final String[] list = date.split( " " );
			return ParseDate.fillDigitalString( ParseDate.getMonth( list[ 0 ] ) ) + "/"
					+ fillDigitalString( Integer.parseInt( list[ 1 ].trim() ) ) + "/"
					+ list[ 2 ].trim();
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.printf( "Failed to parse date to MM/dd/yyyy from %s, will return empty string\n", date );
			return "";
		}
	}

	/**
	 * Convert things like "20th September 2010" to "09/20/2013"
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public static String standardFromStringMonthTypeTwo(final String date) {
		try {
			final String[] list = date.split( " " );
			return ParseDate.fillDigitalString( ParseDate.getMonth( list[ 1 ] ) ) + "/"
					+ fillDigitalString( Integer.parseInt( list[ 0 ].trim().replaceAll( "[^\\d]", "" ) ) ) + "/"
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
		if (date == null || date.equals( "null" )) return date;
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
	 * Convert Calendar date to MM/dd/yyyy
	 * @param calendar
	 * @return
	 */
	public static String standardFromCal(Calendar calendar) {
		return standardFromDate( calendar.getTime() );
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
	 * let me send email with subject containing certain date format like MM/dd/yyyy MMddyyyy for certain days(like
	 * 11/15, 11/19...)
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

	/**
	 * Given a Java calendar, convert it to String format MM/dd/yyyy
	 * @param date
	 * @return
	 */
	public static String standardFromDate(final Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( date );

		return String.format( "%s/%s/%d", fillDigitalString( cal.get( Calendar.MONTH ) + 1 ),
				fillDigitalString( cal.get( Calendar.DATE ) ),
				cal.get( Calendar.YEAR ) );
	}

	/**
	 * Return the next day of a certain day in format MM/dd/yyyy
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getNextWorkingDay(final String date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( new Date( date ) );

		do {
			cal.add( Calendar.DAY_OF_MONTH, +1 );
		} while (ParseDate.isHoliday( cal ));

		return String.format( "%s/%s/%d", fillDigitalString( cal.get( Calendar.MONTH ) + 1 ),
				fillDigitalString( cal.get( Calendar.DATE ) ),
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
		cal.setTime( new Date( date ) );

		do {
			cal.add( Calendar.DAY_OF_MONTH, -1 );
		} while (ParseDate.isHoliday( cal ));

		return String.format( "%s/%s/%d", fillDigitalString( cal.get( Calendar.MONTH ) + 1 ),
				fillDigitalString( cal.get( Calendar.DATE ) ),
				cal.get( Calendar.YEAR ) );
	}

	/**
	 * Get next Friday
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static int getNextFriday(final long date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( new Date( standardFromLong( date ) ) );

		do {
			cal.add( Calendar.DAY_OF_MONTH, +1 );
		} while (cal.get( Calendar.DAY_OF_WEEK ) != Calendar.FRIDAY);

		return (cal.get( Calendar.MONTH ) + 1) * 100 +
				cal.get( Calendar.DATE ) +
				cal.get( Calendar.YEAR ) * 10000;
	}

	@SuppressWarnings("deprecation")
	public static boolean isHoliday(final String date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime( new Date( date ) );
		return isHoliday( cal );
	}
	
	public static boolean isHoliday(final Calendar cal) {
		final int year = cal.get( Calendar.YEAR );
		final int month = cal.get( Calendar.MONTH ) + 1;
		final int dayOfMonth = cal.get( Calendar.DAY_OF_MONTH );

		int dayOfWeek = cal.get( Calendar.DAY_OF_WEEK );
		if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return true;

		if ((month == 12 && dayOfMonth == 25 && year == 2014) || (month == 1 && dayOfMonth == 1 && year == 2014) ||
				(month == 2 && dayOfMonth == 17 && year == 2014) || (month == 4 && dayOfMonth == 18 && year == 2014) ||
				(month == 5 && dayOfMonth == 26 && year == 2014) || (month == 7 && dayOfMonth == 4 && year == 2014) ||
				(month == 9 && dayOfMonth == 1 && year == 2014) || (month == 11 && dayOfMonth == 27 && year == 2014) ||
				(month == 12 && dayOfMonth == 25 && year == 2015) || (month == 1 && dayOfMonth == 1 && year == 2015) ||
				(month == 1 && dayOfMonth == 19 && year == 2015) || (month == 2 && dayOfMonth == 16 && year == 2015) ||
				(month == 4 && dayOfMonth == 3 && year == 2015) || (month == 5 && dayOfMonth == 25 && year == 2015) ||
				(month == 7 && dayOfMonth == 3 && year == 2015) || (month == 9 && dayOfMonth == 7 && year == 2015) ||
				(month == 11 && dayOfMonth == 26 && year == 2015) ||
				(month == 12 && dayOfMonth == 26 && year == 2016) || (month == 1 && dayOfMonth == 1 && year == 2016) ||
				(month == 1 && dayOfMonth == 18 && year == 2016) || (month == 2 && dayOfMonth == 15 && year == 2016) ||
				(month == 3 && dayOfMonth == 25 && year == 2016) || (month == 5 && dayOfMonth == 30 && year == 2016) ||
				(month == 7 && dayOfMonth == 4 && year == 2016) || (month == 9 && dayOfMonth == 5 && year == 2016) ||
				(month == 11 && dayOfMonth == 24 && year == 2016)) return true;

		// more checks
		return false;
	}

	/**
	 * Get MM/dd/yyyy from digital yyyyMMdd
	 * @param sequenceNumber
	 * @return
	 */
	public static String standardFromLong(long date) {
		String d = String.valueOf( date );
		return String.format( "%s/%s/%s", d.substring( 4, 6 ), d.substring( 6, 8 ), d.substring( 0, 4 ) );
	}

	/**
	 * Get digitial yyyyMMdd from standard
	 * @param startDate
	 * @return
	 */
	public static long longFromStandard(String startDate) {
		String[] date = startDate.split( "/" );
		return Long.parseLong( date[ 2 ] ) * 10000 + Long.parseLong( date[ 0 ] ) * 100 + Long.parseLong( date[ 1 ] );
	}

	/** change x to 0x */
	private static String fillDigitalString(final int month) {
		final NumberFormat format = NumberFormat.getInstance();
		format.setMinimumIntegerDigits( 2 );
		return format.format( month );
	}

	public static void main(String[] args) {
		long date = 19890515;
		System.out.println( standardFromLong( date ) );
		date = getNextFriday( date );
		System.out.println( date );
		date = getNextFriday( date );
		System.out.println( date );
		System.out.println( longFromStandard( standardFromLong( date ) ) );

		String date2 = "1st September 2010";
		System.out.println( standardFromStringMonthTypeTwo( date2 ) );

		System.out.println( twoDaysLater );
		System.out.println( compare( twoDaysLater, twoDaysLater ) == 0 );
		
		
		System.out.println(isHoliday( standardFromLong( 20161225 )));
	}
}
