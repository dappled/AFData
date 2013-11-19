package com.dong.dataValidator;

import java.util.Date;

import org.joda.time.LocalDate;

import com.dong.utils.ParseDate;

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
	private static boolean			_tradeDateIsYesterday;

	public static void main(final String[] args) throws Exception {
		ValidatorManager.parseArgs( args );
		// System.out.println(reader.getDate(gsFile));
		ValidatorManager._validator
				.validate( ValidatorManager._localFile, ValidatorManager._brokerFile,
						ValidatorManager._outFile,
						ValidatorManager._date );
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
								ParseDate.MMddyyyyFromStandard( ValidatorManager._date ) );
					}
					break;
				case "/brokerFile":
					ValidatorManager._brokerFile = args[ ++i ];
					if (ValidatorManager._brokerFile.startsWith( "c:\\" )) {
						ValidatorManager._brokerFile = ValidatorManager._brokerFile.replace( "[yyyyMMdd]",
								ParseDate.MMddyyyyFromStandard( ValidatorManager._date ));
					}
					break;
				case "/outFile":
					ValidatorManager._outFile = args[ ++i ];
					if (ValidatorManager._outFile.startsWith( "c:\\" )) {
						if (_tradeDateIsYesterday) { // for activity, we don't need to change date because we do use yesterday's date during validation
							ValidatorManager._outFile = ValidatorManager._outFile.replace( "[MMddyyyy]",
									ParseDate.MMddyyyyFromStandard( ValidatorManager._date ) );
						}
						else { // since position compare will use today's date during validation, however, the position report is for yesterday's difference
							ValidatorManager._outFile = ValidatorManager._outFile.replace( "[MMddyyyy]",
									ParseDate.MMddyyyyFromStandard( ParseDate.getPreviousWorkingDay( new Date() ) ));
						}
					}
					break;
				case "/type":
					if (args[ ++i ].equals( "activity" )) {
						ValidatorManager._validator = new ActivityValidator();
						ValidatorManager._mailSubject = "ActivityMismatch" + ValidatorManager._date;
					}
					else if (args[ i ].equals( "position" )) {
						ValidatorManager._validator = new PositionValidator( _dbServer, _catalog );
						ValidatorManager._mailSubject = "PostitionMismatch" + ValidatorManager._date;
					} else throw new Exception( "ValidatorManager: /type argument inapproporiate" );
					break;
				case "/dbserver":
					_dbServer = args[ ++i ];
					break;
				case "/catalog":
					_catalog = args[ ++i ];
					break;
				case "/tradeDateIsYesterday":
					_tradeDateIsYesterday = Boolean.parseBoolean( args[ ++i ] );
					if (_tradeDateIsYesterday) {
						_date = ParseDate.getPreviousWorkingDay( new Date() );
					} else {
						_date = ParseDate.standardFromyyyyBMMBdddd( LocalDate.now().toString() );
					}
					break;
				default:
					break;
			}
		}
	}
}
