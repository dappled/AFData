package middleware.bbg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import middleware.bbg.beans.DataRequest;
import middleware.bbg.beans.DataRequest.RequestType;
import middleware.bbg.beans.HisResultContainer;
import middleware.bbg.beans.I_ResultContainer;
import middleware.bbg.beans.LookupResultContainer;
import middleware.bbg.beans.RefResultContainer;
import middleware.bbg.beans.SecurityLookupRequest;

import org.apache.activemq.ActiveMQConnectionFactory;

import utils.StringUtils;
import bbgRequestor.bloomberg.BbgNames;
import bbgRequestor.bloomberg.beans.SecurityLookUpResult;
import bbgRequestor.bloomberg.beans.TimeSeries;
import bbgRequestor.bloomberg.beans.TimeUnit;

import java.util.logging.*;

/**
 * This is the server which can publish bbg data request to user defined queue using publishQuest, then return the data
 * returned from clients.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataQuester implements MessageListener {
	/* log */
	private static Logger logger = Logger.getLogger("albertfriedMarketData.middleware.bbg");
	
	/* JMS stuff */
	protected static String							_brokerURL			= "tcp://10.0.0.182:61616";
	protected static ActiveMQConnectionFactory		_factory;
	protected Connection							_connection;
	protected Session								_session;
	protected MessageProducer						_dataRequestSender;
	protected String								_requestQueueName	= "BbgDataRequest";

	/* connection stuff */
	private MessageProducer							_connectionRequestSender;
	private int										_connected;
	private String									_id;											// id for this quester

	/* replying data */
	private HashMap<String, I_ResultContainer<?>>	_results;		// HashMap<correlationId, ResultContainer>, correlationId indicates which quest is this

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Server.
	 * @throws Exception
	 */
	public BbgDataQuester(final String serverName) throws Exception {
		_id = createRandomString();

		/* JMS stuff */
		_factory = new ActiveMQConnectionFactory( _brokerURL );
		// I use async sends for performance reason
		BbgDataQuester._factory.setUseAsyncSend( true );
		_connection = BbgDataQuester._factory.createConnection();
		_session = _connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
		_connection.start();
		_dataRequestSender = _session.createProducer( _session.createQueue( _requestQueueName ) );

		/* connection stuff */
		_connectionRequestSender = _session.createProducer( _session.createQueue( "Connection" ) );
		_connectionRequestSender.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
		this.connectResponser();

		/* misc */
		_results = new HashMap<>();
	}

	/***********************************************************************
	 * Destructor
	 * @throws Exception
	 ***********************************************************************/
	public void close() throws Exception {
		if (_connection != null) {
			_connection.close();
		}
	}

	/***********************************************************************
	 * Quest Methods
	 ***********************************************************************/
	/**
	 * Publish a bbg data request on historical data
	 * @throws Exception
	 */
	public void publishHisQuest(RequestType type, List<String> names, Set<String> fields, HashMap<String, Object> properties, HisResultContainer container)
			throws Exception {
		Destination resultQueue = _session.createTemporaryQueue();
		final MessageConsumer dataResultReceiver = _session.createConsumer( resultQueue );
		dataResultReceiver.setMessageListener( this );

		String msgId = createRandomString();
		_results.put( msgId, container );

		// for historical quest, send by every 10 stocks
		Set<String> s = new HashSet<>();
		int i = 0;
		for (int q = 0; q < names.size(); q++) {
			String stock = names.get( q );
			// send every 10 stocks
			if (i++ < 10) {
				s.add( stock );
				if (q < names.size() - 1) continue;
			}
			DataRequest request = new DataRequest( type, s, fields, properties );

			try {
				final ObjectMessage msg = _session.createObjectMessage( request );
				msg.setJMSType( "Historical" );
				msg.setJMSReplyTo( resultQueue );
				msg.setJMSCorrelationID( msgId );
				_dataRequestSender.send( msg );
			} catch (final JMSException e) {
				e.printStackTrace();
			}

			s = new HashSet<>();
			s.add( stock );
			i = 1;
		}
	}

	/**
	 * Publish quest asking about reference type data
	 * @param queueName
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void publishRefQuest(RequestType type, List<String> names, Set<String> fields, RefResultContainer container) throws Exception {
		Destination resultQueue = _session.createTemporaryQueue();
		final MessageConsumer dataResultReceiver = _session.createConsumer( resultQueue );
		dataResultReceiver.setMessageListener( this );

		String msgId = createRandomString();
		_results.put( msgId, container );

		// for reference quest, send by every 100 stocks
		Set<String> s = new HashSet<>();
		int i = 0;
		for (int q = 0; q < names.size(); q++) {
			String stock = names.get( q );
			// send every 100 stocks
			if (i++ < 100) {	
				s.add( stock );
				if (q < names.size() - 1 ) continue;
			} 
			
			DataRequest request = new DataRequest( type, s, fields, null );

			try {
				final ObjectMessage msg = _session.createObjectMessage( request );
				msg.setJMSType( "Reference" );
				msg.setJMSReplyTo( resultQueue ); // set return queue
				msg.setJMSCorrelationID( msgId );
				_dataRequestSender.send( msg );
			} catch (final JMSException e) {
				e.printStackTrace();
			}

			s = new HashSet<>();
			s.add( stock );
			i = 1;
		}
	}

	/**
	 * Publish look up quest
	 * @param queueName
	 * @param args
	 * @return
	 * @throws JMSException
	 * @throws InterruptedException
	 */
	public void publishLookupQuest(String[] args, LookupResultContainer container) throws JMSException, InterruptedException {
		Destination resultQueue = _session.createTemporaryQueue();
		final MessageConsumer dataResultReceiver = _session.createConsumer( resultQueue );
		dataResultReceiver.setMessageListener( this );

		String msgId = createRandomString();
		_results.put( msgId, container );

		SecurityLookupRequest request = new SecurityLookupRequest( args );
		// send the message
		try {
			final ObjectMessage msg = _session.createObjectMessage( request );
			msg.setJMSType( "Lookup" );
			msg.setJMSReplyTo( resultQueue );
			msg.setJMSCorrelationID( msgId );
			_dataRequestSender.send( msg );
		} catch (final JMSException e) {
			e.printStackTrace();
		}
	}

	public void publishTestQuest() throws JMSException, InterruptedException {
		Destination resultQueue = _session.createTemporaryQueue();
		final MessageConsumer dataResultReceiver = _session.createConsumer( resultQueue );
		dataResultReceiver.setMessageListener( this );
		final ObjectMessage msg = _session.createObjectMessage();
		msg.setJMSType( "Test" );
		msg.setJMSReplyTo( resultQueue ); // set return queue
		_dataRequestSender.send( msg );
	}

	private String createRandomString() {
		Random random = new Random( System.currentTimeMillis() );
		long randomLong = random.nextLong();
		return Long.toHexString( randomLong );
	}

	/** {@link MessageListener} methods */
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(final Message msg) {
		try {
			final ObjectMessage message = (ObjectMessage) msg;
			String messageId = msg.getJMSCorrelationID();
			logger.info( "Receive a msg of type " + message.getJMSType() + " with id " + messageId );

			switch (message.getJMSType()) {
				case "Historical":
					((HisResultContainer) _results.get( messageId ))
							.receiveSolution( (HashMap<String, ? extends TimeSeries<? extends TimeUnit>>) message.getObject() );
					break;
				case "Reference":
					((RefResultContainer) _results.get( messageId )).receiveSolution( (ArrayList<? extends TimeUnit>) message.getObject() );
					break;
				case "Lookup":
					((LookupResultContainer) _results.get( messageId )).receiveSolution( (List<SecurityLookUpResult>) message.getObject() );
					break;
				case "Error":
					System.err.println( "Error happens on remote side: " + message.getStringProperty( "Error" ) );
					_results.get( messageId ).receiveError( message.getStringProperty( "Error" ) );
					break;
				case "Test":
					logger.info( "Test finished" );
					break;
				default:
					System.err.println( "Incorrect message type for " + msg);
					break;
			}
		} catch (final Exception rte) {
			rte.printStackTrace();
		}
	}

	/***********************************************************************
	 * Connection methods
	 ***********************************************************************/
	/** Get connection status, true if connected, false o.w. */
	public boolean connected() {
		return _connected > 0;
	}

	/**
	 * Connect to responser (rn's pc)
	 * @throws JMSException
	 * @throws InterruptedException
	 */
	public void connectResponser() throws JMSException, InterruptedException {
		logger.info( "Waiting for server to accept connection" );
		_connected = 0;
		// set up publisher and listener
		Destination connectionResultQueue = _session.createTemporaryQueue();
		final MessageConsumer connectionResultReceiver = _session.createConsumer( connectionResultQueue );
		connectionResultReceiver.setMessageListener( new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				try {
					if (!msg.getJMSCorrelationID().equals( _id )) return; // incorrect relying msg;
					if (msg.getBooleanProperty( "Connection" )) {
						logger.info( "Connected to responser, ready to send request." );
						_connected = 1;
					} else {
						System.err.println( "Server decliend connection, please try later" );
						_connected = -1;
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		} );

		try {
			final Message msg = _session.createMessage();
			msg.setJMSType( "Connection" );
			msg.setJMSReplyTo( connectionResultQueue );
			msg.setJMSCorrelationID( _id );
			_connectionRequestSender.send( msg );
			logger.info( "Connection request sent" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (_connected == 0) {
			Thread.sleep( 1000 ); // wait till server replies
		}
	}

	/** Indicate responser that this connection will be closed */
	public void publishCloseQuest() throws Exception {
		final Message msg = _session.createMessage();
		msg.setJMSType( "Close" );
		msg.setJMSCorrelationID( _id );
		_connectionRequestSender.send( msg );

		close();
		System.out.println("Closed. Bye");
	}

	/***********************************************************************
	 * Command Line methods
	 ***********************************************************************/
	/**
	 * Publish quest according to command line input, return 0 if good, 1 if error in remote side/request, -1 if want to close current connection
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int startCommandLine(String queueName, String line) throws Exception {
		try {
			int i = 1;
			String[] split = StringUtils.arrayTrimQuotes( StringUtils.splitWIthQuotes( line ) );
			List<String> names;
			Set<String> fields;
			String type;
			I_ResultContainer<?> container;
			switch (split[ 0 ]) {
				case "historical":
					type = split[ i++ ];
					names = Arrays.asList( StringUtils.arrayTrim( split[ i++ ].replace( "\"", "" ).split( "," ) ) ) ;
					fields = new HashSet<>( Arrays.asList( StringUtils.arrayTrim( StringUtils.arrayToUpper( split[ i++ ].replace( "\"", "" ).split( "," ) ) ) ) );
					container = new HisResultContainer( type, new HashSet<>(names) );
					this.publishHisQuest( RequestType.getType( type ), names, fields, parseProperties( split[ i++ ].replace( "\"", "" ).split( "," ) ),
							(HisResultContainer) container );
					while (container.isFinished() == 0) {
						Thread.sleep( 3000 );
					}
					if (container.isFinished() > 0) {
						Map<String, ? extends TimeSeries> res = ((HisResultContainer) container).getSolution();
						for (String n : names) {
							logger.info( "Name: " + n );
							TimeSeries ts = res.get( n );
							Set<String> set = ts.getDates();
							for (String d : set) {
								ts.printPiece( d );
							}
						}
					}
					break;
				case "reference":
					type = split[ i++ ];
					names = Arrays.asList( StringUtils.arrayTrim( split[ i++ ].replace( "\"", "" ).split( "," ) )) ;
					fields = new HashSet<>( Arrays.asList( StringUtils.arrayTrim( StringUtils.arrayToUpper( split[ i++ ].replace( "\"", "" ).split( "," ) ) ) ) );
					container = new RefResultContainer( type, new HashSet<>(names) );
					this.publishRefQuest( RequestType.getType( type ), names, fields, (RefResultContainer) container );
					while (container.isFinished() == 0) {
						Thread.sleep( 3000 );
					}
					if (container.isFinished() > 0) {
						List<? extends TimeUnit> res2 = ((RefResultContainer) container).getSolution();
						for (TimeUnit tu : res2) {
							tu.printPiece();
						}
					}
					break;
				case "lookup":
					container = new LookupResultContainer();
					this.publishLookupQuest( Arrays.copyOfRange( split, 1, split.length ), (LookupResultContainer) container );
					while (container.isFinished() == 0) {
						Thread.sleep( 3000 );
					}
					if (container.isFinished() > 0) {
						List<SecurityLookUpResult> res3 = ((LookupResultContainer) container).getSolution();
						for (SecurityLookUpResult re : res3) {
							re.printPiece();
						}
					}
					break;
				case "close":
					this.publishCloseQuest();
					return -1;
				case "test":
					logger.info( "Test start" );
					this.publishTestQuest();
					break;
				default:
					this.printUsage();
					break;
			}
		} catch (Exception e) {
			System.err.println( "Error happened on server, will close client: " + e );
			this.publishCloseQuest();
			return -1;
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

	private void printUsage() {
		System.out.println( "Usage:" );
		System.out.println( "Bloomberg data grabber." );
		System.out
				.println( "historical type(sec or div) \"names seperated by , no duplicates or error might happen\" \"fields seperated by ,\" \"properties pairs(name:prop) seperated by ,\" or" );
		System.out.println( "reference type(sec or div) \"names seperated by , no duplicates or error might happen\" \"fields seperated by ,\" or" );

		/** {@link bbgRquestor.bloomberg.blpapi.examples.SecurityLookupExample} */
		System.out.println( "lookup args(see bbgRquestor.bloomberg.blpapi.examples.SecurityLookupExample) or" );

		System.out.println( "close to close current connection to the responser" );
	}

	public static void main(String[] args) throws Exception {
		if (!((args.length == 0) || (args.length == 1 && args[0].equals("-t")))) throw new Exception("Takes at most 1 argument(-t to enable logging)");
		if (args.length == 1) logger.setLevel(Level.FINE);
		else logger.setLevel(Level.OFF);
		// initialize jms
		final BbgDataQuester quester = new BbgDataQuester( null );
		final String queueName = "BbgData";
		if (!quester.connected()) return;

		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		while (true) {
			System.out.println( "Ready to listen, please enter" );
			String request = br.readLine();

			int re = quester.startCommandLine( queueName, request );
			if (re < 0) { // current connection closed
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
