package com.jmaerte.simplicial;

import com.jmaerte.simplicial.util.*;
import java.util.*;

/**
 * Created by Julian on 22/06/2017.
 */
public class Simplicial {

    long gen = 0;
    long boundary = 0;
    long smith = 0;

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
        Smith[] smithCache = new Smith[]{
                new Smith(0),
                new Smith(0)
        };

        // Adding empty set.
        cache[0] = new SetList<>();
        cache[0].add(new Wrapper(new int[]{}));
        cache[1] = new SetList<>();
        int r = 1; // Current rank of Ck

        boolean overflowLastTime = false;
        String homology = "[";

        // Dimension is k-1
        for(int k = 1; k <= maxSize; k++) {
            if(k == maxSize && overflowLastTime) {
                homology += "---" + (k == maxSize ? "" : ", ");
                continue;
            }
            generate(k, cache[1]);
//            System.out.println(cache[1]);
            System.out.println("Found " + cache[1].size() + " faces of dimension " + (k-1));
            // the calculated function is del_k : C_k -> C_(k-1).
            try {
                Smith currSmith = smith(boundary(cache[0], cache[1]), false);
                smithCache[1] = currSmith;
                if(overflowLastTime) homology += "---" + (k == maxSize ? "" : ", ");
                else homology += Smith.calculateHom(r, smithCache) + (k == maxSize ? "" : ", ");
                System.out.println(Colors.PURPLE + "-- DONE! --" + Colors.RESET);
                overflowLastTime = false;
            }catch(Exception e) {
                homology += "---" + (k == maxSize ? "" : ", ");
                System.out.println("-- " + Colors.RED + "ERROR: " + Colors.RESET + "Overflow Exception. Please use Bignum-Version! --");
                overflowLastTime = true;
            }
            cache[0] = cache[1];
            smithCache[0] = smithCache[1];
            cache[1] = new SetList<>();
            r = cache[0].size();
        }
        homology += "]";
        System.out.println(homology);

        System.out.println("Calculation of simplex homology groups took " + (System.currentTimeMillis() - ms) + "ms");
        System.out.println("Fragmentation:");
        System.out.println("\tGeneration of Complex: " + gen + "ms");
        System.out.println("\tBoundary matrices: " + boundary + "ms");
        System.out.println("\tSmith: " + smith + "ms");
    }


    private void generate(int k, SetList<Wrapper> fill) {
        long ms = System.currentTimeMillis();
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
        gen += System.currentTimeMillis() - ms;
    }

//    public void boundary(SparseMatrix boundary, SetList<Wrapper> lower, SetList<Wrapper> higher) {
    public Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary(SetList<Wrapper> lower, SetList<Wrapper> higher) throws Exception {
        long ms = System.currentTimeMillis();
        ArrayList<SparseVector> remaining = new ArrayList<>();
        int[] doneCols = new int[higher.size()];
        SparseVector[] rows = new SparseVector[doneCols.length];
        int done = 0;

        for(int i = 0; i < higher.size(); i++) {
            if(i % 500 == 0) System.out.print(Colors.RED_BOLD + "Generating boundary matrices " + Colors.RESET + Colors.CYAN + "" + i + "/" + higher.size() + " rows done!" + Colors.RESET + "\r");
            int[] data = higher.get(i).data;
            SparseVector vector = new SparseVector(lower.size(), Math.min(lower.size(), 10 *data.length));
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
            for(int k = 0; k < vector.occupation; ) {
                int p = binarySearch(doneCols, vector.indices[k], done);
                if(p < done && doneCols[p] == vector.indices[k]) {
                    vector.add(rows[p], - vector.values[k] * rows[p].values[0]);
                }else k++;
            }
            if(vector.occupation == 0) continue;
            if(vector.values[0] == 1 || vector.values[0] == -1) {
                int p = binarySearch(doneCols, vector.indices[0], done);
                System.arraycopy(doneCols, p, doneCols, p + 1, done - p);
                doneCols[p] = vector.indices[0];
                System.arraycopy(rows, p, rows, p + 1, done - p);
                rows[p] = vector;
                for(SparseVector v : remaining) {
                    int j = v.index(vector.indices[0]);
                    if(j < v.occupation && v.indices[j] == vector.indices[0]) {
                        v.add(vector, - v.values[j] * vector.values[0]);
                    }
                }
                done++;
            }else {
                remaining.add(vector);
            }
        }
//        for(SparseVector vector : remaining) {
//            for(int k = 0; k < vector.occupation; ) {
//                int p = binarySearch(doneCols, vector.indices[k], done);
//                if(p < done && doneCols[p] == vector.indices[k]) {
//                    vector.add(rows[p], - vector.values[k] * rows[p].values[0]);
//                }else k++;
//            }
//        }
        boundary += System.currentTimeMillis() - ms;
        return new Vector4D<>(done, doneCols, rows, remaining);
    }

    public Smith smith(Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary, boolean print) throws Exception {
        long ms = System.currentTimeMillis();
        int done = boundary.x;
        int[] doneCols = boundary.y;
        SparseVector[] rows = boundary.z;
        SparseVector[] matrix = new SparseVector[boundary.w.size()];
        int n = matrix.length;
        for(int i = 0; i < boundary.w.size(); i++) {
            if(boundary.w.get(i).occupation == 0) {
                n--;
            }else {
                matrix[i - (matrix.length - n)] = boundary.w.get(i);
            }
        }
        Smith smith = new Smith(16);
        smith.addTo(1, done);
        for(int t = 0; t < n; t++) {
            if(t % 100 == 0) System.out.print(Colors.RED_BOLD + "Calculating smith normal form " + Colors.RESET + Colors.CYAN + "" + (t + done) + "/" + (n + done) + " rows done!" + Colors.RESET + "\r");
            Indexer idx = new Indexer(n-t);

            if(print) {
                System.out.println("Prepare " + t + ":");
                for(SparseVector vec : matrix) System.out.println(vec);
                System.out.println(idx);
            }

            int j = -1;
            for(int i = t; i < n; i++) {
                if(matrix[i].occupation == 0) {
                    SparseVector v = matrix[i];
                    matrix[i] = matrix[--n];
                    matrix[n] = v;
                    i--;
                }else if(matrix[i].indices[0] < j || j < 0) {
                    idx.empty();
                    idx.add(i);
                    j = matrix[i].indices[0];
                }else if(matrix[i].indices[0] == j) {
                    idx.add(i);
                }
            }
            if(j < 0) break; // maybe also catch if idx.isEmpty()
            boolean col = true;
            while(true) {

                if(print) {
                    System.out.println("Begin while, col: " + col + " t: " + t);
                    for(SparseVector vec : matrix) System.out.println(vec);
                    System.out.println(idx);
                }

                if(col) {
                    // Pivotization:
                    int k = -1; // is row index of pivot row.
                    int h = 0; // h is index of indexer, where k lays.
                    for(int l = 0; l < idx.occupation; l++) {
                        int i = idx.indices[l];
                        if (k < 0 || Math.abs(matrix[k].values[0]) > Math.abs(matrix[i].values[0])) {
                            k = i;
                            h = l;
                        }
                    }
                    if(k < 0) return smith;
                    SparseVector temp = matrix[t];
                    matrix[t] = matrix[k];
                    matrix[k] = temp;
                    if(idx.indices[0] != t) {
                        idx.removePos(h);
                    }
                    if(idx.indices[0] == t) idx.removePos(0);
                    Indexer nextIdx = new Indexer(idx.indices.length - 1);
                    for(int l = 0; l < idx.occupation; l++) {
                        int i = idx.indices[l];
                        int lambda = matrix[i].values[0] / matrix[t].values[0];
                        matrix[i].add(matrix[t], - lambda);
                        if(matrix[i].indices[0] == matrix[t].indices[0]) nextIdx.add(i);
                    }
                    idx = nextIdx;
                    if(nextIdx.occupation > 0) {
                        if(matrix[t].occupation > 0) idx.add(t);
                        col = true;
                    }else {
                        col = false;
                    }

                    if(print) {
                        System.out.println("End while");
                        for(SparseVector vec : matrix) System.out.println(vec);
                        System.out.println(idx);
                    }

                }else {
                    for(int i = 1; i < matrix[t].occupation; i++) {
                        matrix[t].values[i] %= matrix[t].values[0];
                        if(matrix[t].values[i] == 0) {
                            matrix[t].remove(i);
                            i--;
                        }
                    }
                    if(matrix[t].occupation == 1) {
                        smith.addTo(Math.abs(matrix[t].values[0]), 1);
                        break;
                    }else {
                        int k = -1;
                        for(int i = 0; i < matrix[t].occupation; i++) {
                            if(k < 0 || Math.abs(matrix[t].values[k]) > Math.abs(matrix[t].values[i])) {
                                k = i;
                            }
                        }
                        // because occupation > 1 and for every h: matrix[t].values[h] < matrix[t].values[0], k != 0.
                        int curr = matrix[t].values[0];
                        matrix[t].values[0] = matrix[t].values[k];
                        matrix[t].values[k] = curr;
                        for(int i = t + 1; i < n; i++) {
                            int l = matrix[i].index(matrix[t].indices[k]);
                            if(l < matrix[i].occupation && matrix[i].indices[l] == matrix[t].indices[k]) {
                                curr = matrix[i].values[l];
                                matrix[i].remove(l);
                                matrix[i].insert(0, matrix[t].indices[0], curr);
                                idx.add(i);
                            }
                        }
                        if(idx.isEmpty()) col = false;
                        else {
                            idx.add(t);
                            col = true;
                        }
                    }

                    if(print) {
                        System.out.println("End while");
                        for(SparseVector vec : matrix) System.out.println(vec);
                        System.out.println(idx);
                    }
                }
            }
        }
        System.out.println(Colors.RED_BOLD + "Calculating smith normal form " + Colors.RESET + Colors.CYAN + "" + (n + done) + "/" + (n + done) + " rows done!" + Colors.RESET + "\r");
        this.smith += System.currentTimeMillis() - ms;
        return smith;
    }

    public int binarySearch(int[] arr, int i, int max) {
        return binarySearch(arr, i, 0, max);
    }

    public int binarySearch(int[] arr, int i, int min, int max) {
        if(max == 0 || i > arr[max - 1]) return max;
        int left = min;
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
        int left = 0;
        int right = list.size();
        while(left < right) {
            int mid = (left + right) / 2;
            if(comparator.compare(list.get(mid), element) > 0) right = mid;
            else if(comparator.compare(list.get(mid), element) < 0) left = mid + 1;
            else return mid;
        }
        return left;
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
