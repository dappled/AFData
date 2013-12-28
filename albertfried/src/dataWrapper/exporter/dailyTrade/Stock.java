package dataWrapper.exporter.dailyTrade;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.StringUtils;
import dataWrapper.DailyTradeAbstract;

/**
 * @author Zhenghong Dong
 */
public class Stock extends DailyTradeAbstract {
	private final float	_commission;

	public Stock(String account, String symbol, String side, Float quantity, Float price, String broker, Float commission) {
		super( account, symbol, side, quantity, price, broker );
		_commission = commission;
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {}

	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int j) {
		final CreationHelper createHelper = wb.getCreationHelper();
		Row row = sheet.createRow( ++j );
		int i = 0;
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getAccount() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSide() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( StringUtils.numberToStringWithoutZeros( getQuantity() ) ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( StringUtils.numberToStringWithoutZeros( getPrice() ) ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getBroker() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( StringUtils.numberToStringWithoutZeros( getCommission() ) ) );
		return j;
	}

	public float getCommission() {
		return _commission;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {
		out.append( getAccount() );
		out.append( ',' );
		out.append( getSymbol() );
		out.append( ',' );
		out.append( getSide() );
		out.append( ',' );
		out.append( StringUtils.numberToStringWithoutZeros( getQuantity() ) );
		out.append( ',' );
		out.append( StringUtils.numberToStringWithoutZeros( getPrice() ) );
		out.append( ',' );
		out.append( getBroker() );
		out.append( ',' );
		out.append( StringUtils.numberToStringWithoutZeros( getCommission() ) );
		out.append( '\n' );
	}
}
