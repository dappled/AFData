package exporter;

import utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class ExportManager {
	private static String		_emailAddress	= null;
	private static String		_mailSubject;
	private static String		_outFile;
	private static ExporterBase	_exporter;
	private static String		_dbServer;
	private static String		_catalog;
	private static String		_ftpAddress;

	public static void main(final String[] args) throws Exception {
		ExportManager.parseArgs( args );
		ExportManager._exporter.report( ExportManager._outFile, ParseDate.today, _ftpAddress );
		// send the reports using email if provided
		if (_emailAddress != null) {
			_exporter.sendEMail( _outFile, _mailSubject, _emailAddress, ParseDate.MMddyyyyFromStandard( ParseDate.yesterday ) );
		}
		_exporter.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	private static void parseArgs(final String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			switch (args[ i ]) {
				case "/mail":
					_emailAddress = args[ ++i ];
					break;
				case "/ftp":
					_ftpAddress = args[ ++i ];
					break;
				case "/outFile":
					ExportManager._outFile = args[ ++i ];
					if (ExportManager._outFile.startsWith( "c:\\" )) {
						ExportManager._outFile = ExportManager._outFile.replace( "[MMddyyyy]",
								ParseDate.MMddyyyyFromStandard( ParseDate.yesterday ) );
					}
					break;
				case "/dbserver":
					_dbServer = args[ ++i ];
					break;
				case "/catalog":
					_catalog = args[ ++i ];
					break;
				case "/type":
					// portfolio margin daily report
					if (args[ ++i ].equals( "pm" )) {
						_exporter = new PMExporter( _dbServer, _catalog );
						_mailSubject = "PortfolioMarginReport";
					} 
					// GSEC upload 
					else if (args[ i ].equals( "gsecUpload" )) {
						_exporter = new GSUploader( _dbServer, _catalog );						
					}
					// bbu
					else if (args[i].equals( "bbu" )) {
						_exporter = new BBU( _dbServer, _catalog );
					}
					else throw new Exception( "ImporterManager: /type argument unsopported: " + args[ i ] );
					break;
				default:
					break;
			}
		}
	}
}
