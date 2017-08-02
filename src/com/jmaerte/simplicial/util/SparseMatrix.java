package com.jmaerte.simplicial.util;

import java.util.*;

/**
 * Created by Julian on 22/06/2017.
 */
public class SparseMatrix {

    int n, m;
    MatrixNode[] rows;
    MatrixNode[] cols;
    int[] rowOcc;
    int[] colOcc;

    public SparseMatrix(int n, int m) {
        this.n = n;
        this.m = m;
        rows = new MatrixNode[n];
        cols = new MatrixNode[m];
        for(int i = 0; i < n; i++) {
            MatrixNode temp = new MatrixNode(i, -1, 0, null, null, null, null);
            temp.right = new MatrixNode(i, m, 0, temp, null, null, null);
            rows[i] = temp;
        }
        for(int j = 0; j < m; j++) {
            MatrixNode temp = new MatrixNode(-1, j, 0, null, null, null, null);
            temp.down = new MatrixNode(n, j, 0, null, null, temp, null);
            cols[j] = temp;
        }
        rowOcc = new int[n];
        colOcc = new int[m];
    }

    public void set(int i, int j, int value) {
        MatrixNode row = rows[i].right;
        while(row.j <= j) {
            row = row.right;
        }
        MatrixNode col = cols[j].down;
        while(col.i <= i) {
            col = col.down;
        }
        MatrixNode node = new MatrixNode(i,j,value, row.left, row, col.up, col);
        col.left.right = node;
        col.left = node;
        row.up.down = node;
        row.up = node;
        rowOcc[i]++;
        colOcc[j]++;
    }

    public Number[] smith() {
        ArrayList<Number> resultList = new ArrayList<>();

        int g = gaussian();
        for(int i = 0; i < g; i++) {
            resultList.add(1);
        }

        Number[] result = new Number[resultList.size()];
        for(int i = 0; i < result.length; i++) result[i] = resultList.get(i);
        return result;
    }

    private int gaussian() {
        int ones = 0;
        for(int j = 0; j < m; j++) {
            MatrixNode col = cols[j].down;
            ArrayList<Integer> pivotRows = new ArrayList<>();
            while(col.i != n) {
                if(col.left.j != -1) {
                    col = col.down;
                    continue;
                }
                if(col.value.equals(1) || col.value.equals(-1)) {
                    pivotRows.add(col.i);
                }
                col = col.down;
            }
            if(!pivotRows.isEmpty()) {
                int pivotRow = -1;
                for(int i : pivotRows) {
                    if(pivotRow < 0) pivotRow = i;
                    else if(rowOcc[pivotRow] > rowOcc[i]) pivotRow = i;
                }
                swapRows(pivotRow, ones);
                ones++;
                //TODO: Print the ones done with gaussian elimination.
                //TODO Eliminate the whole column j.
                MatrixNode node = cols[j].down;
                MatrixNode pivot = rows[pivotRow].right;
                while(node.i != n) {
                    if(node.i != pivotRow) addRows(pivotRow, node.i, -node.value.intValue()/pivot.value.intValue());
                    node = node.down;
                }
            }else {
                //TODO Add j to the still todo columns.(Swap it to the end)
            }
        }
        return ones;
    }

    private void swapRows(int i, int k) {
        //TODO
        MatrixNode node = rows[i].right;
        while(node.j != m) {
            node.up.down = node.down;
            node.down.up = node.up;
            node.i = k;
            node = node.right;
        }
        node = rows[k].right;
        while(node.j != m) {
            node.up.down = node.down;
            node.down.up = node.up;
            node.i = i;
            node = node.right;
        }
        node = rows[i];
        rows[i] = rows[k];
        rows[k] = node;
        rows[k].i = k;
        rows[i].i = i;
        node = node.right;
        while(node.j != m) {
            MatrixNode curr = cols[node.j];
            while(curr.i <= k) {
                curr = curr.down;
            }
            node.down = curr;
            node.up = curr.up;
            curr.up.down = node;
            curr.up = node;
            node = node.right;
        }
        node = rows[i].right;
        while(node.j != m) {
            MatrixNode curr = cols[node.j];
            while(curr.i <= i) {
                curr = curr.down;
            }
            node.down = curr;
            node.up = curr.up;
            curr.up.down = node;
            curr.up = node;
            node = node.right;
        }
    }

    public void addRows(int i, int k, int lambda) {

    }

    private class MatrixNode {
        MatrixNode left, right, up, down;
        Number value;
        int i, j;

        public MatrixNode(int i, int j, Number value, MatrixNode left, MatrixNode right, MatrixNode up, MatrixNode down) {
            this.i = i;
            this.j = j;
            this.value = value;
            this.left = left;
            this.right = right;
            this.up = up;
            this.down = down;
        }
    }
}