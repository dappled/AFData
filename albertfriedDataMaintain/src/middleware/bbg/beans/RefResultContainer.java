package middleware.bbg.beans;

import java.util.ArrayList;
import java.util.Set;

import middleware.bbg.beans.DataRequest.RequestType;
import bbgRequestor.bloomberg.beans.DividendTimeUnit;
import bbgRequestor.bloomberg.beans.SecurityTimeUnit;
import bbgRequestor.bloomberg.beans.TimeUnit;

/**
 * @author Zhenghong Dong
 */
public class RefResultContainer implements I_ResultContainer<ArrayList<? extends TimeUnit>> {
	private RequestType						_type;
	private ArrayList<? extends TimeUnit>	_solutions;
	private Set<String>						_underlyings;
	private int								_finished;

	public RefResultContainer(RequestType type, Set<String> names) throws Exception {
		_type = type;
		_solutions = new ArrayList<>();
		_underlyings = names;
		_finished = 0;
	}
	
	public RefResultContainer(String type, Set<String> names) throws Exception {
		this(RequestType.getType( type ),names);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void receiveSolution(ArrayList<? extends TimeUnit> solution) throws Exception {
		if (_type == RequestType.Div) {
			((ArrayList<DividendTimeUnit>) _solutions).addAll( (ArrayList<DividendTimeUnit>) solution );
		} else if (_type == RequestType.Sec) {
			((ArrayList<SecurityTimeUnit>) _solutions).addAll( (ArrayList<SecurityTimeUnit>) solution );
		} else throw new Exception( "Ref type should be either div or sec" );
		for (TimeUnit tu : solution) {
			if (tu.getName().equals( "CTT US Equity" )) System.out.println("CTT found");
			_underlyings.remove( tu.getName() );
			System.out.println("CTT removed");
			System.out.println(_underlyings.contains( "CTT US Equity" ));
		}
		if (_underlyings.isEmpty()) {
			_finished = 1;
		}
	}

	@Override
	public ArrayList<? extends TimeUnit> getSolution() {
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
