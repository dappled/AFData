package middleware.bbg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import middleware.bbg.beans.HistoricalRequest;
import middleware.bbg.beans.SecurityLoopUpRequest;

import org.apache.activemq.ActiveMQConnectionFactory;

import bbgRequestor.bloomberg.BbgNames;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRquestor.bloomberg.beans.HisDivTS;
import bbgRquestor.bloomberg.beans.HisSecTS;
import bbgRquestor.bloomberg.beans.ReferenceAbstarct;
import bbgRquestor.bloomberg.beans.SecurityLookUpResult;
import bbgRquestor.bloomberg.beans.TimeSeries;
import bbgRquestor.bloomberg.beans.TimeSeries.TSType;

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
	Map<String, ReferenceAbstarct>				_refData;
	List<List<SecurityLookUpResult>>			_lookupResult;
	boolean										_received;

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
	public Map<String, ? extends TimeSeries> publishHisQuest(String queueName, TSType type, List<String> names, List<String> fields,
			HashMap<String, Object> properties) throws Exception {
		// set up publisher and listener
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );
		final MessageConsumer dataResultReceiver = _session.createConsumer( _resultQueue );
		dataResultReceiver.setMessageListener( this );

		String t = (type == TSType.HisDiv) ? "HisDiv" : "HisSec";
		HistoricalRequest request = new HistoricalRequest( t, names, fields, properties );
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
	public List<List<SecurityLookUpResult>> publishLookUpQuest(String queueName, String[] args) throws JMSException, InterruptedException {
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
			System.out.println( "Msg Received" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (!_received) {
			Thread.sleep( 1000 );
		}

		return _lookupResult;
	}

	// ask clients listening to this queue to kill themselves
	public void publishSuicideQuest(String queueName) throws JMSException {
		_dataRequestSender = _session.createProducer( _session.createQueue( queueName ) );
		final Message msg = _session.createMessage();
		_dataRequestSender.send( msg );
	}

	/***********************************************************************
	 * {@link MessageListener} methods
	 ***********************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onMessage(final Message msg) {
		try {
			final ObjectMessage message = (ObjectMessage) msg;
			System.out.println( "receive a msg" );
			switch (message.getJMSType()) {
				case "Historical":
					_hisData = (Map<String, ? extends TimeSeries>) message.getObject();
					break;
				case "Reference":
					_refData = (Map<String, ReferenceAbstarct>) message.getObject();
					break;
				case "Lookup":
					_lookupResult = (List<List<SecurityLookUpResult>>) message.getObject();
					break;
				case "Error":
					System.err.println( "Error happens on remote side: " + message.getStringProperty( "Error" ) );
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

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		// initialize server and client
		final BbgDataServer server = new BbgDataServer( null );

		// request parameters
		HashMap<String, Object> properties = new HashMap<>();
		properties.put( BbgNames.Properties.START, "20110101" );
		properties.put( BbgNames.Properties.END, "20130101" );
		properties.put( BbgNames.Properties.PERIOD, BbgNames.Properties.Period.monthly );
		properties.put( BbgNames.Properties.PERIOD_ADJ, BbgNames.Properties.PeriodAdj.actual );
		properties.put( BbgNames.Properties.RETEID, Boolean.TRUE );
		properties.put( BbgNames.Properties.MAX_POINTS, BbgNames.Properties.maxDataPoints );

		List<String> names = Arrays.asList( "MSFT US EQUITY", "GS US EQUITY" );
		List<String> fields = Arrays.asList( Fields.last, Fields.open );

		// publish quest on security
		String queueName = "BbgData";
		HashMap<String, HisSecTS> res = (HashMap<String, HisSecTS>) server.publishHisQuest( queueName, TSType.HisSec, names, fields, properties );

		for (String n : names) {
			HisSecTS ts = res.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(last):" + ts.getLast( d ) );
				System.out.println( d + "(open):" + ts.getOpen( d ) );
			}
		}

		// publish quest on ref
		HashMap<String, HisDivTS> res2 = (HashMap<String, HisDivTS>) server.publishHisQuest( queueName, TSType.HisDiv, names, Arrays.asList( Fields.divHis ),
				null );

		for (String n : names) {
			HisDivTS ts = res2.get( n );
			for (String d : ts.getDates()) {
				System.out.println( d + "(exDate):" + ts.getExDate( d ) );
				System.out.println( d + "(Type):" + ts.getType( d ) );
			}
		}

		// publish quest on lookup
		//String[] arrgs = { "-r", "instrumentListRequest", "-s", "GS" };
		String[] arrgs = { "-r", "govtListRequest", "-s", "treasury" };
		List<List<SecurityLookUpResult>> res3 = server.publishLookUpQuest( queueName, arrgs );
		
		for (List<SecurityLookUpResult> re : res3) {
			for (SecurityLookUpResult rre : re) {
				System.out.println( rre.getType() );
				System.out.println( rre.getElement() );
			}
		}
		
		server.publishSuicideQuest(queueName);

		server.close();
	}

}
