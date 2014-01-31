package dataWrapper.exporter.portfolioMargin;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Zhenghong Dong
 */
public class PMDailyRecord extends PMAbstract {
	private final float		_risk, _minimum, _requirement;
	private String			_reason;
	private final String	_symbolType;

	/**
	 * @param date
	 * @param symbol
	 * @param requirement
	 */
	public PMDailyRecord(String date, String symbol, String symbolType, float requirement, float risk, float minimum) {
		super( date, symbol );
		_symbolType = symbolType;
		_risk = risk;
		_minimum = minimum;
		_requirement = requirement;
		_reason = "";
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( getRequirement() );
		row.createCell( i++ ).setCellValue( getRisk() );
		row.createCell( i++ ).setCellValue( getMinumum() );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getReason() ) );
	}

	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int index) {
		Row row = sheet.createRow( index++ );
		writeNextForMultipleRecords( wb, row, 0 );
		return index;
	}

	public static int size() {
		return 5;
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

	public String getReason() {
		return _reason;
	}

	public void setReason(String reason) {
		_reason = reason;
	}

	public String getSymbolType() {
		return _symbolType;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {}
}
