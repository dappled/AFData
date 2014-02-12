package bbgRequestor.importer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import middleware.bbg.beans.DataRequest.RequestType;
import middleware.bbg.beans.RefResultContainer;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRequestor.bloomberg.beans.SecurityTimeUnit;

/**
 * @author Zhenghong Dong
 */
public class TermStructureImporter extends BbgImporterBase {
	private final String[]	_names	= { "USSO1Z Curncy", "USSO2Z Curncy", "USSO3Z Curncy", "USSOA Curncy", "USSOB Curncy", "USSOC Curncy", "USSOD Curncy",
									"USSOE Curncy", "USSOF Curncy", "USSOI Curncy", "USSO1 Curncy", "USSO1F Curncy", "USSO2 Curncy", "USSO3 Curncy",
			"USSO4 Curncy", "USSO5 Curncy" };
	private final String[]	_fields	= { Fields.last };

	public TermStructureImporter(String dbServer, String catalog) throws Exception {
		super( dbServer, catalog );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception {
		/** ask client for yield curve data */
		Set<String> stocks = new HashSet<>( Arrays.asList( _names ) );
		RefResultContainer container = new RefResultContainer( RequestType.Div, stocks );
		List<String> tmp = new ArrayList<>();
		tmp.addAll( stocks);
		_quester.publishRefQuest( RequestType.Sec, tmp, new HashSet<String>( Arrays.asList( _fields ) ), container );

		while (container.isFinished() == 0) {
			Thread.sleep( 3000 );
		}

		if (container.isFinished() > 0) {
			/** upload data to the database */
			PreparedStatement insertTermStructure = null;
			String insertString = "insert into " + dbName + " ([Symbol], [Cusip], [Close], [Date]) values (?,?,?,cast('"
					+ tradeDate + "' as Date))";

			try {
				// set up statement
				_conn.setAutoCommit( false );
				insertTermStructure = _conn.prepareStatement( insertString );

				for (SecurityTimeUnit t : (List<SecurityTimeUnit>) container.getSolution()) {
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
