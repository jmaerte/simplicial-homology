package com.jmaerte.util;

import com.jmaerte.simplicial.util.Wrapper;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Julian on 21/06/2017.
 */
public class Shiftable {

    boolean[] shift;
    public int CURRENT_K = 0;
    public int lowest = 0;

    Shiftable(int length) {
        shift = new boolean[length];
    }

    public void reset(int k) {
        if(k > shift.length) {
            return;// throw exception.
        }
        shift = new boolean[shift.length];
        CURRENT_K = k;
        lowest = 0;
        for(int i = 0; i < k; i++) {
            shift[i] = true;
        }
    }

    public boolean isMax() {
        for(int i = 0; i < CURRENT_K; i++) {
            if(!shift[shift.length - i - 1]) return false;
        }
        return true;
    }

    public void shift(int i) {
        if(i + 1 == shift.length) {
            lowest = 0;
            return;
        }
        if(!shift[i]) {
            shift(i+1);
            return;
        }
        if(shift[i + 1]) {
            shift[i] = false;
            shift[lowest] = true;
            lowest++;
            shift(i+1);
        }else {
            shift[i + 1] = true;
            shift[i] = false;
            lowest = 0;
        }
    }

    public void print() {
        String s = "}";
        for(boolean sh : shift) {
            s = (sh ? "1" : "0") + s;
        }
        System.out.println("{" + s);
    }



    public LinkedList<Integer> save(int[] values)
    {
        LinkedList<Integer> list = new LinkedList<Integer>();
        for(int i = 0;i<shift.length;i++)
        {
            if(shift[i])
            {
                list.add(values[i]);
            }
        }
        return list;
    }

    public Wrapper saveAsArray(int[] values)
    {
        int[] result = new int[CURRENT_K];
        for(int i = 0, x = 0;i<values.length;i++)
        {
            if(shift[i])
            {
                result[x++] = values[i];
            }
        }
        return new Wrapper(result);
    }

    public BigInteger saveAsBigInteger(int[] values) {
        if(shift.length != values.length) {
            return BigInteger.valueOf(-1);
        }
        BigInteger result = BigInteger.ZERO;
        for(int i = 0; i < values.length; i++) {
            if(shift[i]) result = result.or(BigInteger.ONE.shiftLeft(values[i] - 1));
        }
        return result;
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
        int size = 3;


        Shiftable shift = new Shiftable(3);
        Set<Wrapper> results = new HashSet<Wrapper>();
        for(int[] array : arr)
        {
            for(int i = 1;i<=array.length;i++)
            {
                shift.reset(i);
                results.add(shift.saveAsArray(array));
                while(!shift.isMax())
                {
                    shift.shift(0);
                    results.add(shift.saveAsArray(array));
                }
            }
        }
        List<Wrapper> list = new ArrayList<Wrapper>(results);
        Collections.sort(list, new Sorter());

        StringBuilder string = new StringBuilder();
        for(Wrapper data : list)
        {
            int[] array = data.data;
            string.append("{");
            for(int i = 0;i<array.length;i++)
            {
                string.append(array[i]);
                if(i + 1 != array.length)
                {
                    string.append(", ");
                }
            }
            string.append("} ");
        }
        System.out.println("Results: "+string);
        System.out.println("Shifter: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");



        nsShiftable = System.nanoTime();
        shift = new Shiftable(3);
        HashMap<Integer, HashSet<BigInteger>> map = new HashMap<>();
        for(int k = 1; k <= 3; k++) {
            map.put(k, new HashSet<>());
        }
        for(int k = 1; k <= 3; k++) {
            HashSet<BigInteger> set = map.get(k);
            shift.reset(k);
            while(!shift.isMax()) {
                for(int[] values : arr) {
                    set.add(shift.saveAsBigInteger(values));
                }
                shift.shift(0);
            }
            for(int[] values : arr) {
                set.add(shift.saveAsBigInteger(values));
                shift.shift(0);
            }
        }
        System.out.println(map);
        System.out.println("Shifter: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");


//        Shiftable shift = new Shiftable(3);
//        for(int[] array : arr)
//        {
//            LinkedList<LinkedList<Integer>> list = new LinkedList<LinkedList<Integer>>();
//            for(int i = 1;i<=array.length;i++)
//            {
//                shift.reset(i);
//                list.add(shift.save(array));
//                while(!shift.isMax())
//                {
//                    shift.shift(0);
//                    list.add(shift.save(array));
//                }
//            }
//            System.out.println("All Values: "+list);
//        }
//        System.out.println("Shifter: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");




//        int m = 100;
//        int k = 60;
//        nsShiftable = System.nanoTime();
//        Shiftable shiftm = new Shiftable(m);
//        shiftm.reset(k);
//        while(!shiftm.isMax()) {
//            shiftm.shift(0);
//            // shiftm.print();
//        }
//        System.out.println("Shifter: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");





//        nsShiftable = System.nanoTime();
//
//        BigInteger v = BigInteger.ZERO;
//        BigInteger cap = BigInteger.ZERO;
//        for(int i = 0; i < 5; i++) {
//            v = v.or(BigInteger.ONE.shiftLeft(i));
//            cap = cap.or(BigInteger.ONE.shiftLeft(m-i));
//        }
//        BigInteger t = BigInteger.ZERO;
//        while(v.compareTo(cap) != 0) {
//            t = v.or(v.subtract(BigInteger.ONE)).add(BigInteger.ONE);
//            v = t.or(t.and(t.multiply(BigInteger.valueOf(-1))).divide(v.and(v.multiply(BigInteger.valueOf(-1)))).shiftRight(1).subtract(BigInteger.ONE));
//        }
//        System.out.println("BigInteger: " + (System.nanoTime() - nsShiftable)/1000000 + "ms");
    }

    public static class Sorter implements Comparator<Wrapper>
    {

        @Override
        public int compare(Wrapper o1, Wrapper o2)
        {
            if(o1.data.length == o2.data.length)
            {
                return 0;
            }
            if(o1.data.length > o2.data.length)
            {
                return 1;
            }
            return -1;
        }

    }
}
