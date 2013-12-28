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
public class PMRequirementImporter extends ImporterBase {

	public PMRequirementImporter(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	@Override
	protected void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception {
		PreparedStatement insertRequirement = null;
		String insertString = "insert into " + dbName
				+ " (ImportedDate,Symbol,SymbolType,Requirement,Risk,Minimum) values (cast('"
				+ tradeDate + "' as Date),?,?,?,?,?)";

		BufferedReader reader = null;
		try {
			// set up statement
			_conn.setAutoCommit( false );
			insertRequirement = _conn.prepareStatement( insertString );

			// read file
			reader = new BufferedReader( new FileReader( localFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			String[] list;
			int count = 0;
			while ((line = reader.readLine()) != null) {
				list = line.split( "\\|" );
				if (list.length == 5) {
					if (count != Integer.parseInt( list[ 3 ].trim() )) throw new Exception(
							"PMRequirementImporter: " + localFile + " corrupted, number of records read is not same as in the trailer" );
				} else if (list.length != 20) throw new Exception(
						"PMRequirementImporter: " + localFile + " corrupted, inapproporate line: " + line );
				else {
					// insert into database
					insertRequirement.setString( 1, list[ 14 ].trim() ); // symbol
					insertRequirement.setString( 2, list[ 15 ].trim() ); // symbolType
					insertRequirement.setFloat( 3, Float.parseFloat( list[ 16 ].trim() ) ); // requirement
					insertRequirement.setFloat( 4, Float.parseFloat( list[ 17 ].trim() ) ); // risk
					insertRequirement.setFloat( 5, Float.parseFloat( list[ 18 ].trim() ) ); // minimum
					insertRequirement.executeUpdate();
					_conn.commit();
					count++;
				}
			}
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
			if (insertRequirement != null) {
				insertRequirement.close();
			}
			_conn.setAutoCommit( true );
			reader.close();
		}
	}
}
