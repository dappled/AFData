package importer;

import java.util.Date;

import utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class ImporterManager {
		private static String			_localFile;
		private static String			_dbName;
		private static ImporterBase	_importer;
		// always represent today
		private static String			_today;
		// always represent yesterday
		private static String			_yesterday;
		private static String			_dbServer;
		private static String			_catalog;
		private static boolean			_wipe = false;

		public static void main(final String[] args) throws Exception {
			_today = ParseDate.standardFromDate( new Date() );
			_yesterday = ParseDate.getPreviousWorkingDay( new Date() );
			ImporterManager.parseArgs( args );
			// System.out.println(reader.getDate(gsFile));
			ImporterManager._importer.dump( ImporterManager._localFile, _dbName, _today, _wipe);

			_importer.close();
		}

		/**
		 * @param args
		 * @throws Exception
		 */
		private static void parseArgs(final String[] args) throws Exception {
			for (int i = 0; i < args.length; i++) {
				switch (args[ i ]) {
					case "/localFile":
						ImporterManager._localFile = args[ ++i ];
						if (ImporterManager._localFile.startsWith( "c:\\" )) {
							ImporterManager._localFile = ImporterManager._localFile.replace( "[MMddyyyy]",
									ParseDate.MMddyyyyFromStandard( _yesterday ) );
						}
						break;
					case "/dbName":
						ImporterManager._dbName = args[ ++i ];
						break;
					case "/type":
						if (args[ ++i ].equals( "etb" )) {
						_importer = new ETBImporter(_dbServer, _catalog);
						}	
						else throw new Exception( "ImporterManager: /type argument inapproporiate" );
						break;
					case "/dbserver":
						_dbServer = args[ ++i ];
						break;
					case "/catalog":
						_catalog = args[ ++i ];
						break;
					default:
						break;
				}
			}
		}
	}
