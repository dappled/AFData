package exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dataWrapper.exporter.bbu.PortfolioElement;
import dataWrapper.exporter.bbu.Stock;

/**
 * @author Zhenghong Dong
 */
public class BBU extends ExporterBase {
	/**
	 * @param dbServer
	 * @param catalog
	 */
	public BBU(String dbServer, String catalog) {
		super( dbServer, catalog );
		setFTPInfo( "u25210279", "ly6P%XS+" );
	}

	@Override
	public void report(String outFile, String date) throws Exception {
		List<Stock> stocks = getStock();
		List<PortfolioElement> portfolioElements = getPortfolioElement();
		exportCSV( stocks, portfolioElements, outFile );
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
				options.add( new PortfolioElement( rs.getString( j++ ), // symbol
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
