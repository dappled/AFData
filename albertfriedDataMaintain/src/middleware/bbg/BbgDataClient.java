package middleware.bbg;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

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

import bbgRequestor.bloomberg.BbgDataGrabber;

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
	MessageProducer								_dataResultSender;
	private boolean								_isFinished	= false;

	/* bbg stuff */
	BbgDataGrabber								_grabber;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Client.
	 * @throws JMSException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public BbgDataClient() throws JMSException, IOException, InterruptedException {
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
	 * @throws InterruptedException
	 ***********************************************************************/
	public void close() throws JMSException, InterruptedException {
		if (_connection != null) {
			_connection.close();
		}
		if (_grabber != null) {
			_grabber.stop();
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
				if (msg instanceof ObjectMessage) {
					System.out.println( "Receive a msg" );
					try {
						ObjectMessage re = _session.createObjectMessage();
						re.setJMSType( msg.getJMSType() );
						try {
							switch (msg.getJMSType()) {
								case "Historical":
									HistoricalRequest request = (HistoricalRequest) ((ObjectMessage) msg).getObject();
									String t = request.getType();
									List<String> names = request.getNames();
									List<String> fields = request.getFields();
									HashMap<String, Object> properties = (HashMap<String, Object>) request.getProperties();
									System.out.println( "Start getting data from bbg" );
									re.setObject( _grabber.getData( t, names, fields, properties ) );
									System.out.println( "Finished getting data from bbg" );
									break;
								case "Reference":
									break;
								case "Lookup":
									SecurityLoopUpRequest requestLookUp = (SecurityLoopUpRequest) ((ObjectMessage) msg).getObject();
									System.out.println( "Start getting data from bbg" );
									re.setObject( (Serializable) _grabber.securityLookUp( requestLookUp.getArgs() ) );
									System.out.println( "Finished getting data from bbg" );
									break;
								default:
									// incorrect msg type
									break;
							}
						} catch (Exception e) {
							// on error when get bbg data, send the error back
							System.err.println( "Error happens, will send error msg back" );
							re.setJMSType( "Error" );
							re.setStringProperty( "Error", e.getMessage() );
						}
						/* send back result */
						// send msg with Persistent mode
						System.out.println( "Start replying" );
						_dataResultSender.send( msg.getJMSReplyTo(), re );
						System.out.println( "Finished replying" );
					} catch (final Exception rte) {
						rte.printStackTrace();
					}
				}
				else {
					// if poison pill
					_isFinished = true;
					System.out.println("Received poison pill, Im gonna kill myself. Thx for using me. cya");
				}
			}
		} );
	}

	public boolean isFinished() {
		return _isFinished;
	}

	public static void main(String[] args) throws Exception {
		final BbgDataClient client = new BbgDataClient();

		// client listen to this queue
		String queueName = "BbgData";
		client.listenTo( queueName );

		while (!client.isFinished()) {
			Thread.sleep( 1000 );
		}
		client.close();
	}

}
