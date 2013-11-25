package dataWrapper;

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
	
}
