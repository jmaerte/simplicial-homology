package com.jmaerte.simplicial;

import com.jmaerte.simplicial.util.*;
import java.util.*;

/**
 * Created by Julian on 22/06/2017.
 */
public class Simplicial {

    Complex c;
    ArrayList<Integer> sizes;
    Comparator<Wrapper> wrapperComparator = new Comparator<Wrapper>() {
        @Override
        public int compare(Wrapper a, Wrapper b) {
            // TODO overwrite, cant afford the arrays to be sorted! Make more efficient!
            if(a.sorted.length != b.sorted.length) return a.sorted.length - b.sorted.length;
            for(int i = 0; i < a.sorted.length; i++) {
                if(a.sorted[i] != b.sorted[i]) return a.sorted[i]-b.sorted[i];
            }
            return 0;
        }
    };

    public Simplicial(Complex c) {
        long ms = System.currentTimeMillis();
        this.c = c;

        // Sort for sizes
        Collections.sort(c.facets, new SizeSorter());

        // Fill sizes array in such a way, that every facet with index i in facets,
        // laying between sizes[j] and sizes[j+1] has the same magnitude.
        sizes = new ArrayList<>();
        sizes.add(0);
        for(int i = 1; i < c.facets.size(); i++) {
            if(c.facets.get(i).length > c.facets.get(i-1).length) sizes.add(i);
        }


        int maxSize = c.facets.get(sizes.get(sizes.size() - 1)).length;

        //Print out / Log the actual information.

        // TODO print info
        System.out.println("Generating simplicial complex for the simplicial complex " + c.name + " with dimension " + (maxSize - 1) + " and " + c.facets.size() + " facets.");

        // Create a cache
        SetList<Wrapper>[] cache = new SetList[2];
        Number[][] smithCache = new Number[][]{
                new Number[0],
                new Number[0]
        };

        // Adding empty set.
        cache[0] = new SetList<>();
        cache[0].add(new Wrapper(new int[]{}));
        cache[1] = new SetList<>();

        // Dimension is k-1
        for(int k = 1; k <= maxSize; k++) {
            generate(k, cache[1]);
            System.out.println("Found " + cache[1].size() + " faces of dimension " + (k-1));
            smithCache[0] = smithCache[1];
            // the calculated function is del_k : C_k -> C_(k-1).
            Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary =  boundary(cache[0], cache[1]);
            Number[] heap = smith(boundary);
            smithCache[1] = new Number[heap.length + 1];
            smithCache[1][0] = boundary.y.length;
            //smithCache[1] is the array, such that the first entry is a number of ones and after there is the block of other smith normal form entries.
//            for(SparseVector v : boundary.w) {
//                System.out.println(v);
//            }
            cache[0] = cache[1];
            cache[1] = new SetList<>();
        }

        System.out.println("Calculation of simplex homology groups took " + (System.currentTimeMillis() - ms) + "ms");
    }


    private void generate(int k, SetList<Wrapper> fill) {
        int minK = 0;
        for(int i = 0; i < sizes.size(); i++) {
            int length = c.facets.get(sizes.get(i)).length;
            if(length < k) {
                minK++;
            }else {
                break;
            }
        }
        Shiftable[] shift = new Shiftable[sizes.size() - minK];
        for(int i = 0; i < shift.length; i++) {
            shift[i] = new Shiftable(c.facets.get(sizes.get(i+minK)).length);
            shift[i].reset(k);
        }
        while(true) {
            boolean isMax = true;
            for(int i = 0; i < shift.length; i++) {
                if(shift[i].isMax()) continue;
                int m = sizes.get(i+minK);
                int M = (i == shift.length - 1) ? c.facets.size() : sizes.get(i+minK+1);
                for(int j = m; j < M; j++) {
                    fill.add(shift[i].get(c.facets.get(j)));
                }
                shift[i].shift();
                isMax &= shift[i].isMax();
            }
            if(isMax) break;
        }
        Collections.sort(fill, wrapperComparator);
    }

//    public void boundary(SparseMatrix boundary, SetList<Wrapper> lower, SetList<Wrapper> higher) {
    public Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary(SetList<Wrapper> lower, SetList<Wrapper> higher) {
        ArrayList<SparseVector> heap = new ArrayList<>();
        int[] doneCols = new int[higher.size()];
        SparseVector[] rows = new SparseVector[doneCols.length];
        int done = 0;
        int zeros = 0;
        boolean generate = true;
        SparseVector last = null;
        for(int i = 0; i < higher.size(); i++) {
            SparseVector vector;
            if(generate) {
                int[] data = higher.get(i).data;
                vector = new SparseVector(lower.size(), data.length);
                for(int l = 0; l < data.length; l++) {
                    int[] element = new int[data.length - 1];
                    for(int k = 0; k < data.length; k++) {
                        if(k == l) continue;
                        if(k > l) element[k-1] = data[k];
                        else element[k] = data[k];
                    }
                    Wrapper wrapper = new Wrapper(element);
                    int j = binarySearch(lower, wrapper, wrapperComparator);
                    int cont = (l%2 == 0) ? 1 : -1;
//                boundary.set(i,j, cont);// Here we see: We generate the matrix row-wise, because j depends on l and l is the incrementing index.
                    vector.set(j, cont);
                }
            }else {
                vector = last;
            }
            generate = true;
            if(vector.getFirstValue() == 0) {
                zeros++;
                continue;
            }
            int ri = vector.getFirstIndex();
            //p = done + 1, falls p größer als alle rj. Ansonsten ist 0 <= p <= done und damit entweder enthalten oder
            // genau die position, an welcher p stehen müsste.
            int p = binarySearch(doneCols, ri, done);
            if(p < done && doneCols[p] == ri) {
                //TODO: Eliminiere den Eintrag a_i,ri durch zeilenaddition von rows[p] auf die aktuelle Zeile.
                vector.add(rows[p], - vector.getFirstValue() * rows[p].getFirstValue()); // we can use multiplication here, because rows[p][pi] = +-1
//                vector = SparseVector.linear(1, vector, - vector.getFirstValue() * rows[p].getFirstValue(), rows[p]);
                i--;
                last = vector;
                generate = false;
            }else {
                if(Math.abs(vector.getFirstValue()) != 1) {
                    heap.add(vector);
                }else {
                    // Insert the new vector into the arrays.
                    System.arraycopy(doneCols, p, doneCols, p + 1, done - p);
                    doneCols[p] = ri;
                    System.arraycopy(rows, p, rows, p + 1, done - p);
                    rows[p] = vector;
                    done++;
                }
            }
        }
        System.out.println(done);
        System.out.println(zeros);
        return new Vector4D<>(done, doneCols, rows, heap);
    }

    public static Number[] smith(Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary) {
        int done = boundary.x;
        int[] doneCols = boundary.y;
        SparseVector[] rows = boundary.z;
        SparseVector[] matrix = new SparseVector[boundary.w.size()];
        for(int i = 0; i < boundary.w.size(); i++) {
            matrix[i] = boundary.w.get(i);
        }
        System.out.println(matrix.length);
        // TODO: Maybe erase this again: (it doesnt really affect this lines. f.e. in Chess 7x7 it hast 6/11 and 19/3972. Not really effective. Still takes up 500ms.
//        for(int l = 0; l < matrix.size(); l++) {
//            SparseVector v = matrix.get(l);
//            if(v.getFirstValue() == 0) {
//                matrix.remove(l);
//                continue;
//            }
//            int i = v.getFirstIndex();
//            int k = binarySearch(doneCols, i, done);
//            if(k < done && doneCols[k] == i) {
//                v.add(rows[k], - v.getFirstValue() * rows[k].getFirstValue());
//                l--;
//            }
//        }
        // End of maybe erase.
        // Hypothetical.


        // 1ms runtime execution. This is the standard procedure in the algorithm.
//        if(matrix.size() == 0) return new Number[0];
//        long now = System.currentTimeMillis();
//        Collections.sort(matrix, new FirstElementSorter());
//        int i = 0;
//        int firstIndex = matrix.get(0).getFirstIndex();
//        while(matrix.get(i).getFirstIndex() == firstIndex) {
//            i++;
//        }
//        int[] values = new int[i];
//        for(int j = 0; j < i; j++) {
//            values[j] = matrix.get(j).getFirstValue();
//        }
//        System.out.println(Utils.gcd(values));
//        System.out.println(System.currentTimeMillis() - now + "ms");
//        System.exit(0);
        ArrayList<Integer> result = new ArrayList<>();
        int entryT = 0;
        boolean next = false;
        int n = matrix.length;
        for(int t = 0; t < n; t++) {
//            System.out.println("Step " + t + ", entryT: " + entryT);
//            for(int i = 0; i < n; i++) {
//                System.out.println(matrix[i]);
//            }

            int j = -1;
            Indexer idx = new Indexer(n-t);
            for(int i = t; i < n; i++) {
                if(j < 0 || matrix[i].getFirstIndex() < j) {
                    j = matrix[i].getFirstIndex();
                    idx.empty();
                    idx.add(i);
                }else if(matrix[i].getFirstIndex() == j) {
                    idx.add(i);
                }
            }
            if(j == -1 || idx.isEmpty()) continue;
            int k = idx.get(0);
            // swap row t with row k
            if(k != t) {
                SparseVector temp = matrix[t];
                matrix[t] = matrix[k];
                matrix[k] = temp;
                idx.removePos(0);
                idx.add(t);
            }
            for(int h = 0; h < idx.occupation; h++) {
                int l = idx.get(h);
                if(l != t) {
                    if(Math.abs(matrix[t].values[0]) != 1) {
                        Vector3D<Integer, Integer, Integer> gcd = Utils.gcd(matrix[t].getFirstValue(), matrix[l].getFirstValue());
                        int alpha = gcd.y;
                        int beta = gcd.z;
                        int gcdVal = gcd.x;
                        int x = matrix[l].getFirstValue() / gcdVal;
                        int y = matrix[t].getFirstValue() / gcdVal;
//                    System.out.println("k = " + k + ", l = " + l + ", a = " + matrix[t].getFirstValue() + ", b = " + matrix[l].getFirstValue() + ", x = " + x + ", y = " + y);
                        SparseVector newT = SparseVector.linear(alpha, matrix[t], beta, matrix[l]);
                        matrix[l] = SparseVector.linear(y, matrix[l], -1 * x, matrix[t]);
                        matrix[t] = newT;
                        if(matrix[l].occupation == 0) {
                            System.out.println("finalize");
                            if(idx.get(idx.occupation - 1) == n-1) {
                                h--;
                                idx.removePos(idx.occupation - 1);
                            }
                            SparseVector v = matrix[n-1];
                            matrix[--n] = matrix[l];
                            matrix[l] = v;
                        }
                    }else {
                        matrix[l] = SparseVector.linear(1, matrix[l], - matrix[l].values[0] * matrix[t].values[0], matrix[t]);
                    }
                }
            }
            // column is eliminated.
//            System.out.println("After column elimination:");
//            for(int i = 0; i < n; i++) {
//                System.out.println(matrix[i]);
//            }
            if(matrix[t].values[0] == 1 || matrix[t].values[0] == -1) {
                result.add(1);
                System.out.println(1);
                next = true;
                continue;
            }
            n = SparseVector.rowEl(matrix, t, n);
            if(entryT != matrix[t].getFirstValue() || next) {
                t--;
                next = false;
            }else {
                next = true;
                result.add(Math.abs(entryT));
                System.out.println(entryT);
            }
            entryT = next ? 0 : matrix[t+1].getFirstValue();
//            System.out.println("After row elimination:");
//            for(int i = 0; i < n; i++) {
//                System.out.println(matrix[i]);
//            }
        }

        System.out.println(n);
        Number[] resultArr = new Number[result.size()];
        System.out.println(result);
        for(int i = 0; i < resultArr.length; i++) {
            resultArr[i] = result.get(i);
        }
        return resultArr;
    }

    public int binarySearch(ArrayList<Integer> arr, int i, int max) {
        if(max == 0 || max >= arr.size() || i > arr.get(max - 1)) return max;
        int left = 0;
        int right = max;
        while(left < right) {
            int mid = (left + right) / 2;
            if(arr.get(mid) > i) right = mid;
            else if(arr.get(mid) < i) left = mid + 1;
            else return mid;
        }
        return left;
    }

    public int binarySearch(int[] arr, int i, int max) {
        if(max == 0 || i > arr[max - 1]) return max;
        int left = 0;
        int right = max;
        while(left < right) {
            int mid = (left + right) / 2;
            if(arr[mid] > i) right = mid;
            else if(arr[mid] < i) left = mid + 1;
            else return mid;
        }
        return left;
    }

    public int binarySearch(ArrayList<Wrapper> list, Wrapper element, Comparator<Wrapper> comparator) {
        int low = 0;
        int high = list.size() - 1;
        while(low <= high) {
            int mid = low + (high - low)/2;
            if(comparator.compare(element, list.get(mid)) < 0) high = mid - 1;
            else if(comparator.compare(element, list.get(mid)) > 0) low = mid + 1;
            else return mid;
        }
        return -1;
    }

    public static class SizeSorter implements Comparator<int[]> {
        public int compare(int[] a, int[] b) {
            if(a.length == b.length)
            {
                return 0;
            }
            if(a.length > b.length)
            {
                return 1;
            }
            return -1;
        }
    }

    public static class FirstElementSorter implements Comparator<SparseVector> {
        public int compare(SparseVector a, SparseVector b) {
            if(a.getFirstIndex() != b.getFirstIndex()) {
                return a.getFirstIndex() - b.getFirstIndex();
            }else if(a.getFirstValue() != b.getFirstValue()){
                return a.getFirstValue() - b.getFirstValue();
            }else {
                return a.occupation - b.occupation;
            }
        }
    }

    public static class Shiftable {

        boolean[] shift;
        public int CURRENT_K = 0;
        public int lowest = 0;
        boolean finished = false;

        public Shiftable(int length) {
            shift = new boolean[length];
        }

        public void reset(int k) {
            if(k > shift.length) {
                return;// throw exception.
            }
            shift = new boolean[shift.length];
            CURRENT_K = k;
            finished = false;
            lowest = 0;
            for(int i = 0; i < k; i++) {
                shift[i] = true;
            }
        }

        public boolean isMax() {
            return finished;
        }

        public void shift() {
            boolean isMax = true;
            for(int i = 0; i < CURRENT_K; i++) {
                if(!shift[shift.length - i - 1]) isMax = false;
            }
            if(isMax) {
                finished = true;
                return;
            }
            shift(0);
        }

        private void shift(int i) {
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

        public Wrapper get(int[] values)
        {
            int[] result = new int[CURRENT_K];
            for(int i = 0, x = 0; i < values.length; i++)
            {
                if(shift[i])
                {
                    result[x++] = values[i];
                }
            }
            return new Wrapper(result);
        }
    }
}
