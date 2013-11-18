package com.dong.dataWrapper;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.dong.utils.DoubleComparator;

/**
 * @author Zhenghong Dong
 */
public class Position extends RecordAbstract {
	private final double	_strike;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public Position(final String symbol, final String type, final double strike, final String side, final int qty,
			final String description) {
		super( symbol, type, side, qty, description );
		_strike = strike;
	}

	/***********************************************************************
	 * Object methods
	 ***********************************************************************/
	@Override
	public boolean equals(final Object record) {
		if (record == null) return false;
		if (record == this) return true;
		if (!(record instanceof ActivityAbstract)) return false;
		return (getSymbol().equals( ((Position) record).getSymbol() ) && // compare symbol
				getSide().equals( ((Position) record).getType() ) && // compare type
				getSide().equals( ((Position) record).getSide() ) && // compare side
				getQuantity() == ((Position) record).getQuantity() && // compare quantity
				DoubleComparator.equal( _strike, ((Position) record).getStrike(), 0.0001 ));// compare strike
	}

	@Override
	public int hashCode() {
		return (int) _strike * 13 + super.hashCode();
	}

	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	@Override
	public void writeNext(final Workbook wb, final Row row, final int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getType() ) );
		row.createCell( i++ ).setCellValue( _strike );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSide() ) );
		row.createCell( i++ ).setCellValue( getQuantity() );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getDescription() ) );
	}

	public static int size() {
		return 6;
	}

	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public double getStrike() {
		return _strike;
	}
}
