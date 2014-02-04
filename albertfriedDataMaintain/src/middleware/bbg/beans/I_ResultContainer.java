package middleware.bbg.beans;

/**
 * @author Zhenghong Dong
 */
public interface I_ResultContainer<T> {	
	/**
	 * Receive one solution ( might be a partial solution of the whole problem )
	 * @param solution one solution. When it's partial solution, it might be hold in some inner data structure and some
	 *            analysis will be done to determine if we have solve the whole problem
	 * @throws Exception 
	 */
	void receiveSolution(T solution) throws Exception;

	/**
	 * @return the global solution of this problem
	 */
	T getSolution();

	/**
	 * @return 1 if the solution for the whole problem has been collected, so we can use {@link I_SolutionCollector#getSolution()} to get the global solution
	 * 		   -1 if error happens
	 *         0 if not finished yet
	 */
	int isFinished();
	
	/**
	 * Receive the error message, should finish this container when we get an unsolvable error as response (solvable error won't send back error message)
	 * @param errorMessage
	 */
	void receiveError(String errorMessage);
}
