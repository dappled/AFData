package bbgRquestor.bloomberg.beans;

/**
 * @author Zhenghong Dong
 */

public class DividendTimeUnit extends TimeUnit {
	private static final long	serialVersionUID	= -344830944108719801L;
	private String				_declareDate;
	private String				_exDate;
	private String				_recordDate;
	private String				_payableDate;
	private double				_amount;
	private String				_frequency;
	private String				_type;
	private double				_optionImpliedLow;
	private double				_optionImpliedHigh;

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

	public double getAmount() {
		return _amount;
	}

	public void setAmount(double amount) {
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

	public double getOptionImpliedLow() {
		return _optionImpliedLow;
	}

	public void setOptionImpliedLow(double optionImpliedLow) {
		this._optionImpliedLow = optionImpliedLow;
	}

	public double getOptionImpliedHigh() {
		return _optionImpliedHigh;
	}

	public void setOptionImpliedHigh(double optionImpliedHigh) {
		this._optionImpliedHigh = optionImpliedHigh;
	}

	@Override
	public void printPiece() {
		System.out.println("Ticker: " + getName());
		if (_declareDate != null) System.out.println("\tdeclaredDate: " + getDeclareDate());
		if (_exDate != null) System.out.println( "\texDate: " + getExDate() );
		if (_recordDate != null) System.out.println( "\trecordDate: " + getRecordDate() );
		if (_payableDate != null) System.out.println( "\tpayableDate: " + getPayableDate() );
		if (_amount != 0) System.out.println( "\tamount: " + getAmount() );
		if (_frequency != null) System.out.println( "\tfrequency: " + getFrequency() );
		if (_type != null) System.out.println( "\ttype: " + getType() );
		if (_optionImpliedLow != 0) System.out.println( "\toptionImpliedLow: " + getOptionImpliedLow() );
		if (_optionImpliedHigh != 0) System.out.println( "\toptionImpliedHigh: " + getOptionImpliedHigh() );
	}
	
	public static void main(String[] args) {
		DividendTimeUnit t = new DividendTimeUnit( "ibm" );
		t.setType( "idk" );;
		t.printPiece();
	}
}
