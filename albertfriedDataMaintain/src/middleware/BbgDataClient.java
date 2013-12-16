package middleware;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import bloomberg.BbgDataGrabber;

import com.bloomberglp.blpapi.Name;

/**
 * This is the client which listen to a queue which will pop bbg data request using listenTo, and will 
 * return the bbg data it get.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataClient {
	// I use async sends for performance reason
	protected static String				_brokerURL	= "tcp://localhost:61616";
	protected static ConnectionFactory	_factory;
	protected Connection				_connection;
	protected Session					_session;
	MessageProducer						_dataResultSender;
	
	/* bbg stuff */
	BbgDataGrabber _grabber;
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
		((ActiveMQConnectionFactory) BbgDataClient._factory).setUseAsyncSend( true );
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
					HashMap<String, ? extends Serializable> ret = null;
					ObjectMessage re = null;
					try {
						switch (msg.getJMSType()) {
							case "Historical":
								HistoricalRequest request = (HistoricalRequest) ((ObjectMessage) msg).getObject();
								String t = request.getType();
								List<String> names = request.getNames();
								List<String> fields = request.getFields();
								HashMap<Name, Object> properties = (HashMap<Name, Object>) request.getProperties();
								ret = _grabber.getData( t, names, fields, properties );
								re = _session.createObjectMessage( ret );
								re.setJMSType( "Historical" );
								break;
							case "Reference":
								break;
							default:
								break;
						}
						/* send back result */
						// send msg with Non_persistent mode, b/c we don't care about broker restart issue here... the
						// Persistent mode is ridiculously slow
						_dataResultSender.send( msg.getJMSReplyTo(), re, DeliveryMode.NON_PERSISTENT,
								Message.DEFAULT_PRIORITY, 1000 );
					} catch (final Exception rte) {
						rte.printStackTrace();
					}
				}
			}
		} );
	}
}
