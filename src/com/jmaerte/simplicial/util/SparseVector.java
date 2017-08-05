package com.jmaerte.simplicial.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Julian on 03/08/2017.
 */
public class SparseVector {

    private static final int MINIMAL_SIZE = 16;

    int length;
    int occupation;
    int[] indices;
    int[] values;

    public SparseVector(int length, int capacity) {
        this.length = length;
        this.occupation = 0;
        int size = 0;
        try {
            size = size(length, capacity);
        }catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        indices = new int[size];
        values = new int[size];
    }

    public SparseVector(int length) {
        this(length, 0);
    }

    public static final SparseVector ZERO(int length) {
        return ZERO(length, 0);
    }

    public static final SparseVector ZERO(int length, int capacity) {
        return new SparseVector(length, capacity);
    }

    private int size(int length, int capacity) throws Exception {
        if(capacity > length) throw new Exception("Capacity must be less than length: " + capacity + " " + length);
        if(capacity < 0) throw new Exception("Capacity must be a non-negative number: " + capacity);
        return Math.min(length, ((capacity / MINIMAL_SIZE) + 1) * MINIMAL_SIZE);
    }

    public void set(int i, int value) {
        if(i < 0 || i >= length) {
            new Exception("Index out of Bounds: " + i).printStackTrace();
            System.exit(1);
        }
        int k = index(i);

        if(k < occupation && indices[k] == i) {
            if(value != 0) {
                values[k] = value;
            }else{
                remove(k);
            }
        }else {
            insert(k, i, value);
        }
    }

    public void remove(int k) {
        occupation--;
        if(occupation - k > 0) {
            System.arraycopy(values, k+1, values, k, occupation - k);
            System.arraycopy(indices, k + 1, indices, k, occupation - k);
        }
    }

    private void insert(int k, int i, int value) {
        if(value == 0) {
            return;
        }
        if(values.length < occupation + 1) {
            mkPlace();
        }
        if(occupation - k > 0) {
            System.arraycopy(values, k, values, k + 1, occupation - k);
            System.arraycopy(indices, k, indices, k + 1, occupation - k);
        }

        values[k] = value;
        indices[k] = i;
        occupation++;
    }

    private void mkPlace() {
        if(values.length == length) {
            new Exception("Can't occupate more place than the vector has.").printStackTrace();
            System.exit(1);
        }

        int capacity = Math.min(length, (occupation * 3) / 2 + 1);
        int[] _values = new int[capacity];
        int[] _indices = new int[capacity];
        System.arraycopy(values, 0 ,_values, 0, occupation);
        System.arraycopy(indices, 0, _indices, 0, occupation);
        values = _values;
        indices = _indices;
    }

    public int getFirstIndex() {
        if(occupation == 0) return -1;
        return indices[0];
    }

    public int getFirstValue() {
        if(occupation == 0) return 0;
        return values[0];
    }

    /** Adds lambda times the vector v to this vector.
     *
     * @param v the vector that shell get added.
     * @param lambda the scalar which the added vector is multiplied.
     */
    public SparseVector add(SparseVector v, int lambda) {
        SparseVector result = new SparseVector(length);
        // TODO: Here is the bottle neck.
        int i = 0;
        int occupation = 0;
        ArrayList<Integer> ind = new ArrayList<>();
        ArrayList<Integer> val = new ArrayList<>();
        for(int j = 0; j < v.occupation; j++) {
            if(i >= this.occupation) {
                ind.add(v.indices[j]);
                val.add(v.values[j]);
            }else if(indices[i] < v.indices[j]) {
                ind.add(indices[i]);
                val.add(values[i]);
                j--;
                i++;
            }else if(indices[i] > v.indices[j]) {
                ind.add(v.indices[j]);
                val.add(v.values[j]);
            }else if(values[i] + lambda * v.values[j] != 0){
                ind.add(v.indices[j]);
                val.add(values[i] + lambda * v.values[j]);
                i++;
            }else {
                i++;
                occupation--;
            }
            occupation++;
        }
        while(i < this.occupation) {
            ind.add(indices[i]);
            val.add(values[i]);
            i++;
        }
        int[] indices = new int[ind.size()];
        int[] values = new int[indices.length];
        for(int k = 0; k < values.length; k++) {
            indices[k] = ind.get(k);
            values[k] = val.get(k);
        }
        result.indices = indices;
        result.values = values;
        result.occupation = occupation;
        return result;
    }

    /** Binary searches for the index k, such that values[k] is the i-th index entry.
     * is values[k] undefined (k=-1 f.e.), so i-th index entry is 0.
     *
     * @param i index to search for
     * @return k
     */
    private int index(int i) {
        if(occupation == 0 || i > indices[occupation - 1]) return occupation;
        int left = 0;
        int right = occupation;
        while(left < right) {
            int mid = (right + left)/2;
            if(indices[mid] > i) right = mid;
            else if(indices[mid] < i) left = mid + 1;
            else return mid;
        }
        return left;
    }

    public String toString() {
        String s = "";
        for(int i = 0; i < occupation; i++) {
            s+= indices[i] + ": " + values[i] + " ";
        }
        return s;
    }

    public SparseIterator iterator() {
        return new SparseIterator(this);
    }

    private class SparseIterator implements Iterator<Integer> {

        SparseVector vector;
        int i = 0;

        public SparseIterator(SparseVector vector) {
            this.vector = vector;
        }

        public boolean hasNext() {
            return i < vector.occupation;
        }

        public Integer next() {
            i++;
            return vector.values[i-1];
        }

        public int index() {
            return indices[i-1];
        }
    }
}
