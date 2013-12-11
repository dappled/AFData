package bloomberg;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bloomberg.BbgNames.FieldValue;
import bloomberg.BbgNames.Period;
import bloomberg.BbgNames.PeriodAdj;
import bloomberg.beans.HisDivTS;
import bloomberg.beans.HisSecTS;
import bloomberg.beans.ReferenceAbstarct;
import bloomberg.beans.ReferenceAbstarct.RefType;
import bloomberg.beans.TimeSeries;
import bloomberg.beans.TimeSeries.TSType;
import bloomberg.beans.TimeUnit;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
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

	@SuppressWarnings("rawtypes")
	public Map<String, ? extends TimeSeries> getHistoricalData(TSType type, List<String> name, List<String> fields, HashMap<Name, Object> properties)
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

		setRequestProperties( request, properties );

		System.out.println( "Sending Request: " + request );
		_session.sendRequest( request, null );

		// wait for events from session.
		if (type == TSType.HisDiv) {
			Map<String, HisDivTS> tmp = new HashMap<>();
			eventLoopTS( _session, tmp, type );
			return tmp;
		} else if (type == TSType.HisSec) {
			Map<String, HisSecTS> tmp = new HashMap<>();
			eventLoopTS( _session, tmp, type );
			return tmp;
		} else {
			throw new Exception("Type support now is historical dividend and historical security");
		}
	}

	/**
	 * Set the properties of request
	 * @throws Exception
	 */
	private void setRequestProperties(Request request, HashMap<Name, Object> properties) throws Exception {
		for (Name n : properties.keySet()) {
			Object prop = properties.get( n );
			if (prop instanceof Boolean) {
				request.set( n, ((Boolean) prop).booleanValue() );
			} else if (prop instanceof String) {
				request.set( n, prop.toString() );
			} else throw new Exception( "property should be either boolean or string" );
		}
	}

	private void eventLoopTS(Session session, Map<String, ? extends TimeSeries<? extends TimeUnit>> map, TSType type) throws Exception
	{
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				processResponseEventTS( event, map, type );
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				processResponseEventTS( event, map, type );
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
	@SuppressWarnings("unchecked")
	private void processResponseEventTS(Event event, Map<String, ? extends TimeSeries<? extends TimeUnit>> map, TSType type) throws Exception {
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

			if (type == TSType.HisDiv) {
				readHisDiv( security, (Map<String, HisDivTS>) map, ticker );
			} else if (type == TSType.HisSec) {
				readHistSec( security, (Map<String, HisSecTS>) map, ticker );
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

	private void readHistSec(Element security, Map<String, HisSecTS> map, String ticker) throws InstantiationException, IllegalAccessException {
		HisSecTS tmp = new HisSecTS();
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {
				System.out.println( "FIELD\t\tVALUE" );
				System.out.println( "-----\t\t-----" );

				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );

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
		map.put( ticker, tmp );
	}

	private void readHisDiv(Element security, Map<String, HisDivTS> map, String ticker) throws InstantiationException, IllegalAccessException {
		HisDivTS tmp = new HisDivTS();
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {
				System.out.println( "FIELD\t\tVALUE" );
				System.out.println( "-----\t\t-----" );

				int numElements = fields.numElements();
				for (int j = 0; j < numElements; ++j) {
					Element fieldData = fields.getElement( j );
					String date = fieldData.getElementAsDate( FieldValue.declaredDate ).toString();
					tmp.setDeclareDate( date );
					switch (fieldData.name().toString()) {
					// case FieldValue.declaredDate:
					// date = fieldData.getValueAsString();
					// tmp.setDeclareDate( date );
					// break;
						case FieldValue.exDate:
							tmp.setExDate( date, fieldData.getValueAsString() );
							break;
						case FieldValue.recordDate:
							tmp.setRecordDate( date, fieldData.getValueAsString() );
							break;
						case FieldValue.payableDate:
							tmp.setPayableDate( date, fieldData.getValueAsString() );
							break;
						case FieldValue.divAmount:
							tmp.setAmount( date, fieldData.getValueAsFloat64() );
							break;
						case FieldValue.divFreq:
							tmp.setFrequency( date, fieldData.getValueAsString() );
							break;
						case FieldValue.divType:
							tmp.setType( date, fieldData.getValueAsString() );
							break;
						default:
							break;
					}

				}
			}
		}
	}

	public Map<String, ReferenceAbstarct> getReferenceData(List<String> name, List<String> fields, HashMap<Name, Object> properties, RefType type) throws Exception {
		Service refDataService = _session.getService( "//blp/refdata" );
		Request request = refDataService.createRequest( "ReferenceDataRequest" );

		Element securities = request.getElement( "securities" );
		for (String security : name) {
			securities.appendValue( security );
		}

		Element field = request.getElement( "fields" );
		for (String f : fields) {
			field.appendValue( f );
		}

		setRequestProperties( request, properties );

		System.out.println( "Sending Request: " + request );
		_session.sendRequest( request, null );
		return null;

		// wait for events from session.
		/*if (type == RefType.) {
			Map<String, > tmp = new HashMap<>();
			eventLoopRef( _session, tmp, type );
			return tmp;
		} else if (type == RefType.) {
			Map<String, > tmp = new HashMap<>();
			eventLoopRef( _session, tmp, type );
			return tmp;
		} else {
			throw new Exception();
		}*/
	}

	@SuppressWarnings("unused")
	private void eventLoopRef(Session session, Map<String, ? extends ReferenceAbstarct> map, RefType type) throws Exception
	{
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				processResponseEventRef( event, map, type );
			}
			else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				processResponseEventRef( event, map, type );
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
	private void processResponseEventRef(Event event, Map<String, ? extends ReferenceAbstarct> map, RefType type) throws Exception {
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

			/*if (type == RefType.HisDiv) {
				readHisDiv( security, (Map<String, HisDivTS>) map, ticker );
			} else if (type == RefType.HisSec) {
				readHistSec( security, (Map<String, HisSecTS>) map, ticker );
			} else {
				throw new Exception( "Now only support reading historical security time series" );
			}*/

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

	private void printErrorInfo(String leadingStr, Element errorInfo)
			throws Exception
	{
		System.out.println( leadingStr + errorInfo.getElementAsString( BbgNames.CATEGORY ) +
				" (" + errorInfo.getElementAsString( BbgNames.MESSAGE ) + ")" );
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public static void main(String[] args) throws Exception {
		BbgDataGrabber grabber = new BbgDataGrabber();
		HashMap<Name, Object> properties = new HashMap<>();
		properties.put( BbgNames.START, "20110101" );
		properties.put( BbgNames.END, "20130101" );
		properties.put( BbgNames.PERIOD, Period.monthly );
		properties.put( BbgNames.PERIOD_ADJ, PeriodAdj.actual );
		properties.put( BbgNames.RETEID, Boolean.TRUE );
		properties.put( BbgNames.MAX_POINTS, BbgNames.maxDataPoints );

		List<String> name = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		Map<String, HisSecTS> res = (Map<String, HisSecTS>) grabber.getHistoricalData( TSType.HisSec, name,
				Arrays.asList( FieldValue.last, FieldValue.open ), properties );

		Map<String, HisDivTS> res2 = (Map<String, HisDivTS>) grabber.getHistoricalData( TSType.HisDiv, name, Arrays.asList( FieldValue.divHis ), properties );

		grabber.stop();
		System.out.printf( "END" );
	}
}
