package com.dong.utils;

public class DoubleComparator {

	public static boolean equal( double d1, double d2, double tolerance ) {
		return Math.abs(d1 - d2) < tolerance;
	}
	
	public static int compare( double d1, double d2, double tolerance ) {
		double diff = d1 - d2;
		if( Math.abs( diff ) < tolerance )
			return 0;
		return (int) Math.signum( diff );
	}
	
}
