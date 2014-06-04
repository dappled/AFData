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

	public static <T> String flatList(T[] list) {
		String re = "";
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				re += list[i].toString() + ";";
			}
		}
		return re;
	}
}
