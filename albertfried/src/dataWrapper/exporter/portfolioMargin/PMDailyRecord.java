package dataWrapper.exporter.portfolioMargin;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import dataWrapper.PMAbstract;

/**
 * @author Zhenghong Dong
 */
public class PMDailyRecord extends PMAbstract {
	private final float	_risk, _minimum, _requirement;

	/**
	 * @param date
	 * @param symbol
	 * @param requirement
	 */
	public PMDailyRecord(String date, String symbol, float requirement, float risk, float minimum) {
		super( date, symbol );
		_risk = risk;
		_minimum = minimum;
		_requirement = requirement;
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( getRequirement() );
		row.createCell( i++ ).setCellValue( getRisk() );
		row.createCell( i++ ).setCellValue( getMinumum() );
	}
	
	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int index) {
		Row row = sheet.createRow( index++ );
		writeNextForMultipleRecords( wb, row, 0 );
		return index;
	}

	public static int size() {
		return 4;
	}

	public float getMinumum() {
		return _minimum;
	}

	public float getRisk() {
		return _risk;
	}

	public float getRequirement() {
		return _requirement;
	}

}
