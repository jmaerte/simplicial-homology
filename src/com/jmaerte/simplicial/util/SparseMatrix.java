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
        for(int i = 0; i < n; i++) {
            MatrixNode head = new MatrixNode(i, -1, FlexInt.ZERO, null, null, null, null);
            MatrixNode tail = new MatrixNode(i, m, FlexInt.ZERO, head, null, null, null);
            head.right = tail;
            rows[i] = head;
        }
        cols = new MatrixNode[m];
        for(int j = 0; j < m; j++) {
            MatrixNode head = new MatrixNode(-1, j, FlexInt.ZERO, null, null, null, null);
            MatrixNode tail = new MatrixNode(n, j, FlexInt.ZERO, null, null, head, null);
            head.down = tail;
            cols[j] = head;
        }
        rowOcc = new int[n];
        colOcc = new int[m];
    }

    public void set(int i, int j, int value) {
        MatrixNode row = rows[i].right;
        MatrixNode col = cols[j].down;
        while(row.j < j) row = row.right;
        while(col.i < i) col = col.down;
        MatrixNode add = new MatrixNode(i, j, new FlexInt(value), row.left, row, col.up, col);
        col.up.down = add;
        col.up = add;
        row.left.right = add;
        row.left = add;
        rowOcc[i]++;
        colOcc[j]++;
    }

    public String toString() {
        String s = "Row representation:\n";
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
//        s+="Col representation:\n";
//        for(int j = 0; j < m; j++) {
//            if(cols[j].down.down == null) continue;
//            MatrixNode col = cols[j];
//            s+="Col " + j + ": (Occupation: " + colOcc[j] + ")\t";
//            while(col.down.down != null) {
//                col = col.down;
//                s+=col.i + " -> " + col.value + "\t";
//            }
//            s+="\n";
//        }
        return s;
    }

    public Number[] smith() {
        ArrayList<Number> resultList = new ArrayList<>();

        System.out.print("Gaussed 0 columns\r");
        Vector2D<Integer, ArrayList<Integer>> gauss = gaussian();
        int g = gauss.x;
        for(int i = 0; i < g; i++) {
            resultList.add(1);
        }
        System.out.println();
        System.out.println(g + " ones found in smith normal form.");
        System.out.print("Valence Algorithm:");

        ArrayList<Integer> todo = gauss.y;

        // System.out.println(resultList);
        System.out.println();
        Number[] result = new Number[resultList.size()];
        for(int i = 0; i < result.length; i++) {
            result[i] = resultList.get(i);
        }
        return result;
    }

    private Vector2D<Integer, ArrayList<Integer>> gaussian() {
        ArrayList<Integer> todo = new ArrayList<>();
        int heap = 0;
        int k = 0;// amount of already eliminated columns
        for(int j = 0; j < m; j++) {
            MatrixNode col = cols[j].down;
            ArrayList<MatrixNode> pivotRows = new ArrayList<>();
            while(col.i != n) {
                if(col.i >= n - heap) break;
                if(col.left.j != -1 || col.i < k) {
                    col = col.down;
                    continue;
                }
                if(col.value.compareTo(new FlexInt(1)) == 0 || col.value.compareTo(new FlexInt(-1)) == 0) {
                    pivotRows.add(col);
                }
                col = col.down;
            }
            if(pivotRows.isEmpty()) {
                // Add the rows with trailing nnz entry in this column to the heap.
                MatrixNode collector = cols[j].down;
                while(collector.i != n) {
                    if(collector.i < k) {
                        collector = collector.down;
                        continue;
                    }
                    if(collector.left.j == -1) {
                        // TODO: Add to stack or smth to process this rows later in the valence algorithm
                        swapRows(collector.i, n - heap - 1);
                        heap++;
                        }
                    collector = collector.down;
                }
            }else {
                MatrixNode pivot = null;
                for(MatrixNode curr : pivotRows) {
                    if(pivot == null || rowOcc[curr.i] < rowOcc[pivot.i]) pivot = curr;
                }
                swapRows(pivot.i, k);
                //TODO: Print the ones done with gaussian elimination.
                //TODO Eliminate the whole column j.
                MatrixNode node = cols[j].down;
                while(node.i != n) {
                    if(node.i != pivot.i && node.i >= k) {
                        addRows(pivot.i, node.i, node.value.divideBy(pivot.value).multiply(new FlexInt(-1)));
                    }
                    node = node.down;
                }
                node = rows[k].right;
                while(node.j != m) {
                    node.up.down = node.down;
                    node.down.up = node.up;
                    node = node.right;
                }
                rows[k].right.right = node;
                node.left = rows[k].right;
                k++;
                System.out.print("\rGaussed " + k + " columns");
                // System.out.println(this);
            }
        }
        return new Vector2D<>(k, todo);
    }

    private void swapRows(int i, int k) {
        //TODO
        if(i == k) return;
        MatrixNode node = rows[i].right;
        while(node.j != m) {
            node.up.down = node.down;
            node.down.up = node.up;
            node.i = k;
            node = node.right;
        }
        node.i = k;
        node = rows[k].right;
        while(node.j != m) {
            node.up.down = node.down;
            node.down.up = node.up;
            node.i = i;
            node = node.right;
        }
        node.i = i;
        node = rows[i];
        rows[i] = rows[k];
        rows[k] = node;
        rows[k].i = k;
        rows[i].i = i;
        node = node.right;
        while(node.j != m) {
            MatrixNode curr = cols[node.j].down;
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
            MatrixNode curr = cols[node.j].down;
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

    public void addRows(int i, int k, FlexInt lambda) {
        MatrixNode main = rows[i].right;
        MatrixNode curr = rows[k].right;
        while(main.j != m) {
            while(curr.j < main.j) curr = curr.right;
            if(curr.j == main.j) {
                if((new FlexInt(main.value)).multiply(lambda).add(curr.value).compareTo(FlexInt.ZERO) == 0) {
                    curr.right.left = curr.left;
                    curr.left.right = curr.right;
                    curr.up.down = curr.down;
                    curr.down.up = curr.up;
                    colOcc[curr.j]--;
                    rowOcc[k]--;
                }else {
                    curr.value = curr.value.add((new FlexInt(lambda)).multiply(main.value));
                }
            }else {
                MatrixNode col = cols[main.j];
                while(col.i < k) col = col.down;
                MatrixNode node = new MatrixNode(k, main.j, main.value.multiply(lambda), curr.left, curr, col.up, col);
                curr.left.right = node;
                curr.left = node;
                col.up.down = node;
                col.up = node;
                colOcc[main.j]++;
                rowOcc[k]++;
            }
            main = main.right;
        }
    }

    private class MatrixNode {
        MatrixNode left, right, up, down;
        FlexInt value;
        int i, j;

        public MatrixNode(int i, int j, FlexInt value, MatrixNode left, MatrixNode right, MatrixNode up, MatrixNode down) {
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