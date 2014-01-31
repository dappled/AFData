package bbgRequetor.importer;

import middleware.bbg.BbgDataServer;
import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public abstract class BbgImporterBase extends GeneralImporterExporter {
	protected BbgDataServer	_server;
	protected final String	_queueName	= "BbgData";

	/***********************************************************************
	 * Constructor
	 * @throws Exception
	 ***********************************************************************/
	public BbgImporterBase(String dbServer, String catalog) throws Exception {
		/* database part */
		super( dbServer, catalog );
		
		_server = new BbgDataServer( null );
	}

	@Override
	public void close() throws Exception {
		super.close();
		_server.publishSuicideQuest( _queueName );
		if (_server != null) _server.close();
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/** get data from localFile, do something, export to destination */
	public void dump(final String localFile, final String destination, final String tradeDate, final int wipe)
			throws Exception {
		if (wipe > 0) {
			wipeData( destination, wipe );
		}
		dumpHelper( localFile, destination, tradeDate );
	}

	/**
	 * Dump local files' info into the database or out file or watever
	 * @throws Exception
	 */
	protected abstract void dumpHelper(String localFile, String destination, String tradeDate) throws Exception;

}
