package com.jmaerte.simplicial.util;

public class Vector4D<T, K, L, W> extends Vector3D<T, K, L> {

    public W w;

    public Vector4D(T x, K y, L z, W w) {
        super(x,y,z);
        this.w = w;
    }

}
