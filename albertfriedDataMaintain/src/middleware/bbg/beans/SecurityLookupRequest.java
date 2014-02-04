package middleware.bbg.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhenghong Dong
 */
public class SecurityLookupRequest implements Serializable {
	private static final long	serialVersionUID	= 3641371400229314590L;
	private final List<String> _args;
	
	public SecurityLookupRequest(String[] args) {
		_args = Arrays.asList( args );
	}

	public List<String> getArgs() {
		return _args;
	}
}
