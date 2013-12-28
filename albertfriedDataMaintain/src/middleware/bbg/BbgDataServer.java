package middleware.bbg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import middleware.bbg.beans.DataRequest;
import middleware.bbg.beans.SecurityLoopUpRequest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import utils.StringUtils;
import bbgRequestor.bloomberg.BbgNames;
import bbgRquestor.bloomberg.beans.SecurityLookUpResult;
import bbgRquestor.bloomberg.beans.SecurityTimeUnit;
import bbgRquestor.bloomberg.beans.TimeSeries;

/**
 * This is the server which can publish bbg data request to user defined queue using publishQuest, then return the data
 * returned from clients.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataServer implements MessageListener {
	protected static String						_brokerURL	= "tcp://10.0.0.155:61616";
	protected static ActiveMQConnectionFactory	_factory;
	protected Connection						_connection;
	protected Session							_session;
	protected MessageProducer					_dataRequestSender;
	protected Destination						_resultQueue;

	/* returning data */
	@SuppressWarnings("rawtypes")
	Map<String, ? extends TimeSeries>			_hisData;
	List<SecurityTimeUnit>						_refData;
	List<SecurityLookUpResult>					_lookupResult;
	boolean										_received;
	boolean										_error		= false;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Server.
	 * @throws JMSException when connection error happens
	 */
	public BbgDataServer(final String serverName) throws JMSException {
		_factory = new ActiveMQConnectionFactory( _brokerURL );
		// I use async sends for performance reason
		BbgDataServer._factory.setUseAsyncSend( true );
		_connection = BbgDataServer._factory.createConnection();
		_session = _connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
		_dataRequestSender = _session.createProducer( null );

		_connection.start();
	}

	/***********************************************************************
	 * Destructor
	 ***********************************************************************/
	public void close() throws JMSException {
		if (_connection != null) {
			_connection.close();
		}
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/**
	 * Publish a bbg data request on historical data
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, ? extends TimeSeries> publishHisQuest(String queueName, String type, List<String> names, List<String> fields,
			HashMap<String, Object> properties) throws Exception {
		// set up publisher and listener
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );
		final MessageConsumer dataResultReceiver = _session.createConsumer( _resultQueue );
		dataResultReceiver.setMessageListener( this );

		DataRequest request = new DataRequest( type, names, fields, properties );
		// send the message
		try {
			_received = false;
			final ObjectMessage msg = _session.createObjectMessage( request );
			msg.setJMSType( "Historical" );
			msg.setJMSReplyTo( _resultQueue ); // set return queue
			// send msg with persistent mode, this time we don't really care about speed but accuracy
			_dataRequestSender.send( msg );
			System.out.println( "Msg sent" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (!_received) {
			Thread.sleep( 1000 );
		}

		return _hisData;
	}

	/**
	 * @param queueName
	 * @param args
	 * @return
	 * @throws JMSException
	 * @throws InterruptedException
	 */
	public List<SecurityLookUpResult> publishLookupQuest(String queueName, String[] args) throws JMSException, InterruptedException {
		// set up publisher and listener
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );
		final MessageConsumer dataResultReceiver = _session.createConsumer( _resultQueue );
		dataResultReceiver.setMessageListener( this );

		SecurityLoopUpRequest request = new SecurityLoopUpRequest( args );
		// send the message
		try {
			_received = false;
			final ObjectMessage msg = _session.createObjectMessage( request );
			msg.setJMSType( "Lookup" );
			msg.setJMSReplyTo( _resultQueue ); // set return queue
			// send msg with persistent mode, this time we don't really care about speed but accuracy
			_dataRequestSender.send( msg );
			System.out.println( "Msg Sent" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (!_received) {
			Thread.sleep( 1000 );
		}

		return _lookupResult;
	}

	/**
	 * @param queueName
	 * @param args
	 * @return
	 * @throws JMSException
	 * @throws InterruptedException
	 */
	public List<SecurityTimeUnit> publishRefQuest(String queueName, List<String> names, List<String> fields) throws JMSException, InterruptedException {
		// set up publisher and listener
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );

		final MessageConsumer dataResultReceiver = _session.createConsumer( _resultQueue );
		dataResultReceiver.setMessageListener( this );
		DataRequest request = new DataRequest( "ref", names, fields, null );
		// send the message
		try {
			_received = false;
			final ObjectMessage msg = _session.createObjectMessage( request );
			msg.setJMSType( "Reference" );
			msg.setJMSReplyTo( _resultQueue ); // set return queue
			// send msg with persistent mode, this time we don't really care about speed but accuracy
			_dataRequestSender.send( msg );
			System.out.println( "Msg sent" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (!_received) {
			Thread.sleep( 1000 );
		}

		return _refData;
	}

	// ask clients listening to this queue to kill themselves
	public void publishSuicideQuest(String queueName) throws JMSException {
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		final Message msg = _session.createMessage();
		_dataRequestSender.send( msg );
		this.close();
	}

	public void publishTestQuest(String queueName) throws JMSException, InterruptedException {
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );

		final MessageConsumer dataResultReceiver = _session.createConsumer( _resultQueue );
		dataResultReceiver.setMessageListener( this );
		_received = false;
		final ObjectMessage msg = _session.createObjectMessage();
		msg.setJMSType( "Test" );
		msg.setJMSReplyTo( _resultQueue ); // set return queue
		_dataRequestSender.send( msg );
		System.out.println( "Msg sent" );
		while (!_received) {
			Thread.sleep( 1000 );
		}
	}

	/**
	 * publish quest according to command line import, return 0 if good, 1 if error in remote side/request, -1 if
	 * finished job
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int startCommandLine(String queueName, String line) throws Exception {
		_error = false;
		int i = 1;
		String[] split = StringUtils.arrayTrimQuotes( StringUtils.splitWIthQuotes( line ) );
		List<String> names;
		List<String> fields;
		switch (split[ 0 ]) {
			case "historical":
				String type = split[ i++ ];
				names = Arrays.asList( StringUtils.arrayTrim( split[ i++ ].replace( "\"", "" ).split( "," ) ) );
				fields = Arrays.asList( StringUtils.arrayTrim( StringUtils.arrayToUpper( split[ i++ ].replace( "\"", "" ).split( "," ) ) ) );
				Map<String, ? extends TimeSeries> res = this.publishHisQuest( queueName, type, names, fields,
						parseProperties( split[ i++ ].replace( "\"", "" ).split( "," ) ) );
				if (_error == true) {// error in response
					return 1;
				}
				for (String n : names) {
					System.out.println( "Name: " + n );
					TimeSeries ts = res.get( n );
					Set<String> set = ts.getDates();
					for (String d : set) {
						ts.printPiece( d );
					}
				}
				break;
			case "lookup":
				List<SecurityLookUpResult> res3 = this.publishLookupQuest( queueName, Arrays.copyOfRange( split, 1, split.length ) );
				if (_error == true) { // error in response
					return 1;
				}
				for (SecurityLookUpResult re : res3) {
					re.printPiece();
				}
				break;
			case "reference":
				names = Arrays.asList( StringUtils.arrayTrim( split[ i++ ].replace( "\"", "" ).split( "," ) ) );
				fields = Arrays.asList( StringUtils.arrayTrim( StringUtils.arrayToUpper( split[ i++ ].replace( "\"", "" ).split( "," ) ) ) );
				List<SecurityTimeUnit> res2 = this.publishRefQuest( queueName, names, fields );
				if (_error == true) {// error in response
					return 1;
				}
				for (SecurityTimeUnit re : res2) {
					re.printPiece();
				}
				break;
			case "close":
				this.publishSuicideQuest( queueName );
				this.close();
				return -1;
			case "test":
				System.out.println( "Test start" );
				this.publishTestQuest( queueName );
				break;
			default:
				this.printUsage();
				break;
		}
		return 0;
	}

	/** get properties from command line */
	private HashMap<String, Object> parseProperties(String[] split) {
		HashMap<String, Object> properties = new HashMap<>();
		for (int i = 0; i < split.length; i++) {
			String[] pair = split[ i ].split( ":" );
			switch (pair[ 0 ]) {
				case "start":
					properties.put( BbgNames.Properties.START, pair[ 1 ] );
					break;
				case "end":
					properties.put( BbgNames.Properties.END, pair[ 1 ] );
					break;
				case "period":
					properties.put( BbgNames.Properties.PERIOD, pair[ 1 ].toUpperCase() );
					break;
				case "periodadj":
					properties.put( BbgNames.Properties.PERIOD_ADJ, pair[ 1 ].toUpperCase() );
					break;
				case "reteid":
					properties.put( BbgNames.Properties.RETEID, Boolean.parseBoolean( pair[ 1 ] ) );
					break;
				case "maxpoint":
					properties.put( BbgNames.Properties.MAX_POINTS, Integer.parseInt( pair[ 1 ] ) );
					break;
				default:
					break;
			}
		}
		return properties;
	}

	/***********************************************************************
	 * {@link MessageListener} methods
	 ***********************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onMessage(final Message msg) {
		try {
			final ObjectMessage message = (ObjectMessage) msg;
			System.out.println( "receive a msg of type " + message.getJMSType() );
			switch (message.getJMSType()) {
				case "Historical":
					_hisData = (Map<String, ? extends TimeSeries>) message.getObject();
					break;
				case "Reference":
					_refData = (List<SecurityTimeUnit>) message.getObject();
					break;
				case "Lookup":
					_lookupResult = (List<SecurityLookUpResult>) message.getObject();
					break;
				case "Error":
					System.err.println( "Error happens on remote side: " + message.getStringProperty( "Error" ) );
					_error = true;
					break;
				case "Test":
					System.out.println( "Test finished" );
					break;
				default:
					System.err.println( "Incorrect message type for " + msg );
					break;
			}
			_received = true;
		} catch (final Exception rte) {
			rte.printStackTrace();
		}
	}

	private void printUsage() {
		System.out.println( "Usage:" );
		System.out.println( "Bloomberg data grabber." );
		System.out
				.println( "historical type(sec or div) \"names seperated by ,\" \"fields seperated by ,\" \"properties pairs(name:prop) seperated by ,\" or" );
		System.out.println( "reference \"names seperated by ,\" \"fields seperated by ,\" or" );

		System.out.println( "lookup args(see bbgRquestor.bloomberg.blpapi.examples.SecurityLookupExample) or" );
		/** {@link bbgRquestor.bloomberg.blpapi.examples.SecurityLookupExample} */
		System.out.println( "close" );
	}

	public static void main(String[] args) throws Exception {
		// start broker
		BrokerService broker = new BrokerService();
		// configure the broker
		broker.setBrokerName( "Dong" );
		broker.addConnector( "tcp://10.0.0.155:61616" );

		broker.start();

		// initialize server and client
		final BbgDataServer server = new BbgDataServer( null );
		final String queueName = "BbgData";
		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		while (true) {
			System.out.println( "Ready to listen, please enter" );
			String request = br.readLine();

			if (server.startCommandLine( queueName, request ) < 0) {
				System.out.println( "Gonna stop" );
				Thread.sleep( 1000 ); // wait client to sleep
				broker.stop();
				return;
			}
		}
		/* // request parameters
		 * HashMap<String, Object> properties = new HashMap<>();
		 * properties.put( BbgNames.Properties.START, "20110101" );
		 * properties.put( BbgNames.Properties.END, "20130101" );
		 * properties.put( BbgNames.Properties.PERIOD, BbgNames.Properties.Period.monthly );
		 * properties.put( BbgNames.Properties.PERIOD_ADJ, BbgNames.Properties.PeriodAdj.actual );
		 * properties.put( BbgNames.Properties.RETEID, Boolean.TRUE );
		 * properties.put( BbgNames.Properties.MAX_POINTS, BbgNames.Properties.maxDataPoints );
		 * List<String> names = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		 * List<String> fields = Arrays.asList( Fields.last, Fields.open );
		 * // publish quest on security
		 * String queueName = "BbgData";
		 * HashMap<String, HisSecTS> res = (HashMap<String, HisSecTS>) server.publishHisQuest( queueName, "HisSec",
		 * names, fields, properties );
		 * for (String n : names) {
		 * HisSecTS ts = res.get( n );
		 * for (String d : ts.getDates()) {
		 * System.out.println( d + "(last):" + ts.getLast( d ) );
		 * System.out.println( d + "(open):" + ts.getOpen( d ) );
		 * }
		 * }
		 * // publish quest on ref
		 * HashMap<String, HisDivTS> res2 = (HashMap<String, HisDivTS>) server.publishHisQuest( queueName, "HisDiv",
		 * names, Arrays.asList( Fields.divHis ),
		 * null );
		 * for (String n : names) {
		 * HisDivTS ts = res2.get( n );
		 * for (String d : ts.getDates()) {
		 * System.out.println( d + "(exDate):" + ts.getExDate( d ) );
		 * System.out.println( d + "(Type):" + ts.getType( d ) );
		 * }
		 * }
		 * // publish quest on lookup
		 * // String[] arrgs = { "-r", "instrumentListRequest", "-s", "GS" };
		 * String[] arrgs = { "-r", "govtListRequest", "-s", "treasury" };
		 * List<List<SecurityLookUpResult>> res3 = server.publishLookUpQuest( queueName, arrgs );
		 * for (List<SecurityLookUpResult> re : res3) {
		 * for (SecurityLookUpResult rre : re) {
		 * System.out.println( rre.getType() );
		 * System.out.println( rre.getElement() );
		 * }
		 * }
		 * server.publishSuicideQuest( queueName ); */

	}
}
