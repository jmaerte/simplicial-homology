package com.jmaerte.simplicial.util;

import java.util.Arrays;

public class Bignum {

    public static final int BASE = Integer.MAX_VALUE;
    private static final long LONG_TRANSFORM = (1l << 32) - 1;

    int[] digits;
    int sign;
    int length;

    /**
     * Creates a Bignum from an unsigned integer.
     * @param sign
     * @param value
     */
    public Bignum(int sign, int value) {
        this.sign = sign;
        this.digits = new int[1];
        digits[0] = value;
        length = 1;
    }

    public Bignum() {
        length = 1;
        sign = 1;
        digits = new int[length];
        digits[0] = 0;
    }

    public Bignum(int value) {
        this.sign = value < 0 ? -1 : 1;
        this.digits = new int[1];
        digits[0] = value < 0 ? -value : value;
        length = 1;
    }

    public Bignum(int sign, int[] digits, int length) {
        this.sign = sign;
        this.length = length;
        this.digits = digits;
    }

    public void addArray(int[] arr, int occupation) {
        if(occupation > digits.length) {
            mkPlace(Math.max(length, occupation) + 1);
        }
        long carry = 0;
        int i = 0;
        while(i < Math.min(length, occupation)) {
            carry = (arr[i] & LONG_TRANSFORM) + (digits[i] & LONG_TRANSFORM) + carry;
            digits[i] = (int)carry;
            carry >>>= 32;
            i++;
        }
        while(i < occupation) {
            carry = (arr[i] & LONG_TRANSFORM) + carry;
            digits[i] = (int) carry;
            carry >>>= 32;
            i++;
        }
        if(carry != 0) {
            digits[++i] = (int)carry;
        }
        length = i;
    }

    private static int[] karatsuba(int[] a, int[] b, int alen, int blen) {
        int k = Utils.next_power_of_two(Math.max(alen, blen));
        int offset = Math.max(k - alen, k - blen); // We know: offset < n/2.
        if(a.length < k) {
            int[] temp = new int[k];
            System.arraycopy(a, 0, temp, 0, alen);
            a = temp;
        }
        if(b.length < k) {
            int[] temp = new int[k];
            System.arraycopy(b, 0, temp, 0, blen);
            b = temp;
        }
        int[] _x = new int[k/2];
        int[] _y = new int[k/2];
        int[] __x = new int[k/2];
        int[] __y = new int[k/2];
        System.arraycopy(a, k/2, _x, 0, k/2);
        System.arraycopy(b, k/2, _y, 0, k/2);
        System.arraycopy(a, 0, __x, 0, k/2);
        System.arraycopy(b, 0, __y, 0, k/2);

    }
    private static int[] karatsuba(int[] a, int[] b, int offset, int n) {

    }

    public void subArray(int[] arr, int occupation) {
        if(occupation > digits.length) {
            mkPlace(occupation);
        }
        long difference = 0;
        int i = 0;
        while(i < Math.min(length, occupation)) {
            difference = (digits[i] & LONG_TRANSFORM) - (arr[i] & LONG_TRANSFORM) + difference;
            digits[i] = (int)difference;
            difference >>= 32;
            i++;
        }
        while(i < occupation) {
            digits[i] = arr[i];
            i++;
        }
        length = Math.max(i, length);
    }

    private void add(Bignum that) {
        if(this.sign == that.sign) addArray(that.digits, that.length);
        else if(this.sign != that.sign) {

        }
    }

    private void sub(int[] dig, int length) {

    }

    public boolean isZero() {
        return length == 1 && digits[0] == 0;
    }

    public int absCompareTo(Bignum that) {
        if(this.length > that.length) return 1;
        if(that.length > this.length) return -1;
        for(int i = length - 1; i >= 0; i--) {
            if(this.digits[i] > that.digits[i]) return 1;
            if(that.digits[i] > this.digits[i]) return -1;
        }
        return 0;
    }

    public String toString() {
        return "Big " + length + " " + Arrays.toString(digits);
    }

    public String toDecimal() {
        return "";
    }

    public Bignum minus(Bignum that) {
        return new Bignum();
    }

    private void mkPlace(int length) {
        if (length < digits.length) return;
        int[] result = new int[length];
        System.arraycopy(digits, 0, result, 0, this.length);
        digits = result;
    }

    private void mkPlace() {
        mkPlace(digits.length * 2);
    }
}