package bbgRequestor.exporter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import middleware.bbg.beans.DataRequest.RequestType;
import middleware.bbg.beans.RefResultContainer;
import utils.ParseDate;
import utils.StringUtils;
import bbgRequestor.bloomberg.BbgNames.Fields;
import bbgRequestor.bloomberg.beans.DividendTimeUnit;
import bbgRequestor.exporter.beans.Option;

/**
 * @author Zhenghong Dong
 */
public class OptionableStocks extends BbgExporterBase {
	private final String[]			_fields	= { Fields.exDate, Fields.divAmount, Fields.nextExDate, Fields.nextDivAmount, Fields.close };
	private HashMap<String, String>	_nameCorrection;

	/**
	 * @param dbServer
	 * @param catalog
	 * @throws Exception
	 */
	public OptionableStocks(String dbServer, String catalog) throws Exception {
		super( dbServer, catalog );
		getNameCorrection();
	}

	/** get Occ Name corrections from database */
	private void getNameCorrection() {
		_nameCorrection = new HashMap<>();

		final String query = "SELECT [OccSymbol], [ActualSymbol] from [clearing].[dbo].[OccSymbolCorrection]";

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
	public void report(String inFile, String outFile, String date) throws Exception {
		String[] outFiles = outFile.split( ";" );
		if (outFiles.length != 2) { throw new Exception( "outFile should be in the format 'optionable stock outfile; exercisable option outfile'" ); }

		// store all stocks satisfying our near dividend criteria
		HashMap<String, DividendTimeUnit> stockWithNearDiv = new HashMap<>();
		bbgGetStockWithNearDiv( stockWithNearDiv, inFile, outFiles[ 0 ] );
		// saveTestTime( stockWithNearDiv, outFiles[ 0 ] );

		// get all positions from database
		LinkedList<Option> calls = getOptionsFromDb( "C" );
		LinkedList<Option> puts = getOptionsFromDb( "P" );

		fileterOutOptions( calls, stockWithNearDiv, "C" );
		fileterOutOptions( puts, stockWithNearDiv, "P" );

		calls.addAll( puts );
		writeExercisable( calls, outFiles[ 1 ] );
	}

	@SuppressWarnings("unchecked")
	private void bbgGetStockWithNearDiv(HashMap<String, DividendTimeUnit> stockWithNearDiv, String inFile, String outFile) throws Exception {
		/* get optionable stock names */
		Set<String> stocks = getOptionableStocks( inFile );

		/* get their div data from bbg */
		try {
			/* result writer */
			final FileWriter writer = new FileWriter( outFile );
			// header
			writer.append( "Symbol,DVD_EX_DT,LAST_DPS_GROSS,BDVD_NEXT_EST_EX_DT,BDVD_PROJ_DIV_AMT,Close\n" );

			RefResultContainer container = new RefResultContainer( RequestType.Div, stocks );
			_quester.publishRefQuest( RequestType.Div, stocks, new HashSet<String>( Arrays.asList( _fields ) ), container );

			while (container.isFinished() == 0) {
				Thread.sleep( 3000 );
			}

			if (container.isFinished() > 0) {
				for (DividendTimeUnit tu : (List<DividendTimeUnit>) container.getSolution()) {
					// record only if this stock has ex_dt in next two business day
					if (divInNearFuture( tu )) {
						stockWithNearDiv.put( tu.getName(), tu );
						writer.append( tu.getName() );
						writer.append( ',' );
						writer.append( tu.getExDate() );
						writer.append( ',' );
						writer.append( StringUtils.numberToStringWithoutZeros( tu.getAmount() ) );
						writer.append( ',' );
						writer.append( tu.getNextExDate() );
						writer.append( ',' );
						writer.append( StringUtils.numberToStringWithoutZeros( tu.getNextAmount() ) );
						writer.append( ',' );
						writer.append( tu.getExtra() );
						writer.append( '\n' );
					}
				}
			}
			writer.flush();
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
			this.close();
		}
	}

	private Set<String> getOptionableStocks(String localFile) throws IOException {
		Set<String> res = new HashSet<>();

		BufferedReader in = null;
		try {
			in = new BufferedReader( new FileReader( localFile ) );
			String line;
			String last = "", current;
			while ((line = in.readLine()) != null) {
				current = line.split( "\t" )[ 1 ].trim();
				if (_nameCorrection.containsKey( current )) {
					String correct = _nameCorrection.get( current );
					if (correct.equals( "ignore" )) continue;
					current = correct;
				}
				if (current.equals( last )) continue; // in optionable stock file, underlying might be duplicate
				last = current;
				res.add( current + " US Equity" );
			}
		} catch (FileNotFoundException e) {
			System.err.println( "No such file exists: " + localFile );
			;
		} catch (IOException e) {
			System.err.println( "Error in reading file " + localFile );
		} finally {
			if (in != null) in.close();
		}

		return res;
	}

	// check if there is a dividend in two days
	private boolean divInNearFuture(DividendTimeUnit tu) throws Exception {
		if (tu.getExDate() == null) return false;
		return (ParseDate.compare( tu.getExDate(), ParseDate.twoDaysLater ) <= 0 && ParseDate.compare( tu.getExDate(), ParseDate.today ) > 0);
	}

	/** get options from databse given type */
	private LinkedList<Option> getOptionsFromDb(String type) {
		LinkedList<Option> res = new LinkedList<>();
		final String query = "SELECT [TradingSymbol], [Underlying], [Maturity], [Strike], [PutCall] from [clearing].[dbo].[GSECPositions] " +
				" where [PutCall] = '" + type + "' and [ImportedDate] = '" + ParseDate.today + "' and [TDQuantity] > 0";

		try (Statement stmt = _conn.createStatement()) {

			ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				int j = 1;
				res.add( new Option( rs.getString( j++ ), // symbol
						rs.getString( j++ ) + " US Equity", // underlying
						rs.getDate( j++ ), // maturity
						rs.getFloat( j++ ), // strike
						rs.getString( j++ ) // putCall
				) );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * Filter out options out of money and not in stockWithNearDiv list
	 * @param options
	 * @param stockWithNearDiv
	 * @throws Exception
	 */
	private void fileterOutOptions(LinkedList<Option> options, HashMap<String, DividendTimeUnit> stockWithNearDiv, String type) throws Exception {
		Set<String> namesInStockWithNearDiv = getName( stockWithNearDiv );
		for (ListIterator<Option> iterator = options.listIterator(); iterator.hasNext();) {
			Option o = iterator.next();
			if (o.getSymbol().contains( "TEST" )) {
				// System.out.println("in");
			}
			// don't need options not have dividend in the near future nor out of money
			if (!namesInStockWithNearDiv.contains( o.getUnderlying() ) || !itm( o, stockWithNearDiv, type )) {
				iterator.remove();
			}
		}
	}

	// get the name of stocks
	private Set<String> getName(HashMap<String, DividendTimeUnit> stockWithNearDiv) {
		return stockWithNearDiv.keySet();
	}

	// see if option is in the money
	private boolean itm(Option o, HashMap<String, DividendTimeUnit> stockWithNearDiv, String type) throws Exception {
		switch (type) {
			case "C":
				return o.getStrike() < Double.parseDouble( stockWithNearDiv.get( o.getUnderlying() ).getExtra() );
			case "P":
				return o.getStrike() > Double.parseDouble( stockWithNearDiv.get( o.getUnderlying() ).getExtra() );
			default:
				throw new Exception( "option type should be either C or P" );
		}
	}

	// write the exercisable options to the outFile
	private void writeExercisable(List<Option> options, String outFile) {
		try {
			final FileWriter writer = new FileWriter( outFile );
			// header
			writer.append( "Symbol,Underlying,Maturity,Strike,PutCall\n" );

			for (Option option : options) {
				writer.append( option.getSymbol() );
				writer.append( ',' );
				writer.append( option.getUnderlying() );
				writer.append( ',' );
				writer.append( ParseDate.standardFromSQLDate( option.getMaturity() ) );
				writer.append( ',' );
				writer.append( StringUtils.numberToStringWithoutZeros( option.getStrike() ) );
				writer.append( ',' );
				writer.append( option.getPutCall() );
				writer.append( '\n' );
			}

			writer.flush();
			writer.close();

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/* private void saveTestTime(HashMap<String, DividendTimeUnit> stockWithNearDiv, String inFile) throws IOException {
	 * BufferedReader in = null;
	 * try {
	 * in = new BufferedReader( new FileReader( inFile ) );
	 * String line, name;
	 * String[] lines;
	 * DividendTimeUnit tu;
	 * in.readLine();
	 * while ((line = in.readLine()) != null) {
	 * lines = line.split( "," );
	 * name = lines[ 0 ];
	 * tu = new DividendTimeUnit( name );
	 * tu.setExDate( lines[ 1 ] );
	 * tu.setAmount( lines[ 2 ].equals( "null" ) ? null : Double.parseDouble( lines[ 2 ] ) );
	 * tu.setNextExDate( lines[ 3 ] );
	 * tu.setNextAmount( lines[ 4 ].equals( "null" ) ? null : Double.parseDouble( lines[ 4 ] ) );
	 * tu.setExtra( lines[5] );
	 * try{
	 * if (divInNearFuture( tu )) {
	 * stockWithNearDiv.put( name, tu );
	 * }} catch (Exception e) {
	 * e.printStackTrace( );
	 * System.err.println(tu.getName());
	 * System.err.println(tu.getExDate());
	 * }
	 * tu = null;
	 * }
	 * } catch (FileNotFoundException e) {
	 * System.err.println( "No such file exists: " + inFile );
	 * } catch (IOException e) {
	 * System.err.println( "Error when reading file: " + inFile );
	 * e.printStackTrace();
	 * } finally {
	 * if (in != null) in.close();
	 * }
	 * } */
}
