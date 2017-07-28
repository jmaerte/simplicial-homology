package com.jmaerte.simplicial.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Julian on 22/06/2017.
 */

public class Wrapper
{
    public int[] data;
    public int[] sorted;
    public final int hash;

    public Wrapper(int[] array)
    {
        data = array;
        sorted = data.clone();
        Arrays.sort(sorted); // TODO Make more efficient.
        hash = Arrays.hashCode(array);
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Wrapper)
        {
            int[] other = ((Wrapper) obj).data;
            if(other.length == data.length)
            {
                return Objects.deepEquals(data, other);
            }
        }
        return false;
    }

    public String toString() {
        String s = "{";
        for(int i = 0; i < data.length; i++) {
            s += data[i] + (i == data.length - 1 ? "" : ", ");
        }
        return s + "}" + hash;
    }
}
