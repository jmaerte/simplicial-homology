package com.jmaerte.util;

import javafx.util.Callback;

import java.util.Vector;

/**
 * Created by Julian on 21/06/2017.
 */
public class Sort {

    public static <T> void quickSort(T[] arr, int low, int high, Callback<Vector<T>, Integer> compare) {
        if (arr == null || arr.length == 0)
            return;
        if (low >= high)
            return;
        // pick the pivot
        int middle = low + (high - low) / 2;
        T pivot = arr[middle];
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            Vector<T> v = new Vector<T>(2);
            v.add(0, arr[i]);
            v.add(1, pivot);
            while (compare.call(v) < 0) {
                i++;
            }
            while (compare.call(v) > 0) {
                j--;
            }
            if (i <= j) {
                T temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
        // recursively sort two sub parts
        if (low < j)
            quickSort(arr, low, j, compare);
        if (high > i)
            quickSort(arr, i, high, compare);
    }

}
