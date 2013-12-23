package importer;

import utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class ImporterManager {
	private static String		_brokerFile;
	private static String		_dbName;
	private static ImporterBase	_importer;
	private static String		_dbServer;
	private static String		_catalog;
	private static boolean		_wipe	= false;

	public static void main(final String[] args) throws Exception {
		ImporterManager.parseArgs( args );
		// System.out.println(reader.getDate(gsFile));
		ImporterManager._importer.dump( ImporterManager._brokerFile, _dbName, ParseDate.today, _wipe );

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
					_wipe = Boolean.parseBoolean( args[++i] );
					break;
				case "/type":
					// easy to borrow
					if (args[ ++i ].equals( "etb" )) {
						_importer = new ETBImporter( _dbServer, _catalog );
					}
					// portfolio requirement	
					else if (args[ i ].equals( "pmsb" )) {
						_importer = new PMRequirementImporter( _dbServer, _catalog );
					}
					// portfolio detail
					else if (args[ i ].equals( "pmdb" )) {
						_importer = new PMDetailImporter( _dbServer, _catalog );
					} 
					// broker trade 
					else if (args[i].equals( "brokerTrade")) {
						_importer = new BrokerTradeImporter(_dbServer, _catalog);
					}
					else throw new Exception( "ImporterManager: /type argument unsopported: " + args[i] );
					break;
				default:
					break;
			}
		}
	}
}
