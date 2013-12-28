package bbgRquestor.bloomberg.beans;

/**
 * @author user
 */
public class HisDivTS extends TimeSeries<DividendTimeUnit>{
	private static final long					serialVersionUID	= 4727075624731007020L;
	
	public HisDivTS(String name) {
		super(name);
		setT(DividendTimeUnit.class);
	}

	public HisDivTS() {
		super();
		setT(DividendTimeUnit.class);
	}
	

	public void setDeclareDate(String date) throws InstantiationException, IllegalAccessException {
		addNode(date);
		this._timeSeriesData.get(date).setDeclareDate(date);
	}
	public String getExDate(String date) {
		return _timeSeriesData.get(date).getExDate();
	}
	public void setExDate(String date, String exDate) {
		this._timeSeriesData.get(date).setExDate(exDate);
	}
	public String getRecordDate(String date) {
		return _timeSeriesData.get(date).getRecordDate();
	}
	public void setRecordDate(String date, String recordDate) {
		this._timeSeriesData.get(date).setRecordDate(recordDate);
	}
	public String getPayableDate(String date) {
		return _timeSeriesData.get(date).getPayableDate();
	}
	public void setPayableDate(String date, String payableDate) {
		this._timeSeriesData.get(date).setPayableDate(payableDate);
	}
	public double getAmount(String date) {
		return _timeSeriesData.get(date).getAmount();
	}
	public void setAmount(String date, double amount) {
		this._timeSeriesData.get(date).setAmount(amount);
	}
	public String getFrequency(String date) {
		return _timeSeriesData.get(date).getFrequency();
	}
	public void setFrequency(String date, String frequency) {
		this._timeSeriesData.get(date).setFrequency(frequency);
	}
	public String getType(String date) {
		return _timeSeriesData.get(date).getType();
	}
	public void setType(String date, String type) {
		this._timeSeriesData.get(date).setType(type);
	}
	public double getOptionImpliedLow(String date) {
		return _timeSeriesData.get(date).getOptionImpliedLow();
	}
	public void setOptionImpliedLow(String date, double optionImpliedLow) {
		this._timeSeriesData.get(date).setOptionImpliedLow(optionImpliedLow);
	}
	public double getOptionImpliedHigh(String date) {
		return _timeSeriesData.get(date).getOptionImpliedHigh();
	}
	public void setOptionImpliedHigh(String date, double optionImpliedHigh) {
		this._timeSeriesData.get(date).setOptionImpliedHigh(optionImpliedHigh);
	}
	
	@Override
	public void printPiece(String date) {
		System.out.println("DeclareDate: " + date);
		_timeSeriesData.get( date ).printPiece();
	}
}
