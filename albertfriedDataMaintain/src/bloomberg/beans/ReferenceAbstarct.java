package bloomberg.beans;

import java.io.Serializable;

/**
 * @author Zhenghong Dong
 */
@SuppressWarnings("serial")
public abstract class ReferenceAbstarct implements Serializable{
	private String _symbol;
	
	public String getSymbol() {
		return _symbol;
	}

	public void setSymbol(String symbol) {
		this._symbol = symbol;
	}
}
