package com.jmaerte.simplicial.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Julian on 22/06/2017.
 */
public class SparseMatrix {

    public HashMap<Integer, LinkedList<Pointer>> register;
    public ArrayList<Integer> content;
    int n, m;

    public SparseMatrix(int n, int m) {
        this.n = n;
        this.m = m;
        register = new HashMap<>(n);
        content = new ArrayList<>();
    }



    private class Pointer implements Comparable<Pointer> {
        int value, pointer;
        Pointer(int value, int pointer) {
            this.value = value;
            this.pointer = pointer;
        }

        public int compareTo(Pointer p) {
            return value - p.value;
        }

        public String toString() {
            return "{v:" + value + ", p: " + pointer + "}";
        }
    }

    public void set(int i, int j, int content) throws ArrayIndexOutOfBoundsException {
        if(i > n) throw new ArrayIndexOutOfBoundsException();
        if(j > m) throw new ArrayIndexOutOfBoundsException();
        LinkedList<Pointer> cols = register.get(i);
        if(cols == null) {
            LinkedList<Pointer> list = new LinkedList();
            list.add(new Pointer(j, this.content.size()));
            this.content.add(content);
            register.put(i, list);
        } else {
            Iterator<Pointer> it = cols.iterator();
            while(it.hasNext()) {
                Pointer next = it.next();
                if(next.value == j) {
                    this.content.set(next.pointer, content);
                    return;
                }
            }
            cols.add(new Pointer(j, this.content.size()));
            this.content.add(content);
        }
    }

    /** Gets the position where to put in the Element element and returns a vector with the boolean value, if its already
     * in aswell as the index where element lays.
     * @param list
     * @param element
     * @param <T>
     * @return
     */
    private <T extends Comparable<T>> Vector2D<Boolean, Integer> getSortedIndex(LinkedList<T> list, T element, int from, int to) {
        int k = from;
        int pos;
        boolean isIn = false;
        while(k < to) {
            T curr = list.get(k);
            if(curr.compareTo(element) == 0) {
                return new Vector2D<>(true, k);
            }else if(curr.compareTo(element) > 0) {
                return new Vector2D<>(false, k);
            }
            k++;
        }
        return new Vector2D<>(false, -1);
    }


    public int[] smith() {
        return null;
    }

    private boolean linearDependent(int i, int j, boolean row) {
        return true;
    }
}