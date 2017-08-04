package com.jmaerte.main;

import com.jmaerte.io.IO;
import com.jmaerte.io.Logger;
import com.jmaerte.simplicial.Simplicial;
import com.jmaerte.simplicial.util.Complex;
import com.jmaerte.simplicial.util.SparseMatrix;
import com.jmaerte.simplicial.util.SparseVector;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Julian on 17/06/2017.
 */
public class Homology {

    public static final String HELP = "Following Arguments can be given to the program call:\n" +
            "+--------------+-----------------------------------------+\n" +
            "|    Option    |               Description               |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| --help       | Shows this menu                         |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| -L [path]    | Turns on the logging option where       |\n" +
            "|              | [path] specifies which folder to log to |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| -C [path]    | Specifies where to read the complex     |\n" +
            "|              | from                                    |\n" +
            "+--------------+-----------------------------------------+\n" +
            "| --BI         | Turns on BigInteger usage               |\n" +
            "+--------------+-----------------------------------------+" +
            "";

    public static Homology INSTANCE;

    // Options:
    public boolean isBigInteger = false,
            isName = false,
            isLog = false;
    public String name = "",
            outputPath,
            path;
    public Logger logger;

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
        logger = new Logger(outputPath + "\\" + name + ".hom", true, isLog);
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
        }
    }
    public void arguments(String arg) {
    }

    public static void main(String[] args) {
        System.out.println("Program for calculating simplicial homology.\nCopyright by Julian Märte 2017.\nReport bugs to maertej@students.uni-marburg.de");
        INSTANCE = new Homology(args);
    }
}
