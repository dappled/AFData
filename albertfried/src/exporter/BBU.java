package exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dataWrapper.exporter.bbu.PortfolioElement;
import dataWrapper.exporter.bbu.Stock;

/**
 * @author Zhenghong Dong
 */
public class BBU extends ExporterBase {
	private HashMap<String, String>	_nameCorrection;
	private final String			_bbuUserName		= "u25210279";
	private final String			_bbuPassword		= "ly6P%XS+";
	private final String			_vineyardUserName	= "Vinyard";
	private final String			_vineyardPassword	= "v1n3yrd";

	/**
	 * @param dbServer
	 * @param catalog
	 */
	public BBU(String dbServer, String catalog) {
		super( dbServer, catalog );
		getNameCorrection();
	}

	/** get Bbu Name corrections from database */
	private void getNameCorrection() {
		_nameCorrection = new HashMap<>();

		final String query = "SELECT [Symbol], [DealId] from [clearing].[dbo].[BbuSymbolCorrection]";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				_nameCorrection.put( rs.getString( j++ ).trim(), rs.getString( j++ ).trim() );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void report(String outFile, String date, String ftpAddress) throws Exception {
		List<Stock> stocks = getStock();
		List<PortfolioElement> portfolioElements = getPortfolioElement();
		exportCSV( stocks, portfolioElements, outFile );

		if (ftpAddress != null) {
			for (String address : ftpAddress.split( ";" )) {
				String[] detail = address.split( ":" );
				if (detail[ 0 ].equals( "vineyard" )) {
					this.uploadFtp( outFile, detail[ 1 ], _vineyardUserName, _vineyardPassword );
				} else if (detail[ 0 ].equals( "bbu" )) {
					this.uploadFtp( outFile, detail[ 1 ], _bbuUserName, _bbuPassword );
				} else throw new Exception( "ftp type should be either vinyard or bbu" );
			}
		}
	}

	private List<Stock> getStock() {
		List<Stock> stocks = new ArrayList<>();
		final String query = "SELECT * FROM clearing.dbo.ActiveSymbolList";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				stocks.add( new Stock( rs.getString( j++ ) // symbol
				) );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stocks;
	}

	private List<PortfolioElement> getPortfolioElement() {
		List<PortfolioElement> options = new ArrayList<>();
		final String query = "SELECT * from clearing.dbo.GSECPos_For_Blmbrg";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				options.add( new PortfolioElement( correctName( rs.getString( j++ ) ), // symbol
						rs.getFloat( j++ ), // tdQty
						rs.getDate( j++ ), // maturity
						rs.getFloat( j++ ), // strike
						rs.getString( j++ ), // putCall
						rs.getFloat( j++ ), // baseMarketPrice
						rs.getString( j++ ), // OCCCode
						rs.getString( j++ ) // account
				) );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return options;
	}

	private String correctName(String name) {
		if (_nameCorrection.containsKey( name )) {
			return _nameCorrection.get( name );
		} else {
			return name;
		}
	}

	private void exportCSV(List<Stock> stocks, List<PortfolioElement> portfolioEliments, String outFile) throws Exception {
		String[] outFiles = outFile.split( ";" );
		if (outFiles.length != 2) { throw new Exception( "outFile should be in the format 'stock outfile; portfolioElements outfile'" ); }
		try {
			/* stock */
			File f = new File( outFiles[ 0 ] );
			if (f.exists()) f.delete();
			FileWriter _out = new FileWriter( outFiles[ 0 ] );
			// header
			_out.append( "Port,Symbol,Qty,Price,Date,Type\n" );

			for (Stock s : stocks) {
				s.writeCSV( _out );
			}
			_out.flush();
			_out.close();

			/* portfolio elements */
			f = new File( outFiles[ 1 ] );
			if (f.exists()) f.delete();
			_out = new FileWriter( outFiles[ 1 ] );
			_out.append( "Port,Symbol,Qty,Price,Date,Type\n" );

			for (PortfolioElement p : portfolioEliments) {
				p.writeCSV( _out );
			}
			_out.flush();
			_out.close();

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
