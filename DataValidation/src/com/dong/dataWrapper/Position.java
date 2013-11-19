package com.dong.dataWrapper;

import java.sql.Date;

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.dong.utils.DoubleComparator;
import com.dong.utils.ParseDate;

/**
 * @author Zhenghong Dong
 */
public class Position extends RecordAbstract {
	private final String	_account;
	private final boolean	_acctHelper;
	private final double	_strike;
	private final Date		_maturity;
	private final String	_underlying;

	/***********************************************************************
	 * Constructor
	 ***********************************************************************/
	public Position(final String account, final String symbol, final String type, final double strike,
			final String side, final int qty, final Date maturity, final String underlying) {
		super( symbol, type, side, qty );
		_strike = strike;
		_account = account;
		_maturity = maturity;
		_acctHelper = _account.equals( "AHLX1209" ) || _account.equals( "020008832" );
		_underlying = underlying;
	}

	/***********************************************************************
	 * Object methods
	 ***********************************************************************/
	@Override
	public boolean equals(final Object record) {
		if (record == null) return false;
		if (record == this) return true;
		if (!(record instanceof Position)) return false;
		return (_acctHelper == ((Position) record).getAccountHelper() && // compare account
				compareMaturity( getMaturity(), ((Position) record).getMaturity() ) && // compare maturity
				this.compareSymbol( (Position) record ) == 0 && // compare symbol
				getType().equals( ((Position) record).getType() ) && // compare type
				compareSide( getSide(), ((Position) record).getSide() ) && // compare side
				getQuantity() == ((Position) record).getQuantity() && // compare quantity
		DoubleComparator.equal( _strike, ((Position) record).getStrike(), 0.0001 ));// compare strike
	}

	@Override
	public int hashCode() {
		return (int) _strike * 13 + _underlying.hashCode() * 31 + getQuantity() * 53 + getType().hashCode() * 23
				+ (_acctHelper ? 1 : 0) + _maturity.hashCode() * 17;
	}

	private int compareSymbol(Position p2) {
		int ret = this.getSymbol().compareTo( p2.getSymbol() );
		if (ret != 0 && this.getUnderlying().equals( p2.getUnderlying() )) ret = 0;
		return ret;
	}

	private boolean compareMaturity(final Date m1, final Date m2) {
		if (m1 == null) return m2 == null;
		return m1.equals( m2 );
	}

	private boolean compareSide(final String s1, final String s2) {
		if (s1 == null) return s2 == null;
		return s1.equals( s2 );
	}

	/** used to sort position, sort also maturity */
/*	public int compareTo(Position p2) {
		int ret = compareSymbol( p2 );
		if (ret == 0) { return getType().compareTo( p2.getType() ); }
		if (ret == 0 && _maturity != null && p2.getMaturity() != null) return _maturity.compareTo( p2.getMaturity() );
		return ret;
	}
*/
	/***********************************************************************
	 * Utilities
	 ***********************************************************************/
	@Override
	public void writeNext(final Workbook wb, final Row row, final int index) {
		int i = index;
		final CreationHelper createHelper = wb.getCreationHelper();
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getAccount() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSymbol() ) );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getType() ) );
		if (_strike == 0) row.createCell( i++ ).setCellValue( createHelper.createRichTextString( "" ) );
		else row.createCell( i++ ).setCellValue( _strike );
		row.createCell( i++ ).setCellValue( createHelper.createRichTextString( getSide() ) );
		row.createCell( i++ ).setCellValue( getQuantity() );
		row.createCell( i++ ).setCellValue(
				createHelper.createRichTextString( ParseDate.standardFromSQLDate( getMaturity() ) ) );
	}

	public static int size() {
		return 7;
	}

	/***********************************************************************
	 * Getter and Setter
	 ***********************************************************************/
	public double getStrike() {
		return _strike;
	}

	public String getAccount() {
		return _account;
	}

	public Date getMaturity() {
		return _maturity;
	}

	private boolean getAccountHelper() {
		return _acctHelper;
	}

	private String getUnderlying() {
		return _underlying;
	}
}
