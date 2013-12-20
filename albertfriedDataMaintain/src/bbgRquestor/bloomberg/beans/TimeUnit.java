package bbgRquestor.bloomberg.beans;

import java.io.Serializable;

/**
 * @author Zhenghong Dong
 */
@SuppressWarnings("serial")
public abstract class TimeUnit implements Serializable {
	private String	_time;

	public String getTime() {
		return _time;
	}

	public void setTime(String time) {
		_time = time;
	}
}
