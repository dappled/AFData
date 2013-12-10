package dataWrapper.exporter.portfolioMargin;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.StringUtils;
import dataWrapper.PMAbstract;

/**
 * @author Zhenghong Dong
 */
public class PMDailyAnalysis extends PMAbstract {
	private final float					_requirement;
	private final float					_risk;
	private final List<PMDailyDetail>	_details;
	private int							_movementIdx		= -1;
	private String						_reason				= "";
	private boolean						_isAnalysized		= false;
	private float[]						_movements			= { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private float						_totalContractQty	= 0;
	private float						_totalStockQty		= 0;

	public PMDailyAnalysis(String date, String symbol, float requirement, float risk) {
		super( date, symbol );
		_requirement = requirement;
		_risk = risk;
		_details = new ArrayList<>();
	}

	public void add(PMDailyDetail record) throws Exception {
		_details.add( record );
		addMovements( record );
		addQuantity( record );
	}

	/**
	 * @param record
	 */
	private void addQuantity(PMDailyDetail record) {
		// stock
		if (record.getStrike() == 0) _totalStockQty += record.getQuantity();
		// option
		else _totalContractQty += Math.abs( record.getQuantity() );
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
			// if requirement == risk, reason of large risk is from one of the 10 movements
			if (_requirement == _risk) {
				for (int i = 0; i < _movements.length; i++) {
					if (_movements[ i ] == -1 * _requirement) {
						_movementIdx = i;
						_reason = _movementIdx >= 5 ? "Up" + String.valueOf( _movementIdx - 4 ) : "Down" + String.valueOf( 5 - _movementIdx );
						break;
					}
				}
				// no fit movements
				if (_movementIdx == -1) {
					// throw new Exception(
					// "PMDailyAnalysisSingle: there is no movements sum equal to the requirement for " + getSymbol() );
					System.err.println( "PMDailyAnalysisSingle: there is no movements sum equal to the requirement for " + getSymbol()
							+ " I'll just use the last one for test" );
					_movementIdx = 9;
				}
			}
			// if requirement > risk, reason of large risk is large amount of contracts
			else {
				if (_totalContractQty == 0) _reason = String.format( "%s stocks", StringUtils.numberToStringWithoutZeros( _totalStockQty ) );
				else _reason = String.format( "%s options", StringUtils.numberToStringWithoutZeros( _totalContractQty ) );
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
			if (_movementIdx == -1) return j; // ignore symbol with less risk than minimal
			final CreationHelper createHelper = wb.getCreationHelper();
			Row row = sheet.createRow( ++j );
			// add header
			row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "Id:" ) );
			row.createCell( 1 ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
			row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "Reason:" ) );
			row.createCell( 3 ).setCellValue( createHelper.createRichTextString( _reason ) );
			row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "Requirement" ) );
			row.createCell( 5 ).setCellValue( getRequirement() );
			row = sheet.createRow( ++j );
			row.createCell( 0 ).setCellValue( createHelper.createRichTextString( "Symbol:" ) );
			row.createCell( 1 ).setCellValue( createHelper.createRichTextString( "Maturity:" ) );
			row.createCell( 2 ).setCellValue( createHelper.createRichTextString( "PutCall:" ) );
			row.createCell( 3 ).setCellValue( createHelper.createRichTextString( "Strike:" ) );
			row.createCell( 4 ).setCellValue( createHelper.createRichTextString( "Quantity:" ) );
			row.createCell( 5 ).setCellValue( createHelper.createRichTextString( "Price:" ) );
			row.createCell( 6 ).setCellValue( createHelper.createRichTextString( _reason ) );

			for (PMDailyDetail record : _details) {
				// only write those records with movement negative, those who we are really interested in
				if (record.getMovements()[ _movementIdx ] < 0) {
					row = sheet.createRow( ++j );
					record.writeNextForAnalysis( wb, row, 0, _movementIdx );
				}
			}
			return j;
		}
	}

	public float getRequirement() {
		return _requirement;
	}

	public String getReason() {
		return _reason;
	}

}
