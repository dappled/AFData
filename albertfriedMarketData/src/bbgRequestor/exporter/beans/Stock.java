package bbgRequestor.exporter.beans;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Zhenghong Dong
 */
public class Stock extends RealBasic {
	private double		_price;
	private double		_stkRate;
	private double		_vol;
	private Double[]	_exAmt;
	private String[]	_exDate;

	/**
	 * @param symbol
	 */
	public Stock(String symbol) {
		super(symbol);
	}

	public String bbgSymbol() {
		if (getSymbol().isEmpty()) return "ZVZZT US Equity";
		return getSymbol() + " US Equity";
	}

	public double getPrice() {
		return _price;
	}

	public void setPrice(double price) {
		this._price = price;
	}

	public double getStkRate() {
		return _stkRate;
	}

	public void setStkRate(double stkRate) {
		this._stkRate = stkRate;
	}

	public double getVol() {
		return _vol;
	}

	public void setVol(double vol) {
		this._vol = vol;
	}

	public Double[] getExAmt() {
		return _exAmt;
	}

	public void setExAmt(Double[] exAmt) {
		this._exAmt = exAmt;
	}

	public String[] getExDate() {
		return _exDate;
	}

	public void setExDate(String[] exDate) {
		this._exDate = exDate;
	}

	@Override
	public void writeCSV(FileWriter out) throws IOException {}

	
}
