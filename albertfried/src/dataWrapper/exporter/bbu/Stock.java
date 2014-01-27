package dataWrapper.exporter.bbu;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.ParseDate;
import dataWrapper.RealBasic;

/**
 * @author Zhenghong Dong
 */
public class Stock extends RealBasic {
	/**
	 * @param symbol
	 */
	public Stock(String symbol) {
		super( symbol );
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {}

	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int rowNum) {
		return 0;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {
		out.append( "stock" );
		out.append( ',' );
		out.append( bbgSymbol() );
		out.append( ',' );
		out.append( "100" );
		out.append( ',' );
		out.append( "0" );
		out.append( ',' );
		out.append( ParseDate.today );
		out.append( ',' );
		out.append( "98" );
		out.append( '\n' );
	}
	
	public String bbgSymbol() {
		if (getSymbol().isEmpty()) return "ZVZZT US Equity";
		return getSymbol() + " US Equity";
	}
}
