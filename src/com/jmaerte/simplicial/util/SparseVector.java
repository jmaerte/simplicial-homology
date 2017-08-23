package com.jmaerte.simplicial.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Julian on 03/08/2017.
 */
public class SparseVector {

    private static final int MINIMAL_SIZE = 16;

    int length;
    public int occupation;
    public int[] indices;
    public BigInteger[] values;

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
        values = new BigInteger[size];
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

    public SparseVector clone() {
        SparseVector v = new SparseVector(length, indices.length);
        int[] _indices = new int[indices.length];
        BigInteger[] _values = new BigInteger[values.length];
        v.occupation = occupation;
        System.arraycopy(indices, 0, _indices, 0, occupation);
        System.arraycopy(values, 0, _values, 0, occupation);
        v.indices = _indices;
        v.values = _values;
        return v;
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
                values[k] = BigInteger.valueOf(value);
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

    public void insert(int k, int i, int value) {
        insert(k, i, BigInteger.valueOf(value));
    }

    public void insert(int k, int i, BigInteger value) {
        if(value.equals(BigInteger.ZERO)) {
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
        BigInteger[] _values = new BigInteger[capacity];
        int[] _indices = new int[capacity];
        System.arraycopy(values, 0 ,_values, 0, occupation);
        System.arraycopy(indices, 0, _indices, 0, occupation);
        values = _values;
        indices = _indices;
    }

    /** Adds lambda times the vector v to this vector.
     *
     * @param v the vector that shell get added.
     * @param lambda the scalar which the added vector is multiplied.
     */
    public int add(SparseVector v, BigInteger lambda) {
        int[] ind = new int[Math.min(occupation + v.occupation, length)];
        BigInteger[] val = new BigInteger[ind.length];
        boolean trailing = true;
        int trailingZeros = 0;
        int occ = 0;
        int i = 0;
        for(int j = 0; j < v.occupation; j++) {
            if(i >= occupation) {
                if(trailing) trailingZeros--;
                ind[occ] = v.indices[j];
                val[occ] = lambda.multiply(v.values[j]);
            }else if(indices[i] < v.indices[j]) {
                trailing = false;
                ind[occ] = indices[i];
                val[occ] = values[i];
                j--;
                i++;
            }else if(indices[i] > v.indices[j]) {
                if(trailing) trailingZeros--;
                ind[occ] = v.indices[j];
                val[occ] = lambda.multiply(v.values[j]);
            }else {
                BigInteger el = values[i].add(lambda.multiply(v.values[j]));
                if(!el.equals(BigInteger.ZERO)) {
                    trailing = false;
                    ind[occ] = indices[i];
                    val[occ] = el;
                    i++;
                }else {
                    if(trailing) trailingZeros++;
                    i++;
                    occ--;
                }
            }
            occ++;
        }
        while(i < occupation) {
            ind[occ] = indices[i];
            val[occ] = values[i];
            occ++;
            i++;
        }
        this.indices = ind;
        this.values = val;
        this.occupation = occ;
        return trailingZeros;
    }

    public BigInteger get(int i) {
        int k = index(i);
        if(k < occupation && indices[k] == i) {
            return values[k];
        }else {
            return BigInteger.ZERO;
        }
    }

    /** Binary searches for the index k, such that values[k] is the i-th index entry.
     * is values[k] undefined (k=-1 f.e.), so i-th index entry is 0.
     *
     * @param i index to search for
     * @return k
     */
    public int index(int i) {
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
        String s = "Occ: " + occupation + " -> ";
        for(int i = 0; i < occupation; i++) {
            s+= indices[i] + ": " + values[i] + " ";
        }
        return s;
    }

    /** linear combinates two vectors.
     *
     * @param a scalar for v
     * @param v first vector
     * @param b scalar for w
     * @param w second vector
     * @return a*v + b*w
     */
    public static SparseVector linear(BigInteger a, SparseVector v, BigInteger b, SparseVector w) {
        int[] indices = new int[Math.min(v.values.length + w.values.length, v.length)];
        BigInteger[] values = new BigInteger[indices.length];
        int occupation = 0;
        int l = 0;
        int i = 0, k = 0;
        for(; i < v.occupation && k < w.occupation;) {
            BigInteger A = a.multiply(v.values[i]);
            BigInteger B = b.multiply(w.values[k]);
            BigInteger C = A.add(B);
            if(v.indices[i] == w.indices[k]) {
                if(!C.equals(BigInteger.ZERO)) {
                    indices[l] = v.indices[i];
                    values[l] = C;
                    l++;
                    occupation++;
                }
                i++;
                k++;
            }else if(v.indices[i] < w.indices[k]) {
                // add v
                if(!A.equals(BigInteger.ZERO)) {
                    indices[l] = v.indices[i];
                    values[l] = A;
                    l++;
                    occupation++;
                }
                i++;
            }else {
                // add w
                if(!B.equals(BigInteger.ZERO)) {
                    indices[l] = w.indices[k];
                    values[l] = B;
                    l++;
                    occupation++;
                }
                k++;
            }
        }
        for(; i < v.occupation;) {
            BigInteger A = a.multiply(v.values[i]);
            if(!A.equals(BigInteger.ZERO)) {
                indices[l] = v.indices[i];
                values[l] = A;
                l++;
                occupation++;
            }
            i++;
        }
        for(; k < w.occupation;) {
            BigInteger B = b.multiply(w.values[k]);
            if(!B.equals(BigInteger.ZERO)) {
                indices[l] = w.indices[k];
                values[l] = B;
                l++;
                occupation++;
            }
            k++;
        }

        SparseVector res = new SparseVector(v.length);
        res.indices = indices;
        res.values = values;
        res.occupation = occupation;
        return res;
    }
}
