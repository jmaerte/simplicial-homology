package com.jmaerte.simplicial.util;

import java.util.Arrays;

public class Bignum {

    public static long LONG = (1L << 32) - 1;

    public int sign;
    public int[] digits;
    public int length;

    /**Creates a signed Bignum from an unsigned array. I.e. -1 is the highest possible number, because it correlates to 11....1
     *
     * @param sign the sign of the resulting Bignum object.
     * @param digits the digits-array
     * @param length length of digits that define the value.
     */
    public Bignum(int sign, int[] digits, int length) {
        this.sign = sign;
        this.digits = digits;
        this.length = length;
    }

    /**Same as above, but cuts off the trailing zero.
     *
     * @param sign sign of the resulting Bignum object.
     * @param digits magnitude.
     */
    public Bignum(int sign, int[] digits) {
        int length = digits.length;
        while(length > 1 && digits[length - 1] == 0) length--;
        this.sign = sign;
        this.digits = digits;
        this.length = length;
    }

    /**Creates an signed Bignum from an signed int. Here we have value-identity between both. I.e. -1 is the smallest possible negative number, even if it correlates with 11....1
     *
     * @param value The signed value for Bignum
     */
    public Bignum(int value) {
        this(value < 0 ? -1 : 1, new int[]{value < 0 ? -value : value});
    }

    public int intValue() {
        return  digits[0];
    }

    public void addMagnitude(int[] magnitude, int length) {
        if(length + 1 > digits.length) mkPlace(length + 1);
        long carry = 0;
        int i = 0;
        while(i < length) {
            carry = (digits[i] & LONG) + (magnitude[i] & LONG) + carry;
            digits[i] = (int) carry;
            carry >>>= 32;
            i++;
        }
        if(length > this.length) System.arraycopy(magnitude, this.length, digits, this.length, length - this.length);
        if(carry != 0) {
            while(i < this.length && ++digits[i] == 0) i++;
            if(i == this.length) {
                if(this.length == digits.length) mkPlace();
                digits[this.length++] = 1;
            }
        }
    }

    public void subMagnitude(int[] magnitude, int length) {
        if(length > digits.length) mkPlace(length + 1);
        long diff = 0;
        int i = 0;
        while(i < length) {
            diff = (digits[i] & LONG) - (magnitude[i] & LONG) + diff;
            digits[i] = (int) diff;
            diff >>= 32;
            i++;
        }
        if(diff != 0) {
            while(digits[i] == 0) {
                digits[i]--;
                i++;
            }
            if(--digits[i] == 0 && i+1 == this.length) this.length = length;
        }
        while(length > 1 && digits[length - 1] == 0) this.length--;
    }

    public void add(Bignum that) {
        if(this.sign == that.sign) addMagnitude(that.digits, that.length);
        else{
            int k = this.compareToAbs(that);
            if(k > 0) {// this > that
                subMagnitude(that.digits, that.length);
            }else if(k == 0) {
                this.length = 1;
                this.digits[0] = 0;
            }else {
                mkPlace(that.length + 1);
                sign = -sign;
                long diff = 0;
                int i = 0;
                while(i < length) {
                    diff = (that.digits[i] & LONG) - (digits[i] & LONG) + diff;
                    digits[i] = (int) diff;
                    diff >>= 32;
                    i++;
                }
                if(that.length > length) {
                    System.arraycopy(that.digits, length, digits, length, that.length - length);
                }
                if(diff != 0) {
                    while(i < that.length && digits[i] == 0) {
                        digits[i]--;
                        i++;
                    }
                    if(--digits[i] == 0 && i+1 == length) length--;
                }
            }
        }
    }

    public void sub(Bignum that) {

    }

    public int compareToAbs(Bignum that) {
        if(this.length > that.length) return 1;
        if(this.length < that.length) return -1;
        for(int i = 0; i < this.length; i++) {
            if(this.digits[i] > that.digits[i]) return 1;
            if(this.digits[i] < that.digits[i]) return -1;
        }
        return 0;
    }

    public String toString() {
        String s = length + "[";
        for(int i = digits.length - 1; i >= 0; i--) s += Integer.toBinaryString(digits[i]) + (i == 0 ? "" : ", ");
        return s + "]";
    }

//    public void add(Bignum that) {
//        if(that.sign == this.sign) addMagnitude(that.digits, that.length);
//        else {
//            int k = compareMagnitude(this, that); // k = this - that
//            if(k == 0) {
//                this.sign = 1;
//                this.digits[0] = 0;
//                this.length = 1;
//            }else if(k > 0) { //this > that
//                subMagnitude(that.digits, that.length);
//            }else {
//
//            }
//        }
//    }

//    public Bignum plus(Bignum that) {
//
//    }

    private void mkPlace() {
        mkPlace(2 * digits.length);
    }

    private void mkPlace(int n) {
        if(digits.length >= n) return;
        int[] temp = new int[n];
        System.arraycopy(digits, 0, temp, 0, length);
        digits = temp;
    }
}
