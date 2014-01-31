package exporter.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.GeneralImporterExporter;
import dataWrapper.exporter.validator.RecordAbstract;

/**
 * @author Zhenghong Dong
 */
public abstract class ValidatorBase extends GeneralImporterExporter {

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ValidatorBase() {
		super();
	}

	public ValidatorBase(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/**
	 * Compare local data and broker data and find possible mismatches
	 * @param localFileName file name or database name
	 * @param brokerFileName file name or database name
	 * @param outFileName file name of mismatch report
	 * @param tradeDate the date we want to compare
	 * @throws Exception
	 */
	public void validate(final String localFile, final String brokerFile, final String outFile, final String tradeDate, final boolean wipe)
			throws Exception {
		// wipe GSEC position who is older than 7 days
		if (wipe == true) {		
			wipeData( brokerFile );
			
		}
		final List<List<RecordAbstract>> brokerTmp = readBrokerFile( brokerFile, tradeDate );
		final List<RecordAbstract> brokerDiffList = new ArrayList<>( brokerTmp.get( 0 ) );
		final List<RecordAbstract> brokerOtherDay = new ArrayList<>( brokerTmp.get( 1 ) );
		final List<RecordAbstract> brokerExtra = new ArrayList<>( brokerTmp.get( 2 ) );
		final List<RecordAbstract> brokerList = new ArrayList<>( brokerDiffList );
		brokerList.addAll( brokerOtherDay );
		brokerList.addAll( brokerExtra );
		// sort returning list according to their symbol. So more readable
		Collections.sort( brokerDiffList );
		Collections.sort( brokerOtherDay );
		Collections.sort( brokerExtra );
		Collections.sort( brokerList );

		final List<RecordAbstract> brokerDiffListCpy = new ArrayList<>( brokerDiffList );

		final List<List<RecordAbstract>> localTmp = readLocalFile( localFile, tradeDate );
		final List<RecordAbstract> localDiffList = new ArrayList<>( localTmp.get( 0 ) );
		final List<RecordAbstract> localOtherDay = new ArrayList<>( localTmp.get( 1 ) );
		final List<RecordAbstract> localExtra = new ArrayList<>( localTmp.get( 2 ) );
		final List<RecordAbstract> localList = new ArrayList<>( localDiffList );
		localList.addAll( localOtherDay );
		localList.addAll( localExtra );
		// sort returning list according to their symbol. So more readable
		Collections.sort( localDiffList );
		Collections.sort( localOtherDay );
		Collections.sort( localExtra );
		Collections.sort( localList );

		// get records in GSEC but not database
		brokerDiffList.removeAll( localDiffList );
		// get records in database but not GSEC
		localDiffList.removeAll( brokerDiffListCpy );
		// write to an xls file
		exportXls( outFile, localDiffList, brokerDiffList, localOtherDay, brokerOtherDay, localExtra, brokerExtra,
				localList, brokerList );
	}

	/**
	 * Export the differences and extra info into an xls file
	 * @param outFile
	 * @param localDiffList
	 * @param brokerDiffList
	 * @param localOtherDay
	 * @param brokerOtherDay
	 * @param localExtra
	 * @param brokerExtra
	 * @param localList
	 * @param brokerList
	 * @throws Exception
	 */
	protected abstract void exportXls(String outFile, List<RecordAbstract> localDiffList, List<RecordAbstract> brokerDiffList,
			List<RecordAbstract> localOtherDay, List<RecordAbstract> brokerOtherDay, List<RecordAbstract> localExtra,
			List<RecordAbstract> brokerExtra, List<RecordAbstract> localList, List<RecordAbstract> brokerList)
			throws Exception;

	/**
	 * Read from local, store info into a list of list
	 * @param localFile
	 * @param tradeDate
	 * @return
	 * @throws Exception
	 */
	protected abstract List<List<RecordAbstract>> readLocalFile(String localFile, String tradeDate) throws Exception;

	/**
	 * Read from broker, store info into a list of list
	 * @param brokerFile
	 * @param tradeDate
	 * @return
	 * @throws Exception
	 */
	protected abstract List<List<RecordAbstract>> readBrokerFile(String brokerFile, String tradeDate) throws Exception;

}
