package dataWrapper;

import utils.DoubleComparator;

public abstract class ActivityAbstract extends RecordAbstract {
	private final String	_type, _tradeDate;
	private double			_price;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ActivityAbstract(final String tradeDate, final String symbol, final String type, final String side,
			final int qty, final double price,
			final String description) {
		super(symbol,side,qty,description);
		_tradeDate = tradeDate;
		_type = type;
		_price = price;
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
				getSymbol().equals( ((ActivityAbstract) record).getSymbol() ) && // compare symbol
				_type.equals( ((ActivityAbstract) record).getType() ) && // compare type
				getSide().equals( ((ActivityAbstract) record).getSide() ) && // compare side
				getQuantity() == ((ActivityAbstract) record).getQuantity() && // compare quantity
				DoubleComparator.equal( _price, ((ActivityAbstract) record).getPrice(), 0.0001 ));// compare price
	}

	@Override
	public int hashCode() {
		return _tradeDate.hashCode() * 29 +  _type.hashCode() * 17 + (int) _price* 13 + super.hashCode();
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
		setPrice( (_price * getQuantity() + record.getPrice() * record.getQuantity()) / (getQuantity() + record.getQuantity()) );
		setQuantity( getQuantity() + record.getQuantity() );
	}
	
	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public String getTradeDate() {
		return _tradeDate;
	}

	public String getType() {
		return _type;
	}

	public double getPrice() {
		return _price;
	}

	public void setPrice(final double amt) {
		_price = amt;
	}
}
