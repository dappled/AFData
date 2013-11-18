package com.dong.dataWrapper;

import com.dong.utils.poi.PoiRecord;

/**
 * @author Zhenghong Dong
 */
public abstract class RecordAbstract implements Comparable<RecordAbstract>, PoiRecord {
	private final String	_symbol;		// symbol
	private final String	_type;			// base product: equity or option
	private final String	_side;			// different for different implementation: for Activity: B/S; for Position:C/P/null(equity)
	private int				_qty;			// quantity
	private String			_description;	// different for different implementation: for Position: pending position

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public RecordAbstract(final String symbol, final String type, final String side, final int qty,
			final String description) {
		_symbol = symbol;
		_type = type;
		_side = side;
		_qty = qty;
		_description = description;
	}

	/***********************************************************************
	 * Comparable methods
	 ***********************************************************************/
	@Override
	public int compareTo(final RecordAbstract o2) {
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
		return (_symbol.equals( ((ActivityAbstract) record).getSymbol() ) && // compare symbol

				_type.equals( ((ActivityAbstract) record).getSide() ) && // compare side
				_qty == ((ActivityAbstract) record).getQuantity()); // compare quantity

	}

	@Override
	public int hashCode() {
		return _symbol.hashCode() * 31 + _qty * 53 + _type.hashCode() * 23;
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/

	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/

	public String getSymbol() {
		return _symbol;
	}

	public int getQuantity() {
		return _qty;
	}

	public void setQuantity(final int qty) {
		_qty = qty;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(final String description) {
		_description = description;
	}

	public String getType() {
		return _type;
	}

	public String getSide() {
		return _side;
	}
}
