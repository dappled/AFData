package bloomberg.beans;

/**
 * @author Zhenghong Dong
 */

public class HistoricalDividendTimeUnit extends TimeUnit{
	private static final long	serialVersionUID	= -344830944108719801L;
	private String	declareDate;
	private  String	exDate;
	private  String	recordDate;
	private  String	payableDate;
	private  double	amount;
	private  String	frequency;
	private  String	type;
	private  double	optionImpliedLow;
	private  double	optionImpliedHigh;
	
	public String getDeclareDate() {
		return declareDate;
	}
	public void setDeclareDate(String declareDate) {
		this.declareDate = declareDate;
	}
	public String getExDate() {
		return exDate;
	}
	public void setExDate(String exDate) {
		this.exDate = exDate;
	}
	public String getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}
	public String getPayableDate() {
		return payableDate;
	}
	public void setPayableDate(String payableDate) {
		this.payableDate = payableDate;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public double getOptionImpliedLow() {
		return optionImpliedLow;
	}
	public void setOptionImpliedLow(double optionImpliedLow) {
		this.optionImpliedLow = optionImpliedLow;
	}
	public double getOptionImpliedHigh() {
		return optionImpliedHigh;
	}
	public void setOptionImpliedHigh(double optionImpliedHigh) {
		this.optionImpliedHigh = optionImpliedHigh;
	}
}
