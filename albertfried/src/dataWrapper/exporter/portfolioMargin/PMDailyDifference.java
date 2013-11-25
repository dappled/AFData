package dataWrapper.exporter.portfolioMargin;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import dataWrapper.PMAbstract;

/**
 * @author Zhenghong Dong
 */
public class PMDailyDifference extends PMAbstract {
	private final float	_difference;
	private final float	_requirement;

	public PMDailyDifference(String importDate, String symbol, float requirement, float difference) {
		super( importDate, symbol );
		_difference = difference;
		_requirement = requirement;
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( getRequirement() );
		row.createCell( i++ ).setCellValue( getDifference() );
	}
	
	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int index) {
		Row row = sheet.createRow( index++ );
		writeNextForMultipleRecords( wb, row, 0 );
		return index;
	}

	public static int size() {
		return 3;
	}

	public float getDifference() {
		return _difference;
	}

	public float getRequirement() {
		return _requirement;
	}
}
