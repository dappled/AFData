package importer;

import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public abstract class ImporterBase extends GeneralImporterExporter {
		/***********************************************************************
		 * Constructor
		 ***********************************************************************/
		public ImporterBase(String dbServer, String catalog) {
			super( dbServer, catalog );
		}


		/***********************************************************************
		 * Utilities
		 ***********************************************************************/
		public void dump(final String localFile, final String dbName, final String tradeDate, final boolean wipe)
				throws Exception {
			if (wipe == true) {
				wipeData(dbName);
			}
			dumpHelper(localFile, dbName, tradeDate);
		}

		/** Dump local files' info into the database 
		 * @throws Exception */
		protected abstract void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception;

	}
