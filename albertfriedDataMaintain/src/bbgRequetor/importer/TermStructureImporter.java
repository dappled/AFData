package bbgRequetor.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRquestor.bloomberg.beans.SecurityTimeUnit;

/**
 * @author Zhenghong Dong
 */
public class TermStructureImporter extends BbgImporterBase {
	private final String[]	names	= { "USSO1Z Curncy", "USSO2Z Curncy","USSO3Z Curncy","USSOA Curncy","USSOB Curncy","USSOC Curncy","USSOD Curncy","USSOE Curncy","USSOF Curncy","USSOI Curncy","USSO1 Curncy","USSO1F Curncy","USSO2 Curncy","USSO3 Curncy","USSO4 Curncy","USSO5 Curncy" };
	private final String[]	fields	= { Fields.last };

	public TermStructureImporter(String dbServer, String catalog) throws Exception {
		super( dbServer, catalog );
	}

	@Override
	protected void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception {
		/** ask client for yield curve data */
		List<SecurityTimeUnit> res = null;
		res = _server.publishRefQuest( _queueName, Arrays.asList( names ), Arrays.asList( fields ) );
	
		/** upload data to the database */
		if (res != null) {
			PreparedStatement insertTermStructure = null;
			String insertString = "insert into " + dbName + " ([Symbol], [Cusip], [Close], [Date]) values (?,?,?,cast('"
					+ tradeDate + "' as Date))";

			try {
				// set up statement
				_conn.setAutoCommit( false );
				insertTermStructure = _conn.prepareStatement( insertString );

				for (SecurityTimeUnit t : res) {
					String name = t.getName().replace( " Curncy", "" );
					insertTermStructure.setString( 1, name );
					insertTermStructure.setString( 2, name ); // for term structure, cusip == symbol
					insertTermStructure.setDouble( 3, t.getLast() );
					insertTermStructure.executeUpdate();
					_conn.commit();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				if (_conn != null) {
					System.err.print( "Transaction is being rolled back" );
					_conn.rollback();
				}
			} finally {
				if (insertTermStructure != null) {
					insertTermStructure.close();
				}
				_conn.setAutoCommit( true );
			}
		}
	}

}
