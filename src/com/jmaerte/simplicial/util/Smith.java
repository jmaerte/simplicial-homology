package com.jmaerte.simplicial.util;

public class Smith {

    int[] amount;
    int[] values;
    int occupation;
    int rank;

    public Smith(int initialCapacity) {
        amount = new int[initialCapacity];
        values = new int[initialCapacity];
        occupation = 0;
    }

    public void addTo(int value, int count) {
        int k = index(value);
        if(k < occupation && values[k] == value) {
            amount[k] += count;
        }else {
            insert(k, value, count);
        }
        rank += count;
    }

    public void insert(int k, int value, int count) {
        if(value == 0) {
            return;
        }
        if(values.length < occupation + 1) {
            mkPlace();
        }
        if(occupation - k > 0) {
            System.arraycopy(values, k, values, k + 1, occupation - k);
            System.arraycopy(amount, k, amount, k + 1, occupation - k);
        }

        values[k] = value;
        amount[k] = count;
        occupation++;
    }

    private void mkPlace() {
        int capacity = (occupation * 3) / 2 + 1;
        int[] _values = new int[capacity];
        int[] _amount = new int[capacity];
        System.arraycopy(values, 0 ,_values, 0, occupation);
        System.arraycopy(amount, 0, _amount, 0, occupation);
        values = _values;
        amount = _amount;
    }

    public int index(int value) {
        if(occupation == 0 || value > values[occupation - 1]) return occupation;
        int left = 0;
        int right = occupation;
        while(left < right) {
            int mid = (right + left)/2;
            if(values[mid] > value) right = mid;
            else if(values[mid] < value) left = mid + 1;
            else return mid;
        }
        return left;
    }

    public String toString() {
        String s = "[Value: amount, ";
        for(int i = 0; i < occupation; i++) {
            s += values[i] + ": " + amount[i] + ((i == occupation - 1) ? "" : ", ");
        }
        return s + "]";
    }

    public static String calculateHom(int fi, Smith[] cache) {
        if(cache.length < 2) return "";
        int rank = fi - cache[0].rank;
        String s = "";
        for(int k = 0; k < cache[1].occupation; k++) {
            if(cache[1].values[k] == 1) continue;
            s += (s == "" ? "" : " + ") + "Z_" + cache[1].values[k] + "^" + cache[1].amount[k];
        }
        if(rank - cache[1].rank != 0) s += (s == "" ? "" : " + ") + "Z^" + (rank - cache[1].rank);
        return s == "" ? "0" : s;
    }
}
