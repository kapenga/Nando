package logic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BitArray {
    private final long[] bits;
    private final int bitLength;
    private final String tag;
    private int trueCount;
    private boolean hashNeedsRecalculation;
    private boolean trueCountNeedsRecalculation;
    private long hashCode;

    private final static Boolean MAKETAG = false;

    private BitArray(int bitLength, String tag)
    {
        this.bitLength = bitLength;

        int longLength = (bitLength >>> 6) + ((bitLength & 63) > 0 ? 1 : 0);
        bits = new long[longLength];

        this.tag = MAKETAG ? tag : "";
        trueCount = 0;
        hashNeedsRecalculation = true;
        trueCountNeedsRecalculation = false;
    }

    private BitArray(int bitLength, long[] bits, String tag)
    {
        this.bitLength = bitLength;
        this.bits = bits;
        if(MAKETAG) {
            while(tag.startsWith("!!"))
                tag = tag.substring(2);

            this.tag = tag;
        }
        else
            this.tag = "";
        long offMask = (bitLength & 63)==0 ? Long.MAX_VALUE : (1L << (bitLength & 63))-1;
        this.bits[this.bits.length-1] &= offMask;
        //recalculateTrueCount();
        hashNeedsRecalculation = true;
        trueCountNeedsRecalculation = true;
    }

    public static BitArray create(int bitLength, String tag)
    {
        return new BitArray(bitLength, tag);
    }

    public void setBit(int index)
    {
        bits[index>>>6] |= 1L << (index & 63);

        trueCount++;
        hashNeedsRecalculation = true;
    }

    public boolean getBit(int index)
    {
        return ((bits[index>>>6] >>> (index & 63)) & 1) == 1L;
    }

    private void recalculateTrueCount()
    {
        trueCount = 0;
        int l = bits.length;
        for(int i = 0; i < l; i++)
            trueCount += Long.bitCount(bits[i]);
    }

    public static BitArray invert(BitArray source)
    {
        return new BitArray(source.bitLength, MathBox.invert(source.bits), "!"+source.tag);
    }

    public static BitArray and(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.and(sourceA.bits, sourceB.bits), "("+sourceA.tag+"&"+sourceB.tag+")");
    }

    public static BitArray andNot(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.andNot(sourceA.bits, sourceB.bits), "("+sourceA.tag+"&!"+sourceB.tag+")");
    }

    public static BitArray or(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.or(sourceA.bits, sourceB.bits), "("+sourceA.tag+"|"+sourceB.tag+")");
    }

    public static BitArray orNot(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.orNot(sourceA.bits, sourceB.bits), "("+sourceA.tag+"|!"+sourceB.tag+")");
    }


    public static BitArray xor(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.xor(sourceA.bits, sourceB.bits), "("+sourceA.tag+"^"+sourceB.tag+")");
    }

    public static BitArray nor(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.nor(sourceA.bits, sourceB.bits), "!("+sourceA.tag+"|"+sourceB.tag+")");
    }

    public static BitArray nand(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.nand(sourceA.bits, sourceB.bits), "!("+sourceA.tag+"&"+sourceB.tag+")");
    }

    public static BitArray xnor(BitArray sourceA, BitArray sourceB)
    {
        return new BitArray(sourceA.bitLength, MathBox.xnor(sourceA.bits, sourceB.bits), "!("+sourceA.tag+"^"+sourceB.tag+")");
    }

    public static BitArray createEmptyArray(BitArray source, boolean fillWithTrues)
    {
        BitArray result = new BitArray(source.bitLength,"0");
        if(fillWithTrues)
            return BitArray.invert(result);
        return result;
    }

    public static int getScore(BitArray a, boolean invert, BitArray toCheckAgainst, BitArray cares)
    {
        int score = 0;

        for(int i = 0; i < a.bits.length; i++)
            score += Long.bitCount(((invert ? ~a.bits[i] : a.bits[i]) ^ toCheckAgainst.bits[i]) & cares.bits[i]);

        return score;
    }

    public static int getScore(BitArray a, BitArray toCheckAgainst, BitArray cares, int upperLimit)
    {
        int score = 0;

        for(int i = 0; i < a.bits.length; i++) {
            score += Long.bitCount(((a.bits[i]) ^ toCheckAgainst.bits[i]) & cares.bits[i]);
            if(score > upperLimit)
                return score;
        }

        return score;
    }

    public static int[] getDualScore(BitArray a, BitArray toCheckAgainst, BitArray cares, int upperLimit)
    {
        int[] score = new int[2];

        for (int i = 0; i < a.bits.length; i++) {
            long c = cares.bits[i];
            if (c != 0) {
                long b = toCheckAgainst.bits[i];
                long ab = a.bits[i];
                long as0 = (ab ^ b) & c;
                long as1 = (~ab ^ b) & c;

                if(as0 != 0 && score[0] <= upperLimit)
                    score[0] += Long.bitCount(as0);
                if(as1 != 0 && score[1] <= upperLimit)
                    score[1] += Long.bitCount(as1);
                if (score[0] > upperLimit && score[1] > upperLimit)
                    return score;
            }
        }

        return score;
    }

    public int getTrueCount()
    {
        if(trueCountNeedsRecalculation)
        {
            recalculateTrueCount();
            trueCountNeedsRecalculation = false;
        }
        return trueCount;
    }

    public String getTag()
    {
        return tag;
    }

    public static void print(BitArray[] columns, BitArray mask)
    {
        int[][][] counts = new int[columns.length][columns.length][4];

        for(int i = 0; i < mask.bitLength; i++)
        {
            if(mask.getBit(i))
            {
                boolean[] bools = new boolean[columns.length];
                for(int j = 0; j < columns.length; j++) {
                    boolean b = columns[j].getBit(i);
                    System.out.print(b ? '1' : '0');
                    bools[j] = b;
                }

                for(int j = 0; j < columns.length-1; j++)
                    for(int k = j+1; k < columns.length; k++)
                        counts[j][k][(bools[j]?2:0)+(bools[k]?1:0)]++;
                System.out.println();
            }
        }

        for(int i = 0; i < columns.length; i++) {
            for (int j = 0; j < columns.length; j++)
            {
                System.out.print('(');
                for(int k = 0; k < 4; k++)
                    System.out.print(counts[i][j][k] + ",");
                System.out.print(") ");
            }
            System.out.println();
        }
    }

    public long getHash()
    {
        if(hashNeedsRecalculation) {
            hashCode = 1234567890123456789l;
            int l = bits.length;

            //Idea is that it doesn't matter if this is the inverse of something else. They should end up having the same hash code.
            if (bits[0] < 0) {
                for (int i = 0; i < l-1; i++)
                    hashCode = MathBox.murmur64(hashCode) + (~bits[i] * 31);
                long offMask = (bitLength & 63)==0 ? Long.MAX_VALUE : (1L << (bitLength & 63))-1;
                hashCode = MathBox.murmur64(hashCode) + ((~bits[l-1] & offMask) * 31);
            } else {
                for (int i = 0; i < l; i++)
                    hashCode = MathBox.murmur64(hashCode) + (bits[i] * 31);
            }

            hashNeedsRecalculation = false;
        }
        return hashCode;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(getHash());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof BitArray)
        {
            BitArray other = (BitArray)obj;
            return getHash() == other.getHash();
        }
        return false;
    }

    public boolean exactEquals(BitArray other)
    {
        if(other.getBitLength() != this.getBitLength())
            return false;

        for(int i = 0; i < bits.length; i++)
            if(this.bits[i] != other.bits[i])
                return false;

        return true;
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(tag);
        outputStream.writeInt(bitLength);
        for(int i = 0; i < bits.length; i++)
            outputStream.writeLong(bits[i]);
    }

    public static BitArray read(DataInputStream inputStream) throws IOException {
        String tag = inputStream.readUTF();
        int bitLength = inputStream.readInt();
        int longLength = (bitLength >>> 6) + ((bitLength & 63) > 0 ? 1 : 0);
        long[] bits = new long[longLength];
        for(int i = 0; i < longLength; i++)
            bits[i] = inputStream.readLong();

        return new BitArray(bitLength, bits, tag);
    }

    public int getBitLength()
    {
        return bitLength;
    }
}
