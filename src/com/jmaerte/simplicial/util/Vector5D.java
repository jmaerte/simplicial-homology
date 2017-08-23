package com.jmaerte.simplicial.util;

/**
 * Created by Julian on 23/08/2017.
 */
public class Vector5D<T, K, L, W, V> extends Vector4D<T, K, L, W> {

    public V v;

    public Vector5D(T t, K k, L l, W w, V v) {
        super(t,k,l,w);
        this.v = v;
    }

}
