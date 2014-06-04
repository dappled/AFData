package bbgRequestor.importer;

import middleware.bbg.BbgDataQuester;
import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public abstract class BbgImporterBase extends GeneralImporterExporter {
	protected BbgDataQuester	_quester;
	protected final String	_queueName	= "BbgData";

	/***********************************************************************
	 * Constructor
	 * @throws Exception
	 ***********************************************************************/
	public BbgImporterBase(String dbServer, String catalog) throws Exception {
		/* database part */
		super( dbServer, catalog );
		
		_quester = new BbgDataQuester( null );
	}

	@Override
	public void close() throws Exception {
		super.close();
		if (_quester!=null) _quester.publishCloseQuest();
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
