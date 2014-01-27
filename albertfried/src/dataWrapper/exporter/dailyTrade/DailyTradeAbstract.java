package dataWrapper.exporter.dailyTrade;

import dataWrapper.RealBasic;


/**
 * @author Zhenghong Dong
 */
public abstract class DailyTradeAbstract extends RealBasic {
	private final String	_account, _side;
	private final float		_qty;
	private final float		_price;
	private final String	_broker;

	/**
	 * @param symbol
	 * @throws Exception 
	 */
	public DailyTradeAbstract(String account,String symbol,String side, float quantity, float price, String broker) {
		super( symbol );
		_account = account;
		_side = side;
		_qty = quantity;
		_price = price;
		_broker = broker;
	}
	
	public String getAccount() {
		return _account;
	}

	public String getSide() {
		return _side;
	}

	public float getQuantity() {
		return _qty;
	}

	public float getPrice() {
		return _price;
	}

	public String getBroker() {
		return _broker;
	}
}
