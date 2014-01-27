package dataWrapper.exporter.bbu;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.ParseDate;
import utils.StringUtils;
import dataWrapper.RealBasic;

/**
 * 
 * @author Zhenghong Dong
 */
public class PortfolioElement extends RealBasic {
	private final float	_tdQty, _strike, _baseMarketPrice;
	private final String	_putCall, _OCCCode, _account;
	private final Date		_maturity;

	/**
	 * @param symbol
	 */
	public PortfolioElement(String symbol, float tdQty, Date maturity, float strike, String putCall, float baseMarketPrice, String OCCCode, String account) {
		super( symbol );
		_tdQty = tdQty;
		_strike = strike;
		_baseMarketPrice = baseMarketPrice;
		_putCall = putCall;
		_OCCCode = OCCCode;
		_account = account;
		_maturity = maturity;
	}

	@Override
	public void writeNextForMultipleRecords(Workbook wb, Row row, int index) {}

	@Override
	public int writeNextForSingleRecord(Workbook wb, Sheet sheet, int rowNum) {
		return 0;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {
		out.append( "vineyard" );
		out.append( ',' );
		out.append( bbgSymbol() );
		out.append( ',' );
		out.append( String.valueOf( getTDQty() ) );
		out.append( ',' );
		out.append( round() );
		out.append( ',' );
		out.append( ParseDate.today );
		out.append( ',' );
		out.append( "98" );
		out.append( '\n' );
	}

	public String bbgSymbol() {
		if (getSymbol().isEmpty()) return "ZVZZT US Equity";
		else {
			if (getMaturity() == null) {
				return getSymbol() + " US Equity";
			} else {
				return getSymbol() + " US " + ParseDate.standardFromDate( getMaturity() ) + " " + getPutCall()
						+ StringUtils.numberToStringWithoutZeros( getStrike() ) + " Equity";
			}
		}
	}

	public String round() {
		DecimalFormat df = new DecimalFormat( "#.####" );
		df.setRoundingMode( RoundingMode.UP );
		return df.format( getBaseMarketPrice() );
	}

	public float getTDQty() {
		return _tdQty;
	}

	public float getStrike() {
		return _strike;
	}

	public float getBaseMarketPrice() {
		return _baseMarketPrice;
	}

	public String getPutCall() {
		return _putCall;
	}

	public String getOCCCode() {
		return _OCCCode;
	}

	public String getAccount() {
		return _account;
	}

	public Date getMaturity() {
		return _maturity;
	}
}
