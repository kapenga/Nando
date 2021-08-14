package logic;

import java.util.Arrays;

public class MathBox {


    public static long murmur64(long h) {
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    public static long[] invert(long[] input)
    {
        long[] output = new long[input.length];
        for(int i = 0; i < output.length; i++)
            output[i] = ~input[i];
        return output;
    }

    public static long[] and(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = a[i] & b[i];
        return output;
    }

    public static long[] nand(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = ~(a[i] & b[i]);
        return output;
    }

    public static long[] or(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = a[i] | b[i];
        return output;
    }

    public static long[] xor(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = a[i] ^ b[i];
        return output;
    }

    public static long[] xnor(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = ~(a[i] ^ b[i]);
        return output;
    }

    public static long[] nor(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = ~(a[i] | b[i]);
        return output;
    }

    public static long[] andNot(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = a[i] & (~b[i]);
        return output;
    }

    public static long[] orNot(long[] a, long[] b)
    {
        long[] output = new long[a.length];
        for(int i = 0; i < output.length; i++)
            output[i] = a[i] | (~b[i]);
        return output;
    }

}