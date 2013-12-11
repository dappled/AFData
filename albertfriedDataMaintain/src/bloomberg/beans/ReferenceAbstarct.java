package bloomberg.beans;

import java.io.Serializable;

/**
 * @author Zhenghong Dong
 */
@SuppressWarnings("serial")
public abstract class ReferenceAbstarct implements Serializable{
	public static class RefType {
		private final String _type;
		private RefType(String type) {
			_type = type;
		}
		public String toString() {return _type; }
	}
	private String _symbol;
	
	public String getSymbol() {
		return _symbol;
	}

	public void setSymbol(String symbol) {
		this._symbol = symbol;
	}
}
