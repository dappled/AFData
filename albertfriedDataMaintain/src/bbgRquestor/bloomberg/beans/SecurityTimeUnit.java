package bbgRquestor.bloomberg.beans;

import bbgRequestor.bloomberg.BbgNames.Fields;

/**
 * @author Zhenghong Dong
 */
public class SecurityTimeUnit extends TimeUnit {
	private static final long	serialVersionUID	= 1792434071620051534L;
	private double				_last;
	private double				_ask;
	private double				_bid;
	private double				_high;
	private double				_open;
	private double				_low;
	private double				_close;
	private double				_volume;
	private double				_pe;
	private long				_sharesOutstanding;
	private double				_vwap;
	private double				_currentQuartelyEEPS;
	private double				_callIV;
	private double				_putIV;

	public SecurityTimeUnit(String name) {
		super( name );
	}

	public double getLast() {
		return _last;
	}

	public void setLast(double last) {
		this._last = last;
	}

	public double getAsk() {
		return _ask;
	}

	public void setAsk(double ask) {
		this._ask = ask;
	}

	public double getBid() {
		return _bid;
	}

	public void setBid(double bid) {
		this._bid = bid;
	}

	public double getHigh() {
		return _high;
	}

	public void setHigh(double high) {
		this._high = high;
	}

	public double getOpen() {
		return _open;
	}

	public void setOpen(double open) {
		this._open = open;
	}

	public double getLow() {
		return _low;
	}

	public void setLow(double low) {
		this._low = low;
	}

	public double getClose() {
		return _close;
	}

	public void setClose(double close) {
		this._close = close;
	}

	public double getVolume() {
		return _volume;
	}

	public void setVolume(double volume) {
		this._volume = volume;
	}

	public double getPE() {
		return _pe;
	}

	public void setPE(double pe) {
		this._pe = pe;
	}

	public double getSharesOutstanding() {
		return _sharesOutstanding;
	}

	public void setSharesOutstanding(long sharesOutstanding) {
		this._sharesOutstanding = sharesOutstanding;
	}

	public double getVwap() {
		return _vwap;
	}

	public void setVwap(double vwap) {
		this._vwap = vwap;
	}

	public double getCurrentQuartelyEEPS() {
		return _currentQuartelyEEPS;
	}

	public void setCurrentQuarterlyEEPS(double currentQuartelyEEPS) {
		this._currentQuartelyEEPS = currentQuartelyEEPS;
	}

	public double getCallIV() {
		return _callIV;
	}

	public void setCallIV(double callIV) {
		this._callIV = callIV;
	}

	public double getPutIV() {
		return _putIV;
	}

	public void setPutIV(double putIV) {
		this._putIV = putIV;
	}

	@Override
	public void printPiece() {
		System.out.println("Ticker: " + getName());
		if (_last > 0) System.out.println( "\tlast: " + getLast() );
		if (_bid > 0) System.out.println( "\tbid: " + getBid() );
		if (_ask > 0) System.out.println( "\task: " + getAsk() );
		if (_open > 0) System.out.println( "\topen: " + getOpen() );
		if (_close > 0) System.out.println( "\tclose: " + getClose() );
		if (_high > 0) System.out.println( "\thigh: " + getHigh() );
		if (_low > 0) System.out.println( "\tlow: " + getLow() );
		if (_volume > 0) System.out.println( "\tvolume: " + getVolume() );
		if (_pe > 0) System.out.println( "\tpe: " + getPE() );
		if (_sharesOutstanding > 0) System.out.println( "\tsharesOutstanding: " + getSharesOutstanding() );
		if (_vwap > 0) System.out.println( "\tvwap: " + getVwap() );
		if (_currentQuartelyEEPS > 0) System.out.println( "\tcurretQuartelyEEPS: " + getCurrentQuartelyEEPS() );
		if (_callIV > 0) System.out.println( "\tcallIV: " + getCallIV() );
		if (_putIV > 0) System.out.println( "\tputIV: " + getPutIV() );
	}
	
	public static void main(String[] args) {
		SecurityTimeUnit t = new SecurityTimeUnit( "test" );
		t.setPE( 100 );
		t.printPiece();
	}

	@Override
	public void set(String fields, Object value) {
		switch (fields) {
			case Fields.ask:
				this.setAsk( (Double)value );
				break;
			default:
				break;
		}
	}
}
