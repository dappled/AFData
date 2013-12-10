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

import myMonteCarlo.DataWrapper.OptionType;
import myMonteCarlo.Stats.I_PathStats;
import myMonteCarlo.Stats.PathStat;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * This is the server which can publish monte carlo questions to user defined queue using publishQuest, then calculate 
 * its price using simulation result returned from clients.
 * 
 * @author Zhenghong Dong
 */
public class Server implements MessageListener {
	protected int						MAX_DELTA_PERCENT	= 1;
	protected static String				_brokerURL			= "tcp://localhost:61616";
	protected static ConnectionFactory	_factory;
	protected Connection				_connection;
	protected Session					_session;
	protected MessageProducer			_simulationQuestSender;
	protected Destination				_resultQueue;

	// one batch contains 100 requests
	private final int					BATCH_SIZE			= 100;
	// number of results received for last batch of simulation requests sent
	private int							_nRecieved;
	private I_PathStats<Double>			_stat;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	/**
	 * Generate a Simulation Server.
	 * @throws JMSException when connection error happens
	 */
	public Server(final String serverName) throws JMSException {
		Server._factory = new ActiveMQConnectionFactory( Server._brokerURL );
		// I use async sends for performance reason
		((ActiveMQConnectionFactory) Server._factory).setUseAsyncSend( true );
		_connection = Server._factory.createConnection();
		_session = _connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
		_simulationQuestSender = _session.createProducer( null );

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
	 * Price an option with given information.
	 * @param queueName the name of the queue you want to use
	 * @param type type of option
	 * @param currentPrice current strike price
	 * @param dailyVol daily volatility of that stock
	 * @param dailyRate daily intereset rate
	 * @param TTM time to maturity
	 * @param strike strike price
	 * @param err how much error we can bear in price, 1% means 1% of "true price"
	 * @param stoppingCriteria how confidence(in probability) do we believe the option price has converged
	 * @param maxSimulation maximum number of simulations we want to run
	 * @return the value of the option
	 * @throws Exception
	 */
	public double publishQuest(final String queueName, final OptionType type, final double currentPrice,
			final double dailyVol,
			final double dailyRate, final int TTM,
			final double strike, final double err, final double stoppingCriteria,
			final int maxSimulation) throws Exception {
		// set up publisher and listener
		_simulationQuestSender = _session.createProducer( _session.createQueue( queueName ) );
		_resultQueue = _session.createQueue( "receiver_" + queueName );
		final MessageConsumer simulationResultReceiver = _session.createConsumer( _resultQueue );
		simulationResultReceiver.setMessageListener( this );

		// do the monte carlo stuff
		return getPrice( type, currentPrice, dailyVol, dailyRate, TTM, strike, err, stoppingCriteria, maxSimulation );
	}

	/** helper function of publishQuest, do the real monte carlo stuff */
	private double getPrice(final OptionType type, final double currentPrice, final double dailyVol,
			final double dailyRate, final int TTM, final double strike, final double err,
			final double stoppingCriteria, final int maxSimulation) throws Exception {
		// create path stats monitor
		_stat = PathStat.newPathStat();

		// main loop
		int n = 0; // number of simulations
		final double tol = new NormalDistribution().inverseCumulativeProbability( 1 - (1 - stoppingCriteria) / 2 );
		do {
			// sends out BATCH_SIZE amount of simulation quests
			publishBatchSimulation( type, currentPrice, dailyVol, dailyRate, TTM, strike );

			// loop until we have received BATCH_SIZE amount of simulations
			while (_nRecieved < BATCH_SIZE) {
				Thread.sleep( 1 );  // idk why but w/o the sleep in this while loop making it infinite loop...
									// I will be really grateful if you help me figure out why...
			}
			_nRecieved = 0; // reset _nRecieved for next batch
			n += BATCH_SIZE; // update total number of simulations
		}// loop while not satisfying the stopping criteria
		while ((_stat.sampleVariance() == 0 || err * _stat.sampleMean() * Math.sqrt( n )
				/ Math.sqrt( _stat.sampleVariance() ) < tol) && n < maxSimulation);

		// if out of loop b/c fail to converge
		if (n >= maxSimulation) throw new Exception( "Fail to converge." );
		// return discounted price
		return _stat.sampleMean() * Math.exp( -dailyRate * TTM );
	}

	/** Sends out BATCH_SIZE amount of simulation quests to the _simulationQuestSender */
	private void publishBatchSimulation(final OptionType type, final double currentPrice, final double dailyVol,
			final double dailyRate, final int TTM, final double strike) {
		for (int i = 0; i < BATCH_SIZE; i++) {
			try {
				final StreamMessage msg = _session.createStreamMessage();
				msg.writeDouble( currentPrice );
				msg.writeDouble( dailyVol );
				msg.writeDouble( dailyRate );
				msg.writeInt( TTM );
				msg.writeDouble( strike );
				msg.setStringProperty( "type", type.toString() );
				msg.setJMSReplyTo( _resultQueue ); // set return queue
				// send msg with Non_persistent mode, b/c we don't care about broker restart issue here... the
				// Persistent mode is ridiculously slow
				_simulationQuestSender.send( msg, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 1000 );
			} catch (final JMSException e) {
				e.printStackTrace();
			}
		}
	}

	/***********************************************************************
	 * {@link MessageListener} methods
	 ***********************************************************************/
	@Override
	public void onMessage(final Message msg) {
		try {
			final StreamMessage message = (StreamMessage) msg;
			_stat.addPathStat( message.readDouble() ); // add return stats to _stat
			_nRecieved++; // we received a new msg!
		} catch (final Exception rte) {
			rte.printStackTrace();
		}
	}
}
