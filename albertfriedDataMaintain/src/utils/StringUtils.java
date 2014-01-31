package utils;


/**
 * @author Zhenghong Dong
 */
public class StringUtils {
	public static String numberToStringWithoutZeros(double num){	
		if (num == (int) num) return String.format( "%d", (int ) num);
		return String.format( "%s", num );
	}
	
	public static String numberToStringWithoutZeros(Double num){
		if (num == null) return null;
		double n = num;
		if (n == (int) n) return String.format( "%d", (int ) n);
		return String.format( "%s", n );
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
	
	public static String[] arrayToUpper(String[] l) {
		for (int i = 0; i < l.length; i++) {
			l[i] = l[i].toUpperCase();
		}
		return l;
	}
	
	public static String[] arrayTrimQuotes(String[] l) {
		for (int i = 0; i < l.length; i++) {
			l[i] = l[i].replaceAll("^\"|\"$", "");
		}
		return l;
	}
	
	public static String[] arrayTrim(String[] l) {
		for (int i = 0; i < l.length; i++) {
			l[i] = l[i].trim();
		}
		return l;
	}
	
	public static String[] splitWIthQuotes(String l) {
		return l.split( "[ ]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)" );
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtils.numberToStringWithoutZeros( 33.4 ));
		String[] a = {"\"aaa\"", "bbb", "ccc" };
		System.out.println(arrayToUpper( a )[0]);
		System.out.println(arrayTrimQuotes( a )[0]);
	}
}
