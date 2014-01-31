package bbgRequetor.exporter;

import java.io.FileInputStream;
import java.io.IOException;

import middleware.bbg.BbgDataServer;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import utils.GeneralImporterExporter;

/**
 * @author Zhenghong Dong
 */
public abstract class BbgExporterBase extends GeneralImporterExporter {
	protected String		_FTPPassword	= null;
	protected String		_FTPUsername	= null;
	protected BbgDataServer	_server;
	protected final String	_queueName		= "BbgData";

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public BbgExporterBase(String dbServer, String catalog) throws Exception {
		super( dbServer, catalog );
		//_server = new BbgDataServer( null );
	}

	@Override
	public void close() throws Exception {
		super.close();
		if (_server != null) _server.publishSuicideQuest( _queueName );
		if (_server != null) _server.close();
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/**
	 * Generate report from inFile and save as outFile for certain date
	 * @param inFile
	 * @param outFile
	 * @param date
	 * @throws Exception
	 */
	public abstract void report(final String inFile, final String outFile, final String date) throws Exception;

	/**
	 * Upload files to ftp
	 * @param outFile files to be uploaded, should be seperated by ;
	 * @param ftpAddress
	 */
	public void uploadFtp(String outFile, String ftpAddress) {
		FTPClient client = new FTPClient();
		FileInputStream fis = null;

		int reply;
		boolean upload;
		try {
			client.connect( ftpAddress );
			client.login( getFTPUserName(), getFTPPassword() );
			reply = client.getReplyCode();

			if (!FTPReply.isPositiveCompletion( reply )) {
				client.disconnect();
				System.err.println( "FTP server refused connection." );
				System.exit( 1 );
			} else {
				for (String file : outFile.split( ";" )) {
					fis = new FileInputStream( file );
					// System.out.println( file.substring( file.lastIndexOf( "\\" ) + 1 ) );
					upload = client.storeFile( file.substring( file.lastIndexOf( "\\" ) + 1 ), fis );
					if (upload == false) {
						System.err.println( "Fail to upload " + file.substring( file.lastIndexOf( "\\" ) + 1 ) );
					}
					System.out.println( "Upload " + file.substring( file.lastIndexOf( "\\" ) + 1 ) + " successfully" );
					fis.close();
				}
			}
			client.logout();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String getFTPUserName() {
		return _FTPUsername;
	}

	protected String getFTPPassword() {
		return _FTPPassword;
	}

	protected void setFTPInfo(String userName, String psw) {
		_FTPUsername = userName;
		_FTPPassword = psw;
	}
}
