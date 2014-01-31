package exporter.validator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.poi.PoiRecord;
import utils.poi.WriteXls;
import dataWrapper.exporter.validator.Position;
import dataWrapper.exporter.validator.RecordAbstract;

/**
 * @author Zhenghong Dong
 */
public class PositionValidator extends ValidatorBase {

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public PositionValidator(final String dbServer, final String catalog) throws SQLException {
		super( dbServer, catalog );
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	/***********************************************************************
	 * Read GSECPosition database
	 ***********************************************************************/
	@Override
	protected List<List<RecordAbstract>> readBrokerFile(final String brokerFile, final String tradeDate) {
		final List<RecordAbstract> gsecPostion = new ArrayList<>();
		final String query = "select  Account, TradingSymbol, BaseProduct, Strike, PutCall, TDQuantity, Maturity,Underlying"
				+ " from "
				+ brokerFile
				+ " where [ImportedDate]=cast('"
				+ tradeDate
				+ "' AS DATE) order by TradingSymbol";

		try (Statement stmt = _conn.createStatement()) {

			final ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				if (rs.getString( 2 ).equals( "" )) continue; // ignore record with symbol ""
				if (rs.getInt( 6 ) == 0) continue; // ignore record with quantity 0, Goldman keep records even expired,
													// which will have a quantity = 0
				gsecPostion.add( new Position(
						rs.getString( 1 ), // account
						rs.getString( 2 ), // symbol
						rs.getString( 3 ).toLowerCase(), // type
						rs.getDouble( 4 ), // strike: 0 for equity
						rs.getString( 5 ), // side: P/C for option, NULL for equity
						rs.getInt( 6 ), // quantity
						rs.getDate( 7 ), // maturity
						rs.getString( 8 ) ) );// underlying
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( gsecPostion );
		ret.add( new ArrayList<RecordAbstract>() );
		ret.add( new ArrayList<RecordAbstract>() );
		return ret;
	}

	/***********************************************************************
	 * Read Position_from_TradeBlotter view
	 ***********************************************************************/
	@Override
	protected List<List<RecordAbstract>> readLocalFile(final String localFile, final String tradeDate) {
		final List<RecordAbstract> tradeBlotterPosition = new ArrayList<>();
		final String query = "select Account, Symbol,Strike,CallPut,Quantity,ExpirationDate, UnderlyingSymbol"
				+ " from " + localFile + " order by Symbol";

		try (Statement stmt = _conn.createStatement()) {

			final ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				if (rs.getString( 2 ).equals( "" )) continue; // ignore stock with symbol ""
				tradeBlotterPosition.add( new Position(
						rs.getString( 1 ), // account
						rs.getString( 2 ), // symbol
						rs.getDouble( 3 ) == 0 ? "equity" : "option", // type
						rs.getDouble( 3 ), // strike: 0 for equity
						rs.getString( 4 ), // side: P/C for option, NULL for equity
						rs.getInt( 5 ), // quantity
						rs.getDate( 6 ), // maturity
						rs.getString( 7 ) ) ); // underlyingSymbol
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( tradeBlotterPosition );
		ret.add( new ArrayList<RecordAbstract>() );
		ret.add( new ArrayList<RecordAbstract>() );
		return ret;
	}

	/***********************************************************************
	 * {@link ValidatorBase} methods
	 ***********************************************************************/
	@Override
	protected void exportXls(final String outFileName,
			final List<RecordAbstract> tradeBlotterDiffList, final List<RecordAbstract> GSECDiffList,
			final List<RecordAbstract> tradeBlotterOtherDay, final List<RecordAbstract> GSECOtherDay,
			final List<RecordAbstract> tradeBlotterExtraList, final List<RecordAbstract> GSECExtraList,
			final List<RecordAbstract> tradeBlotterList, final List<RecordAbstract> GSECList) throws Exception {
		try {
			// create sheet
			final Workbook wb = new HSSFWorkbook();
			Sheet sheet = wb.createSheet( "difference" );
			createHeader( wb, sheet );
			final FileOutputStream fileOut = new FileOutputStream( outFileName );
			wb.write( fileOut );
			fileOut.close();

			// sizeList
			final List<Integer> sizeList = new ArrayList<>();
			sizeList.add( Position.size() );
			sizeList.add( Position.size() );

			// add difference
			List<List<? extends PoiRecord>> tmp = new ArrayList<>();
			tmp.add( tradeBlotterDiffList );
			tmp.add( GSECDiffList );
			WriteXls.appendMultipleRecords( outFileName, "difference", tmp, sizeList );

			// add reformatted tradesummary stuff
			WriteXls.appendSingleRecord( outFileName, "position from tradeblotter", tradeBlotterList );

			// add GSEC stuff
			WriteXls.appendSingleRecord( outFileName, "GSECPosition", GSECList );

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
		row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "In TradeBlotter but not GSEC" ) );
		row.createCell( 9 ).setCellValue( createHelper.createRichTextString( "In GSEC but not TradeBlotter" ) );

		row = sheet.createRow( (short) 1 );
		row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "Account" ) );
		row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Maturity" ) );
		row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Strike" ) );
		row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "PutCall" ) );
		row.createCell( 5 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 6 ).setCellValue( createHelper.createRichTextString( "Type" ) );

		row.createCell( 9 ).setCellValue( createHelper.createRichTextString( "Account" ) );
		row.createCell( 10 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 11 ).setCellValue( createHelper.createRichTextString( "Maturity" ) );
		row.createCell( 12 ).setCellValue( createHelper.createRichTextString( "Strike" ) );
		row.createCell( 13 ).setCellValue( createHelper.createRichTextString( "PutCall" ) );
		row.createCell( 14 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 15 ).setCellValue( createHelper.createRichTextString( "Type" ) );
	}

}
