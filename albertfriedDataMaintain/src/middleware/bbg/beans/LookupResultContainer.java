package middleware.bbg.beans;

import java.util.List;

import bbgRequestor.bloomberg.beans.SecurityLookUpResult;

/**
 * @author Zhenghong Dong
 */
public class LookupResultContainer implements I_ResultContainer<List<SecurityLookUpResult>> {
	private List<SecurityLookUpResult>	_solutions;
	private int							_finished;

	public LookupResultContainer() {
		_finished = 0;
	}

	/** LookUpResult should be small, so will be sent together. */
	@Override
	public void receiveSolution(List<SecurityLookUpResult> solution) throws Exception {
		_solutions = solution;
		_finished = 1;
	}

	@Override
	public List<SecurityLookUpResult> getSolution() {
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
