package middleware.bbg.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * @author Zhenghong Dong
 */
public class DataRequest implements Serializable {
	private static final long	serialVersionUID	= -7346393496648035393L;
	private final RequestType _type;
	private final Set<String> _names;
	private final Set<String> _fields;
	private final HashMap<String, Object> _properties;
	
	public static class RequestType implements Serializable{
		private static final long	serialVersionUID	= 1L;
		public static final RequestType	Sec	= new RequestType( "sec" );
		public static final RequestType	Div	= new RequestType( "div" );

		private final String		_type;

		private RequestType(final String type) {
			_type = type;
		}
		
		public static RequestType getType(final String type) throws Exception {
			switch (type) {
				case "sec":
					return Sec;
				case "div":
					return Div;
				default:
					throw new Exception( "Data request type should be sec or div" );
			}
		}

		@Override
		public String toString() {
			return _type;
		}
	}
	
	public DataRequest(String type, Set<String> names, Set<String> fields, HashMap<String, Object> properties) throws Exception {
		_names = names;
		_fields = fields;
		_properties = properties;
		_type = RequestType.getType( type );
	}
	
	public DataRequest(RequestType type, Set<String> names, Set<String> fields, HashMap<String, Object> properties) throws Exception {
		_names = names;
		_fields = fields;
		_properties = properties;
		_type = type;
	}

	public RequestType getType() {
		return _type;
	}

	public Set<String> getNames() {
		return _names;
	}

	public Set<String> getFields() {
		return _fields;
	}

	public HashMap<String, Object> getProperties() {
		return _properties;
	}
} 
