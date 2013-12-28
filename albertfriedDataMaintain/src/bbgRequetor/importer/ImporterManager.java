package bbgRequetor.importer;

import utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class ImporterManager {
	private static String			_brokerFile;
	private static String			_dbName;
	private static BbgImporterBase	_importer;
	private static String			_dbServer;
	private static String			_catalog;
	private static int				_wipe	= 0; // 0 means no wipe at all, 1 means remove today's earlier imports only, 2 means only keep this week's data

	public static void main(final String[] args) throws Exception {
		ImporterManager.parseArgs( args );

		// System.out.println(reader.getDate(gsFile));
		_importer.dump( ImporterManager._brokerFile, _dbName, ParseDate.today, _wipe ); 

		_importer.close();

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	private static void parseArgs(final String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			switch (args[ i ]) {
				case "/brokerFile":
					ImporterManager._brokerFile = args[ ++i ];
					if (ImporterManager._brokerFile.startsWith( "c:\\" )) {
						ImporterManager._brokerFile = ImporterManager._brokerFile.replace( "[yyyyMMdd]",
								ParseDate.yyyyMMddFromStandard( ParseDate.yesterday ) );
					}
					break;
				case "/dbName":
					ImporterManager._dbName = args[ ++i ];
					break;
				case "/dbserver":
					_dbServer = args[ ++i ];
					break;
				case "/catalog":
					_catalog = args[ ++i ];
					break;
				case "/wipe":
					_wipe = Integer.parseInt( args[ ++i ] );
					break;
				case "/type":
					// term structure
					if (args[ ++i ].equals( "ts" )) {
						_importer = new TermStructureImporter( _dbServer, _catalog );
					}
					else throw new Exception( "ImporterManager: /type argument unsopported: " + args[ i ] );
					break;
				default:
					break;
			}
		}
	}
}
