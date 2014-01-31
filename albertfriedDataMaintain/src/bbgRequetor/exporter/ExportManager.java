package bbgRequetor.exporter;

import utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class ExportManager {
	private static String		_addressList	= null;
	private static String		_mailSubject;
	private static String		_outFile;
	private static String		_inFile;
	private static BbgExporterBase	_exporter;
	private static String		_dbServer;
	private static String		_catalog;
	private static String		_ftpAddress;

	public static void main(final String[] args) throws Exception {
		ExportManager.parseArgs( args );
		ExportManager._exporter.report( ExportManager._inFile, ExportManager._outFile, ParseDate.today );
		// send the reports using email if provided
		if (_addressList != null) {
			_exporter.sendEMail( _outFile, _mailSubject, _addressList, ParseDate.MMddyyyyFromStandard( ParseDate.yesterday ) );
		}
		// send the reports using ftp if provided
		if (_ftpAddress != null) {
			_exporter.uploadFtp( _outFile, _ftpAddress);
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
					_addressList = args[ ++i ];
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
				case "/inFile":
					ExportManager._inFile = args[++i];
					if (ExportManager._inFile.startsWith( "c:\\" )) {
						ExportManager._inFile = ExportManager._inFile.replace( "[MMddyyyy]",
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
					// optionable stocks
					if (args[ ++i ].equals( "optional" )) {
						_exporter = new OptionableStocks( _dbServer, _catalog );
						_mailSubject = "OptionableStocks";
					} 
					//// GSEC upload 
					//else if (args[ i ].equals( "gsecUpload" )) {
					//	_exporter = new GSUploader( _dbServer, _catalog );						
					//}
					
					else throw new Exception( "ImporterManager: /type argument unsopported: " + args[ i ] );
					break;
				default:
					break;
			}
		}
	}
}
