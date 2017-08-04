package com.jmaerte.simplicial.util;

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
    public void add(SparseVector v, int lambda) {
        for(int i = 0; i < v.occupation; i++) {
            int k = index(v.indices[i]);
            if(k < occupation && indices[k] == v.indices[i]) {
                if(values[k] + lambda * v.values[i] != 0) {
                    values[k] += lambda * v.values[i];
                }else {
                    remove(k);
                }
            }else {
                insert(k, v.indices[i], lambda * v.values[i]);
            }
        }
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


}
