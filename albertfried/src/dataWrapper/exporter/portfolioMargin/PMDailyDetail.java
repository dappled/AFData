package dataWrapper.exporter.portfolioMargin;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import dataWrapper.PMAbstract;

/**
 * @author Zhenghong Dong
 */
public class PMDailyDetail extends PMAbstract {
	private final String	_id, _ticker, _maturity, _putCall;
	private final float		_strike, _quantity, _price;
	private final float[]	_movements	= new float[ 10 ];

	/**
	 * @param date
	 * @param symbol
	 * @param requirement
	 */
	public PMDailyDetail(String date, String id, String symbol, String ticker, String mat, String putCall, float strike, float qty, float price, float down5, float down4, float down3,
			float down2, float down1, float up1, float up2, float up3, float up4, float up5) {
		super( date, symbol );
		_id = id;
		_ticker = ticker;
		_maturity = mat;
		_putCall = putCall;
		_strike = strike;
		_quantity = qty;
		_price = price;
		// movement matrix
		_movements[ 0 ] = down5;
		_movements[ 1 ] = down4;
		_movements[ 2 ] = down3;
		_movements[ 3 ] = down2;
		_movements[ 4 ] = down1;
		_movements[ 5 ] = up1;
		_movements[ 6 ] = up2;
		_movements[ 7 ] = up3;
		_movements[ 8 ] = up4;
		_movements[ 9 ] = up5;
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getTicker() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getMaurity() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getPutCall() ) );
		row.createCell( i++ ).setCellValue( getStrike() );
		row.createCell( i++ ).setCellValue( getQuantity() );
		row.createCell( i++ ).setCellValue( getPrice() );
		row.createCell( i++ ).setCellValue( getMovements()[ 0 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 1 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 2 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 3 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 4 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 5 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 6 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 7 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 8 ] );
		row.createCell( i++ ).setCellValue( getMovements()[ 9 ] );
	}

	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int index) {
		Row row = sheet.createRow( index++ );
		writeNextForMultipleRecords( wb, row, 0 );
		return index;
	}

	/**
	 * This is used by PMDailyAnalysisSingle, which will only print the movement we are interest of
	 * @param wb
	 * @param row
	 * @param index
	 * @param interestedIdx the index we are interest of
	 */
	protected void writeNextForAnalysis(Workbook wb, Row row, int index, int interestedIdx) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getTicker() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getMaurity() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getPutCall() ) );
		row.createCell( i++ ).setCellValue( getStrike() );
		row.createCell( i++ ).setCellValue( getQuantity() );
		row.createCell( i++ ).setCellValue( getPrice() );
		row.createCell( i++ ).setCellValue( getMovements()[ interestedIdx ] );
	}

	public static int size() {
		return 16;
	}

	public String getMaurity() {
		return _maturity;
	}

	public float getStrike() {
		return _strike;
	}

	public float getQuantity() {
		return _quantity;
	}

	public float getPrice() {
		return _price;
	}

	public float[] getMovements() {
		return _movements;
	}

	public String getPutCall() {
		return _putCall;
	}
	
	public String getId() {
		return _id;
	}

	public String getTicker() {
		return _ticker;
	}
}
