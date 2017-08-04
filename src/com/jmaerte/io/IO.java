package com.jmaerte.io;

import com.jmaerte.main.Homology;
import com.jmaerte.simplicial.util.Complex;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Julian on 29/06/2017.
 */
public class IO {

    public static int[][] loadArrayFromFile(String path, char setOpener, char setCloser, char separator) {
        BufferedReader reader = null;
        try{
             reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(
                                    new File(path)
                            ),
                            Charset.forName("UTF-8")
                    )
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
        int c;
        String curr = "";
        ArrayList<int[]> arrayList = new ArrayList<>();
        ArrayList<Integer> currentSet = null;
        boolean superSetOpened = false;
        try {
            while((c = reader.read()) != -1) {
                if(c == setOpener) {
                    if(!superSetOpened) {
                        superSetOpened = true;
                        continue;
                    }
                    currentSet = new ArrayList<>();
                } else if(!superSetOpened) {
                    continue;
                } else if(c == setCloser) {
                    if(curr != "") currentSet.add(Integer.valueOf(curr));
                    curr = "";
                    Collections.sort(currentSet);
                    int[] set = new int[currentSet.size()];
                    for(int i = 0; i < set.length; i++) {
                        set[i] = currentSet.get(i);
                    }
                    arrayList.add(set);
                } else if(c == separator) {
                    currentSet.add(Integer.valueOf(curr));
                    curr = "";
                } else if(48 <= c && c <= 57) {
                    curr += (char)c;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        int[][] result = new int[arrayList.size()][];
        for(int i = 0; i < result.length; i++) {
            result[i] = arrayList.get(i);
        }
        return result;
    }


    public static Complex loadComplexFromFile(String path, char setOpener, char setCloser, char separator, boolean isName) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(
                                    new File(path)
                            ),
                            Charset.forName("UTF-8")
                    )
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
        int c;
        String curr = "";
        String name = "";
        int n = 0;
        ArrayList<int[]> arrayList = new ArrayList<>();
        ArrayList<Integer> currentSet = null;
        boolean superSetOpened = false;
        try {
            while((c = reader.read()) != -1) {
                if(c == setOpener) {
                    if(!superSetOpened) {
                        superSetOpened = true;
                        continue;
                    }
                    currentSet = new ArrayList<>();
                } else if(!superSetOpened) {
                    if(c != (int)'=' && c != (int)':' && c != (int)' ') {
                        name += (char)c;
                    }
                    continue;
                } else if(c == setCloser) {
                    if(curr != "") {
                        int j = Integer.valueOf(curr);
                        if(j > n) n = j;
                        currentSet.add(j);
                    }
                    curr = "";
                    if(currentSet.size() <= 0) continue;
                    int[] set = new int[currentSet.size()];
                    for(int i = 0; i < set.length; i++) {
                        set[i] = currentSet.get(i);
                    }
                    arrayList.add(set);
                    currentSet = new ArrayList<>();
                } else if(c == separator) {
                    if(curr == "") continue;
                    int i = Integer.valueOf(curr);
                    if(i > n) n = i;
                    currentSet.add(i);
                    curr = "";
                } else if(48 <= c && c <= 57) {
                    curr += (char)c;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return new Complex(arrayList, n, isName ? Homology.INSTANCE.name : name);
    }

    public static void setHomePath(String path) {
        // TODO Save path

    }

    public static String getHomeDir() {
        return "";
    }
}
