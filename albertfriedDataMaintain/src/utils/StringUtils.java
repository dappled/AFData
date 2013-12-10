package utils;

/**
 * @author Zhenghong Dong
 */
public class StringUtils {
	public static String numberToStringWithoutZeros(double num){	
		if (num == (int) num) return String.format( "%d", (int ) num);
		return String.format( "%s", num );
	}
	
	public static String numberToStringWithoutZeros(float num){	
		if (num == (int) num) return String.format( "%d", (int ) num);
		return String.format( "%s", num );
	}
	
	public static int firstOccuranceOfArray(String obj, String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			if (obj.indexOf( strings[i] ) > -1) return obj.indexOf( strings[i] );
		}
		return -1;
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtils.numberToStringWithoutZeros( 33.4 ));
	}
}
