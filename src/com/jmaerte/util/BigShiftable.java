package com.jmaerte.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Julian on 22/06/2017.
 */
public class BigShiftable {

    BigInteger shift;
    public int CURRENT_K = 0;
    public int lowest = 0;
    private int length;

    BigShiftable(int length) {
        shift = BigInteger.ZERO;
        this.length = length;
    }

    public void reset(int k) {
        if(k > length) {
            return;// throw exception.
        }
        shift = BigInteger.ZERO;
        CURRENT_K = k;
        lowest = 0;
        for(int i = 0; i < k; i++) {
            shift = shift.or(BigInteger.ONE.shiftLeft(i));
        }
    }

    public boolean isMax() {
        for(int i = 0; i < CURRENT_K; i++) {
            if(shift.and(BigInteger.ONE.shiftLeft(length - i - 1)).compareTo(BigInteger.ZERO) == 0) return false;
        }
        return true;
    }

    public void shift(int i) {
        if(i + 1 == length) {
            lowest = 0;
            return;
        }
        if(shift.and(BigInteger.ONE.shiftLeft(i)).compareTo(BigInteger.ZERO) == 0) {
            shift(i+1);
            return;
        }
        if(shift.and(BigInteger.ONE.shiftLeft(i + 1)).compareTo(BigInteger.ZERO) != 0) {
            shift = shift.xor(BigInteger.ONE.shiftLeft(i));
            shift = shift.or(BigInteger.ONE.shiftLeft(lowest));
            lowest++;
            shift(i+1);
        }else {
            shift = shift.or(BigInteger.ONE.shiftLeft(i + 1));
            shift = shift.xor(BigInteger.ONE.shiftLeft(i));
            lowest = 0;
        }
    }

    public void print() {
        String s = "}";
        for(int i = 0; i < length; i++) {
            s = (shift.and(BigInteger.ONE.shiftLeft(i)).compareTo(BigInteger.ZERO) != 0 ? "1" : "0") + s;
        }
        System.out.println("{" + s);
    }

    public static void main(String[] args) {

        int[][] arr = new int[][]{
                {1,2,6},{1,6,4},{2,3,6},
                {3,6,7},{1,3,7},{1,5,7},
                {4,5,6},{5,6,9},{6,7,8},
                {6,8,9},{8,9,10},{7,8,10},
                {5,7,10},{4,5,10},{1,5,9},
                {1,2,9},{2,9,10},{2,3,10},
                {1,3,10},{1,4,10}
        };

        long nsShiftable = System.nanoTime();
        BigShiftable shift = new BigShiftable(3);
        HashMap<Integer, HashSet<BigInteger>> map = new HashMap<>();
        for(int k = 1; k <= 3; k++) {
            map.put(k, new HashSet<>());
        }
        for(int k = 1; k <= 3; k++) {
            HashSet<BigInteger> set = map.get(k);
            shift.reset(k);
            while(!shift.isMax()) {
                for(int[] values : arr) {
                    set.add(shift.shift);
                }
                shift.shift(0);
            }
            for(int[] values : arr) {
                set.add(shift.shift);
                shift.shift(0);
            }
        }
        System.out.println(map);
        System.out.println("Shifter: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");
    }
}
