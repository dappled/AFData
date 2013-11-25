package exporter;

import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public abstract class ExporterBase extends GeneralImporterExporter {
	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ExporterBase(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	public abstract void report(final String localFile, final String date) throws Exception;

}
