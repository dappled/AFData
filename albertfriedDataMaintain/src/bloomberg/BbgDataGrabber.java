package bloomberg;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.ParseDate;
import bloomberg.BbgNames.FieldValue;
import bloomberg.BbgNames.Period;
import bloomberg.BbgNames.PeriodAdj;
import bloomberg.beans.HisDivTS;
import bloomberg.beans.HisSecTS;
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
	public HashMap<String, ? extends TimeSeries> getData(String t, List<String> name, List<String> fields, HashMap<Name, Object> properties)
			throws Exception {
		Service refDataService = _session.getService( "//blp/refdata" );
		Request request;
		TSType type;
		switch (t) {
			case "HisDiv":
				type = TSType.HisDiv;
				request = refDataService.createRequest( "ReferenceDataRequest" );
				break;
			case "HisSec":
				type = TSType.HisSec;
				request = refDataService.createRequest( "HistoricalDataRequest" );
				break;
			default:
				throw new Exception( "Type support now is historical dividend and historical security" );
		}

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
			HashMap<String, HisDivTS> tmp = new HashMap<>();
			eventLoop( _session, tmp, type );
			return tmp;
		} else if (type == TSType.HisSec) {
			HashMap<String, HisSecTS> tmp = new HashMap<>();
			eventLoop( _session, tmp, type );
			return tmp;
		}
		return null;
	}

	/**
	 * Set the properties of request
	 * 
	 * @throws Exception
	 */
	private void setRequestProperties(Request request,
			HashMap<Name, Object> properties) throws Exception {
		if (properties == null) return;
		for (Name n : properties.keySet()) {
			Object prop = properties.get( n );
			if (prop instanceof Boolean) {
				request.set( n, ((Boolean) prop).booleanValue() );
			} else if (prop instanceof String) {
				request.set( n, (String) prop );
			} else if (prop instanceof Integer) {
				request.set( n, ((Integer) prop).intValue() );
			} else {
				throw new Exception(
						"property should be either boolean or string" );
			}
		}
	}

	private void eventLoop(Session session,
			HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map,
			TSType type) throws Exception {
		boolean done = false;
		while (!done) {
			Event event = session.nextEvent();
			if (event.eventType() == Event.EventType.PARTIAL_RESPONSE) {
				System.out.println( "Processing Partial Response" );
				processResponseEvent( event, map, type );
			} else if (event.eventType() == Event.EventType.RESPONSE) {
				System.out.println( "Processing Response" );
				processResponseEvent( event, map, type );
				done = true;
			} else {
				MessageIterator msgIter = event.messageIterator();
				while (msgIter.hasNext()) {
					Message msg = msgIter.next();
					if (event.eventType() == Event.EventType.SESSION_STATUS) {
						if (msg.messageType().equals( "SessionTerminated" )
								|| msg.messageType().equals(
										"SessionStartupFailure" )) {
							done = true;
						}
					}
				}
			}
		}
	}

	// ignore below comment by bbg, they said this and make the function return
	// void...
	// return true if (processing is completed, false otherwise
	private void processResponseEvent(Event event,
			HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map,
			TSType type) throws Exception {
		MessageIterator msgIter = event.messageIterator();
		while (msgIter.hasNext()) {
			Message msg = msgIter.next();
			if (msg.hasElement( BbgNames.RESPONSE_ERROR )) {
				printErrorInfo( "REQUEST FAILED: ",
						msg.getElement( BbgNames.RESPONSE_ERROR ) );
				continue;
			}

			Element security = msg.getElement( BbgNames.SECURITY_DATA );
			if (type != TSType.HisSec) {
				for (int i = 0; i < security.numValues(); i++) {
					extractSingleSecurityValue( security.getValueAsElement( i ),
							map, type );
				}
			} else {
				extractSingleSecurityValue( security, map, type );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void extractSingleSecurityValue(Element security,
			HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map,
			TSType type) throws Exception {
		String ticker = security.getElementAsString( BbgNames.SECURITY );
		if (security.hasElement( "securityError" )) {
			printErrorInfo( "\tSECURITY FAILED: ",
					security.getElement( BbgNames.SECURITY_ERROR ) );
			return;
		}

		if (type == TSType.HisDiv) {
			readHisDiv( security, (HashMap<String, HisDivTS>) map, ticker );
		} else if (type == TSType.HisSec) {
			readHistSec( security, (HashMap<String, HisSecTS>) map, ticker );
		} else {
			throw new Exception(
					"Now only support reading historical security time series" );
		}

		Element fieldExceptions = security
				.getElement( BbgNames.FIELD_EXCEPTIONS );
		if (fieldExceptions.numValues() > 0) {
			for (int k = 0; k < fieldExceptions.numValues(); ++k) {
				Element fieldException = fieldExceptions.getValueAsElement( k );
				printErrorInfo(
						fieldException.getElementAsString( BbgNames.FIELD_ID )
								+ "\t\t",
						fieldException.getElement( BbgNames.ERROR_INFO ) );
			}
		}
	}

	private void readHistSec(Element security, HashMap<String, HisSecTS> map,
			String ticker) throws InstantiationException,
			IllegalAccessException {
		HisSecTS tmp = new HisSecTS( ticker );
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {

				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );

					String date = ParseDate.standardFromBbgDate( fieldData.getElementAsDate( BbgNames.DATE ).toString() );
					tmp.addNode( date );
					if (fieldData.hasElement( FieldValue.open )) tmp.setOpen( date,	fieldData.getElementAsFloat64( FieldValue.open ) );
					if (fieldData.hasElement( FieldValue.close )) tmp.setClose( date, fieldData.getElementAsFloat64( FieldValue.close ) );
					if (fieldData.hasElement( FieldValue.high )) tmp.setHigh( date,	fieldData.getElementAsFloat64( FieldValue.high ) );
					if (fieldData.hasElement( FieldValue.low )) tmp.setLow( date, fieldData.getElementAsFloat64( FieldValue.low ) );
					if (fieldData.hasElement( FieldValue.last )) tmp.setLast( date, fieldData.getElementAsFloat64( FieldValue.last ) );
					if (fieldData.hasElement( FieldValue.volume )) tmp.setVolume( date, fieldData.getElementAsFloat64( FieldValue.volume ) );
					if (fieldData.hasElement( FieldValue.pe )) tmp.setPE( date,fieldData.getElementAsFloat64( FieldValue.pe ) );
					if (fieldData.hasElement( FieldValue.sharesOutstanding )) tmp.setSharesOutstanding(
							date,fieldData.getElementAsInt64( FieldValue.sharesOutstanding ) );
					if (fieldData.hasElement( FieldValue.vwap )) tmp.setVwap( date,fieldData.getElementAsFloat64( FieldValue.vwap ) );
					if (fieldData.hasElement( FieldValue.currentQuarterEEPS )) tmp.setCurrentQuarterEEPS(
							date,fieldData.getElementAsFloat64( FieldValue.currentQuarterEEPS ) );
					if (fieldData.hasElement( FieldValue.callIV )) tmp.setCallIV( date, fieldData.getElementAsFloat64( FieldValue.callIV ) );
					if (fieldData.hasElement( FieldValue.putIV )) tmp.setPutIV( date, fieldData.getElementAsFloat64( FieldValue.putIV ) );
				}
			}
		}
		map.put( ticker, tmp );
	}

	private void readHisDiv(Element security, HashMap<String, HisDivTS> map,
			String ticker) throws InstantiationException,
			IllegalAccessException {
		HisDivTS tmp = new HisDivTS( ticker );
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA )
					.getElement( FieldValue.divHis );
			if (fields.numValues() > 0) {
				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );
					String date = ParseDate.standardFromBbgDate( fieldData
							.getElementAsDate( FieldValue.declaredDate )
							.toString() );
					tmp.setDeclareDate( date );

					if (fieldData.hasElement( FieldValue.exDate )) tmp.setExDate( date,
							fieldData.getElementAsString( FieldValue.exDate ) );
					if (fieldData.hasElement( FieldValue.recordDate )) tmp.setRecordDate( date, fieldData
							.getElementAsString( FieldValue.recordDate ) );
					if (fieldData.hasElement( FieldValue.payableDate )) tmp.setPayableDate( date, fieldData
							.getElementAsString( FieldValue.payableDate ) );
					if (fieldData.hasElement( FieldValue.divAmount )) tmp.setAmount( date, fieldData
							.getElementAsFloat64( FieldValue.divAmount ) );
					if (fieldData.hasElement( FieldValue.divFreq )) tmp.setFrequency( date, fieldData
							.getElementAsString( FieldValue.divFreq ) );
					if (fieldData.hasElement( FieldValue.divType )) tmp.setType( date, fieldData
							.getElementAsString( FieldValue.divType ) );
				}
			}
		}
		map.put( ticker, tmp );
	}

	private void printErrorInfo(String leadingStr, Element errorInfo)
			throws Exception {
		System.out.println( leadingStr
				+ errorInfo.getElementAsString( BbgNames.CATEGORY ) + " ("
				+ errorInfo.getElementAsString( BbgNames.MESSAGE ) + ")" );
	}

	@SuppressWarnings("unchecked")
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
		List<String> fields = Arrays.asList( FieldValue.last, FieldValue.open );
		Map<String, HisSecTS> res = (Map<String, HisSecTS>) grabber.getData("HisSec", name, fields, properties );

		for (String n : name) {
			HisSecTS ts = res.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(last):" + ts.getLast( d ) );
				System.out.println( d + "(open):" + ts.getOpen( d ) );
			}
		}

		Map<String, HisDivTS> res2 = (Map<String, HisDivTS>) grabber.getData("HisDiv", name, Arrays.asList( FieldValue.divHis ), null );

		grabber.stop();

		for (String n : name) {
			HisDivTS ts = res2.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(exDate):" + ts.getExDate( d ) );
				System.out.println( d + "(Type):" + ts.getType( d ) );
			}
		}
		System.out.printf( "END" );
	}
}
