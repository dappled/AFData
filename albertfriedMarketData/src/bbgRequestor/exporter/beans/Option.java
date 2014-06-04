package bbgRequestor.exporter.beans;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;

/**
 * @author Zhenghong Dong
 */
public class Option extends RealBasic {
	private final String	_putCall;
	private final Date		_maturity;
	private final Double	_strike;
	private Stock			_stock;
	private Double			_parityPrice;
	private Double			_price;
	
	public Option(String symbol, String underlying, Date maturity, double strike, String putCall) {
		super(symbol);
		_stock = new Stock(underlying);
		_putCall = putCall;
		_maturity = maturity;
		_strike = strike;
	}

	public Stock getUnderlying() {
		return _stock;
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

	public double getParityPrice() {
		if (_parityPrice == null) return 0;
		return _parityPrice;
	}

	public void setParityPrice(double price)
	{
		_parityPrice = price;
	}

	public double getPrice() {
		if (_price == null) return 0;
		return _price;
	}

	public void setPrice(Double price) {
		this._price = price;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {}

}
