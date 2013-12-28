package importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Zhenghong Dong
 */
public class ETBImporter extends ImporterBase {

	public ETBImporter(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	@Override
	protected void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception {
		PreparedStatement insertETB = null;
		String insertString = "insert into " + dbName + " (ImportedDate,Symbol) values (cast('"
				+ tradeDate + "' as Date),?)";

		BufferedReader reader = null;
		try {
			// set up statement
			_conn.setAutoCommit( false );
			insertETB = _conn.prepareStatement( insertString );

			// read file
			reader = new BufferedReader( new FileReader( localFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			String nextLine = null;
			try {
				line = reader.readLine();
			} catch (final ArrayIndexOutOfBoundsException e) {
				System.out.println( "ETBImporter: " + localFile
						+ " corrupted, at least two lines should be here (header and tailer)" );
			}
			int count = 0;
			String[] list;
			while ((nextLine = reader.readLine()) != null) {
				list = line.split( "\\|" );
				if (list.length != 2) throw new Exception(
						"ETBImporter: " + localFile + " corrupted, inapproporate line: " + line );
				else {
					// insert into database
					insertETB.setString( 1, list[ 0 ].trim() );
					insertETB.executeUpdate();
					_conn.commit();
					count++;
				}
				line = nextLine;
			}
			// check trailer for file integration
			if ((list = line.split( "\\|" )).length != 2) throw new Exception(
					"ETBImporter: " + localFile + " corrupted, inapproporate line: " + line );
			else if (count != Integer.parseInt( list[ 1 ] )) throw new Exception(
					"ETBImporter: " + localFile + " corrupted, number of records read is not same as in the tailer" );
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
			if (_conn != null) {
				System.err.print( "Transaction is being rolled back" );
				_conn.rollback();
			}
		} finally {
			if (insertETB != null) {
				insertETB.close();
			}
			_conn.setAutoCommit( true );
			reader.close();
		}
	}
}
