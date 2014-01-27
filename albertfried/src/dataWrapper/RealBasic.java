package dataWrapper;

import java.io.FileWriter;
import java.io.IOException;

import utils.poi.PoiRecord;

/**
 * Basic Class
 * @author Zhenghong Dong
 */
public abstract class RealBasic implements PoiRecord {
	private final String  _symbol;
	
	public RealBasic(String symbol) {
		_symbol = symbol;
	}
	
	public String getSymbol() {
		return _symbol;
	}
	
	public abstract void writeCSV(FileWriter out) throws IOException;
}
