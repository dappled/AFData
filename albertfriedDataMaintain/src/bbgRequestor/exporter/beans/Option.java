package bbgRequestor.exporter.beans;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

/**
 * @author Zhenghong Dong
 */
public class Option extends RealBasic{
	private final String _underlying, _putCall;
	private final Date _maturity;
	private final Double _strike;
	private String _exDate;


	public Option(String symbol, String underlying, Date maturity, double strike, String putCall ) {
		super( symbol );
		_underlying = underlying;
		_putCall = putCall;
		_maturity = maturity;
		_strike = strike;
	}

	public String getUnderlying() {
		return _underlying;
	}

	public String getPutCall() {
		return _putCall;
	}

	public Date getMaturity() {
		return _maturity;
	}

	public Double getStrike() {
		return _strike;
	}

	
	public String getExDate() {
		return _exDate;
	}

	public void setExDate(String exDate) {
		this._exDate = exDate;
	}
	
	@Override
	public void writeCSV(FileWriter out) throws IOException {}

}
