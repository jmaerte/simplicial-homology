package com.jmaerte.simplicial.util;

import java.util.ArrayList;

/**
 * Created by Julian on 30/06/2017.
 */
public class Complex {

    public ArrayList<int[]> facets;
    public int n;
    public String name;

    public Complex(ArrayList<int[]> facets, int n, String name) {
        this.facets = facets;
        this.n = n;
        this.name = name;
    }
}
