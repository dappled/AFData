package bloomberg.beans;



/**
 * @author Zhenghong Dong
 */
public class HistoricalTimeUnit extends TimeUnit{
	private static final long	serialVersionUID	= 1792434071620051534L;
	private double	_last;
	private double	_ask;
	private double	_bid;
	private double	_high;
	private double	_open;
	private double	_low;
	private double	_close;
	private double	_volume;
	private double	_pe;
	private long	_sharesOutstanding;
	private double	_vwap;
	private double	_currentQuartelyEEPS;
	private double	_callIV;
	private double	_putIV;

	public HistoricalTimeUnit() {}
	
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

	public void setCurrentQuartelyEEPS(double currentQuartelyEEPS) {
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
}
