package dataWrapper.exporter.validator;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.poi.PoiRecord;

/**
 * Data structure that represents a single trade summary record
 * @author Zhenghong Dong
 */
public class LocalActivity extends ActivityAbstract {
	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public LocalActivity(final String date, final String symbol, final String type, final String side, final int qty,
			final double price, final String description) throws Exception {
		super( date, symbol, type, side, qty, price, description );
	}

	/***********************************************************************
	 * {@link PoiRecord} Methods
	 ***********************************************************************/
	@Override
	public void writeNextForMultipleRecords(final Workbook wb, final Row row, final int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getTradeDate() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getType() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSide() ) );
		row.createCell( i++ ).setCellValue( getQuantity() );
		row.createCell( i++ ).setCellValue( getPrice() );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getDescription() ) );
	}
	
	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int index) {
		Row row = sheet.createRow( index++ );
		writeNextForMultipleRecords( wb, row, 0 );
		return index;
	}
	
	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	@Override
	public String toString() {
		return String.format( "TradeDate: %s, Symbol: %s, Type: %s, Side: %s, Qty: %d, Price: %f, Description: %s",
				getTradeDate(), getSymbol(), getType(), getSide(), getQuantity(), getPrice(), getDescription() );
	}
	
	public static int size() { return 7; }
	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
}
