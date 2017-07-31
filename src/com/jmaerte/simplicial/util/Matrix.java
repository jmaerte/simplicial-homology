package com.jmaerte.simplicial.util;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
        System.out.println("Matrix-Size: " + n + "x" + m);
        int min = Math.min(n,m);
        long startTime = System.currentTimeMillis();
        printProgress(startTime, min, 1);
        int c = 0, l = 0;
        int r = 0, k = 0;
        ArrayList<Integer> resultList = new ArrayList<>();
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
        int block = 0;
        if(c < r) {
//            if(eliminateCol(k,l)) {
//                MatrixNode row = rows[k].right;
//                while(row.j != l) {
//                    row.up.down = row.down;
//                    row.down.up = row.up;
//                    row = row.right;
//                }
//                rows[k].right = row;
//                row = row.right;
//                while(row.j != m) {
//                    row.up.down = row.down;
//                    row.down.up = row.up;
//                    row = row.right;
//                }
//                rows[k].right.right = row;
//                row.left = rows[k].right;
//                rowOcc[k] = 1;
//                swap(0,0,k,l, resultList);
//                block++;
//            }else {
//                eliminateRow(k,l);
//            }
            if(eliminateCol(k,l) && eliminateRow(k,l)) {
                swap(block, block, k, l, resultList);
                block++;
            }
        }else {
//            if(eliminateRow(k,l)) {
//                MatrixNode col = cols[l].down;
//                while(col.i != k) {
//                    col.left.right = col.right;
//                    col.right.left = col.left;
//                    col = col.down;
//                }
//                cols[l].down = col;
//                col = col.down;
//                while(col.i != n) {
//                    col.left.right = col.right;
//                    col.right.left = col.left;
//                    col = col.down;
//                }
//                cols[l].down.down = col;
//                col.up = cols[l].down;
//                colOcc[l] = 1;
//                swap(0,0,k,l, resultList);
//                block++;
//            }else {
//                eliminateCol(k, l);
//            }
            if(eliminateRow(k,l) && eliminateCol(k,l)) {
                swap(block, block, k, l, resultList);
                block++;
            }
        }
        MatrixNode pivot = null;
        while((pivot = pivot(block)) != null) {
            if(eliminate(pivot)) {
                swap(block, block, pivot.i, pivot.j, resultList);
                block++;
            }
//            if(colOcc[l] > rowOcc[k]) {
//                if(eliminateRow(k, l)) {
//                    MatrixNode col = cols[l].down;
//                    while(col.i != k) {
//                        col.left.right = col.right;
//                        col.right.left = col.left;
//                        col = col.down;
//                    }
//                    cols[l].down = col;
//                    col = col.down;
//                    while(col.i != n) {
//                        col.left.right = col.right;
//                        col.right.left = col.left;
//                        col = col.down;
//                    }
//                    cols[l].down.down = col;
//                    col.up = cols[l].down;
//                    colOcc[l] = 1;
//                    swap(block,block, k, l, resultList);
//                    block++;
//                }else {
//                    eliminateCol(k, l);
//                }
//                if(eliminateRow(k,l) && eliminateCol(k,l)){
//                    swap(block, block, k, l, resultList);
//                    block++;
//                }
//            }else {
//                if(eliminateCol(k,l)) {
//                    MatrixNode row = rows[k].right;
//                    while(row.j != l) {
//                        row.up.down = row.down;
//                        row.down.up = row.up;
//                        colOcc[row.j]--;
//                        row = row.right;
//                    }
//                    rows[k].right = row;
//                    row.left = rows[k];
//                    row = row.right;
//                    while(row.j != m) {
//                        row.up.down = row.down;
//                        row.down.up = row.up;
//                        colOcc[row.j]--;
//                        row = row.right;
//                    }
//                    rows[k].right.right = row;
//                    row.left = rows[k].right;
//                    rowOcc[k] = 1;
//                    swap(block, block, k, l, resultList);
//                    block++;
//                }else {
//                    eliminateRow(k,l);
//                }
//                if(eliminateCol(k,l) && eliminateRow(k,l)) {
//                    swap(block, block, k, l, resultList);
//                    block++;
//                }
//            }
            printProgress(startTime, min, block);
        }
        printProgress(startTime, min, min);
        int[] result = new int[resultList.size()];
        for(int f = 0; f < result.length; f++) {
            result[f] = resultList.get(f);
        }
        System.out.print("\n");
        return result;
    }

    private boolean eliminate(MatrixNode node) {
        boolean eliminated = true;
        int c = colOcc[node.j];
        int r = rowOcc[node.i];
        int value = node.value;
        boolean rowEliminable = rowEliminable(node);
        boolean colEliminable = colEliminable(node);
        System.out.println(rowEliminable + " " + colEliminable);
        if(rowEliminable && colEliminable) {
            if(c > r) {
                //eliminate col
                MatrixNode curr = cols[node.j].down;
                while(curr.i != n) {
                    if (curr.i == node.i) continue;
                    addRow(node.i, curr.i, -curr.value / value);
                    curr = curr.down;
                }
                //eliminate row without additions.
                curr = rows[node.i].right;
                while(curr.j != m) {
                    if(curr.j == node.j) continue;
                    remove(curr, false,true);
                    colOcc[curr.j]--;
                    rowOcc[curr.i]--;
                    curr = curr.right;
                }
                node.left = rows[node.i];
                node.right = curr;
                rows[node.i].right = node;
                curr.left = node;
            } else {
                //eliminate row
                MatrixNode curr = rows[node.i].right;
                while(curr.j != m) {
                    if (curr.j == node.j) continue;
                    addCol(node.j, curr.j, -curr.value / value);
                    curr = curr.right;
                }
                //eliminate col without additions.
                curr = cols[node.j].down;
                while(curr.i != n) {
                    if(curr.i == node.i) continue;
                    remove(curr, true,false);
                    colOcc[curr.j]--;
                    rowOcc[curr.i]--;
                    curr = curr.down;
                }
                node.up = cols[node.j];
                node.down = curr;
                cols[node.j].down = node;
                curr.up = node;
            }
        }else if(rowEliminable && !colEliminable) {
            eliminated = false;
            //eliminate row
            MatrixNode curr = rows[node.i].right;
            while(curr.j != m) {
                if(curr.j == node.j) continue;
                addCol(node.j, curr.j, -curr.value/value);
                curr = curr.right;
            }
            //add maximum to col without additions.
            curr = cols[node.j].down;
            while(curr.i != n) {
                if(curr.i == node.i) continue;
                curr.value-= (curr.value / value) * value;
                if(curr.value == 0) {
                    remove(curr, true, true);
                    colOcc[curr.j]--;
                    rowOcc[curr.i]--;
                }
                curr = curr.down;
            }
        }else if(colEliminable && !rowEliminable) {
            eliminated = false;
            //eliminate col
            MatrixNode curr = cols[node.j].down;
            while(curr.i != n) {
                if(curr.i == node.i) continue;
                addRow(node.i, curr.i, -curr.value/value);
                curr = curr.down;
            }

            //add maximum to row without additions.
            curr = rows[node.i].right;
            while(curr.j != m) {
                if(curr.j == node.j) continue;
                curr.value-= (curr.value / value) * value;
                if(curr.value == 0) {
                    remove(curr, true, true);
                    colOcc[curr.j]--;
                    rowOcc[curr.i]--;
                }
                curr = curr.right;
            }
        }else {
            eliminated = false;
            if(c > r) {
                //eliminate col
                MatrixNode curr = cols[node.j].down;
                while(curr.i != n) {
                    if (curr.i == node.i) continue;
                    addRow(node.i, curr.i, -curr.value / value);
                    curr = curr.down;
                }
                //eliminate row
                curr = rows[node.i].right;
                while(curr.j != m) {
                    if (curr.j == node.j) continue;
                    addCol(node.j, curr.j, -curr.value / value);
                    curr = curr.right;
                }
            }else {
                //eliminate row
                MatrixNode curr = rows[node.i].right;
                while(curr.j != m) {
                    if (curr.j == node.j) continue;
                    addCol(node.j, curr.j, -curr.value / value);
                    curr = curr.right;
                }
                //eliminate col
                curr = cols[node.j].down;
                while(curr.i != n) {
                    if (curr.i == node.i) continue;
                    addRow(node.i, curr.i, -curr.value / value);
                    curr = curr.down;
                }
            }
        }
        return eliminated;
    }

    private void remove(MatrixNode node, boolean fromRow, boolean fromCol) {
        if(fromRow) {
            node.left.right = node.right;
            node.right.left = node.left;
        }
        if(fromCol) {
            node.up.down = node.down;
            node.down.up = node.up;
        }
    }

    private boolean rowEliminable(MatrixNode node) {
        boolean result = true;
        MatrixNode curr = rows[node.i].right;
        while(curr.j != m) {
            if(curr.value % node.value != 0) result = false;
            curr = curr.right;
        }
        return result;
    }

    private boolean colEliminable(MatrixNode node) {
        boolean result = true;
        MatrixNode curr = cols[node.j].down;
        while(curr.i != n) {
            if(curr.value % node.value != 0) result = false;
            curr = curr.down;
        }
        return result;
    }

    /** Takes a cell (k,l), such that row k only contains this cell as non-null entry as well as col l and a target position (i,j), where to swap this cell to.
     *
     * @param i target row
     * @param j target col
     * @param k swapping row
     * @param l swapping col
     */
    private void swap(int i, int j, int k, int l, ArrayList<Integer> list) {
        int value = rows[k].right.value;
        if(i == k && j == l) {
            rows[i] = new MatrixNode(i, -1, 0, null, null, null, new MatrixNode(i, m, 0, null, null, null, null));
            rows[i].right.left = rows[i];
            cols[j] = new MatrixNode(-1, j, 0, null, new MatrixNode(n, j, 0, null, null, null, null), null, null);
            cols[j].down.up = cols[j];
            list.add(Math.abs(value));
            return;
        }
        rows[k].right = rows[k].right.right;
        rows[k].right.left = rows[k];
        cols[l].down = cols[l].down.down;
        cols[l].down.up = cols[l];
        MatrixNode row = rows[i].right;
        while(row.j != m) {
            row.up.down = row.down;
            row.down.up = row.up;
            MatrixNode col = cols[row.j];
            while(col.i < k) {
                col = col.down;
            }
            row.up = col.up;
            row.down = col;
            col.up.down = row;
            col.up = row;
            row.i = k;
            row = row.right;
        }
        row.i = k;
        rows[k] = rows[i];
        rows[k].i = k;
        rows[i] = new MatrixNode(i, -1, 0, null, null, null, new MatrixNode(i, m, 0, null, null, null, null));
        rows[i].right.left = rows[i];

        MatrixNode col = cols[j].down;
        while(col.i != n) {
            col.left.right = col.right;
            col.right.left = col.left;
            row = rows[col.i];
            while(row.j < l) {
                row = row.right;
            }
            col.left = row.left;
            col.right = row;
            row.left.right = col;
            row.left = col;
            col.j = l;
            col = col.down;
        }
        col.j = l;
        cols[l] = cols[j];
        cols[l].j = l;
        cols[j] = new MatrixNode(-1, j, 0, null, new MatrixNode(n, j, 0, null, null, null, null), null, null);
        cols[j].down.up = cols[j];

        rowOcc[k] = rowOcc[i];
        colOcc[l] = colOcc[j];
        rowOcc[i] = colOcc[j] = 0;
        list.add(Math.abs(value));
    }

    private MatrixNode pivot(int block) {
        if(block >= n || block >= m) return null;
        MatrixNode node = null;
        for(int k = block; k < n; k++) {
            MatrixNode row = rows[k];
            while(row.right.right != null) {
                row = row.right;
                if(node == null) {
                    node = row;
                } else if(Math.abs(row.value) < node.value || (Math.abs(row.value) == node.value && colOcc[row.j] + rowOcc[row.i] < colOcc[node.j] + rowOcc[node.i])) {
                    node = row;
                }
            }
        }
        return node;
    }
    //TODO: Fix occupation counters.
    private boolean eliminateCol(int i, int j) {
        boolean eliminated = true;
        MatrixNode node = rows[i];
        while(node.j != j) {
            node = node.right;
        }
        MatrixNode curr = node.up;
        while(curr.i != -1) {
            int lambda = curr.value / node.value;
            if(lambda * node.value != curr.value) {
                eliminated = false;
            }
            addRow(i, curr.i, -lambda);
            curr = curr.up;
        }
        curr = node.down;
        while(curr.i != n) {
            int lambda = curr.value / node.value;
            if(lambda * node.value != curr.value) {
                eliminated = false;
            }
            addRow(i, curr.i, -lambda);
            curr = curr.down;
        }
        return eliminated;
    }

    private void addRow(int i, int k, int l) {
        MatrixNode fix = rows[i].right;
        MatrixNode target = rows[k].right;
        while(fix.j != m) {
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
                target.left.right = curr;
                target.left = curr;
                colOcc[fix.j]++;
                rowOcc[k]++;
            }
            fix = fix.right;
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
        MatrixNode fix = cols[j].down;
        MatrixNode target = cols[k].down;
        while(fix.i != n) {
            while(target.i < fix.i) {
                target = target.down;
            }
//            if(target.j == m) { Maybe exclude
//
//            }
            if(target.i == fix.i) {
                if(target.value + l * fix.value == 0) {
                    target.up.down = target.down;
                    target.down.up = target.up;
                    target.left.right = target.right;
                    target.right.left = target.left;
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
                MatrixNode curr = new MatrixNode(fix.i, k, l*fix.value, target.up, target, row.left, row);
                row.left.right = curr;
                row.left = curr;
                target.up.down = curr;
                target.up = curr;
                rowOcc[fix.i]++;
                colOcc[k]++;
            }
            fix = fix.down;
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

    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print("\r" + string.toString());
    }
}
