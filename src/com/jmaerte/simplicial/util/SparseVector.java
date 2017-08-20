package com.jmaerte.simplicial.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Julian on 03/08/2017.
 */
public class SparseVector implements Comparable<SparseVector> {

    private static final int MINIMAL_SIZE = 16;

    int length;
    public int occupation;
    public int[] indices;
    public int[] values;

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

    public SparseVector clone() {
        SparseVector v = new SparseVector(length, indices.length);
        int[] _indices = new int[indices.length];
        int[] _values = new int[values.length];
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

    public void insert(int k, int i, int value) {
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
    public int add(SparseVector v, int lambda) {
        int[] ind = new int[Math.min(occupation + v.occupation, length)];
        int[] val = new int[ind.length];
        boolean trailing = true;
        int trailingZeros = 0;
        int occ = 0;
        int i = 0;
        for(int j = 0; j < v.occupation; j++) {
            if(i >= occupation) {
                if(trailing) trailingZeros--;
                ind[occ] = v.indices[j];
                val[occ] = lambda * v.values[j];
            }else if(indices[i] < v.indices[j]) {
                trailing = false;
                ind[occ] = indices[i];
                val[occ] = values[i];
                j--;
                i++;
            }else if(indices[i] > v.indices[j]) {
                if(trailing) trailingZeros--;
                ind[occ] = v.indices[j];
                val[occ] = lambda * v.values[j];
            }else {
                if(values[i] + lambda * v.values[j] != 0) {
                    trailing = false;
                    ind[occ] = indices[i];
                    val[occ] = values[i] + lambda * v.values[j];
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

    public int get(int i) {
        int k = index(i);
        if(k < occupation && indices[k] == i) {
            return values[k];
        }else {
            return 0;
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

    public Vector2D<Integer, int[]> gcd() {
        return Utils.gcd(values, occupation);
    }

    /** linear combinates two vectors.
     *
     * @param a scalar for v
     * @param v first vector
     * @param b scalar for w
     * @param w second vector
     * @return a*v + b*w
     */
    public static SparseVector linear(int a, SparseVector v, int b, SparseVector w) {
        int[] indices = new int[Math.min(v.values.length + w.values.length, v.length)];
        int[] values = new int[indices.length];
        int occupation = 0;
        int l = 0;
        int i = 0, k = 0;
        for(; i < v.occupation && k < w.occupation;) {
            if(v.indices[i] == w.indices[k]) {
                if(a * v.values[i] + b * w.values[k] != 0) {
                    indices[l] = v.indices[i];
                    values[l] = a * v.values[i] + b * w.values[k];
                    l++;
                    occupation++;
                }
                i++;
                k++;
            }else if(v.indices[i] < w.indices[k]) {
                // add v
                if(a * v.values[i] != 0) {
                    indices[l] = v.indices[i];
                    values[l] = a * v.values[i];
                    l++;
                    occupation++;
                }
                i++;
            }else {
                // add w
                if(b * w.values[k] != 0) {
                    indices[l] = w.indices[k];
                    values[l] = b * w.values[k];
                    l++;
                    occupation++;
                }
                k++;
            }
        }
        for(; i < v.occupation;) {
            if(a * v.values[i] != 0) {
                indices[l] = v.indices[i];
                values[l] = a * v.values[i];
                l++;
                occupation++;
            }
            i++;
        }
        for(; k < w.occupation;) {
            if(b * w.values[k] != 0) {
                indices[l] = w.indices[k];
                values[l] = b * w.values[k];
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

    public static int rowEl(SparseVector[] matrix, int t, int n) {
        for(int i = 1; i < matrix[t].occupation; i++) {
            Vector3D<Integer, Integer, Integer> gcd = Utils.gcd(matrix[t].values[0], matrix[t].values[i]);
            // a = values[0], b = values[i];
            int alpha = gcd.y;
            int beta = gcd.z;
            int gcdVal = gcd.x;
            int y = matrix[t].values[0] / gcdVal;
            int x = matrix[t].values[i] / gcdVal;
            // substitute column matrix[t].indices[0] through alpha * matrix[t].indices[0]-th col + beta * matrix[t].indices[i]-th col
            // and column matrix[t].indices[i] through y * matrix.indices[i]-th col - x * matrix.indices[0]-th col.
            matrix[t].values[0] = gcdVal;
            matrix[t].remove(i);
            for(int k = t + 1; k < n; k++) {
                int main = matrix[k].get(matrix[t].indices[0]);
                int second = matrix[k].get(matrix[t].indices[i]);
                matrix[k].set(matrix[t].indices[0], alpha * main + beta * second);
                matrix[k].set(matrix[t].indices[i], y * second - x * main);
                if(matrix[k].occupation == 0) {
                    SparseVector temp = matrix[n-1];
                    matrix[--n] = matrix[k];
                    matrix[k] = temp;
                }
//                int l = matrix[k].index(matrix[t].indices[i]);
//                if(l < matrix[k].occupation && matrix[k].indices[l] == matrix[t].indices[i]) {
//                    matrix[k].set(matrix[t].indices[0], alpha * matrix[k].get(matrix[t].indices[0]) + beta * matrix[k].values[l]);
//                    matrix[k].set(matrix[t].indices[i], y * matrix[k].values[l] - x * matrix[k].get(matrix[t].indices[0]));
//                    if(matrix[k].occupation == 0) {
//                        SparseVector temp = matrix[n-1];
//                        matrix[--n] = matrix[k];
//                        matrix[k] = temp;
//                    }
//                }
            }
            i--;
        }
        return n;
    }

    public int compareTo(SparseVector v) {
        if(v.getFirstIndex() != getFirstIndex()) return getFirstIndex() - v.getFirstIndex();
        int result = 0;
        int i = 0;
        while(result == 0) {
            if(get(i) != v.get(i)) result = get(i) - v.get(i);
            i++;
        }
        return result;
    }
}
