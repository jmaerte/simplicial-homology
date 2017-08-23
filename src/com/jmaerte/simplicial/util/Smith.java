package com.jmaerte.simplicial.util;

import java.math.BigInteger;

public class Smith {

    int[] amount;
    BigInteger[] values;
    int occupation;

    public Smith(int initialCapacity) {
        amount = new int[initialCapacity];
        values = new BigInteger[initialCapacity];
        occupation = 0;
    }

    public void addTo(BigInteger value, int count) {
        int k = index(value);
        if(k < occupation && values[k].equals(value)) {
            amount[k] += count;
        }else {
            insert(k, value, count);
        }
    }

    public void insert(int k, BigInteger value, int count) {
        if(value.equals(BigInteger.ZERO)) {
            return;
        }
        if(values.length < occupation + 1) {
            mkPlace();
        }
        if(occupation - k > 0) {
            System.arraycopy(values, k, values, k + 1, occupation - k);
            System.arraycopy(amount, k, amount, k + 1, occupation - k);
        }

        values[k] = value;
        amount[k] = count;
        occupation++;
    }

    private void mkPlace() {
        int capacity = (occupation * 3) / 2 + 1;
        BigInteger[] _values = new BigInteger[capacity];
        int[] _amount = new int[capacity];
        System.arraycopy(values, 0 ,_values, 0, occupation);
        System.arraycopy(amount, 0, _amount, 0, occupation);
        values = _values;
        amount = _amount;
    }

    public int index(BigInteger value) {
        if(occupation == 0 || value.compareTo(values[occupation - 1]) > 0) return occupation;
        int left = 0;
        int right = occupation;
        while(left < right) {
            int mid = (right + left)/2;
            if(values[mid].compareTo(value) > 0) right = mid;
            else if(values[mid].compareTo(value) < 0) left = mid + 1;
            else return mid;
        }
        return left;
    }

    public String toString() {
        String s = "[Value: amount, ";
        for(int i = 0; i < occupation; i++) {
            s += values[i] + ": " + amount[i] + ((i == occupation - 1) ? "" : ", ");
        }
        return s + "]";
    }
}
