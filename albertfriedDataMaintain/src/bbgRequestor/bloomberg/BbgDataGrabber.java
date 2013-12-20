package bbgRequestor.bloomberg;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.DateUtils;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRquestor.bloomberg.beans.HisDivTS;
import bbgRquestor.bloomberg.beans.HisSecTS;
import bbgRquestor.bloomberg.beans.SecurityLookUpResult;
import bbgRquestor.bloomberg.beans.TimeSeries;
import bbgRquestor.bloomberg.beans.TimeSeries.TSType;
import bbgRquestor.bloomberg.beans.TimeUnit;
import bbgRquestor.bloomberg.blpapi.examples.SecurityLookupExample;

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

	@SuppressWarnings("rawtypes")
	public HashMap<String, ? extends TimeSeries> getData(String t, List<String> name, List<String> fields, HashMap<String, Object> properties)
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
	private void setRequestProperties(Request request, HashMap<String, Object> properties) throws Exception {
		if (properties == null) return;
		for (String n : properties.keySet()) {
			Object prop = properties.get( n );
			if (prop instanceof Boolean) {
				request.set( n, ((Boolean) prop).booleanValue() );
			} else if (prop instanceof String) {
				request.set( n, (String) prop );
			} else if (prop instanceof Integer) {
				request.set( n, ((Integer) prop).intValue() );
			} else {
				throw new Exception( "property should be either boolean or string" );
			}
		}
	}

	private void eventLoop(Session session, HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map, TSType type) throws Exception {
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
						if (msg.messageType().equals( "SessionTerminated" ) || msg.messageType().equals( "SessionStartupFailure" )) {
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
	private void processResponseEvent(Event event, HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map, TSType type) throws Exception {
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
	private void extractSingleSecurityValue(Element security, HashMap<String, ? extends TimeSeries<? extends TimeUnit>> map, TSType type) throws Exception {
		String ticker = security.getElementAsString( BbgNames.SECURITY );
		if (security.hasElement( "securityError" )) {
			printErrorInfo( "\tSECURITY FAILED: ", security.getElement( BbgNames.SECURITY_ERROR ) );
			return;
		}

		if (type == TSType.HisDiv) {
			readHisDiv( security, (HashMap<String, HisDivTS>) map, ticker );
		} else if (type == TSType.HisSec) {
			readHistSec( security, (HashMap<String, HisSecTS>) map, ticker );
		} else {
			throw new Exception( "Now only support reading historical security time series" );
		}

		Element fieldExceptions = security.getElement( BbgNames.FIELD_EXCEPTIONS );
		if (fieldExceptions.numValues() > 0) {
			for (int k = 0; k < fieldExceptions.numValues(); ++k) {
				Element fieldException = fieldExceptions.getValueAsElement( k );
				printErrorInfo( fieldException.getElementAsString( BbgNames.FIELD_ID ) + "\t\t", fieldException.getElement( BbgNames.ERROR_INFO ) );
			}
		}
	}

	private void readHistSec(Element security, Map<String, HisSecTS> map, String ticker) throws InstantiationException, IllegalAccessException {
		HisSecTS tmp = new HisSecTS( ticker );
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA );
			if (fields.numValues() > 0) {

				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );

					String date = DateUtils.standardFromBbgDate( fieldData.getElementAsDate( BbgNames.DATE ).toString() );
					tmp.addNode( date );
					if (fieldData.hasElement( Fields.open )) tmp.setOpen( date,	fieldData.getElementAsFloat64( Fields.open ) );
					if (fieldData.hasElement( Fields.close )) tmp.setClose( date, fieldData.getElementAsFloat64( Fields.close ) );
					if (fieldData.hasElement( Fields.high )) tmp.setHigh( date, fieldData.getElementAsFloat64( Fields.high ) );
					if (fieldData.hasElement( Fields.low )) tmp.setLow( date, fieldData.getElementAsFloat64( Fields.low ) );
					if (fieldData.hasElement( Fields.last )) tmp.setLast( date, fieldData.getElementAsFloat64( Fields.last ) );
					if (fieldData.hasElement( Fields.volume )) tmp.setVolume( date, fieldData.getElementAsFloat64( Fields.volume ) );
					if (fieldData.hasElement( Fields.pe )) tmp.setPE( date, fieldData.getElementAsFloat64( Fields.pe ) );
					if (fieldData.hasElement( Fields.sharesOutstanding )) tmp.setSharesOutstanding(date,fieldData.getElementAsInt64( Fields.sharesOutstanding ) );
					if (fieldData.hasElement( Fields.vwap )) tmp.setVwap( date,	fieldData.getElementAsFloat64( Fields.vwap ) );
					if (fieldData.hasElement( Fields.currentQuarterEEPS )) tmp.setCurrentQuarterEEPS(date,fieldData.getElementAsFloat64( Fields.currentQuarterEEPS ) );
					if (fieldData.hasElement( Fields.callIV )) tmp.setCallIV( date, fieldData.getElementAsFloat64( Fields.callIV ) );
					if (fieldData.hasElement( Fields.putIV )) tmp.setPutIV( date,fieldData.getElementAsFloat64( Fields.putIV ) );
				}
			}
		}
		map.put( ticker, tmp );
	}

	private void readHisDiv(Element security, Map<String, HisDivTS> map,
			String ticker) throws InstantiationException,
			IllegalAccessException {
		HisDivTS tmp = new HisDivTS( ticker );
		if (security.hasElement( BbgNames.FIELD_DATA )) {
			Element fields = security.getElement( BbgNames.FIELD_DATA )
					.getElement( Fields.divHis );
			if (fields.numValues() > 0) {
				for (int j = 0; j < fields.numValues(); ++j) {
					Element fieldData = fields.getValueAsElement( j );
					String date = DateUtils.standardFromBbgDate( fieldData
							.getElementAsDate( Fields.declaredDate )
							.toString() );
					tmp.setDeclareDate( date );

					if (fieldData.hasElement( Fields.exDate )) tmp.setExDate( date,
							fieldData.getElementAsString( Fields.exDate ) );
					if (fieldData.hasElement( Fields.recordDate )) tmp.setRecordDate( date, fieldData
							.getElementAsString( Fields.recordDate ) );
					if (fieldData.hasElement( Fields.payableDate )) tmp.setPayableDate( date, fieldData
							.getElementAsString( Fields.payableDate ) );
					if (fieldData.hasElement( Fields.divAmount )) tmp.setAmount( date, fieldData
							.getElementAsFloat64( Fields.divAmount ) );
					if (fieldData.hasElement( Fields.divFreq )) tmp.setFrequency( date, fieldData
							.getElementAsString( Fields.divFreq ) );
					if (fieldData.hasElement( Fields.divType )) tmp.setType( date, fieldData
							.getElementAsString( Fields.divType ) );
				}
			}
		}
		map.put( ticker, tmp );
	}
	
	public List<List<SecurityLookUpResult>> securityLookUp(List<String> args) {
		return (new SecurityLookupExample().run( args.toArray( new String[args.size()] ) ));
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
		HashMap<String, Object> properties = new HashMap<>();
		properties.put( BbgNames.Properties.START, "20110101" );
		properties.put( BbgNames.Properties.END, "20130101" );
		properties.put( BbgNames.Properties.PERIOD, BbgNames.Properties.Period.monthly );
		properties.put( BbgNames.Properties.PERIOD_ADJ, BbgNames.Properties.PeriodAdj.actual );
		properties.put( BbgNames.Properties.RETEID, Boolean.TRUE );
		properties.put( BbgNames.Properties.MAX_POINTS, BbgNames.Properties.maxDataPoints );

		List<String> name = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		List<String> fields = Arrays.asList( Fields.last, Fields.open );
		HashMap<String, HisSecTS> res = (HashMap<String, HisSecTS>) grabber.getData("HisSec", name, fields, properties );

		for (String n : name) {
			HisSecTS ts = res.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(last):" + ts.getLast( d ) );
				System.out.println( d + "(open):" + ts.getOpen( d ) );
			}
		}
		
		HashMap<String, HisDivTS> res2 = (HashMap<String, HisDivTS>) grabber.getData( "HisDiv", name, Arrays.asList( Fields.divHis ), null );

		for (String n : name) {
			HisDivTS ts = res2.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(exDate):" + ts.getExDate( d ) );
				System.out.println( d + "(Type):" + ts.getType( d ) );
			}
		}

		String[] arrgs = {"-r","instrumentListRequest", "-s", "treasury" };
		List<List<SecurityLookUpResult>> res3 = grabber.securityLookUp( Arrays.asList( arrgs ) );
		
		for (List<SecurityLookUpResult> re : res3) {
			for (SecurityLookUpResult rre : re) {
				System.out.println(rre.getType());
				System.out.println(rre.getElement());
			}
		}
		grabber.stop();
		System.out.printf( "END" );
	}
}
