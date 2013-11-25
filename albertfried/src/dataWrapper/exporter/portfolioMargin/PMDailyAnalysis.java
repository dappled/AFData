package dataWrapper.exporter.portfolioMargin;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import dataWrapper.PMAbstract;

/**
 * @author Zhenghong Dong
 */
public class PMDailyAnalysis extends PMAbstract {
	private final float					_requirement;
	private final List<PMDailyDetail>	_details;
	private int							_idx			= -1;
	private boolean						_isAnalysized	= false;
	private float[]						_movements		= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public PMDailyAnalysis(String date, String symbol, float requirement) {
		super( date, symbol );
		_requirement = requirement;
		_details = new ArrayList<>();
	}

	public void add(PMDailyDetail record) throws Exception {
		_details.add( record );
		addMovements( record );
	}

	private void addMovements(PMDailyDetail record) throws Exception {
		if (record.getMovements().length != _movements.length) throw new Exception( "PMDailyAnalysisSingle: adding a movement vector for " +
				record.toString() + " which has a difference size of " + record.getMovements().length );
		for (int i = 0; i < _movements.length; i++) {
			_movements[ i ] += record.getMovements()[ i ];
		}
	}

	public void analysis() throws Exception {
		if (_isAnalysized) return;
		else {
			for (int i = 0; i < _movements.length; i++) {
				if (_movements[ i ] == -1 * _requirement) {
					_idx = i;
				}
			}
			// no fit movements
			if (_idx == -1) {
				//throw new Exception( "PMDailyAnalysisSingle: there is no movements sum equal to the requirement for " + getSymbol() );
				System.err.println("PMDailyAnalysisSingle: there is no movements sum equal to the requirement for " + getSymbol() +" Ill just use the last one for test");
				_idx = 9;
			}
			_isAnalysized = true;
		}
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {}
	
	@Override
	public int writeNextForSingleRecord(final Workbook wb, Sheet sheet, int j) {
		if (!_isAnalysized) {
			System.err.println( "PMDailyAnalysisSingle: this records for " + getSymbol() + " has not been analized before, will write nothing" );
			return 0;
		} else {
			final CreationHelper createHelper = wb.getCreationHelper();
			Row row = sheet.createRow( j++ );
			row = sheet.createRow( j++ ); // jump a row
			// add header
			row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "Symbol:" ) );
			row.createCell( 1 ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
			row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Reason:" ) );
			String reason = _idx >= 5 ? "Up" + String.valueOf( _idx - 4 ) : "Down" + String.valueOf( 5 - _idx );
			row.createCell( 3 ).setCellValue( createHelper.createRichTextString( reason ) );
			row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "Requirement" ) );
			row.createCell( 5 ).setCellValue( getRequirement() );
			row = sheet.createRow( j++ );
			row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "Maturity:" ) );
			row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "PutCall:" ) );
			row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Strike:" ) );
			row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Quantity:" ) );
			row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "Price:" ) );
			row.createCell( 5 ).setCellValue( createHelper.createRichTextString( reason ) );

			for (PMDailyDetail record : _details) {
				// only write those records with movement negative, those who we are really interested in
				if (record.getMovements()[ _idx ] < 0) {
					row = sheet.createRow( j++ );
					record.writeNextForAnalysis( wb, row, 0, _idx );
				}
			}
			return j;
		}
	}
	
	public float getRequirement() {
		return _requirement;
	}

}
