package com.dong.dataValidator;

import java.util.Date;

import com.dong.utils.ParseDate;

public class ValidatorManager {
	private static String			_addressList;
	private static String			_localFile;
	private static String			_brokerFile;
	private static String			_outFile;
	private static ValidatorBase	_validator;
	// used for database search, database imported date will always be at today's date ( since they are imported every
	// 5:00 AM ), but they are indeed yesterday's data, that's why we use _yesterday below.
	private static String			_date;
	// always represents yesterday
	private static String			_yesterday;
	private static String			_mailSubject;
	private static String			_dbServer;
	private static String			_catalog;

	public static void main(final String[] args) throws Exception {
		_yesterday = ParseDate.getPreviousWorkingDay( new Date() );
		ValidatorManager.parseArgs( args );
		// System.out.println(reader.getDate(gsFile));
		ValidatorManager._validator
				.validate( ValidatorManager._localFile, ValidatorManager._brokerFile,
						ValidatorManager._outFile,
						ValidatorManager._date );
		_validator.sendEMail( _outFile, _mailSubject, _addressList, ParseDate.MMddyyyyFromStandard( _yesterday) );

		_validator.close();
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
								ParseDate.MMddyyyyFromStandard( _yesterday ) );
					}
					break;
				case "/brokerFile":
					ValidatorManager._brokerFile = args[ ++i ];
					if (ValidatorManager._brokerFile.startsWith( "c:\\" )) {
						ValidatorManager._brokerFile = ValidatorManager._brokerFile.replace( "[yyyyMMdd]",
								ParseDate.yyyyMMddFromStandard( _yesterday ) );
					}
					break;
				case "/outFile":
					ValidatorManager._outFile = args[ ++i ];
					if (ValidatorManager._outFile.startsWith( "c:\\" )) {
						ValidatorManager._outFile = ValidatorManager._outFile.replace( "[MMddyyyy]",
								ParseDate.MMddyyyyFromStandard( _yesterday ) );
					}
					break;
				case "/type":
					if (args[ ++i ].equals( "activity" )) {
						_date = ParseDate.getPreviousWorkingDay( new Date() );
						ValidatorManager._validator = new ActivityValidator();
						ValidatorManager._mailSubject = "ActivityMismatch";
					}
					else if (args[ i ].equals( "position" )) {
						_date = ParseDate.standardFromDate( new Date() );
						ValidatorManager._validator = new PositionValidator( _dbServer, _catalog );
						ValidatorManager._mailSubject = "PostitionMismatch";
					} else throw new Exception( "ValidatorManager: /type argument inapproporiate" );
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
