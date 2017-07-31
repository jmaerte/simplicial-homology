package com.jmaerte.simplicial.util;

/**
 * Created by Julian on 29/07/2017.
 */
public class Vector3D<T, K, L> extends Vector2D<T, K> {

    public L z;

    public Vector3D(T x, K y, L z) {
        super(x,y);
        this.z = z;
    }

}
