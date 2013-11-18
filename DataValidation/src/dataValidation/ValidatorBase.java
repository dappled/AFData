package dataValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import dataWrapper.RecordAbstract;

/**
 * @author Zhenghong Dong
 */
public abstract class ValidatorBase {
	protected final String _dbServer;
	protected final String _catalog;
	
	public ValidatorBase() {
		_dbServer = null;
		_catalog = null;
	}
	
	public ValidatorBase(String dbServer, String catalog) {
		_dbServer = dbServer;
		_catalog = catalog;
	}
	
	/**
	 * Compare local data and broker data and find possible mismatches
	 * @param localFileName file name or database name 
	 * @param brokerFileName file name or database name 
	 * @param outFileName file name of mismatch report
	 * @param tradeDate the date we want to compare
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void validate(final String localFile, final String brokerFile, final String outFile, final String tradeDate)
			throws Exception {
		final List<List<RecordAbstract>> brokerTmp = readBrokerFile( brokerFile, tradeDate );
		final List<RecordAbstract> brokerDiffList = new ArrayList<>( brokerTmp.get( 0 ) );
		final List<RecordAbstract> brokerOtherDay = new ArrayList<>( brokerTmp.get( 1 ) );
		final List<RecordAbstract> brokerExtra = new ArrayList<>( brokerTmp.get( 2 ) );
		final List<RecordAbstract> brokerList = new ArrayList<>( brokerDiffList );
		brokerList.addAll( brokerOtherDay );
		brokerList.addAll( brokerExtra );
		// sort returning list according to their symbol. So more readable
		Collections.sort( brokerDiffList );
		Collections.sort( brokerOtherDay );
		Collections.sort( brokerExtra );
		Collections.sort( brokerList );

		final List<RecordAbstract> brokerDiffListCpy = new ArrayList<>( brokerDiffList );

		final List<List<RecordAbstract>> localTmp = readLocalFile( localFile, tradeDate );
		final List<RecordAbstract> localDiffList = new ArrayList<>( localTmp.get( 0 ) );
		final List<RecordAbstract> localOtherDay = new ArrayList<>( localTmp.get( 1 ) );
		final List<RecordAbstract> localExtra = new ArrayList<>( localTmp.get( 2 ) );
		final List<RecordAbstract> localList = new ArrayList<>( localDiffList );
		localList.addAll( localOtherDay );
		localList.addAll( localExtra );
		// sort returning list according to their symbol. So more readable
		Collections.sort( localDiffList );
		Collections.sort( localOtherDay );
		Collections.sort( localExtra );
		Collections.sort( localList );

		// get records in GSEC but not database
		brokerDiffList.removeAll( localDiffList );
		// get records in database but not GSEC
		localDiffList.removeAll( brokerDiffListCpy );
		// write to an xls file
		exportXls( outFile, localDiffList, brokerDiffList, localOtherDay, brokerOtherDay, localExtra, brokerExtra,
				localList, brokerList );
	}

	/**
	 * Export the differences and extra info into an xls file
	 * @param outFile
	 * @param localDiffList
	 * @param brokerDiffList
	 * @param localOtherDay
	 * @param brokerOtherDay
	 * @param localExtra
	 * @param brokerExtra
	 * @param localList
	 * @param brokerList
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	protected void exportXls(String outFile, List<RecordAbstract> localDiffList, List<RecordAbstract> brokerDiffList,
			List<RecordAbstract> localOtherDay, List<RecordAbstract> brokerOtherDay, List<RecordAbstract> localExtra,
			List<RecordAbstract> brokerExtra, List<RecordAbstract> localList, List<RecordAbstract> brokerList) throws Exception {}

	/**
	 * Read from local, store info into a list of list
	 * @param localFile
	 * @param tradeDate
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected abstract List<List<RecordAbstract>> readLocalFile(String localFile, String tradeDate) throws Exception;

	/**
	 * Read from broker, store info into a list of list
	 * @param brokerFile
	 * @param tradeDate
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	protected abstract List<List<RecordAbstract>> readBrokerFile(String brokerFile, String tradeDate) throws Exception;

	/**
	 * Send the result report to people
	 * @param outFileName
	 * @param addressList
	 */
	public void sendEMail(final String outFileName, final String mailSubject, final String addressList) {
		final String username = "rn@albertfried.com";
		final String password = "cljt123#";

		final Properties props = new Properties();
		props.put( "mail.smtp.auth", true );
		props.put( "mail.smtp.starttls.enable", true );
		props.put( "mail.smtp.host", "smtp.emailsrvr.com" );
		// props.put("mail.smtp.port", "587");

		final Session session = Session.getInstance( props,
				new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication( username, password );
			}
		} );

		try {
			final String fileName = mailSubject;
			final Message message = new MimeMessage( session );
			message.setFrom( new InternetAddress( "rn@albertfried.com" ) );
			message.setRecipients( Message.RecipientType.TO,
					InternetAddress.parse( addressList) );
			message.setSubject( fileName );
			// message.setText("PFA");

			MimeBodyPart messageBodyPart = new MimeBodyPart();

			final Multipart multipart = new MimeMultipart();

			messageBodyPart = new MimeBodyPart();
			final String file = outFileName;
			final DataSource source = new FileDataSource( file );
			messageBodyPart.setDataHandler( new DataHandler( source ) );
			messageBodyPart.setFileName( fileName + ".xls" );
			multipart.addBodyPart( messageBodyPart );

			message.setContent( multipart );

			Transport.send( message );


		} catch (final MessagingException e) {
			System.err.println("TrdeValidator: sendEmail: fail to send " + mailSubject);
			e.printStackTrace();
		}
	}
}
