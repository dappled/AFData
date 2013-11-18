package com.dong.dataValidator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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

import com.dong.dataWrapper.BrokerActivity;
import com.dong.dataWrapper.LocalActivity;
import com.dong.dataWrapper.Position;
import com.dong.dataWrapper.RecordAbstract;
import com.dong.utils.poi.PoiRecord;
import com.dong.utils.poi.WriteXls;

/**
 * @author Zhenghong Dong
 */
public class PositionValidator extends ValidatorBase {
	private Connection	_conn	= null;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public PositionValidator(final String dbServer, final String catalog) throws SQLException {
		super( dbServer, catalog );
		// String name="cmscim";

		final String url = "jdbc:sqlserver://" + dbServer + ";integratedSecurity=true;";
		try {
			_conn = DriverManager.getConnection( url );
		} catch (final SQLException e) {
			e.printStackTrace();
		}
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
		final String query = "select [BaseProduct],[TradingSymbol],[Strike],[PutCall],[TDQuantity],[PendingPosition],[ImportedDate]"
				+ " from " + brokerFile + " where [ImportedDate]=cast('" + tradeDate + "' AS DATE)";

		try (Statement stmt = _conn.createStatement()) {

			final ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				gsecPostion.add( new Position( rs.getString( 2 ), // symbol
						rs.getString( 1 ), // type
						rs.getDouble( 3 ), // strike: 0 for equity
						rs.getString( 4 ), // side: P/C for option, NULL for equity
						rs.getInt( 5 ), // quantity
						rs.getString( 6 ) ) );// pending position
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( gsecPostion );
		return ret;
	}

	/***********************************************************************
	 * Read Position_from_TradeBlotter view
	 ***********************************************************************/
	@Override
	protected List<List<RecordAbstract>> readLocalFile(final String localFile, final String tradeDate) {
		final List<RecordAbstract> tradeBlotterPosition = new ArrayList<>();
		final String query = "select [BaseProduct],[TradingSymbol],[Strike],[PutCall],[TDQuantity],[PendingPosition],[ImportedDate]"
				+ " from " + localFile + " where [ImportedDate]=cast('" + tradeDate + "' AS DATE)";

		try (Statement stmt = _conn.createStatement()) {

			final ResultSet rs = stmt.executeQuery( query );
			while (rs.next()) {
				tradeBlotterPosition.add( new Position( rs.getString( 2 ), // symbol
						rs.getString( 1 ), // type
						rs.getDouble( 3 ), // strike: 0 for equity
						rs.getString( 4 ), // side: P/C for option, null for equity
						rs.getInt( 5 ), // quantity
						rs.getString( 6 ) ) );// pending position
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		final List<List<RecordAbstract>> ret = new ArrayList<>();
		ret.add( tradeBlotterPosition );
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
			sizeList.add( LocalActivity.size() );
			sizeList.add( BrokerActivity.size() );

			// add difference
			List<List<? extends PoiRecord>> tmp = new ArrayList<>();
			tmp.add( tradeBlotterDiffList );
			tmp.add( GSECDiffList );
			WriteXls.append( outFileName, "difference", tmp, sizeList );

			// add reformatted tradesummary stuff
			tmp = new ArrayList<>();
			tmp.add( tradeBlotterList );
			WriteXls.append( outFileName, "position from tradeblotter", tmp, sizeList );

			// add GSEC stuff
			tmp = new ArrayList<>();
			tmp.add( GSECList );
			WriteXls.append( outFileName, "GSECPosition", tmp, sizeList );

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
		row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Type" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Strike" ) );
		row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Side" ) );
		row.createCell( 13 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 6 ).setCellValue( createHelper.createRichTextString( "PendingPostion" ) );

		row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "Symbol" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Type" ) );
		row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Strike" ) );
		row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Side" ) );
		row.createCell( 13 ).setCellValue( createHelper.createRichTextString( "Qty" ) );
		row.createCell( 6 ).setCellValue( createHelper.createRichTextString( "PendingPostion" ) );
	}

}
