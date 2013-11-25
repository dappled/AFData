package dataWrapper.exporter.validator;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.poi.PoiRecord;

/**
 * Data structure that represents a single activity summary record
 * @author Zhenghong Dong
 */
public class BrokerActivity extends ActivityAbstract {
	private double _principal;
	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public BrokerActivity(final String tradeDate, final String symbol, final String type, final String side, final int qty, final double price,
			final double principal, final String description) throws Exception {
		super( tradeDate, symbol, type, side, qty, price, description );
		_principal = principal;
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
		row.createCell( i++ ).setCellValue( getPrincipal() );
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
	public void add(ActivityAbstract record) {
		super.add(record);
		_principal += ((BrokerActivity) record).getPrincipal();
	}
	@Override
	public String toString() {
		return String.format( "TradeDate: %s, Symbol: %s, Type: %s, Side: %s, Price:%f, Qty: %d, Principal Amount: %f, Description: %s",
				getTradeDate(), getSymbol(), getType(), getSide(), getPrice(), getQuantity(), getPrincipal(), getDescription() );
	}
	
	public static int size() { return 8; }
	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public double getPrincipal() { return _principal; }
}
