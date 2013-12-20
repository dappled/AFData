package utils;


/**
 * @author Zhenghong Dong
 */
public class CollectionUtils {
	public static double[] doubleFromDouble(Double[] org) {
		double[] ret = new double[org.length];
		
		for (int i = 0; i < ret.length; i++) {
			ret[i] = org[i];
		}
		return ret;
	}
	
}
