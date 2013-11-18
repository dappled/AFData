package dataWrapper;

import org.joda.time.LocalDate;

import utils.ParseDate;

/**
 * Data structure that represents a single activity record
 * @author Zhenghong Dong
 */
public class ActivityOld {
	private final String		_symbol, _settleCode, _type;
	private final LocalDate		_tradeDate, _settleValueDate, _processDate;
	private final int			_qty;
	private final String		_blotter;
	private final double		_settlePrice, _settlePrincipalAmount, _totalClrComBrkFees, _settleInterest, _totalFee,
	_totalRegFee, _totalRebatesExpenses, _settleNetAmount, _settleNotionalValue;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ActivityOld(final String[] list) throws Exception {
		// check list length
		if (list.length != 293) throw new Exception( "Acitivity: Line format incorrect." );

		// get useful fields
		// symbol
		if (list[ 14 ].trim().equals( "Equity" )) {
			_symbol = list[ 16 ].trim();
		} else {
			_symbol = list[ 65 ].trim();
		}
		// qty
		_qty = (int) Double.parseDouble( list[ 69 ].trim() ); // not sure
		// type
		if (list[ 202 ].trim().equals( "" )) {
			if (_qty > 0) {
				_type = "BUY";
			} else {
				_type = "SELL";
			}
		} else _type = list[ 202 ].trim();
		// tradeDate
		_tradeDate = ParseDate.stringToDate( list[ 72 ].trim() );
		// settleValueDate
		_settleValueDate = ParseDate.stringToDate( list[ 73 ].trim() );
		// processDate
		_processDate = ParseDate.stringToDate( list[ 74 ].trim() );
		// settlePrincipalAmount
		_settlePrincipalAmount = Double.parseDouble( list[ 83 ].trim() );
		// settlePrice
		if (_type.equals( "AJC" )) {
			_settlePrice = Math.abs( _settlePrincipalAmount );
		} else {
			_settlePrice = Double.parseDouble( list[ 82 ].trim() );
		}
		// totalClrComBrkFees
		_totalClrComBrkFees = Double.parseDouble( list[ 87 ].trim() );
		// settleInterest
		_settleInterest = Double.parseDouble( list[ 89 ].trim() ); // not sure
		// totalFee
		_totalFee = Double.parseDouble( list[ 94 ].trim() );
		// totalRegFe
		_totalRegFee = Double.parseDouble( list[ 102 ].trim() );
		// totalRebate
		_totalRebatesExpenses = Double.parseDouble( list[ 94 ].trim() ); // not sure
		// settleNetAmount
		_settleNetAmount = Double.parseDouble( list[ 111 ].trim() );
		// settleNotionalValue
		_settleNotionalValue = Double.parseDouble( list[ 113 ].trim() ); // not sure
		// settleCode
		_settleCode = list[ 81 ].trim(); // not sure
		// setBlotter
		_blotter = list[ 78 ].trim(); // not sure

	}


	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public String getSymbol() { return _symbol; }
	public String getSettleCode() { return _settleCode; }
	public String getType() { return _type; }
	public LocalDate getTradeDate() { return _tradeDate; }
	public LocalDate getSettleValueDate() { return _settleValueDate; }
	public LocalDate getProcessDate() { return _processDate; }
	public int getQty() { return _qty; }
	public String getBlotter() { return _blotter; }
	public double getSettlePrice() { return _settlePrice; }
	public double getSettlePrincipalAmount() { return _settlePrincipalAmount; }
	public double getTotalClrComBrkFees() { return _totalClrComBrkFees; }
	public double getSettleInterest() { return _settleInterest; }
	public double getTotalFee() { return _totalFee; }
	public double getTotalRegFee() { return _totalRegFee; }
	public double getTotalRebatesExpenses() { return _totalRebatesExpenses; }
	public double getSettleNetAmount() { return _settleNetAmount; }
	public double getSettleNotionalValue() { return _settleNotionalValue; }

	/*    public static void main(String[] args) throws Exception {
        String date = "20131107";
        Activity a = new Activity("");
        System.out.println(a.parseDate(date));
    }*/
}
