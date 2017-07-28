package com.jmaerte.util;

import java.util.Vector;

/**
 * Created by Julian on 18/06/2017.
 */
public class SparseMatrix {

    int[] columnRegister;
    byte[] content;
    int rowRank;
    int level = 0;
    /**Creates a sparse matrix
     *
     * @param rowRank highest not null amount in one row
     */
    public SparseMatrix(int rowCount, int rowRank, int fill) {
        this.rowRank = rowRank;
        columnRegister = new int[rowCount*rowRank];
        content = new byte[fill];
    }

    public void set(int i, int j, byte value) {
        columnRegister[i*rowRank + j] = level;
        content[level] = value;
        level++;
    }

}
