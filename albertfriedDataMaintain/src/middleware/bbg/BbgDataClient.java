package middleware.bbg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

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
import middleware.bbg.beans.SecurityLoopUpRequest;

import org.apache.activemq.ActiveMQConnectionFactory;

import bbgRequestor.bloomberg.BbgDataGrabber;
import bbgRquestor.bloomberg.blpapi.examples.CopyOfRefDataExample;

/**
 * This is the client which listen to a queue which will pop bbg data request using listenTo, and will
 * return the bbg data it get.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataClient {
	// I use async sends for performance reason
	protected static String						_brokerURL	= "tcp://10.0.0.155:61616";
	protected static ActiveMQConnectionFactory	_factory;
	protected Connection						_connection;
	protected Session							_session;
	protected MessageProducer					_dataResultSender;
	private boolean								_isFinished	= false;
	private int									_connected;

	/* bbg stuff */
	BbgDataGrabber								_grabber;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Client.
	 * @throws Exception
	 */
	public BbgDataClient() throws Exception {
		BbgDataClient._factory = new ActiveMQConnectionFactory( BbgDataClient._brokerURL );
		// I use async sends for performance reason
		BbgDataClient._factory.setUseAsyncSend( true );
		_connection = BbgDataClient._factory.createConnection();
		_session = _connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
		_dataResultSender = _session.createProducer( null );
		_connection.start();

		_grabber = new BbgDataGrabber();
	}

	/***********************************************************************
	 * Destructor
	 * @throws Exception
	 ***********************************************************************/
	public void close() throws Exception {
		if (_grabber != null) {
			_grabber.stop();
		}
		if (_connection != null) {
			_connection.close();
		}
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/**
	 * Listen to a queue, get bbg data request quest if exists and sends back result from bbg.
	 * @param queueName the name of the queue
	 * @throws Exception when bad things happened
	 */
	public void listenTo(final String queueName) throws Exception {
		// set up listener
		final Destination destination = _session.createQueue( queueName );
		final MessageConsumer messageConsumer = _session.createConsumer( destination );
		messageConsumer.setMessageListener( new MessageListener() {
			@Override
			public void onMessage(final Message msg) {
				try {
					System.out.println( "Receive a request of type " + msg.getJMSType() );
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
				if (msg instanceof ObjectMessage) {
					try {
						ObjectMessage re = _session.createObjectMessage();
						re.setJMSType( msg.getJMSType() );
						DataRequest request;
						List<String> names, fields;
						String t;
						try {
							switch (msg.getJMSType()) {
								case "Historical":
									request = (DataRequest) ((ObjectMessage) msg).getObject();
									t = request.getType();
									names = request.getNames();
									fields = request.getFields();
									HashMap<String, Object> properties = (HashMap<String, Object>) request.getProperties();
									//System.out.println( "Start getting data from bbg" );
									re.setObject( _grabber.getTSData( t, names, fields, properties ) );
									//System.out.println( "Finished getting data from bbg" );
									break;
								case "Reference":
									request = (DataRequest) ((ObjectMessage) msg).getObject();
									t = request.getType();
									names = request.getNames();
									fields = request.getFields();
									//System.out.println( "Start getting data from bbg" );
									re.setObject( (Serializable) _grabber.getRefData( t, names, fields ) );
									//System.out.println( "Finished getting data from bbg" );
									break;
								case "Lookup":
									SecurityLoopUpRequest requestLookUp = (SecurityLoopUpRequest) ((ObjectMessage) msg).getObject();
									//System.out.println( "Start getting data from bbg" );
									re.setObject( (Serializable) _grabber.securityLookUp( requestLookUp.getArgs() ) );
									//System.out.println( "Finished getting data from bbg" );
									break;
								case "Test":
									System.out.println( "Strat testing" );
									CopyOfRefDataExample.main( null );
									System.out.println( "Finished testing" );
									break;
								default:
									// incorrect msg type
									break;
							}
						} catch (Exception e) {
							// on error when get bbg data, send the error back
							System.err.println( "Error happens, will send error msg back" );
							e.printStackTrace();
							re.setJMSType( "Error" );
							re.setStringProperty( "Error", e.getMessage() );
						}
						/* send back result */
						// send msg with Persistent mode
						//System.out.println( "Start replying" );
						_dataResultSender.send( msg.getJMSReplyTo(), re );
						//System.out.println( "Finished replying" );
					} catch (final Exception rte) {
						rte.printStackTrace();
					}
				}
				else {
					// if poison pill
					_isFinished = true;
					System.out.println( "Received poison pill, Im gonna kill myself. Thx for using me. cya" );
				}
			}

		} );
	}

	public boolean isFinished() {
		return _isFinished;
	}

	/**
	 * connect to 'server'(rn's pc)
	 * @return true if successfully connected, false if not
	 * @throws JMSException
	 * @throws InterruptedException
	 */
	public boolean connectServer() throws JMSException, InterruptedException {
		System.out.println( "Waiting for server to accept connection" );
		_connected = 0;
		// set up publisher and listener
		MessageProducer connectionRequestSender = _session.createProducer( _session.createQueue( "Connection" ) );
		connectionRequestSender.setDeliveryMode( DeliveryMode.NON_PERSISTENT );
		Destination connectionResultQueue = _session.createTemporaryQueue();
		final MessageConsumer connectionResultReceiver = _session.createConsumer( connectionResultQueue );
		connectionResultReceiver.setMessageListener( new MessageListener() {
			@Override
			public void onMessage(Message msg) {
				try {
					if (msg.getBooleanProperty( "Connection" )) {
						System.out.println( "Connected to server, ready to receive request." );
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

		// send the message
		try {
			final Message msg = _session.createMessage();
			msg.setJMSType( "Connection" );
			msg.setJMSReplyTo( connectionResultQueue ); // set return queue
			// send msg with persistent mode, this time we don't really care about speed but accuracy
			connectionRequestSender.send( msg );
			//System.out.println( "Connection request sent" );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (_connected == 0) {
			Thread.sleep( 1000 ); // wait till server replies
		}

		if (_connected == -1) { // server busy
			return false;
		} else { // accepted connection
			return true;
		}
	}

	public static void main(String[] args) throws Exception {
		final BbgDataClient client = new BbgDataClient();

		if (!client.connectServer()) { return; }

		// client listen to this queue
		String queueName = "BbgData";
		client.listenTo( queueName );

		while (!client.isFinished()) {
			Thread.sleep( 3000 );
		}

		client.close();
	}
}
