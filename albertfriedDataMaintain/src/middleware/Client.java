package middleware;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;

import myMonteCarlo.Path.GBMPath;
import myMonteCarlo.Path.StockPath;
import myMonteCarlo.PayOut.AsianCall;
import myMonteCarlo.PayOut.EuropeanCall;
import myMonteCarlo.PayOut.PayOut;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.joda.time.DateTime;

/**
 * This is the client which listen to a queue which will pop monte carlo simulation request using listenTo, and will 
 * return one single simulation result.
 * 
 * @author Zhenghong Dong
 */
public class Client {
	// I use async sends for performance reason
	protected static String				_brokerURL	= "tcp://localhost:61616";
	protected static ConnectionFactory	_factory;
	protected Connection				_connection;
	protected Session					_session;
	MessageProducer						_simulationResultSender;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Client.
	 * @throws JMSException
	 */
	public Client() throws JMSException {
		Client._factory = new ActiveMQConnectionFactory( Client._brokerURL );
		// I use async sends for performance reason
		((ActiveMQConnectionFactory) Client._factory).setUseAsyncSend( true );
		_connection = Client._factory.createConnection();
		_session = _connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
		_simulationResultSender = _session.createProducer( null );

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
	 * Listen to a queue, get simulation quest if exists and sends back one simulation result.
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
				if (msg instanceof StreamMessage) {
					try {
						// send back result
						final StreamMessage re = _session.createStreamMessage();
						re.writeDouble( getPrice( msg ) );
						// send msg with Non_persistent mode, b/c we don't care about broker restart issue here... the
						// Persistent mode is ridiculously slow
						_simulationResultSender.send( msg.getJMSReplyTo(), re, DeliveryMode.NON_PERSISTENT,
								Message.DEFAULT_PRIORITY, 1000 );
					} catch (final Exception rte) {
						rte.printStackTrace();
					}
				}
			}
		} );
	}

	/** Return one simulation payoff of an option, whose information is contained in a message. */
	private double getPrice(final Message msg) throws Exception {
		// get simulation data
		final StreamMessage message = (StreamMessage) msg;
		final double currentPrice = message.readDouble();
		final double dailyVol = message.readDouble();
		final double dailyRate = message.readDouble();
		final int TTM = message.readInt();
		final double strike = message.readDouble();
		final String type = msg.getStringProperty( "type" );

		// get simulation result given data
		final StockPath path = new GBMPath( DateTime.now(), currentPrice, dailyRate, dailyVol, TTM );
		final PayOut po;
		if (type.equals( "EuropeanCall" )) {
			po = new EuropeanCall( strike );
		} else if (type.equals( "AsianCall" )) {
			po = new AsianCall( strike );
		} else throw new Exception(
				"Client: Inapproporate type - shoule be AsianCall/EuropeanCall but " + type );

		return po.getPayout( path );
	}
}
