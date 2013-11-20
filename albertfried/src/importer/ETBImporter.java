package importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

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

	    String insertString = "insert into " + dbName + " (ImportDate,Symbol) values (?,?)";
	    String key = "";
	    try {
	        _conn.setAutoCommit(false);
	        insertETB = _conn.prepareStatement(insertString);
	        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
	        Date parsed = (Date) format.parse(tradeDate);
	        insertETB.setDate(1, new java.sql.Date(parsed.getTime()));
	        insertETB.setString(2, key);
	        insertETB.executeUpdate();
	        _conn.commit();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        if (_conn != null) {
	                System.err.print("Transaction is being rolled back");
	                _conn.rollback();
	            
	        }
	    } finally {
	        if (insertETB != null) {
	            insertETB.close();
	        }
	        _conn.setAutoCommit(true);
	    }

		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( localFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			while ((line = reader.readLine()) != null) {
				final String[] list = line.split( "\\|" );
				if (list.length == 1) {
					break; // last line
				} else if (list.length != 293) throw new Exception(
						"ETBImporter: easytoborrowlist corrupted, inapproporate line: " + line );
				else {
					
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}

	}
}
