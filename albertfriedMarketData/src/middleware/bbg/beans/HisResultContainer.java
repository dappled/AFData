package middleware.bbg.beans;

import java.util.HashMap;
import java.util.Set;

import middleware.bbg.beans.DataRequest.RequestType;
import bbgRequestor.bloomberg.beans.HisDivTs;
import bbgRequestor.bloomberg.beans.HisSecTs;
import bbgRequestor.bloomberg.beans.TimeSeries;
import bbgRequestor.bloomberg.beans.TimeUnit;

/**
 * @author Zhenghong Dong
 */
public class HisResultContainer implements I_ResultContainer<HashMap<String, ? extends TimeSeries<? extends TimeUnit>>> {
	private RequestType													_type;
	private HashMap<String, ? extends TimeSeries<? extends TimeUnit>>	_solutions;
	private Set<String>													_underlyings;
	private int															_finished;

	public HisResultContainer(RequestType type, Set<String> names) throws Exception {
		_type = type;
		_solutions = new HashMap<>();
		_underlyings = names;
		_finished = 0;
	}
	
	public HisResultContainer(String type, Set<String> names) throws Exception {
		this(RequestType.getType( type ), names);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void receiveSolution(HashMap<String, ? extends TimeSeries<? extends TimeUnit>> solution) throws Exception {
		if (_type == RequestType.Div) {
			((HashMap<String, HisDivTs>) _solutions).putAll( (HashMap<String, HisDivTs>) solution );
		} else if (_type == RequestType.Sec) {
			((HashMap<String, HisSecTs>) _solutions).putAll( (HashMap<String, HisSecTs>) solution );
		} else throw new Exception( "Ref type should be either div or sec" );
		for (TimeSeries<? extends TimeUnit> ts : solution.values()) {
			_underlyings.remove( ts.getName() );
		}
		if (_underlyings.isEmpty()) {
			_finished = 1;
		}
	}

	@Override
	public HashMap<String, ? extends TimeSeries<? extends TimeUnit>> getSolution() {
		return _solutions;
	}

	@Override
	public int isFinished() {
		return _finished;
	}

	@Override
	public void receiveError(String errorMessage) {
		_finished = -1;
	}
}
