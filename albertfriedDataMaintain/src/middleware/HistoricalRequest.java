package middleware;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.bloomberglp.blpapi.Name;

/**
 * @author Zhenghong Dong
 */
public class HistoricalRequest implements Serializable {
	private static final long	serialVersionUID	= -7346393496648035393L;
	private final String _type;
	private final List<String> _names;
	private final List<String> _fields;
	private final HashMap<Name, Object> _properties;
	
	public HistoricalRequest(String type, List<String> names, List<String> fields, HashMap<Name, Object> properties) {
		_names = names;
		_fields = fields;
		_properties = properties;
		_type = type;
	}

	public String getType() {
		return _type;
	}

	public List<String> getNames() {
		return _names;
	}

	public List<String> getFields() {
		return _fields;
	}

	public HashMap<Name, Object> getProperties() {
		return _properties;
	}
	
	
} 
