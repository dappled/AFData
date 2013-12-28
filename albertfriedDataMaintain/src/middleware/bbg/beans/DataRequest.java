package middleware.bbg.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author Zhenghong Dong
 */
public class DataRequest implements Serializable {
	private static final long	serialVersionUID	= -7346393496648035393L;
	private final String _type;
	private final List<String> _names;
	private final List<String> _fields;
	private final HashMap<String, Object> _properties;
	
	public DataRequest(String type, List<String> names, List<String> fields, HashMap<String, Object> properties) {
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

	public HashMap<String, Object> getProperties() {
		return _properties;
	}
} 
