package middleware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import bloomberg.beans.ReferenceAbstarct;
import bloomberg.beans.TimeSeries;
import bloomberg.beans.TimeSeries.TSType;

import com.bloomberglp.blpapi.Name;

/**
 * This is the server which can publish bbg data request to user defined queue using publishQuest, then return the data
 * returned from clients.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataServer implements MessageListener {
	protected int						MAX_DELTA_PERCENT	= 1;
	protected static String				_brokerURL			= "tcp://localhost:61616";
	protected static ConnectionFactory	_factory;
	protected Connection				_connection;
	protected Session					_session;
	protected MessageProducer			_dataRequestSender;
	protected Destination				_resultQueue;

	/* returning data */
	@SuppressWarnings("rawtypes")
	Map<String, ? extends TimeSeries>	_hisData;
	Map<String, ReferenceAbstarct>		_refData;
	boolean								_received;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Server.
	 * @throws JMSException when connection error happens
	 */
	public BbgDataServer(final String serverName) throws JMSException {
		BbgDataServer._factory = new ActiveMQConnectionFactory( BbgDataServer._brokerURL );
		// I use async sends for performance reason
		((ActiveMQConnectionFactory) BbgDataServer._factory).setUseAsyncSend( true );
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
	public Map<String, ? extends TimeSeries> publishHisQuest(String queueName, TSType type, List<String> names, List<String> fields, HashMap<Name, Object> properties) throws Exception {
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
			// send msg with Non_persistent mode, b/c we don't care about broker restart issue here... the
			// Persistent mode is ridiculously slow
			_dataRequestSender.send( msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 1000 );
		} catch (final JMSException e) {
			e.printStackTrace();
		}

		while (!_received) {
			Thread.sleep( 1000 );
		}
		
		return _hisData;
	}

	/***********************************************************************
	 * {@link MessageListener} methods
	 ***********************************************************************/
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onMessage(final Message msg) {
		try {
			final ObjectMessage message = (ObjectMessage) msg;
			switch (message.getJMSType()) {
				case "Historical":
					_hisData = (Map<String, ? extends TimeSeries>) message.getObject();
					break;
				case "Reference":
					_refData = (Map<String, ReferenceAbstarct>) message.getObject();
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
}
