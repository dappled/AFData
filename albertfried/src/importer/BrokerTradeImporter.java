package importer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Zhenghong Dong
 */
public class BrokerTradeImporter extends ImporterBase {
	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public BrokerTradeImporter(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	@Override
	protected void dumpHelper(String localFile, String dbName, String tradeDate) throws Exception {
		PreparedStatement insertBrokerTrade = null;
		PreparedStatement searchBrokerTrade = null;
		String searchString = "select * from "
				+ dbName
				+ " where TradeDate = ? and Symbol = ? and Maturity = ? and StrikePrice = ? and CallPut = ? and Side = ? and Quantity = ? and AvgPrice = ? and Broker = ?";
		String insertString = "insert into " + dbName + " (Symbol,Maturity,StrikePrice,CallPut,Side, Quantity,AvgPrice,Broker) values (?,?,?,?,?,?,?,?)";

		BufferedReader reader = null;
		try {
			// set up statement
			_conn.setAutoCommit( false );
			insertBrokerTrade = _conn.prepareStatement( insertString );
			searchBrokerTrade = _conn.prepareStatement( searchString );
			// read file
			reader = new BufferedReader( new FileReader( localFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			String nextLine = null;
			try {
				line = reader.readLine();
			} catch (final ArrayIndexOutOfBoundsException e) {
				System.out.println( "BrokerTradeImporter: " + localFile
						+ " corrupted" );
			}
			String[] list;
			while ((nextLine = reader.readLine()) != null) {
				list = line.split( " " );
				if (list.length != 6) throw new Exception(
						"BrokerTradeImporter: " + localFile + " corrupted, inapproporate line: " + line );
				else {
					// test if exists
					searchBrokerTrade.setString( 1, list[ 0 ].trim() );
					searchBrokerTrade.setString( 2, list[ 1 ].trim() );
					String sc = list[ 2 ].trim();
					searchBrokerTrade.setFloat( 3, Float.parseFloat( sc.substring( 0, sc.length() ) ) );
					searchBrokerTrade.setString( 4, sc.substring( sc.length() ).toUpperCase() );
					searchBrokerTrade.setString( 5, Float.parseFloat( list[ 3 ].trim() ) > 0 ? "B" : "S" );
					searchBrokerTrade.setFloat( 6, Float.parseFloat( list[ 3 ].trim() ) );
					searchBrokerTrade.setFloat( 7, Float.parseFloat( list[ 5 ].trim() ) );
					searchBrokerTrade.setString( 8, list[ 6 ].trim() );
					ResultSet rs = searchBrokerTrade.executeQuery();
					if (!rs.next()) {
						// insert into database if doesn't exist
						insertBrokerTrade.setString( 1, list[ 0 ].trim() );
						insertBrokerTrade.setString( 2, list[ 1 ].trim() );
						insertBrokerTrade.setFloat( 3, Float.parseFloat( sc.substring( 0, sc.length() ) ) );
						insertBrokerTrade.setString( 4, sc.substring( sc.length() ).toUpperCase() );
						insertBrokerTrade.setString( 5, Float.parseFloat( list[ 3 ].trim() ) > 0 ? "B" : "S" );
						insertBrokerTrade.setFloat( 6, Float.parseFloat( list[ 3 ].trim() ) );
						insertBrokerTrade.setFloat( 7, Float.parseFloat( list[ 5 ].trim() ) );
						insertBrokerTrade.setString( 8, list[ 6 ].trim() );
						
						
						insertBrokerTrade.executeUpdate();
						_conn.commit();
						
						
					}
				}
				line = nextLine;
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
			if (insertBrokerTrade != null) {
				insertBrokerTrade.close();
			}
			_conn.setAutoCommit( true );
			reader.close();
		}
	}
}
