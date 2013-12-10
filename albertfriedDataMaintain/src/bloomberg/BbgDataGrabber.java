package bloomberg;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bloomberg.BbgNames.FieldValue;
import bloomberg.BbgNames.PeriodicityAdjustment;
import bloomberg.BbgNames.PeriodicitySelection;
import bloomberg.beans.FutDiv;
import bloomberg.beans.HistoricalDividendTimeUnit;
import bloomberg.beans.ReferenceAbstarct;
import bloomberg.beans.SecTS;
import bloomberg.beans.TimeSeries;
import bloomberg.beans.TimeUnit;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;

/**
 * @author Zhenghong Dong
 */
public class BbgDataGrabber {

	private String	d_host;
	private int		d_port;

	private Session	_session;

	public BbgDataGrabber() throws IOException, InterruptedException {
		d_host = "localhost";
		d_port = 8194;

		SessionOptions sessionOptions = new SessionOptions();
		sessionOptions.setServerHost( d_host );
		sessionOptions.setServerPort( d_port );

		System.out.println( "Connecting to " + d_host + ":" + d_port );
		_session = new Session( sessionOptions );
		if (!_session.start()) {
			System.err.println( "Failed to start session." );
			_session = null;
		}
		if (!_session.openService( "//blp/refdata" )) {
			System.err.println( "Failed to open //blp/refdata" );
			_session = null;
		}
	}
	
	public void stop() throws InterruptedException {
		if (_session != null) {
			_session.stop();
		}
	}
	public Map<String, SecTS> getHistoricalData(List<String> name, List<String> fields, String startDate, String endDate, String adj, String sel)
			throws Exception {
		Service refDataService = _session.getService( "//blp/refdata" );
		Request request = refDataService.createRequest( "HistoricalDataRequest" );

		Element securities = request.getElement( "securities" );
		for (String security : name) {
			securities.appendValue( security );
		}

		Element field = request.getElement( "fields" );
		for (String f : fields) {
			field.appendValue( f );
		}

		request.set( "periodicityAdjustment", adj );
		request.set( "periodicitySelection", sel );
		request.set( "startDate", startDate );
		request.set( "endDate", endDate );
		request.set( "maxDataPoints", BbgNames.maxDataPoints );
		request.set( "returnEids", true );

		System.out.println( "Sending Request: " + request );
		_session.sendRequest( request, null );

		// wait for events from session.
		Map<String, SecTS> ret = new HashMap<>();
		eventLoopTS( _session, ret, SecTS.class );
		return ret;
	}

	private <T extends TimeSeries<? extends TimeUnit>> void eventLoopTS(Session session, Map<String, T> map, Class<T> clazz) throws Exception
	{
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				processResponseEventTS( event, map, clazz );
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				processResponseEventTS( event, map, clazz );
				done = true;
			} else {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					System.out.println( msg.asElement() );
					if (event.eventType() == Event.EventType.SESSION_STATUS) {
						if (msg.messageType().equals( "SessionTerminated" ) ||
								msg.messageType().equals( "SessionStartupFailure" )) {
							done = true;
						}
					}
				}
			}
		}
	}

	// ignore below comment by bbg, they said this and make the function return void...
	// return true if (processing is completed, false otherwise
	private <T extends TimeSeries<? extends TimeUnit>> void processResponseEventTS(Event event, Map<String, T> map, Class<T> clazz) throws Exception {
		MessageIterator msgIter = event.messageIterator();
		while (msgIter.hasNext()) {
			Message msg = msgIter.next();
			if (msg.hasElement( BbgNames.RESPONSE_ERROR )) {
				printErrorInfo( "REQUEST FAILED: ", msg.getElement( BbgNames.RESPONSE_ERROR ) );
				continue;
			}

			Element security = msg.getElement( BbgNames.SECURITY_DATA );
			String ticker = security.getElementAsString( BbgNames.SECURITY );

			System.out.println( "\nTicker: " + ticker );
			if (security.hasElement( "securityError" )) {
				printErrorInfo( "\tSECURITY FAILED: ",
						security.getElement( BbgNames.SECURITY_ERROR ) );
				continue;
			}

			T tmp;
			if (map.containsKey( ticker )) {
				tmp = map.get( ticker );
			} else {
				tmp = clazz.newInstance();
			}
			if (tmp instanceof SecTS) {
				readSecTS( security, (SecTS) tmp );
			} else {
				throw new Exception( "Now only support reading historical security time series" );
			}

			System.out.println( "" );
			Element fieldExceptions = security.getElement( BbgNames.FIELD_EXCEPTIONS );
			if (fieldExceptions.numValues() > 0) {
				System.out.println( "FIELD\t\tEXCEPTION" );
				System.out.println( "-----\t\t---------" );
				for (int k = 0; k < fieldExceptions.numValues(); ++k) {
					Element fieldException =
							fieldExceptions.getValueAsElement( k );
					printErrorInfo( fieldException.getElementAsString( BbgNames.FIELD_ID ) +
							"\t\t", fieldException.getElement( BbgNames.ERROR_INFO ) );
				}
			}
		}
	}

	private void readSecTS(Element security, SecTS tmp) throws InstantiationException, IllegalAccessException {
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {
				System.out.println( "FIELD\t\tVALUE" );
				System.out.println( "-----\t\t-----" );

				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );
					/*System.out.println( fieldData.name() + "\t\t" +
							fieldData.getValueAsString() );*/

					String date = fieldData.getElementAsDate( BbgNames.DATE ).toString();
					tmp.addNode( date );
					if (fieldData.hasElement( FieldValue.open )) tmp.setOpen( date, fieldData.getElementAsFloat64( FieldValue.open ) );
					if (fieldData.hasElement( FieldValue.high )) tmp.setHigh( date, fieldData.getElementAsFloat64( FieldValue.high ) );
					if (fieldData.hasElement( FieldValue.low )) tmp.setLow( date, fieldData.getElementAsFloat64( FieldValue.low ) );
					if (fieldData.hasElement( FieldValue.last )) tmp.setClose( date, fieldData.getElementAsFloat64( FieldValue.last ) );
					if (fieldData.hasElement( FieldValue.volume )) tmp.setVolume( date, fieldData.getElementAsFloat64( FieldValue.volume ) );
					if (fieldData.hasElement( FieldValue.pe )) tmp.setPE( date, fieldData.getElementAsFloat64( FieldValue.pe ) );
					if (fieldData.hasElement( FieldValue.sharesOutstanding )) tmp.setSharesOutstanding( date,
							fieldData.getElementAsInt64( FieldValue.sharesOutstanding ) );
					if (fieldData.hasElement( FieldValue.vwap )) tmp.setVwap( date, fieldData.getElementAsFloat64( FieldValue.vwap ) );
					if (fieldData.hasElement( FieldValue.currentQuarterEEPS )) tmp.setCurrentQuarterEEPS( date,
							fieldData.getElementAsFloat64( FieldValue.currentQuarterEEPS ) );
					if (fieldData.hasElement( FieldValue.callIV )) tmp.setCallIV( date, fieldData.getElementAsFloat64( FieldValue.callIV ) );
					if (fieldData.hasElement( FieldValue.putIV )) tmp.setPutIV( date, fieldData.getElementAsFloat64( FieldValue.putIV ) );
				}
			}
		}
	}

	public <T extends ReferenceAbstarct> Map<String, T> getReferenceData(List<String> name, Class<T> clazz, List<String> fields) throws Exception {
		Service refDataService = _session.getService( "//blp/refdata" );
		Request request = refDataService.createRequest( "ReferenceDataRequest" );

		Element securities = request.getElement( "securities" );
		for (String security : name) {
			securities.appendValue( security );
		}

		Element field = request.getElement( "fields" );
		// if historical div
		if (clazz.equals( HistoricalDividendTimeUnit.class )) {
			field.appendValue( FieldValue.declaredDate );
			field.appendValue( FieldValue.exDate );
			field.appendValue( FieldValue.recordDate );
			field.appendValue( FieldValue.payableDate );
			field.appendValue( FieldValue.divAmount );
			field.appendValue( FieldValue.divFreq );
			field.appendValue( FieldValue.divType );
		} else if (clazz.equals( FutDiv.class )) {}
		else {
			for (String f : fields) {
				field.appendValue( f );
			}
		}

		System.out.println( "Sending Request: " + request );
		_session.sendRequest( request, null );

		// wait for events from session.
		Map<String, T> ret = new HashMap<>();
		eventLoopRef( _session, ret, clazz );

		_session.stop();

		return ret;
	}

	private <T extends ReferenceAbstarct> void eventLoopRef(Session session, Map<String, T> map, Class<T> clazz) throws Exception
	{
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				processResponseEventRef( event, map, clazz );
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				processResponseEventRef( event, map, clazz );
				done = true;
			} else {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					System.out.println( msg.asElement() );
					if (event.eventType() == Event.EventType.SESSION_STATUS) {
						if (msg.messageType().equals( "SessionTerminated" ) ||
								msg.messageType().equals( "SessionStartupFailure" )) {
							done = true;
						}
					}
				}
			}
		}
	}

	// ignore below comment by bbg, they said this and make the function return void...
	// return true if (processing is completed, false otherwise
	private <T extends ReferenceAbstarct> void processResponseEventRef(Event event, Map<String, T> map, Class<T> clazz) throws Exception {
		MessageIterator msgIter = event.messageIterator();
		while (msgIter.hasNext()) {
			Message msg = msgIter.next();
			if (msg.hasElement( BbgNames.RESPONSE_ERROR )) {
				printErrorInfo( "REQUEST FAILED: ", msg.getElement( BbgNames.RESPONSE_ERROR ) );
				continue;
			}

			Element security = msg.getElement( BbgNames.SECURITY_DATA );
			String ticker = security.getElementAsString( BbgNames.SECURITY );

			System.out.println( "\nTicker: " + ticker );
			if (security.hasElement( "securityError" )) {
				printErrorInfo( "\tSECURITY FAILED: ",
						security.getElement( BbgNames.SECURITY_ERROR ) );
				continue;
			}

			T tmp = clazz.newInstance();
			if (tmp instanceof HistoricalDividendTimeUnit) {
				readHisDiv( security, (HistoricalDividendTimeUnit) tmp );
			} else {
				throw new Exception( "Now only support reading historical security time series" );
			}

			System.out.println( "" );
			Element fieldExceptions = security.getElement( BbgNames.FIELD_EXCEPTIONS );
			if (fieldExceptions.numValues() > 0) {
				System.out.println( "FIELD\t\tEXCEPTION" );
				System.out.println( "-----\t\t---------" );
				for (int k = 0; k < fieldExceptions.numValues(); ++k) {
					Element fieldException =
							fieldExceptions.getValueAsElement( k );
					printErrorInfo( fieldException.getElementAsString( BbgNames.FIELD_ID ) +
							"\t\t", fieldException.getElement( BbgNames.ERROR_INFO ) );
				}
			}
		}
	}

	private void readHisDiv(Element security, HistoricalDividendTimeUnit tmp) throws InstantiationException, IllegalAccessException {
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {
				System.out.println( "FIELD\t\tVALUE" );
				System.out.println( "-----\t\t-----" );

				int numElements = fields.numElements();
				for (int j = 0; j < numElements; ++j) {
					Element field = fields.getElement( j );
					switch (field.name().toString()) {
						case FieldValue.declaredDate:
							tmp.setDeclareDate( field.getValueAsString() );
							break;
						case FieldValue.exDate:
							tmp.setExDate( field.getValueAsString() );
							break;
						case FieldValue.recordDate:
							tmp.setRecordDate( field.getValueAsString() );
							break;
						case FieldValue.payableDate:
							tmp.setPayableDate( field.getValueAsString() );
							break;
						case FieldValue.divAmount:
							tmp.setAmount( field.getValueAsFloat64() );
							break;
						case FieldValue.divFreq:
							tmp.setFrequency( field.getValueAsString() );
							break;
						case FieldValue.divType:
							tmp.setType( field.getValueAsString() );
							break;
						default:
							break;
					}

				}
			}
		}
	}

	private void printErrorInfo(String leadingStr, Element errorInfo)
			throws Exception
	{
		System.out.println( leadingStr + errorInfo.getElementAsString( BbgNames.CATEGORY ) +
				" (" + errorInfo.getElementAsString( BbgNames.MESSAGE ) + ")" );
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		BbgDataGrabber grabber = new BbgDataGrabber();
		Map<String, SecTS> res = grabber.getHistoricalData( Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" ),
				Arrays.asList( FieldValue.last, FieldValue.open ), "20110101",
				"20130101",
				PeriodicityAdjustment.actual, PeriodicitySelection.weekly );
		Map<String, HistoricalDividendTimeUnit> res2 = grabber.getReferenceData( Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" ), HistoricalDividendTimeUnit.class, null );
		
		grabber.stop();
		System.out.printf( "END" );
	}
}
