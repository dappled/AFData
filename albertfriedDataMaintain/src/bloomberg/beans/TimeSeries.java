package bloomberg.beans;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author Zhenghong Dong
 */
@SuppressWarnings("serial")
public abstract class TimeSeries<T extends TimeUnit> implements Serializable {
	private String				_name;
	protected LinkedHashMap<String, T>	_timeSeriesData;
	private Class<T> clazz;

	public TimeSeries(String name) {
		_name = name;
		_timeSeriesData = new LinkedHashMap<>();
	}

	public TimeSeries() {
		_timeSeriesData = new LinkedHashMap<>();
	}

	public void addNode(String time) throws InstantiationException, IllegalAccessException {
		_timeSeriesData.put( time, clazz.newInstance() );
	}

	public LinkedHashMap<String, T> getTSData() {
		return _timeSeriesData;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}
}
