package com.jmaerte.simplicial.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Julian on 17/07/2017.
 */
public class Matrix {

    int n, m;
    MatrixNode[] cols;
    MatrixNode[] rows;
    int[] colOcc;
    int[] rowOcc;

    public Matrix(int n, int m) {
        this.n = n;
        this.m = m;
        rows = new MatrixNode[n];
        for(int i = 0; i < n; i++) {
            MatrixNode head = new MatrixNode(i, -1, 0, null, null, null, null);
            MatrixNode tail = new MatrixNode(i, m, 0, null, null, head, null);
            head.right = tail;
            rows[i] = head;
        }
        cols = new MatrixNode[m];
        for(int j = 0; j < m; j++) {
            MatrixNode head = new MatrixNode(-1, j, 0, null, null, null, null);
            MatrixNode tail = new MatrixNode(n, j, 0, head, null, null, null);
            head.down = tail;
            cols[j] = head;
        }
        rowOcc = new int[n];
        colOcc = new int[m];
    }

    public void set(int i, int j, int content) {
        MatrixNode row = rows[i];
        MatrixNode col = cols[j];
        while(row.right != null && row.j < j) row = row.right;
        while(col.down != null && col.i < i) col = col.down;
        MatrixNode add = new MatrixNode(i, j, content, col.up, col, row.left, row);
        col.up.down = add;
        col.up = add;
        row.left.right = add;
        row.left = add;
        rowOcc[i]++;
        colOcc[j]++;
    }

    public String toString() {
        String s = "";
        for(int i = 0; i < n; i++) {
            if(rows[i].right.right == null) continue;
            MatrixNode row = rows[i];
            s+="Row " + i + ": (Occupation: " + rowOcc[i] + ")\t";
            while(row.right.right != null) {
                row = row.right;
                s+=row.j + " -> " + row.value + "\t";
            }
            s+="\n";
        }
        return s;
    }

    public int[] smith() {
        //Minimale besetzung Spalte s, minimale besetzung zeile z
        // falls s < z, so wähle Spalte s als zielspalte und aus s dann die Zeile p, sodass (s,p) -x-> 0 und p hat minimale besetzung aller Zeilen, die diese eigenschaft erfüllen.
        int c = 0, l = 0;
        int r = 0, k = 0;
        for(int i = 0; i < n; i++) {
            if(rowOcc[i] > r) {
                k = i;
                r = rowOcc[i];
            }
        }
        for(int j = 0; j < m; j++) {
            if(colOcc[j] > c) {
                l = j;
                c = colOcc[j];
            }
        }
        if(c < r) {// Choose pivot in such a way, that the occupation of the row and column in which the element is in are minimal.
            MatrixNode col = cols[l];
            r = 0;
            k = 0;
            while(col.down.down != null) {
                col = col.down;
                if(rowOcc[col.i] > r) {
                    k = col.i;
                    r = rowOcc[col.i];
                }
            }
        }else{
            MatrixNode row = rows[k];
            c = 0;
            l = 0;
            while(row.right.right != null) {
                row = row.right;
                if(colOcc[row.j] > c) {
                    l = row.j;
                    c = colOcc[row.j];
                }
            }
        }
        int rowsDone = 0;
        int colsDone = 0;
        if(c > r) {
            if(eliminateCol(k,l)) {
                MatrixNode row = rows[k].right;
                while(row.j != l) {
                    row.up.down = row.down;
                    row.down.up = row.up;
                    row = row.right;
                }
                rows[k].right = row;
                row.right = new MatrixNode(k, m, 0, null, null, row, null);
                rowsDone++;
                colsDone++;
            }else {
                if(eliminateRow(k,l)) rowsDone++;
            }
        }else {
            if(eliminateRow(k,l)) {
                MatrixNode col = cols[l].down;
                while(col.i != k) {
                    col.left.right = col.right;
                    col.right.left = col.left;
                    col = col.down;
                }
                cols[l].down = col;
                col.down = new MatrixNode(n, l, 0, col, null, null, null);
                rowsDone++;
                colsDone++;
            }else {
                if(eliminateCol(k, l)) colsDone++;
            }
        }
        while(rowsDone < n || colsDone < m) {
            Vector2D<Integer, Integer> v = pivot();
            l = v.t;
            k = v.k;
            if(colOcc[l] < rowOcc[k]) {
                if(eliminateRow(k, l)) {
                    MatrixNode col = cols[l].down;
                    while(col.i != k) {
                        col.left.right = col.right;
                        col.right.left = col.left;
                        col = col.down;
                    }
                    cols[l].down = col;
                    col.down = new MatrixNode(n, l, 0, col, null, null, null);
                    rowsDone++;
                    colsDone++;
                }else {
                    if(eliminateCol(k, l)) colsDone++;
                }
            }else {
                if(eliminateCol(k,l)) {
                    MatrixNode row = rows[k].right;
                    while(row.j != l) {
                        row.up.down = row.down;
                        row.down.up = row.up;
                        row = row.right;
                    }
                    rows[k].right = row;
                    row.right = new MatrixNode(k, m, 0, null, null, row, null);
                    rowsDone++;
                    colsDone++;
                }else {
                    if(eliminateRow(k,l)) rowsDone++;
                }
            }
        }
        return null;
    }

    private Vector2D<Integer, Integer> pivot() {
        int i = 0, j = 0;
        int value = 0;
        for(int k = 0; k < n; k++) {
            MatrixNode row = rows[k];
            while(row.right.right != null) {
                row = row.right;
                int curr = Math.abs(row.value);
                if(curr > value) {
                    value = curr;
                    i = k;
                    j = row.j;
                }
            }
        }
        return new Vector2D<>(i,j);
    }

    private boolean eliminateCol(int i, int j) {
        boolean eliminated = true;
        MatrixNode node = rows[i];
        while(node.j != j) {
            node = node.right;
        }
        MatrixNode curr = node.up;
        while(curr.i != -1) {
            int lambda = curr.value / node.value;
            if(lambda*node.value != curr.value) {
                eliminated = false;
            }
            addRow(i, curr.i, -lambda);
            curr = curr.up;
        }
        curr = node.down;
        while(curr.i != n) {
            int lambda = curr.value / node.value;
            if(lambda*node.value != curr.value) {
                eliminated = false;
            }
            addRow(i, curr.i, -lambda);
            curr = curr.down;
        }
        return eliminated;
    }

    private void addRow(int i, int k, int l) {
        MatrixNode fix = rows[i];
        MatrixNode target = rows[k].right;
        while(fix.right.right != null) {
            fix = fix.right;
            while(target.j < fix.j) {
                target = target.right;
            }
//            if(target.j == m) { Maybe exclude
//
//            }
            if(target.j == fix.j) {
                if(target.value + l * fix.value == 0) {
                    target.left.right = target.right;
                    target.right.left = target.left;
                    target.up.down = target.down;
                    target.down.up = target.up;
                    rowOcc[target.i]--;
                    colOcc[target.j]--;
                }else{
                    target.value += l * fix.value;
                }
            }else {
                MatrixNode col = cols[fix.j];
                while(col.i < k) {
                    col = col.down;
                }
                MatrixNode curr = new MatrixNode(k, fix.j, l*fix.value, col.up, col, target.left, target);
                col.up.down = curr;
                col.up = curr;
                target.left = curr;
                colOcc[fix.j]++;
                rowOcc[k]++;
            }
        }
    }

    private boolean eliminateRow(int i, int j) {
        boolean eliminated = true;
        MatrixNode node = cols[j];
        while(node.i != i) {
            node = node.down;
        }
        MatrixNode curr = node.left;
        while(curr.j != -1) {
            int lambda = curr.value / node.value;
            if(lambda*node.value != curr.value) {
                eliminated = false;
            }
            addCol(j, curr.j, -lambda);
            curr = curr.left;
        }
        curr = node.right;
        while(curr.j != m) {
            int lambda = curr.value / node.value;
            if(lambda*node.value != curr.value) {
                eliminated = false;
            }
            addCol(j, curr.j, -lambda);
            curr = curr.right;
        }
        return eliminated;
    }

    private void addCol(int j, int k, int l) {
        MatrixNode fix = cols[j];
        MatrixNode target = cols[k].down;
        while(fix.down.down != null) {
            fix = fix.down;
            while(target.i < fix.i) {
                target = target.down;
            }
//            if(target.j == m) { Maybe exclude
//
//            }
            if(target.i == fix.i) {
                if(target.value + l * fix.value == 0) {
                    target.left.right = target.right;
                    target.right.left = target.left;
                    target.up.down = target.down;
                    target.down.up = target.up;
                    rowOcc[target.i]--;
                    colOcc[target.j]--;
                }else{
                    target.value += l * fix.value;
                }
            }else {
                MatrixNode row = rows[fix.i];
                while(row.j < k) {
                    row = row.right;
                }
                MatrixNode curr = new MatrixNode(fix.i, k, l*fix.value, target.up, target.down, row.left, row);
                row.left.right = curr;
                row.left = curr;
                target.left = curr;
                colOcc[k]++;
                rowOcc[fix.i]++;
            }
        }
    }

    public void clear() {

    }

    public class MatrixNode {
        MatrixNode right, left;//nextCol = go right, previousCol
        MatrixNode up, down;
        int value;
        int i, j;

        public MatrixNode(int i, int j, int value, MatrixNode up, MatrixNode down, MatrixNode left, MatrixNode right) {
            this.i = i;
            this.j = j;
            this.value = value;
            this.right = right;
            this.left = left;
            this.down = down;
            this.up = up;
        }
    }
}
