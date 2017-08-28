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
        int n = Utils.next_power_of_two(Math.max(alen,blen));
        if(a.length < n) {
            int[] temp = new int[n];
            System.arraycopy(a, 0, temp, 0, alen);
            a = temp;
        }
        if(b.length < n) {
            int[] temp = new int[n];
            System.arraycopy(b, 0, temp, 0, blen);
            b = temp;
        }
        int[] result = new int[2*n];
        if(n == 1) {
            long carry = (a[0] & LONG_TRANSFORM) * (b[0] & LONG_TRANSFORM);
            result[0] = (int) carry;
            carry >>>= 32;
            result[1] = (int) carry;
        }else {
            karatsuba(result, 0, a, 0, b, 0, n/2);
            karatsuba(result, n/2, a, 0, b, n/2, n/2);
            karatsuba(result, n/2, a, n/2, b, 0, n/2);
            karatsuba(result, n, a, n/2, b, n/2, n/2);
        }
        return result;
    }

    private static void karatsuba(int[] result, int offset, int[] x, int offsetX, int[] y, int offsetY, int n) {
        if(n == 1) {
            long carry = (result[offset] & LONG_TRANSFORM) + (x[offsetX] & LONG_TRANSFORM) * (y[offsetY] & LONG_TRANSFORM);
            result[offset] = (int) carry;
            carry >>>= 32;
            int i = 1;
            while(carry != 0) {
                carry = (result[offset + i] & LONG_TRANSFORM) + carry;
                result[offset + i] = (int) carry;
                carry >>>= 32;
            }
        }else {
            karatsuba(result, offset, x, offsetX, y, offsetY, n/2);
            karatsuba(result, offset + n/2, x, offsetX, y, offsetY + n/2, n/2);
            karatsuba(result, offset + n/2, x, offsetX + n/2, y, offsetY, n/2);
            karatsuba(result, offset + n, x, offsetX + n/2, y, offsetY + n/2, n/2);
        }
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
        if(this.sign == that.sign) {

        }

    }

    public void multiply(Bignum that) {
        if(that.length == 1 && that.digits[0] == 0 || length == 1 && digits[0] == 0) {
            this.length = 1;
            this.digits[0] = 0;
            this.sign = 1;
        }
        int[] prod = karatsuba(this.digits, that.digits, this.length, that.length);
        int length = prod.length;
        while(length > 1 && prod[length - 1] == 0) {
            length--;
        }
        this.length = length;
        this.digits = prod;
        this.sign = (this.sign == that.sign) ? 1 : -1;
    }

    public void multiply(int mul) {
        if(digits.length < length + 1) mkPlace(length + 1);
        long carry = 0;
        for(int i = 0; i < length; i++) {
            carry = (digits[i] & LONG_TRANSFORM) * mul + carry;
            digits[i] = (int) carry;
            carry >>>= 32;
        }
        if(carry != 0) {
            digits[length] = (int) carry;
            length++;
        }
    }

    public Bignum times(Bignum that) {
        if(that.length == 1 && that.digits[0] == 0 || length == 1 && digits[0] == 0) {
            this.length = 1;
            this.digits[0] = 0;
            this.sign = 1;
        }
        int[] prod = karatsuba(this.digits, that.digits, this.length, that.length);
        int length = prod.length;
        while(length > 1 && prod[length - 1] == 0) {
            length--;
        }
        return new Bignum((this.sign == that.sign) ? 1 : -1, prod, length);
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
        System.arraycopy(digits, 0, result, 0, length);
        digits = result;
    }

    private void mkPlace() {
        mkPlace(digits.length * 2);
    }

    public String toBinary() {
        String s = "";
        for(int i = length - 1; i >= 0; i--) {
            s += (i == length -1 ? "": " ") + intToString(digits[i], 8);
        }
        return s;
    }

    public static String intToString(int number, int groupSize) {
        StringBuilder result = new StringBuilder();

        for(int i = 31; i >= 0 ; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }
}