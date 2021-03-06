package bbgRequestor.bloomberg;

import com.bloomberglp.blpapi.Name;

/**
 * @author Zhenghong Dong
 */
public class BbgNames {
	public static final Name	SECURITY_DATA		= new Name("securityData");
	public static final Name	SECURITY			= new Name("security");
	public static final Name	FIELD_DATA			= new Name("fieldData");
	public static final Name	RESPONSE_ERROR		= new Name("responseError");
	public static final Name	SECURITY_ERROR		= new Name("securityError");
	public static final Name	FIELD_EXCEPTIONS	= new Name("fieldExceptions");
	public static final Name	FIELD_ID			= new Name("fieldId");
	public static final Name	ERROR_INFO			= new Name("errorInfo");
	public static final Name	CATEGORY			= new Name("category");
	public static final Name	MESSAGE				= new Name("message");
	public static final Name	DATE				= new Name("date");

	public static class Properties {
		public static final String	START			= "startDate";
		public static final String	END				= "endDate";
		public static final String	MAX_POINTS		= "maxDataPoints";
		public static final String	RETEID			= "returnEids";
		public static final String	PERIOD			= "periodicitySelection";
		public static final String	PERIOD_ADJ		= "periodicityAdjustment";

		public static final int		maxDataPoints	= Integer.MAX_VALUE;

		public static class Period {
			public static final String	weekly			= "WEEKLY";
			public static final String	daily			= "DAILY";
			public static final String	monthly			= "MONTHLY";
			public static final String	quarterly		= "QUARTERLY";
			public static final String	semiAnnually	= "SEMI_ANNUALLY";
			public static final String	yearly			= "YEARLY";
		}

		public static class PeriodAdj {
			public static final String	actual		= "ACTUAL";
			public static final String	calendar	= "CALENDAR";
			public static final String	fiscal		= "FISCAL";
		}
	}

	public static class Fields {
		public static final String	last				= "PX_LAST";
		public static final String	ask					= "PX_ASK";
		public static final String	bid					= "PX_BID";
		public static final String	mid					= "PX_MID";
		public static final String	high				= "PX_HIGH";
		public static final String	open				= "PX_OPEN";
		public static final String	low					= "PX_LOW";
		public static final String	close				= "PX_CLOSE";
		public static final String	volume				= "VOLUME";
		public static final String	pe					= "PE_RATIO";
		public static final String	sharesOutstanding	= "EQY_SH_OUT";
		public static final String	vwap				= "EQY_WEIGHTED_AVG_PX";
		public static final String	currentQuarterEEPS	= "EEPS_CUR_QTR";
		public static final String	callIV				= "3MO_CALL_IMP_VOL";
		public static final String	putIV				= "3MO_PUT_IMP_VOL";

		public static final String	dvdHis				= "DVD_HIST";
		public static final String	declaredDate		= "DVD_DECLARED_DT";
		public static final String	exDate				= "DVD_EX_DT";
		public static final String	recordDate			= "DVD_RECORD_DT";
		public static final String	payableDate			= "DVD_PAY_DT";
		public static final String	divAmount			= "LAST_DPS_GROSS";
		public static final String	divFreq				= "Dividend Frequency";
		public static final String	divType				= "Dividend Type";
		public static final String	OptionImpliedLow	= "OptionImpliedLow";
		public static final String	OptionImpliedHigh	= "Opt";
		public static final String	DivPerShare			= "EQY_DPS";
		public static final String	nextExDate			= "BDVD_NEXT_EST_EX_DT";
		public static final String	nextDivAmount		= "BDVD_PROJ_DIV_AMT";
	}
}
