package com.jmaerte.util;

import java.math.BigInteger;

public class Search {
	
	/**Binary search:
	 * 
	 * @return -1 if element is not contained. i if element is in position i.
	 */
	public static int binarySearch(BigInteger[] a, BigInteger key) {
        int lo = 0;
        int hi = a.length - 1;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            int mid = lo + (hi - lo) / 2;
            if      (key.compareTo(a[mid]) < 0) hi = mid - 1;
            else if (key.compareTo(a[mid]) > 0) lo = mid + 1;
            else return mid;
        }
        return -1;
    }
	
}
