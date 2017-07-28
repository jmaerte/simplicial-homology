package com.jmaerte.util;

import com.jmaerte.simplicial.util.SetList;
import com.jmaerte.simplicial.util.Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Julian on 22/06/2017.
 */
public class Simplicial {

    ArrayList<int[]> facets;
    ArrayList<Integer> sizes;
    public HashMap<Integer, SetList<Wrapper>> faces;

    public Simplicial(ArrayList<int[]> facets, int n) {
        long ms = System.currentTimeMillis();
        // Sort for sizes
        Collections.sort(facets, new SizeSorter());

        // Fill sizes array in such a way, that every facet with index i in facets,
        // laying between sizes[j] and sizes[j+1] has the same magnitude.
        sizes = new ArrayList<>();
        sizes.add(0);
        for(int i = 1; i < facets.size(); i++) {
            if(facets.get(i).length > facets.get(i-1).length) sizes.add(i);
        }

        // Create faces HashMap. Here the faces will be saved in such a way, that
        // faces.get(k) returns a HashSet with all the faces of magnitude k, which means dimension k-1.
         faces = new HashMap<>();
        int maxSize = facets.get(sizes.get(sizes.size() - 1)).length;
        for(int i = 0; i <= maxSize; i++) {
            faces.put(i, new SetList<>());
        }
        // Adding empty set.
        faces.get(0).add(new Wrapper(new int[0]));

        // now we run through generating all faces of a given size, so we don't
        // f.e. generate the same subset of a set with magnitude k while only changing the booleans affecting sets
        // with higher magnitude.
        this.facets = facets;
        for(int i = 0; i < sizes.size(); i++) {
            generate(i, faces);
        }
        for(int k = 0; k < faces.size(); k++) {
            Collections.sort(faces.get(k), new Comparator<Wrapper>() {
                @Override
                public int compare(Wrapper a, Wrapper b) {
                    if(a.data.length != b.data.length) return a.data.length - b.data.length;
                    for(int i = 0; i < a.data.length; i++) {
                        if(a.data[i] != b.data[i]) return a.data[i]-b.data[i];
                    }
                    return 0;
                }
            });
        }
        // Now saving the made progress from HashSet into ArrayList, so we can enumerate the basis.
//        this.faces = new HashMap<>();
//        for(int i = 0; i <= maxSize; i++) {
//            this.faces.put(i, new ArrayList<>(faces.get(i)));
//        }
        // TODO: Generating boundary matrices.



        System.out.println("Generation of simplicial complex took " + (System.currentTimeMillis() - ms) + "ms");
    }

    public void generate(int i, HashMap<Integer, SetList<Wrapper>> faces) {
        int size = facets.get(sizes.get(i)).length;
        int m = sizes.get(i);
        int M = i == sizes.size() - 1 ? facets.size() : sizes.get(i+1); // maximum index with this same size as facets.get(sizes.get(i));
        Shiftable shiftable = new Shiftable(size);
        for(int k = 1; k <= size; k++) {
            SetList<Wrapper> kth = faces.get(k);
            try {
                shiftable.reset(k);
            } catch(Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
            while(!shiftable.isMax()) {
                // running through all sets with magnitude sizes.get(i)
                for(int j = m; j < M; j++) {
                    kth.add(shiftable.get(facets.get(j)));
                }
                shiftable.shift(0);
            }
            for(int j = m; j < M; j++) {
                kth.add(shiftable.get(facets.get(j)));
            }
        }
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

        public Shiftable(int length) {
            shift = new boolean[length];
        }

        public void reset(int k) {
            if(k > shift.length) {
                return;// throw exception.
            }
            shift = new boolean[shift.length];
            CURRENT_K = k;
            lowest = 0;
            for(int i = 0; i < k; i++) {
                shift[i] = true;
            }
        }

        public boolean isMax() {
            for(int i = 0; i < CURRENT_K; i++) {
                if(!shift[shift.length - i - 1]) return false;
            }
            return true;
        }

        public void shift(int i) {
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
            for(int i = 0, x = 0;i<values.length;i++)
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
