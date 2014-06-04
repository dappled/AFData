package middleware.bbg;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import middleware.bbg.beans.SecurityLookupRequest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import bbgRequestor.bloomberg.BbgDataGrabber;
import bbgRequestor.bloomberg.blpapi.examples.CopyOfRefDataExample;

/**
 * This is the client which listen to a queue which will pop bbg data request using listenTo, and will
 * return the bbg data it get.
 * 
 * @author Zhenghong Dong
 */
public class BbgDataQuestResponser {
	/* log */
	private static Logger						_logger				= Logger.getLogger("albertfriedMarketData.middleware.bbg");

	/* JMS stuff */
	// I use async sends for performance reason
	protected BrokerService						_broker;
	protected static String						_brokerURL			= "tcp://10.0.0.182:61616";
	protected static ActiveMQConnectionFactory	_factory;
	protected Connection						_connection;
	protected Session							_session;
	protected MessageProducer					_dataResultSender;
	protected String							_requestQueueName	= "BbgDataRequest";
	private ExecutorService						_responseService;

	/* connection stuff */
	private HashSet<String>						_connections;
	protected Destination						_connectionDestination;
	protected MessageProducer					_connectionReplySender;

	/* bbg stuff */
	BbgDataGrabber								_grabber;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Client.
	 * @throws Exception
	 */
	public BbgDataQuestResponser() throws Exception {
		/* bbg part */
		_grabber = new BbgDataGrabber(_logger);

		/* broker part */
		_broker = new BrokerService();
		// configure the broker
		_broker.setBrokerName("VineyardBbgDataCenter");
		_broker.addConnector(_brokerURL);
		_broker.start();
		System.out.println("Broker is up");

		/* JMS part */
		BbgDataQuestResponser._factory = new ActiveMQConnectionFactory(BbgDataQuestResponser._brokerURL);
		// I use async sends for performance reason
		BbgDataQuestResponser._factory.setUseAsyncSend(true);
		_connection = BbgDataQuestResponser._factory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		_dataResultSender = _session.createProducer(null);
		_connection.start();
		_responseService = Executors.newCachedThreadPool();
		listenToQuester();

		/* connection part */
		_connections = new HashSet<>();
		_connectionDestination = null;
		_connectionReplySender = _session.createProducer(null);
		_connectionReplySender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		listenToConnection();
	}

	/***********************************************************************
	 * Destructor
	 * @throws Exception
	 ***********************************************************************/
	public void close() throws Exception {
		if (_grabber != null) {
			_grabber.stop();
		}
		shutdownAndAwaitTermination(_responseService);
		if (_connection != null) {
			_connection.close();
		}
		if (_broker != null) {
			_broker.stop();
		}
	}

	void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS)) System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/**
	 * Listen to a queue, get request quest sends back result from bbg.
	 * @param queueName the name of the queue
	 * @throws JMSException
	 * @throws Exception when bad things happened
	 */
	public void listenToQuester() throws JMSException {
		// set up listener
		final Destination destination = _session.createQueue(_requestQueueName);
		final MessageConsumer messageConsumer = _session.createConsumer(destination);
		messageConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(final Message msg) {
				_responseService.execute(new QuestHandler(msg));
			}
		});
	}

	private class QuestHandler implements Runnable {
		Message	msg;

		public QuestHandler(Message m) {
			msg = m;
		}

		@Override
		public void run() {
			try {
				_logger.info("Receive a request of type " + msg.getJMSType() + "with id " + msg.getJMSCorrelationID());
			} catch (JMSException e1) {
				e1.printStackTrace();
			}

			try {
				if (msg instanceof ObjectMessage) {
					ObjectMessage re = _session.createObjectMessage();
					re.setJMSType(msg.getJMSType());
					re.setJMSCorrelationID(msg.getJMSCorrelationID());

					DataRequest request;
					Set<String> names, fields;
					RequestType t;
					try {
						switch (msg.getJMSType()) {
							case "Historical":
								request = (DataRequest) ((ObjectMessage) msg).getObject();
								t = request.getType();
								names = request.getNames();
								fields = request.getFields();
								HashMap<String, Object> properties = request.getProperties();
								_logger.info("Start getting data from bbg");
								re.setObject(_grabber.getTsData(t, names, fields, properties));
								_logger.info("Finished getting data from bbg");
								break;
							case "Reference":
								request = (DataRequest) ((ObjectMessage) msg).getObject();
								t = request.getType();
								names = request.getNames();
								fields = request.getFields();
								_logger.info("Start getting data from bbg");
								re.setObject((Serializable) _grabber.getRefData(t, names, fields));
								_logger.info("Finished getting data from bbg");
								break;
							case "Lookup":
								SecurityLookupRequest requestLookUp = (SecurityLookupRequest) ((ObjectMessage) msg).getObject();
								_logger.info("Start getting data from bbg");
								re.setObject((Serializable) _grabber.securityLookUp(requestLookUp.getArgs()));
								_logger.info("Finished getting data from bbg");
								break;
							case "Test":
								_logger.info("Strat testing");
								CopyOfRefDataExample.main(null);
								_logger.info("Finished testing");
								break;
							default:
								// incorrect msg type
								break;
						}
					} catch (Exception e) {
						// on error when get bbg data, send the error back
						System.err.println("Error happens, will send error msg back");
						e.printStackTrace();
						re.setJMSType("Error");
						re.setStringProperty("Error", e.getMessage());
					}
					/* send back result */
					_logger.info("Start replying");
					_dataResultSender.send(msg.getJMSReplyTo(), re);
					_logger.info("Finished replying");

				}
				else {
					System.err.println("Received a msg from " + msg.getJMSCorrelationID()
							+ " with incorrect msg type(should be object message), will ignore it");
				}
			} catch (final Exception rte) {
				rte.printStackTrace();
			}
		}
	}

	/**
	 * Listen to connection queue
	 * @throws JMSException
	 */
	public void listenToConnection() throws JMSException {
		/* set up listener */
		_connectionDestination = _session.createQueue("Connection");
		MessageConsumer connectionRequestReceiver = _session.createConsumer(_connectionDestination);
		connectionRequestReceiver.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(final Message msg) {
				try {
					String id = msg.getJMSCorrelationID();
					switch (msg.getJMSType()) {
					// connect connection request
						case "Connection":
							System.out.println("Receive a connection request from " + msg.getJMSCorrelationID());
							Message re = _session.createMessage();
							re.setJMSType(msg.getJMSType());
							re.setJMSCorrelationID(id);
							// refuse this connection request if already connected to it or too many connectors at this moment
							if (_connections.contains(id) || _connections.size() >= 5) {
								re.setBooleanProperty("Connection", false);
								System.err.println("Cannot accept this connection request now");
							}
							// accept connection
							else {
								_connections.add(id);
								re.setBooleanProperty("Connection", true);
								System.out.println("Accept connection");
							}
							_connectionReplySender.send(msg.getJMSReplyTo(), re);
							break;
						// close connection request
						case "Close":
							System.out.println(id + " wants to close its connection");
							_connections.remove(id);
							System.out.println(id + " disconnected");
							break;
						default:
							System.err.println("Incorrect connection request type");
							break;
					}

				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("Start listening to questers");
	}

	public static void main(String[] args) throws Exception {
		if (!((args.length == 0) || (args.length == 1 && args[0].equals("-t")))) throw new Exception("Takes at most 1 argument(-t to enable logging)");
		if (args.length == 1) _logger.setLevel(Level.FINE);
		new BbgDataQuestResponser();

		while (true) {
			Thread.sleep(3000);
		}
	}
}
