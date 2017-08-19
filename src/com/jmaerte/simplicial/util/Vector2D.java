package com.jmaerte.simplicial.util;

import java.util.ArrayList;

/**
 * Created by Julian on 02/07/2017.
 */
public class Vector2D <T,K> {

    public T x;
    public K y;

    public Vector2D(T x, K y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static <T,K extends Comparable<K>> int binarySearch(ArrayList<Vector2D<T, K>> arr, K y, int max) {
        if(max == 0 || max >= arr.size() || y.compareTo(arr.get(max - 1).y) > 0) return max;
        int left = 0;
        int right = max;
        while(left < right) {
            int mid = (left + right) / 2;
            if(arr.get(mid).y.compareTo(y) > 0) right = mid;
            else if(arr.get(mid).y.compareTo(y) < 0) left = mid + 1;
            else return mid;
        }
        return left;
    }

}
