package dataWrapper.exporter.validator;

import java.io.FileWriter;
import java.io.IOException;

import utils.DoubleComparator;

public abstract class ActivityAbstract extends RecordAbstract {
	private final String	_tradeDate;	// date is in MM/dd/yyyy format
	private double			_price;
	private String			_description;	

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public ActivityAbstract(final String tradeDate, final String symbol, final String type, final String side,
			final int qty, final double price,
			final String description) {
		super( symbol, type, side, qty);
		_tradeDate = tradeDate;
		_price = price;
		_description = description;
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
				getType().equals( ((ActivityAbstract) record).getType() ) && // compare type
				getSide().equals( ((ActivityAbstract) record).getSide() ) && // compare side
				getQuantity() == ((ActivityAbstract) record).getQuantity() && // compare quantity
				DoubleComparator.equal( _price, ((ActivityAbstract) record).getPrice(), 0.0001 ));// compare price
	}

	@Override
	public int hashCode() {
		return _tradeDate.hashCode() * 29 + (int) _price * 13 + super.hashCode();
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
		setPrice( (_price * getQuantity() + record.getPrice() * record.getQuantity())
				/ (getQuantity() + record.getQuantity()) );
		setQuantity( getQuantity() + record.getQuantity() );
	}

	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public String getTradeDate() {
		return _tradeDate;
	}

	public double getPrice() {
		return _price;
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
	
	@Override
	public void writeCSV(FileWriter out) throws IOException {}
}
