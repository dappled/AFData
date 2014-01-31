package exporter.validator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.ParseDate;
import utils.poi.PoiRecord;
import utils.poi.WriteXls;
import dataWrapper.exporter.validator.BrokerActivity;
import dataWrapper.exporter.validator.LocalActivity;
import dataWrapper.exporter.validator.RecordAbstract;

public class ActivityValidator extends ValidatorBase {
	public ActivityValidator() {}

	/***********************************************************************
	 * {@link ValidatorBase} methods
	 ***********************************************************************/
	@Override
	/** Read GSEC file */
	protected List<List<RecordAbstract>> readBrokerFile(final String brokerFile, final String tradeDate) throws Exception {
		final HashMap<RecordKey, BrokerActivity> gsMap = new HashMap<>(); // for normal equity and option b/s
		final List<RecordAbstract> gsOtherDay = new ArrayList<>(); // other day stuff
		final List<RecordAbstract> gsExtraList = new ArrayList<>(); // for OA/OE stuff

		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( brokerFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			while ((line = reader.readLine()) != null) {
				final String[] list = line.split( "\\|" );
				if (list.length == 5) {
					break; // trailer
				} else if (list.length != 293) throw new Exception(
						"ActivityValidator: TrdFile corrupted, inapproporate line: " + line );
				else {
					// add new record
					final BrokerActivity record = brokerGetActivity( list );
					final RecordKey key = new RecordKey( record );
					// combine records with same (symbol, type, tradeDate, side) pair
					if ((key._type.equals( "equity" ) || key._type.equals( "option" ))
							&& (key._tradeDate.equals( tradeDate ))) {
						if (gsMap.containsKey( key )) {
							gsMap.get( key ).add( record );
						} else {
							gsMap.put( key, record );
						}
					} else if (!key._tradeDate.equals( tradeDate )) { // for other day stuff
						gsOtherDay.add( record );
					} else {// for OA/OE... stuff
						gsExtraList.add( record );
					}
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

		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( new ArrayList<RecordAbstract>( gsMap.values() ) );
		ret.add( gsOtherDay );
		ret.add( gsExtraList );
		return ret;
	}

	/** parse a string list into an activity */
	private BrokerActivity brokerGetActivity(final String[] list) throws Exception {
		// tradeDate
		final String tradeDate = ParseDate.standardFromyyyyMMdd( list[ 72 ].trim() );
		// symbol
		String symbol;
		String type = list[ 14 ].trim().toLowerCase();
		if (type.equals( "equity" )) {
			symbol = list[ 16 ].trim();
		} else if (type.equals( "option" )) {
			symbol = ActivityValidator.parseOptionName( list[ 65 ].trim() );
		} else throw new Exception( "ActivityValidator: record neither equity nor option." );
		// qty
		final int qty = (int) Double.parseDouble( list[ 70 ].trim() ); // not sure
		// type
		if (!list[ 203 ].trim().equals( "" )) {
			type += ("," + list[ 203 ].trim()); // "OA" "OE" "AJC" etc
		} else if (list[ 200 ].startsWith( "A/C" ) || list[ 200 ].startsWith( "TA/C" )) {
			type += ("," + list[ 200 ].trim().split( " " )[ 0 ]); // "TA/C" "A/C" stuff
		}

		// side
		final String side = qty > 0 ? "B" : "S"; // buy or sell
		// description
		final String description = list[ 200 ].trim(); // added information about this record
		// price
		final double price = Math.abs( Double.parseDouble( list[ 82 ].trim() ) );
		// settlePrincipalAmount
		final double principal = -Double.parseDouble( list[ 83 ].trim() );
		return new BrokerActivity( tradeDate, symbol, type, side, qty, price, principal, description );
	}

	/**
	 * parse things like "DVN  DEC 21 2013  65.000 C" to "DVB 12/21/2013 C 65" to compare to tradesummary file format.
	 * Ugly and non generic...
	 */
	private static String parseOptionName(final String data) {
		try {
			final StringTokenizer st = new StringTokenizer( data );
			final String namePart = st.nextToken();
			final String datePart = st.nextToken() + " " + st.nextToken() + " " + st.nextToken();
			final String[] lastPart = { st.nextToken(), st.nextToken() };
			final double strike = Double.parseDouble( lastPart[ 0 ] );
			String ret = String.format( "%s %s %s", namePart, ParseDate.standardFromStringMonthTypeOne( datePart ),
					lastPart[ 1 ] );
			if (strike == (int) strike) return String.format( "%s %d", ret, (int) strike );
			else return String.format( "%s %s", ret, strike );
		} catch (final Exception e) {
			System.err.println( "TrdeValidator: Failed to parse option name " + data + ", will use empty string" );
			e.printStackTrace();
			return "";
		}
	}

	@Override
	/** Read TradeSummary file */
	protected List<List<RecordAbstract>> readLocalFile(final String summaryFile, final String tradeDate)
			throws Exception {
		final HashMap<RecordKey, LocalActivity> summaryMap = new HashMap<>();
		final List<RecordAbstract> summaryOtherDay = new ArrayList<>();
		final List<RecordAbstract> summaryExtraList = new ArrayList<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( summaryFile ) );
			reader.readLine(); // ignore header

			// read fields
			String line = null;
			while ((line = reader.readLine()) != null) {
				final String[] list = line.split( "," );
				if (!list[ 1 ].trim().equals( "WIC01" )) {
					continue; // ignore account other than "WIC01"
				}
				final String symbol = list[ 2 ].trim();
				final String description = list[ 6 ].trim();
				String type = (symbol.split( " " ).length == 1) ? "equity" : "option";
				type += (description.contains( "Exercise" ) || description.contains( "Assignment" ) || description.contains( "SA" ) || description
						.contains( "OA" )) ? ("," + description) : "";
				final String side = list[ 3 ].trim();
				final double price = Double.parseDouble( list[ 5 ].trim() );
				final int qty = summaryGetQty( side, Integer.parseInt( list[ 4 ].trim() ) );

				final LocalActivity record = new LocalActivity( list[ 0 ].trim(), // date
						symbol, // symbol
						type, // type
						side, // side
						qty, // qty
						price, // price
						description ); // description
				final RecordKey key = new RecordKey( record );
				// combine records with same (symbol, type, tradeDate, side) pair
				if ((type.equals( "equity" ) || type.equals( "option" )) && (tradeDate.equals( key._tradeDate ))) {
					if (summaryMap.containsKey( key )) {
						summaryMap.get( key ).add( record );
					} else {
						summaryMap.put( key, record );
					}
				} else if (!tradeDate.equals( key._tradeDate )) {
					summaryOtherDay.add( record );
				} else {
					summaryExtraList.add( record );
				}
			}
		} catch (final NumberFormatException e) {
			System.err
					.println( "TrdeValidator: Corrupted tradeSummary file, can't parse numbers (price, quantity etc)" );
			e.printStackTrace();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}

		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( new ArrayList<RecordAbstract>( summaryMap.values() ) );
		ret.add( summaryOtherDay );
		ret.add( summaryExtraList );
		return ret;
	}

	/** return the quantity given trade side and abs quantity */
	private int summaryGetQty(final String side, final int quantity) throws Exception {
		if (side.equals( "B" )) return quantity;
		else if (side.equals( "S" )) return -1 * quantity;
		else throw new Exception( "TrdeValidator: Incorrect trade side in tradeSummary file" );
	}

	@Override
	/** export to xls file */
	protected void exportXls(final String outFileName,
			final List<RecordAbstract> summaryDiffList, final List<RecordAbstract> GSECDiffList,
			final List<RecordAbstract> summaryOtherDay, final List<RecordAbstract> GSECOtherDay,
			final List<RecordAbstract> summaryExtraList, final List<RecordAbstract> GSECExtraList,
			final List<RecordAbstract> summaryList, final List<RecordAbstract> GSECList) throws Exception {
		try {
			// create sheet
			final Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet( "difference" );
			createHeader( wb, sheet );
			sheet = wb.createSheet( "activity of different day" );
			createHeader( wb, sheet );
			sheet = wb.createSheet( "OA OE DIV AJC etc records" );
			createHeader( wb, sheet );
			final FileOutputStream fileOut = new FileOutputStream( outFileName );
			wb.write( fileOut );
			fileOut.close();

			// sizeList
			final List<Integer> sizeList = new ArrayList<>();
			sizeList.add( LocalActivity.size() );
			sizeList.add( BrokerActivity.size() );

			// add difference
			List<List<? extends PoiRecord>> tmp = new ArrayList<>();
			tmp.add( summaryDiffList );
			tmp.add( GSECDiffList );
			WriteXls.appendMultipleRecords( outFileName, "difference", tmp, sizeList );

			// add other day stuff
			tmp = new ArrayList<>();
			tmp.add( summaryOtherDay );
			tmp.add( GSECOtherDay );
			WriteXls.appendMultipleRecords( outFileName, "activity of different day", tmp, sizeList );

			// add OA/OE/AJC etc stuff
			tmp = new ArrayList<>();
			tmp.add( summaryExtraList );
			tmp.add( GSECExtraList );
			WriteXls.appendMultipleRecords( outFileName, "OA OE DIV AJC etc records", tmp, sizeList );

			// add reformatted tradesummary stuff
			WriteXls.appendSingleRecord( outFileName, "aggregated tradesummary", summaryList );

			// add GSEC stuff
			WriteXls.appendSingleRecord( outFileName, "aggregated GSEC", GSECList );

		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/** Add header file */
	private void createHeader(final Workbook wb, final Sheet sheet) {
		final CreationHelper createHelper = wb.getCreationHelper();

		// add header
		Row row = sheet.createRow( (short) 0 );
		row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "In TradeSummary but not GSEC" ) );
		row.createCell( 9 ).setCellValue( createHelper.createRichTextString( "In GSEC but not TradeSummary" ) );

		row = sheet.createRow( (short) 1 );
		row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "TradeDate" ) );
		row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Type" ) );
		row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Side" ) );
		row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 5 ).setCellValue( createHelper.createRichTextString( "Price" ) );
		row.createCell( 6 ).setCellValue( createHelper.createRichTextString( "Description" ) );

		row.createCell( 9 ).setCellValue( createHelper.createRichTextString( "TradeDate" ) );
		row.createCell( 10 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 11 ).setCellValue( createHelper.createRichTextString( "Type" ) );
		row.createCell( 12 ).setCellValue( createHelper.createRichTextString( "Side" ) );
		row.createCell( 13 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 14 ).setCellValue( createHelper.createRichTextString( "Price" ) );
		row.createCell( 15 ).setCellValue( createHelper.createRichTextString( "Principal" ) );
		row.createCell( 16 ).setCellValue( createHelper.createRichTextString( "Description" ) );
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/** key features that distinguish record from each other */
	private class RecordKey {
		public String	_symbol;
		public String	_type;
		public String	_side;
		public String	_tradeDate;

		public RecordKey(final BrokerActivity record) {
			_symbol = record.getSymbol();
			_type = record.getType();
			_side = record.getSide();
			_tradeDate = record.getTradeDate();
		}

		public RecordKey(final LocalActivity record) {
			_symbol = record.getSymbol();
			_type = record.getType();
			_side = record.getSide();
			_tradeDate = record.getTradeDate();
		}

		@Override
		public boolean equals(final Object record) {
			if (record == null) return false;
			if (record == this) return true;
			if (!(record instanceof RecordKey)) return false;
			return (_tradeDate.equals( ((RecordKey) record)._tradeDate ) && // compare trade date
					_symbol.equals( ((RecordKey) record)._symbol ) && // compare symbol
					_type.equals( ((RecordKey) record)._type ) && // compare type
			_side.equals( ((RecordKey) record)._side )); // compare side
		};

		@Override
		public int hashCode() {
			return _tradeDate.hashCode() * 13 + _symbol.hashCode() * 31 + _type.hashCode() * 17 + _side.hashCode() * 29;
		}
	}
}
