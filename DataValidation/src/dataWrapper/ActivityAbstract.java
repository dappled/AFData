package dataWrapper;

import utils.DoubleComparator;

public abstract class ActivityAbstract extends RecordAbstract<ActivityAbstract> {
	private final String	_symbol, _type, _tradeDate, _side;
	private String			_description;
	private int				_qty;
	private double			_price;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ActivityAbstract(final String tradeDate, final String symbol, final String type, final String side,
			final int qty, final double price,
			final String description) {
		_tradeDate = tradeDate;
		_symbol = symbol;
		_side = side;
		_type = type;
		_qty = qty;
		_price = price;
		_description = description;
	}
	
	/***********************************************************************
	 * Comparable methods
	 ***********************************************************************/
	@Override
	public int compareTo(final ActivityAbstract o2) {
		return getSymbol().compareTo( o2.getSymbol() );
	}

	/***********************************************************************
	 * Object methods
	 ***********************************************************************/
	@Override
	public boolean equals(final Object record) {
		if (record == null) return false;
		if (record == this) return true;
		if (!(record instanceof ActivityAbstract)) return false;
		return (_tradeDate.equals( ((ActivityAbstract) record).getTradeDate() ) && // compare trade date
				_symbol.equals( ((ActivityAbstract) record).getSymbol() ) && // compare symbol
				_type.equals( ((ActivityAbstract) record).getType() ) && // compare type
				_side.equals( ((ActivityAbstract) record).getSide() ) && // compare side
				_qty == ((ActivityAbstract) record).getQuantity() && // compare quantity
				DoubleComparator.equal( _price, ((ActivityAbstract) record).getPrice(), 0.0001 ));// compare price
	};

	@Override
	public int hashCode() {
		return _tradeDate.hashCode() * 29 + _symbol.hashCode() * 31 + _type.hashCode() * 17 + _qty * 53 + (int) _price
				* 13 + _side.hashCode() * 23;
	}
	
	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	public void add(final ActivityAbstract record) {
		updatePriceQty( record );
		if (!record.getDescription().equals( getDescription() )) {
			setDescription( getDescription() + "," + record.getDescription() );
		}
	}

	private void updatePriceQty(final ActivityAbstract record) {
		setPrice( (_price * _qty + record.getPrice() * record.getQuantity()) / (_qty + record.getQuantity()) );
		setQuantity( _qty + record.getQuantity() );
	}
	
	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public String getTradeDate() {
		return _tradeDate;
	}

	public String getSymbol() {
		return _symbol;
	}

	public String getType() {
		return _type;
	}

	public int getQuantity() {
		return _qty;
	}

	public double getPrice() {
		return _price;
	}

	public void setQuantity(final int qty) {
		_qty = qty;
	}

	public void setPrice(final double amt) {
		_price = amt;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(final String description) {
		_description = description;
	}

	public String getSide() {
		return _side;
	}
}
