package bbgRquestor.bloomberg.beans;

import java.io.Serializable;

/**
 * @author Zhenghong Dong
 */
@SuppressWarnings("serial")
public abstract class TimeUnit implements Serializable {
	private String	_time;
	private String _name;
	
	public TimeUnit(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}
	public String getTime() {
		return _time;
	}

	public void setTime(String time) {
		_time = time;
	}

	public abstract void printPiece();
	
	public abstract void set(String fields, Object value);
}
