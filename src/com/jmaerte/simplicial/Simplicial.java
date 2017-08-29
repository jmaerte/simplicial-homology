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
        Smith[] smithCache = new Smith[]{
                new Smith(0),
                new Smith(0)
        };

        // Adding empty set.
        cache[0] = new SetList<>();
        cache[0].add(new Wrapper(new int[]{}));
        cache[1] = new SetList<>();

        // Dimension is k-1
        for(int k = 1; k <= maxSize; k++) {
            generate(k, cache[1]);
//            System.out.println(cache[1]);
            System.out.println("Found " + cache[1].size() + " faces of dimension " + (k-1));
            // the calculated function is del_k : C_k -> C_(k-1).
            Smith currSmith = smith(boundary(cache[0], cache[1]), false);
            System.out.println(currSmith);
            smithCache[1] = currSmith;
            cache[0] = cache[1];
            smithCache[0] = smithCache[1];
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
        ArrayList<SparseVector> remaining = new ArrayList<>();
        int[] doneCols = new int[higher.size()];
        SparseVector[] rows = new SparseVector[doneCols.length];
        int done = 0;

        for(int i = 0; i < higher.size(); i++) {
            if(i % 500 == 0) System.out.print(Colors.YELLOW_BACKGROUND + "Generating boundary matrices" + Colors.RESET + Colors.CYAN + "" + i + "/" + higher.size() + " rows done!" + Colors.RESET + "\r");
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
                    // TODO: Bloomfilter.
                    int j = v.index(vector.indices[0]);
                    if(j < v.occupation && v.indices[j] == vector.indices[0]) {
                        v.add(vector, - v.values[j] * vector.values[0]);
                    }
                }
                done++;
                // TODO: Subtract from every vector in rows[p].
            }else {
                remaining.add(vector);
            }
        }

//        ArrayList<SparseVector> heap = new ArrayList<>();
//        int[] doneCols = new int[higher.size()];
//        SparseVector[] rows = new SparseVector[doneCols.length];
//        int done = 0;
//        int zeros = 0;
//        boolean generate = true;
//        SparseVector last = null;
//        for(int i = 0; i < higher.size(); i++) {
//            SparseVector vector;
//            if(generate) {
//                int[] data = higher.get(i).data;
//                vector = new SparseVector(lower.size(), data.length);
//                for(int l = 0; l < data.length; l++) {
//                    int[] element = new int[data.length - 1];
//                    for(int k = 0; k < data.length; k++) {
//                        if(k == l) continue;
//                        if(k > l) element[k-1] = data[k];
//                        else element[k] = data[k];
//                    }
//                    Wrapper wrapper = new Wrapper(element);
//                    int j = binarySearch(lower, wrapper, wrapperComparator);
//                    int cont = (l%2 == 0) ? 1 : -1;
////                boundary.set(i,j, cont);// Here we see: We generate the matrix row-wise, because j depends on l and l is the incrementing index.
//                    vector.set(j, cont);
//                }
//            }else {
//                vector = last;
//            }
//            generate = true;
//            if(vector.getFirstValue() == 0) {
//                zeros++;
//                continue;
//            }
//            int ri = vector.getFirstIndex();
//            //p = done + 1, falls p größer als alle rj. Ansonsten ist 0 <= p <= done und damit entweder enthalten oder
//            // genau die position, an welcher p stehen müsste.
//
//            int p = binarySearch(doneCols, ri, done);
//            if(p < done && doneCols[p] == ri) {
//                vector.add(rows[p], - vector.getFirstValue() * rows[p].getFirstValue()); // we can use multiplication here, because rows[p][pi] = +-1
////                vector = SparseVector.linear(1, vector, - vector.getFirstValue() * rows[p].getFirstValue(), rows[p]);
//                i--;
//                last = vector;
//                generate = false;
//            }else {
//                if(Math.abs(vector.getFirstValue()) != 1) {
//                    for(int l = 1; l < vector.occupation; l++) {
//                        int h = binarySearch(doneCols, vector.indices[l], done);
//                        if(h < done && doneCols[h] == vector.indices[l]) {
//                            vector.add(rows[h], - vector.values[l] * rows[p].getFirstValue());
//                            l--;
//                        }
//                    }
//                    if(vector.occupation > 0) heap.add(vector);
//                }else {
//                    // Insert the new vector into the arrays.
//                    System.arraycopy(doneCols, p, doneCols, p + 1, done - p);
//                    doneCols[p] = ri;
//                    System.arraycopy(rows, p, rows, p + 1, done - p);
//                    rows[p] = vector;
//                    done++;
//                }
//            }
//        }
        return new Vector4D<>(done, doneCols, rows, remaining);
    }

    public static Smith smith(Vector4D<Integer, int[], SparseVector[], ArrayList<SparseVector>> boundary, boolean print) {
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
            if(t % 100 == 0) System.out.print(Colors.YELLOW_BACKGROUND + "Calculating smith normal form" + Colors.RESET + Colors.CYAN + "" + (t + done) + "/" + (n + done) + " rows done!" + Colors.RESET + "\r");
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
//                        if(matrix[i].occupation == 0) {
//                            SparseVector N = matrix[--n];
//                            matrix[n + 1] = matrix[i];
//                            matrix[i] = N;
//                            if(idx.indices[idx.occupation - 1] == n) {
//                                l--;
//                                idx.indices[idx.occupation - 1] = 0;
//                                idx.occupation--;
//                            }
//                        }
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
//                            if(matrix[i].occupation == 0) {
//                                SparseVector N = matrix[--n];
//                                matrix[n + 1] = matrix[i];
//                                matrix[i] = N;
//                                i--;
//                            }
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
        System.out.println(Colors.PURPLE + "-- DONE! --" + Colors.RESET);
        return smith;

        // ALGORITHM OLD:

//        Smith result = new Smith(16);
//        Vector2D<Integer, Integer> last = new Vector2D<>(done, 1);
//        for(int t = 0; t < n; t++) {
//            Indexer idx = new Indexer(n-t);
//
//            if(print) {
//                System.out.println("Start:");
//                for(SparseVector vec : matrix) System.out.println(vec);
//                System.out.println(idx);
//            }
//
//            int j = -1;
//            for(int i = t; i < n; i++) {
//                if(matrix[i].occupation == 0) {
//                    SparseVector v = matrix[i];
//                    matrix[i] = matrix[--n];
//                    matrix[n] = v;
//                    i--;
//                }else if(matrix[i].indices[0] < j || j < 0) {
//                    idx.empty();
//                    idx.add(i);
//                    j = matrix[i].indices[0];
//                }else if(matrix[i].indices[0] == j) {
//                    idx.add(i);
//                }
//            }
//            if(j < 0) break; // maybe also catch if idx.isEmpty()
//            SparseVector curr = matrix[t];      //
//            matrix[t] = matrix[idx.indices[0]]; // Without restriction:
//            matrix[idx.indices[0]] = curr;      // a_(t,j) != 0
//            idx.removePos(0);                 //
//            // idx holds all the indices of rows, that have a nnz entry in same column,
//            // where row t has its trailing nnz entry after reordering. (j)
//
//            // in turns: first gcd the column entries, until the column j only contains nnz entry at (t,j).
//            // Then eliminate row afap.
//            while(true) {
//                if(print) {
//                    System.out.println("begin while:");
//                    for(SparseVector vec : matrix) System.out.println(vec);
//                    System.out.println(idx);
//                }
//
//                // eliminate column with gcd procedure:
//                for(int i = 0; i < idx.occupation; i++) {
//                    int a = matrix[t].values[0];
//                    int b = matrix[idx.indices[i]].values[0];
//                    Vector3D<Integer, Integer, Integer> gcdTuple = Utils.gcd(a,b);
//                    int gcd = gcdTuple.x;
//                    int alpha = gcdTuple.y;
//                    int beta = gcdTuple.z;
//                    int x = b / gcd;
//                    int y = a / gcd;
//                    SparseVector newRow = SparseVector.linear(alpha, matrix[t], beta, matrix[idx.indices[i]]);
//                    matrix[idx.indices[i]] = SparseVector.linear(y, matrix[idx.indices[i]], -x, matrix[t]);
//                    matrix[t] = newRow;
//                }
//                idx.empty();
//                // Column j is empty except place (t,j). So try to eliminate row:
//                int k = -1;
//                for(int i = 1; i < matrix[t].occupation; i++) {
//                    matrix[t].values[i] = matrix[t].values[i] % matrix[t].values[0];
//                    if(matrix[t].values[i] == 0) {
//                        matrix[t].remove(i);
//                        i--;
//                    }else if(k < 0 || matrix[t].values[k] > matrix[t].values[i]) {
//                        k = i;
//                    }
////                    int c = matrix[t].values[i];
////                    if(c % matrix[t].values[0] == 0) {
////                        matrix[t].remove(i);
////                        i--;
////                    }else {
////                        if(k < 0 || matrix[t].values[k] > c ) {
////                            k = i;
////                        }
////                        matrix[t].values[i] = c % matrix[t].values[0];
////                    }
//                }
//                if(k > 0) { // Therefore it exists a nnz entry in row t besides the one in col j after elimination.
//                    // gcd column:
//                    int a = matrix[t].values[0];
//                    int b = matrix[t].values[k];
//                    Vector3D<Integer, Integer, Integer> gcdTuple = Utils.gcd(a,b);
//                    int gcd = gcdTuple.x;
//                    int beta = gcdTuple.z;
//                    int y = a / gcd;
//                    for(int i = t + 1; i < n; i++) {
//                        if(matrix[i].occupation == 0) {
//                            SparseVector v = matrix[i];
//                            matrix[i] = matrix[--n];
//                            matrix[n] = v;
//                            i--;
//                        }else {
//                            int l = matrix[i].index(matrix[t].indices[k]);
//                            if(l < matrix[i].occupation && matrix[i].indices[l] == matrix[t].indices[k]) {
//                                int c = matrix[i].values[l];
//                                matrix[i].values[l] *= y;
//                                if(beta != 0) { // sufficient, because matrix[i].values[l] != 0
//                                    matrix[i].insert(0, matrix[t].indices[0], beta * matrix[i].values[l]);
//                                    idx.add(i);
//                                }
//                            }
//                        }
//                    }
//                    matrix[t].values[0] = gcd;
//                    matrix[t].remove(k);
//                }
//
//                if(print) {
//                    System.out.println("end while:");
//                    for(SparseVector vec : matrix) System.out.println(vec);
//                    System.out.println(idx);
//                }
//
//                if(matrix[t].occupation == 1 && idx.isEmpty()) {
//                    if(last == null) last = new Vector2D<>(1, Math.abs(matrix[t].values[0]));
//                    else if(last.y == Math.abs(matrix[t].values[0])) last.x++;
//                    else {
//                        result.addTo(last.y, last.x);
//                        last = new Vector2D<>(1, Math.abs(matrix[t].values[0]));
//                    }
//                    break;
//                }
//            }
//        }
//        if(last != null) result.addTo(last.y, last.x);
//        System.out.println(result);
//        return result;


//        int entryT = 0;
//        boolean next = false;
//        int n = matrix.length;
//        for(int t = 0; t < n; t++) {
////            System.out.println("Step " + t + ", entryT: " + entryT);
////            for(int i = 0; i < n; i++) {
////                System.out.println(matrix[i]);
////            }
//
//            int j = -1;
//            Indexer idx = new Indexer(n-t);
//            for(int i = t; i < n; i++) {
//                if(j < 0 || matrix[i].getFirstIndex() < j) {
//                    j = matrix[i].getFirstIndex();
//                    idx.empty();
//                    idx.add(i);
//                }else if(matrix[i].getFirstIndex() == j) {
//                    idx.add(i);
//                }
//            }
//            if(j == -1 || idx.isEmpty()) continue;
//            int k = idx.get(0);
//            // swap row t with row k
//            if(k != t) {
//                SparseVector temp = matrix[t];
//                matrix[t] = matrix[k];
//                matrix[k] = temp;
//                idx.removePos(0);
//                idx.add(t);
//            }
//            for(int h = 0; h < idx.occupation; h++) {
//                int l = idx.get(h);
//                if(l != t) {
//                    if(Math.abs(matrix[t].values[0]) != 1) {
//                        Vector3D<Integer, Integer, Integer> gcd = Utils.gcd(matrix[t].getFirstValue(), matrix[l].getFirstValue());
//                        int alpha = gcd.y;
//                        int beta = gcd.z;
//                        int gcdVal = gcd.x;
//                        int x = matrix[l].getFirstValue() / gcdVal;
//                        int y = matrix[t].getFirstValue() / gcdVal;
////                    System.out.println("k = " + k + ", l = " + l + ", a = " + matrix[t].getFirstValue() + ", b = " + matrix[l].getFirstValue() + ", x = " + x + ", y = " + y);
//                        SparseVector newT = SparseVector.linear(alpha, matrix[t], beta, matrix[l]);
//                        matrix[l] = SparseVector.linear(y, matrix[l], -1 * x, matrix[t]);
//                        matrix[t] = newT;
//                        if(matrix[l].occupation == 0) {
//                            System.out.println("finalize");
//                            if(idx.get(idx.occupation - 1) == n-1) {
//                                h--;
//                                idx.removePos(idx.occupation - 1);
//                            }
//                            SparseVector v = matrix[n-1];
//                            matrix[--n] = matrix[l];
//                            matrix[l] = v;
//                        }
//                    }else {
//                        matrix[l] = SparseVector.linear(1, matrix[l], - matrix[l].values[0] * matrix[t].values[0], matrix[t]);
//                    }
//                }
//            }
//            // column is eliminated.
////            System.out.println("After column elimination:");
////            for(int i = 0; i < n; i++) {
////                System.out.println(matrix[i]);
////            }
//            if(matrix[t].values[0] == 1 || matrix[t].values[0] == -1) {
//                result.add(1);
//                System.out.println(1);
//                next = true;
//                continue;
//            }
//            n = SparseVector.rowEl(matrix, t, n);
//            if(entryT != matrix[t].getFirstValue() || next) {
//                t--;
//                next = false;
//            }else {
//                next = true;
//                result.add(Math.abs(entryT));
//                System.out.println(entryT);
//            }
//            entryT = next ? 0 : matrix[t+1].getFirstValue();
////            System.out.println("After row elimination:");
////            for(int i = 0; i < n; i++) {
////                System.out.println(matrix[i]);
////            }
//        }
//
//        System.out.println(n);
//        Number[] resultArr = new Number[result.size()];
//        System.out.println(result);
//        for(int i = 0; i < resultArr.length; i++) {
//            resultArr[i] = result.get(i);
//        }
//        return resultArr;
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
//        int low = 0;
//        int high = list.size() - 1;
//        while(low <= high) {
//            int mid = low + (high - low)/2;
//            if(comparator.compare(element, list.get(mid)) < 0) high = mid - 1;
//            else if(comparator.compare(element, list.get(mid)) > 0) low = mid + 1;
//            else return mid;
//        }
//        return -1;
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
