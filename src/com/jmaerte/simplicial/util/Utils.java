package com.jmaerte.simplicial.util;

import java.util.ArrayList;

/**
 * Created by Julian on 08/08/2017.
 */
public class Utils {

    public static Vector3D<Integer, Integer, Integer> gcd(int a, int b) {
        return euclidean(a,b);
    }

    private static Vector3D<Integer, Integer, Integer> euclidean(int a, int b) {
        if(b == 0) return new Vector3D<>(a, 1, 0);
        Vector3D<Integer, Integer, Integer> temp = euclidean(b, a%b);
        return new Vector3D<>(temp.x, temp.z, temp.y - (a/b)*temp.z);
    }

    /** Calculates the gcd of k values.
     *
     * @param values values array
     * @param occupation k
     * @return [gcd(values|0...k), a0, ..., ak] where a0,...,ak are the bezout coefficients of the gcd-representation.
     */
    public static Vector2D<Integer, int[]> gcd(int[] values, int occupation) {
        int k = occupation;
        int[] alpha = new int[k];
        alpha[k-1] = 1;
        int x = values[k - 1];
        for(int i = k - 2; i >= 0; i--) {
            Vector3D<Integer, Integer, Integer> gcd = gcd(values[i], x);
            x = gcd.x;
            alpha[i] = gcd.y;
            for(int l = k - 1; l > i; l--) {
                alpha[l] *= gcd.z;
            }
        }
        return new Vector2D<>(x, alpha);
    }

    public static Vector2D<Integer, int[]> gcd(int[] values) {
        return gcd(values, values.length);
    }
}
