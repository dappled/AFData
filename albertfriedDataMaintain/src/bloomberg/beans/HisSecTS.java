package bloomberg.beans;


/**
 * security time series / historical data downloaded from bloomberg
 * @author Zhenghong Dong
 */
public class HisSecTS extends TimeSeries<HistoricalSecurityTimeUnit>{
	private static final long					serialVersionUID	= 4727075624731007020L;
	
	public HisSecTS(String name) {
		super(name);
		setT(HistoricalSecurityTimeUnit.class);
	}

	public HisSecTS() {
		super();
		setT(HistoricalSecurityTimeUnit.class);
	}

	public double getLast(String date) {
		return _timeSeriesData.get( date ).getLast();
	}

	public void setLast(String date, double last) {
		_timeSeriesData.get( date ).setLast( last );
	}

	public double getAsk(String date) {
		return _timeSeriesData.get( date ).getAsk();
	}

	public void setAsk(String date, double ask) {
		_timeSeriesData.get( date ).setAsk( ask );
	}

	public double getBid(String date) {
		return _timeSeriesData.get( date ).getBid();
	}

	public void setBid(String date, double bid) {
		_timeSeriesData.get( date ).setBid( bid );
	}

	public double getHigh(String date) {
		return _timeSeriesData.get( date ).getHigh();
	}

	public void setHigh(String date, double high) {
		_timeSeriesData.get( date ).setHigh( high );
	}

	public double getOpen(String date) {
		return _timeSeriesData.get( date ).getOpen();
	}

	public void setOpen(String date, double open) {
		_timeSeriesData.get( date ).setOpen( open );
	}

	public double getLow(String date) {
		return _timeSeriesData.get( date ).getLow();
	}

	public void setLow(String date, double low) {
		this._timeSeriesData.get( date ).setLow( low );
	}

	public double getClose(String date) {
		return _timeSeriesData.get( date ).getClose();
	}

	public void setClose(String date, double close) {
		_timeSeriesData.get( date ).setClose( close );
	}

	public double getVolume(String date) {
		return _timeSeriesData.get( date ).getVolume();
	}

	public void setVolume(String date, double volume) {
		this._timeSeriesData.get( date ).setVolume( volume );
	}

	public double getPE(String date) {
		return _timeSeriesData.get( date ).getPE();
	}

	public void setPE(String date, double pe) {
		this._timeSeriesData.get( date ).setPE( pe );
	}

	public double getSharesOutstanding(String date) {
		return _timeSeriesData.get( date ).getSharesOutstanding();
	}

	public void setSharesOutstanding(String date, long sharesOutstanding) {
		this._timeSeriesData.get( date ).setSharesOutstanding( sharesOutstanding );
	}

	public double getVwap(String date) {
		return _timeSeriesData.get( date ).getVwap();
	}

	public void setVwap(String date, double vwap) {
		this._timeSeriesData.get( date ).setVwap( vwap );
	}

	public double getCurrentQuartelyEEPS(String date) {
		return _timeSeriesData.get( date ).getCurrentQuartelyEEPS();
	}

	public void setCurrentQuarterEEPS(String date, double currentQuartelyEEPS) {
		this._timeSeriesData.get( date ).setCurrentQuartelyEEPS( currentQuartelyEEPS );
	}

	public double getCallIV(String date) {
		return _timeSeriesData.get( date ).getCallIV();
	}

	public void setCallIV(String date, double callIV) {
		this._timeSeriesData.get( date ).setCallIV( callIV );
	}

	public double getPutIV(String date) {
		return _timeSeriesData.get( date ).getPutIV();
	}

	public void setPutIV(String date, double putIV) {
		this._timeSeriesData.get( date ).setPutIV( putIV );
	}

}
