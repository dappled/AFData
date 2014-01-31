package bbgRquestor.bloomberg.beans;

import bbgRequestor.bloomberg.BbgNames.Fields;

/**
 * @author Zhenghong Dong
 */

public class DividendTimeUnit extends TimeUnit {
	private static final long	serialVersionUID	= -344830944108719801L;
	private String				_declareDate;
	private String				_exDate;
	private Double				_amount;
	private String				_nextExDate;
	private Double				_nextAmount;
	private String				_recordDate;
	private String				_payableDate;
	private String				_frequency;
	private String				_type;
	private Double				_optionImpliedLow;
	private Double				_optionImpliedHigh;
	private String				_extra;

	public DividendTimeUnit(String name) {
		super( name );
	}

	public String getDeclareDate() {
		return _declareDate;
	}

	public void setDeclareDate(String declareDate) {
		this._declareDate = declareDate;
	}

	public String getExDate() {
		return _exDate;
	}

	public void setExDate(String exDate) {
		this._exDate = exDate;
	}

	public String getNextExDate() {
		return _nextExDate;
	}

	public void setNextExDate(String nextExDate) {
		this._nextExDate = nextExDate;
	}

	public Double getNextAmount() {
		return _nextAmount;
	}

	public void setNextAmount(Double nextAmount) {
		this._nextAmount = nextAmount;
	}

	public String getRecordDate() {
		return _recordDate;
	}

	public void setRecordDate(String recordDate) {
		this._recordDate = recordDate;
	}

	public String getPayableDate() {
		return _payableDate;
	}

	public void setPayableDate(String payableDate) {
		this._payableDate = payableDate;
	}

	public Double getAmount() {
		return _amount;
	}

	public void setAmount(Double amount) {
		this._amount = amount;
	}

	public String getFrequency() {
		return _frequency;
	}

	public void setFrequency(String frequency) {
		this._frequency = frequency;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}

	public Double getOptionImpliedLow() {
		return _optionImpliedLow;
	}

	public void setOptionImpliedLow(Double optionImpliedLow) {
		this._optionImpliedLow = optionImpliedLow;
	}

	public Double getOptionImpliedHigh() {
		return _optionImpliedHigh;
	}

	public void setOptionImpliedHigh(Double optionImpliedHigh) {
		this._optionImpliedHigh = optionImpliedHigh;
	}

	public String getExtra() {
		return _extra;
	}

	public void setExtra(String extra) {
		this._extra = extra;
	}

	@Override
	public void printPiece() {
		System.out.println( "Ticker: " + getName() );
		if (_declareDate != null) System.out.println( "\tdeclaredDate: " + getDeclareDate() );
		if (_exDate != null) System.out.println( "\texDate: " + getExDate() );
		if (_recordDate != null) System.out.println( "\trecordDate: " + getRecordDate() );
		if (_payableDate != null) System.out.println( "\tpayableDate: " + getPayableDate() );
		if (_amount != null) System.out.println( "\tamount: " + getAmount() );
		if (_nextExDate != null) System.out.println( "\tnext exDate: " + getNextExDate() );
		if (_nextAmount != null) System.out.println( "\tnext amount: " + getNextAmount() );
		if (_frequency != null) System.out.println( "\tfrequency: " + getFrequency() );
		if (_type != null) System.out.println( "\ttype: " + getType() );
		if (_optionImpliedLow != null) System.out.println( "\toptionImpliedLow: " + getOptionImpliedLow() );
		if (_optionImpliedHigh != null) System.out.println( "\toptionImpliedHigh: " + getOptionImpliedHigh() );
		if (_extra != null) System.out.println( "\textra info: " + getExtra() );
	}

	public static void main(String[] args) {
		DividendTimeUnit t = new DividendTimeUnit( "ibm" );
		t.setType( "idk" );
		;
		t.printPiece();
	}

	@Override
	public void set(String fields, Object value) {
		switch (fields) {
			case Fields.exDate:
				this.setExDate( (String) value );
				break;
			case Fields.divAmount:
				this.setAmount( (Double) value );
				break;
			case Fields.nextExDate:
				this.setNextExDate( (String) value );
				break;
			case Fields.nextDivAmount:
				this.setNextAmount( (Double) value );
				break;
			default:
				break;
		}
	}
}
