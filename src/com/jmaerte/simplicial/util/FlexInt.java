package com.jmaerte.simplicial.util;

import java.math.BigInteger;

/**
 * Created by Julian on 02/08/2017.
 */
public class FlexInt implements Comparable<FlexInt> {

    public static final FlexInt ZERO = new FlexInt(0);

    public long l;

    public FlexInt(long l) {
        this.l = l;
    }

    public FlexInt(FlexInt f) {
        l = f.l;
    }

    public FlexInt add(FlexInt f) {
        return new FlexInt(l+ f.l);
    }

    public FlexInt multiply(FlexInt f) {
        return new FlexInt(l * f.l);
    }

    public FlexInt divideBy(FlexInt f) {
        return new FlexInt(l / f.l);
    }

    public int compareTo(FlexInt f) {
        return (f.l == l)?(0):((f.l-l < 0)? -1 : 1);
    }

    public String toString() {
        return l+"";
    }

    public FlexInt modulo(FlexInt f) {
        return new FlexInt(l%f.l);
    }

//    public static final FlexInt ZERO = new FlexInt(0);
//
//    public boolean overflowed;
//    Number n;
//
//    public FlexInt(long l) {
//        overflowed = false;
//        n = l;
//    }
//    public FlexInt(BigInteger bi) {
//        overflowed = true;
//        n = bi;
//    }
//
//    public FlexInt(FlexInt flexInt) {
//        overflowed = flexInt.overflowed;
//        n = flexInt.n;
//    }
//
//    public String toString() {
//        return n.toString();
//    }
//
//    public FlexInt add(FlexInt flexInt) {
//        if(flexInt.overflowed && overflowed) {
//            n = ((BigInteger)n).add((BigInteger)flexInt.n);
//        }else if(flexInt.overflowed) {
//            n = BigInteger.valueOf((long)n).add((BigInteger)flexInt.n);
//        }else if(overflowed) {
//            ((BigInteger)n).add(BigInteger.valueOf((long)flexInt.n));
//        }else {
//            try {
//                n = Math.addExact((long)n,(long)flexInt.n);
//            }catch(ArithmeticException e) {
//                overflowed = true;
//                n = BigInteger.valueOf((long)n).add(BigInteger.valueOf((long)flexInt.n));
//            }
//        }
//        return this;
//    }
//
//    public FlexInt multiply(FlexInt flexInt) {
//        if(flexInt.overflowed && overflowed) {
//            n = ((BigInteger)n).multiply((BigInteger)flexInt.n);
//        }else if(flexInt.overflowed) {
//            n = BigInteger.valueOf((long)n).multiply((BigInteger)flexInt.n);
//        }else if(overflowed) {
//            n = ((BigInteger)n).multiply(BigInteger.valueOf((long)flexInt.n));
//        }else {
//            try{
//                n = Math.multiplyExact((long)n, (long)flexInt.n);
//            }catch(ArithmeticException e) {
//                overflowed = true;
//                n = BigInteger.valueOf((long)n).multiply(BigInteger.valueOf((long)flexInt.n));
//            }
//        }
//        return this;
//    }
//
//    public FlexInt divideBy(FlexInt flexInt) {
//        if(flexInt.overflowed && overflowed) {
//            n = ((BigInteger)n).divide((BigInteger)flexInt.n);
//        }else if(flexInt.overflowed) {
//            n = BigInteger.valueOf((long)n).divide((BigInteger)flexInt.n);
//        }else if(overflowed) {
//            n = ((BigInteger)n).divide(BigInteger.valueOf((long)flexInt.n));
//        }else {
//            n = (long)n/(long)flexInt.n;
//        }
//        return this;
//    }
//
//    public int compareTo(FlexInt flexInt) {
//        if(flexInt.overflowed) {
//            if(overflowed) {
//                return ((BigInteger)n).compareTo((BigInteger)flexInt.n);
//            }else {
//                return BigInteger.valueOf((long)n).compareTo((BigInteger)flexInt.n);
//            }
//        }else {
//            if(overflowed) {
//                return ((BigInteger)n).compareTo(BigInteger.valueOf((long)flexInt.n));
//            }else {
//                return (int)Math.signum((long)n - (long)flexInt.n);
//            }
//        }
//    }
}
