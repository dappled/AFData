package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
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

/**
 * @author Zhenghong Dong
 */
public abstract class GeneralImporterExporter {
	protected Connection	_conn;

	public GeneralImporterExporter() {
		_conn = null;
	}

	public GeneralImporterExporter(String dbServer, String catalog) {
		final String url = "jdbc:sqlserver://" + dbServer + ";integratedSecurity=true;";
		try {
			_conn = DriverManager.getConnection( url );
		} catch (final SQLException e) {
			System.err.println( "Fail to connect to " + dbServer );
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	public void close() throws SQLException {
		if (_conn != null) _conn.close();
	}

	/**
	 * Wipe every data who is older than 7 days
	 * @param dbName
	 */
	public void wipeData(final String dbName) {
		Calendar cal = Calendar.getInstance();
		String query;
		// wipe today's previous import if exists (so we won't import twice for today)
		if (!dbName.equals( "[Clearing].[dbo].[GSECPositions]" )) {
			cal.setTime( new Date() );
			query = "delete"
					+ " from " + dbName + " where [ImportedDate]=cast('" + ParseDate.standardFromDate( cal.getTime() )
					+ "' AS DATE)";

			try (Statement stmt = _conn.createStatement()) {

				stmt.executeUpdate( query );
				// System.out.println(re);
			} catch (SQLException e) {
				System.err.println( "Fail to wipe data from " + dbName + " for dates before "
						+ ParseDate.standardFromDate( cal.getTime() ) );
			}
		}

		// wipe data one week older
		cal.add( Calendar.DAY_OF_MONTH, -7 );
		query = "delete"
				+ " from " + dbName + " where [ImportedDate]<=cast('" + ParseDate.standardFromDate( cal.getTime() )
				+ "' AS DATE)";
		try (Statement stmt = _conn.createStatement()) {
			stmt.executeUpdate( query );
			// System.out.println(re);
		} catch (SQLException e) {
			System.err.println( "Fail to wipe data from " + dbName + " for dates before "
					+ ParseDate.standardFromDate( cal.getTime() ) );
		}
	}

	/**
	 * Send the result report to people
	 * @param outFileName
	 * @param addressList
	 */
	public void sendEMail(final String outFileName, final String mailSubject, final String addressList,
			final String date) {
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
			final Message message = new MimeMessage( session );
			message.setFrom( new InternetAddress( "rn@albertfried.com" ) );
			message.setRecipients( Message.RecipientType.TO,
					InternetAddress.parse( addressList ) );
			message.setSubject( mailSubject + " for " + date );

			MimeBodyPart messageBodyPart = new MimeBodyPart();

			final Multipart multipart = new MimeMultipart();

			messageBodyPart = new MimeBodyPart();
			final String file = outFileName;
			final DataSource source = new FileDataSource( file );
			messageBodyPart.setDataHandler( new DataHandler( source ) );
			messageBodyPart.setFileName( mailSubject + date + ".xls" );
			multipart.addBodyPart( messageBodyPart );

			message.setContent( multipart );

			Transport.send( message );

		} catch (final MessagingException e) {
			System.err.println( "TrdeValidator: sendEmail: fail to send " + mailSubject );
			e.printStackTrace();
		}
	}
}
