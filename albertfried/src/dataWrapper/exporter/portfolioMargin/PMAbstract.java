package dataWrapper.exporter.portfolioMargin;

import dataWrapper.RealBasic;

/**
 * @author rn
 */
public abstract class PMAbstract extends RealBasic {
	private final String	_importDate;

	/**
	 * @param symbol
	 */
	public PMAbstract(String date, String symbol) {
		super( symbol );
		_importDate = date;
	}

	public String getImportDate() {
		return _importDate;
	}
}
