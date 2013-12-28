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
public class Option extends DailyTradeAbstract {
	private final String	_year, _month, _day, _putCall;
	private final float		_strike;

	public Option(String account, String symbol, String side, float quantity, float price, String broker, String year, String month, String day, String puCall,
			float strike) {
		super( account, symbol, side, quantity, price, broker );
		_year = year;
		_month = month;
		_day = day;
		_putCall = puCall;
		_strike = strike;
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
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getYear() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getMonth() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getDay() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getPutCall() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( StringUtils.numberToStringWithoutZeros( getStrike() ) ) );
		return j;
	}

	public String getYear() {
		return _year;
	}

	public String getMonth() {
		return _month;
	}

	public String getDay() {
		return _day;
	}

	public String getPutCall() {
		return _putCall;
	}

	public float getStrike() {
		return _strike;
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
		out.append( getYear() );
		out.append( ',' );
		out.append( getMonth() );
		out.append( ',' );
		out.append( getDay() );
		out.append( ',' );
		out.append( getPutCall() );
		out.append( ',' );
		out.append( StringUtils.numberToStringWithoutZeros( getStrike() ) );
		out.append( '\n' );
	}
}
