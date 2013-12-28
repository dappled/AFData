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
	public void dump(final String localFile, final String dbName, final String tradeDate, final int wipe)
			throws Exception {
		if (wipe > 0) {
			wipeData( dbName, wipe );
		}
		dumpHelper( localFile, dbName, tradeDate );
	}

	/**
	 * Dump local files' info into the database
	 * @throws Exception
	 */
	protected abstract void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception;

}
