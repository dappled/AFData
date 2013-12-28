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

import dataWrapper.DailyTradeAbstract;
import dataWrapper.exporter.dailyTrade.Option;
import dataWrapper.exporter.dailyTrade.Stock;

/**
 * @author Zhenghong Dong
 */
public class GSUploader extends ExporterBase {
	private final String	_FTPPassword	= "side450a";
	private final String	_FTPUsername	= "u747113";

	/**
	 * @param dbServer
	 * @param catalog
	 */
	public GSUploader(String dbServer, String catalog) {
		super( dbServer, catalog );
	}

	@Override
	public void report(String outFile, String date) throws Exception {
		List<DailyTradeAbstract> stock = getStock();
		List<DailyTradeAbstract> option = getOption();

		exportCSV( stock, option, outFile );
	}

	private List<DailyTradeAbstract> getStock() {
		List<DailyTradeAbstract> stock = new ArrayList<>();
		final String query = "select * from Trading.dbo.GSEC_StockUpload";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				stock.add( new Stock( rs.getString( j++ ), // account
						rs.getString( j++ ), // symbol
						rs.getString( j++ ), // side
						rs.getFloat( j++ ), // qty
						rs.getFloat( j++ ), // price
						rs.getString( j++ ), // broker
						rs.getFloat( j++ ) ) ); // commission
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stock;
	}

	private List<DailyTradeAbstract> getOption() {
		List<DailyTradeAbstract> option = new ArrayList<>();
		final String query = "select * from Trading.dbo.GSEC_OptionUpload";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				option.add( new Option( rs.getString( j++ ), // account
						rs.getString( j++ ), // symbol
						rs.getString( j++ ), // side
						rs.getFloat( j++ ), // qty
						rs.getFloat( j++ ), // price
						rs.getString( j++ ), // broker
						rs.getString( j++ ), // year
						rs.getString( j++ ), // month
						rs.getString( j++ ), // day
						rs.getString( j++ ), // putcall
						rs.getFloat( j++ ) ) ); // strike
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return option;
	}

	private void exportCSV(List<DailyTradeAbstract> stock, List<DailyTradeAbstract> option, String outFile) throws Exception {
		String[] outFiles = outFile.split( ";" );
		if (outFiles.length != 2) { throw new Exception( "outFile should be in the format 'stock outfile; option outfile'" ); }
		try {
			/* stock */
			File f = new File( outFiles[ 0 ] );
			if (f.exists()) f.delete();
			FileWriter _out = new FileWriter( outFiles[ 0 ] );
			_out.append( "ACCOUNT," );
			_out.append( "SYMBOL," );
			_out.append( "SIDE," );
			_out.append( "QUANTITY," );
			_out.append( "PRICE," );
			_out.append( "BROKER," );
			_out.append( "COMM" );
			_out.append( '\n' );

			for (DailyTradeAbstract s : stock) {
				s.writeCSV( _out );
			}
			_out.flush();
			_out.close();

			/* option */
			f = new File( outFiles[ 1 ] );
			if (f.exists()) f.delete();
			_out = new FileWriter( outFiles[ 1 ] );
			_out.append( "ACCOUNT," );
			_out.append( "SYMBOL," );
			_out.append( "SIDE," );
			_out.append( "QUANTITY," );
			_out.append( "PRICE," );
			_out.append( "BROKER," );
			_out.append( "EXPYEAR," );
			_out.append( "EXPMONTH," );
			_out.append( "EXPDAY," );
			_out.append( "PUTCALL," );
			_out.append( "STRIKEPRICE" );
			_out.append( '\n' );

			for (DailyTradeAbstract o : option) {
				o.writeCSV( _out );
			}
			_out.flush();
			_out.close();

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getFTPUserName() {
		return _FTPUsername;
	}

	@Override
	protected String getFTPPassword() {
		return _FTPPassword;
	}

}
