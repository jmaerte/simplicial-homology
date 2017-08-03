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
            SparseMatrix matrix = new SparseMatrix(cache[1].size(), cache[0].size());
            boundary(matrix, cache[0], cache[1]);
            smithCache[0] = smithCache[1];
            smithCache[1] = matrix.smith();
//            System.out.println(matrix.register);
            cache[0] = cache[1];
            cache[1] = new SetList<>();
        }


        System.out.println("Generation of simplicial complex took " + (System.currentTimeMillis() - ms) + "ms");
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

    public void boundary(SparseMatrix boundary, SetList<Wrapper> lower, SetList<Wrapper> higher) {
        for(int i = 0; i < higher.size(); i++) {
            int[] data = higher.get(i).data;
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
                boundary.set(i,j, cont);
//                System.out.println(i + ", " + j + " => " + cont);
            }
        }
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
