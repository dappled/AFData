package utils;

/**
 * @author Zhenghong Dong
 */
public class StringUtils {
	public static String numberToStringWithoutZeros(double num){
		if (num == (int) num) return String.format( "%d", (int ) num);
		return String.format( "%s", num );
	}
}
