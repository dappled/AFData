package dataValidation;

import java.util.Date;

import org.apache.poi.hssf.record.DBCellRecord;

import utils.ParseDate;

public class ValidatorManager {
	private static String			_addressList;
	private static String			_localFile;
	private static String			_brokerFile;
	private static String			_outFile;
	private static ValidatorBase	_validator;
	private static String			_date;
	private static String			_mailSubject;
	private static String			_dbServer;
	private static String			_catalog;

	public static void main(final String[] args) throws Exception {
		ValidatorManager._date = ParseDate.getPreviousWorkingDay( new Date() );
		ValidatorManager.parseArgs( args );

		// System.out.println(reader.getDate(gsFile));
		ValidatorManager._validator.validate( ValidatorManager._localFile, ValidatorManager._brokerFile,
				ValidatorManager._outFile, ParseDate.MMSlashDDSlashYYYY( ParseDate.MMDDYYYY( ValidatorManager._date ) ) );
		// reader.sendEMail(outFile, _mailSubject, _addressList );
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	private static void parseArgs(final String[] args) throws Exception {
		for (int i = 0; i < args.length; i++) {
			switch (args[ i ]) {
				case "/mail":
					ValidatorManager._addressList = args[ ++i ];
					break;
				case "/localFile":
					ValidatorManager._localFile = args[ ++i ];
					if (ValidatorManager._localFile.startsWith( "c:\\" )) {
						ValidatorManager._localFile = ValidatorManager._localFile.replace( "[MMddyyyy]",
								ParseDate.MMDDYYYY( ValidatorManager._date ) );
					}
					break;
				case "/brokerFile":
					ValidatorManager._brokerFile = args[ ++i ];
					if (ValidatorManager._brokerFile.startsWith( "c:\\" )) {
						ValidatorManager._brokerFile = ValidatorManager._brokerFile.replace( "[yyyyMMdd]",
								ValidatorManager._date );
					}
					break;
				case "/outFile":
					ValidatorManager._outFile = args[ ++i ];
					if (ValidatorManager._outFile.startsWith( "c:\\" )) {
						ValidatorManager._outFile = ValidatorManager._outFile.replace( "[MMddyyyy]",
								ParseDate.MMDDYYYY( ValidatorManager._date ) );
					}
					break;
				case "/type":
					if (args[ ++i ].equals( "activity" )) {
						ValidatorManager._validator = new ActivityValidator();
						ValidatorManager._mailSubject = "ActivityMismatch" + ValidatorManager._date;
					}
					else if (args[ i ].equals( "position" )) {
						ValidatorManager._validator = new PositionValidator(_dbServer, _catalog);
						ValidatorManager._mailSubject = "PostitionMismatch" + ValidatorManager._date;
					} else throw new Exception( "ValidatorManager: /type argument inapproporiate" );
					break;
				case "/dbserver":
					_dbServer = args[++i];
					break;
				case "/catalog":
					_catalog = args[++i];
					break;
				default:
					break;
			}
		}
	}
}
