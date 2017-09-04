package com.jmaerte.main;

import com.jmaerte.io.IO;
import com.jmaerte.simplicial.Simplicial;
import com.jmaerte.simplicial.util.*;

import java.math.BigInteger;

/**
 * Created by Julian on 17/06/2017.
 */
public class Homology {

    public static final String HELP = "Following Arguments can be given to the program call:\n" +
            "+==============+=========================================+\n" +
            "|    Option    |               Description               |\n" +
            "+==============+=========================================+\n" +
            "| --help       | Shows this menu                         |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| -L [path]    | Turns on the logging option where       |\n" +
            "|              | [path] specifies which folder to log to |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| -C [path]    | Specifies where to read the complex     |\n" +
            "|              | from                                    |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| --S          | Shows smith normal form instead of      |\n" +
            "|              | homology groups.                        |\n" +
            "+==============+=========================================+";

    public static Homology INSTANCE;

    // Options:
    public boolean isBigInteger = false,
            isName = false,
            isLog = false;
    public String name = "",
            outputPath,
            path;

    public Homology(String[] args) {

//        int[][] arr = new int[][]{
//                {1,2,6},{1,6,4},{2,3,6},
//                {3,6,7},{1,3,7},{1,5,7},
//                {4,5,6},{5,6,9},{6,7,8},
//                {6,8,9},{8,9,10},{7,8,10},
//                {5,7,10},{4,5,10},{1,5,9},
//                {1,2,9},{2,9,10},{2,3,10},
//                {1,3,10},{1,4,10}
//        };

//        int m = 100;
//        int maxSize = 15;
//        int minSize = 10;
//        int n = 8000;
//        int[][] arr = new int[m][];
//        for(int i = 0; i < m; i++) {
//            int length = (int)(Math.random()*(maxSize - minSize)) + minSize;
//            HashSet<Integer> set = new HashSet<>();
//            for(int j = 0; j < length; j++) {
//                set.add((int)(Math.random()*(m-1))+1);
//            }
//            arr[i] = new int[set.size()];
//            Iterator<Integer> it = set.iterator();
//            int curr = 0;
//            while(it.hasNext()) {
//                arr[i][curr] = it.next();
//                curr++;
//            }
//        }
        // Loading Options. For more information take a look at Readme.md.

        // check if linear works.
//        SparseVector v = new SparseVector(3);
//        SparseVector w = new SparseVector(3);
//        v.indices = new int[]{1,2};
//        v.values = new int[]{2,2};
//        v.occupation = 2;
//        w.indices = new int[]{0,2};
//        w.values = new int[]{1,1};
//        w.occupation = 2;
//        System.out.println(SparseVector.linear(1, v, -2, w));
//        System.exit(0);
        // example 0:
//        ArrayList<SparseVector> rows0 = new ArrayList<>();
//        SparseVector v01 = new SparseVector(4);
//        SparseVector v02 = new SparseVector(4);
//        SparseVector v03 = new SparseVector(4);
//        v01.occupation = 4;
//        v01.indices = new int[]{0,1,2,3};
//        v01.values = new int[]{8,9,10,11};
//        v02.occupation = 4;
//        v02.indices = new int[]{0,1,2,3};
//        v02.values = new int[]{17,80,10,7};
//        v03.occupation = 4;
//        v03.indices = new int[]{0,1,2,3};
//        v03.values = new int[]{6,9,2,8};
//        rows0.add(v01);
//        rows0.add(v02);
//        rows0.add(v03);
//        System.out.println(Simplicial.smith(new Vector4D<>(0, new int[0], new SparseVector[0], rows0), true));
//        System.exit(0);

        // example 1:
//        ArrayList<SparseVector> rows1 = new ArrayList<>();
//        SparseVector v11 = new SparseVector(2);
//        SparseVector v12 = new SparseVector(2);
//        v11.occupation = 2;
//        v11.indices = new int[]{0,1};
//        v11.values = new int[]{2,4};
//        v12.occupation = 2;
//        v12.indices = new int[]{0,1};
//        v12.values = new int[]{-2,6};
//
//        rows1.add(v11);
//        rows1.add(v12);
//        Number[] arr1 = Simplicial.smith(new Vector4D<>(0, new int[0], new SparseVector[0], rows1), true);
//        for(Number n : arr1) {
//            System.out.println("simp: " + n);
//        }
//        System.exit(0);

//        // example 2:
//        ArrayList<SparseVector> rows = new ArrayList<>();
//        SparseVector v1 = new SparseVector(3);
//        SparseVector v2 = new SparseVector(3);
//        SparseVector v3 = new SparseVector(3);
//        v1.indices = new int[]{0,1,2};
//        v1.values = new int[]{2,4,4};
//        v1.occupation = 3;
//        v2.indices = new int[]{0,1,2};
//        v2.values = new int[]{-6,6,12};
//        v2.occupation = 3;
//        v3.indices = new int[]{0,1,2};
//        v3.values = new int[]{10,-4,-16};
//        v3.occupation = 3;
//        rows.add(v1);
//        rows.add(v2);
//        rows.add(v3);
//        Smith arr = Simplicial.smith(new Vector4D<>(0, new int[0], new SparseVector[0], rows), true);
//        System.out.println(arr);
//        System.exit(0);

//        // example 3:
//        ArrayList<SparseVector> rows2 = new ArrayList<>();
//        SparseVector v21 = new SparseVector(3);
//        SparseVector v22 = new SparseVector(3);
//        SparseVector v23 = new SparseVector(3);
//        v21.occupation = 2;
//        v22.occupation = 3;
//        v23.occupation = 2;
//        v21.indices = new int[]{0,2};
//        v22.indices = new int[]{0,1,2};
//        v23.indices = new int[]{0,1};
//        v21.values = new int[]{2,5};
//        v22.values = new int[]{4,1,2};
//        v23.values = new int[]{6,2};
//        rows2.add(v21);
//        rows2.add(v22);
//        rows2.add(v23);
//        System.out.println(Simplicial.smith(new Vector4D<>(0, new int[0], new SparseVector[0], rows2), true));
//        System.exit(0);
        long ns1 = 0;
        long ns2 = 0;
        Bignum a = new Bignum(1, new int[1], 1);
        BigInteger b = BigInteger.ZERO;
        for(int i = 0; i < 100; i++) {
            long ns = System.nanoTime();
            a.add(new Bignum(1, new int[]{-1}, 1));
            ns1 += System.nanoTime()-ns;
            ns = System.nanoTime();
            b = b.add(new BigInteger("11111111111111111111111111111111", 2));
            ns2 += System.nanoTime()-ns;
        }
        System.out.println(a);
        System.out.println(b);
        System.out.println(ns1);
        System.out.println(ns2);
        System.exit(0);

        for(int i = 0; i < args.length; i++) {
            if(args[i].length() > 0) {
                switch(args[i].charAt(0)) {
                    case '-':
                        if(args[i].length() < 2) throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                        if(args[i].charAt(1) == '-') {
                            if(args[i].length() < 3) throw new IllegalArgumentException("Not a valid argument: " + args[i]);
                            doubleOption(args[i].substring(2, args[i].length()));
                        } else {
                            if(i == args.length - 1) throw new IllegalArgumentException("Expected argument after: " + args[i]);
                            option(args[i].substring(1, args[i].length()), args[i+1]);
                            i++;
                        }
                        break;
                    default:
                        arguments(args[i]);
                        break;
                }
            }
        }

        // int[][] arr = IO.loadArrayFromFile("C:\\Users\\Julian\\Desktop\\chess77.txt", '[', ']', ',');
        Complex c = IO.loadComplexFromFile(path, '[', ']', ',', isName);
        name = c.name;
        long ms = System.currentTimeMillis();
        Simplicial simpl = new Simplicial(c);

//        System.out.println(simpl.binarySearch(simpl.faces.get(2), new Wrapper(new int[]{2, 10})));

        // System.out.println(simpl.faces);
//        for(int k = 0; k < simpl.faces.size(); k++) {
//            System.out.println("Found " + simpl.faces.get(k).size() + " faces with dimension " + (k-1));
//        }
        System.out.println("Program finished after " + (System.currentTimeMillis() - ms) + "ms");

    }




    public void doubleOption(String qualifier) {
        switch(qualifier) {
            case "help":
                System.out.println(HELP);
                System.exit(0);
                break;
            case "BI":// turns on BigInteger usage
                isBigInteger = true;
                break;
        }
    }
    public void option(String qualifier, String value) {
        switch(qualifier) {
            case "C":// load file without using home dir
                path = value;
                break;
            case "L":// turn on logging
                isLog = true;
                outputPath = value;
                break;
            case "from":// from H_{k0}
                break;
            case "to":// to H_{k1}
                break;
        }
    }
    public void arguments(String arg) {
    }

    public static void main(String[] args) {
        System.out.println("Program for calculating simplicial homology.\nCopyright by Julian Märte 2017.\nReport bugs to maertej@students.uni-marburg.de");
        INSTANCE = new Homology(args);
    }
}
