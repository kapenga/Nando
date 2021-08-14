package bri;

//Boolean Rule Induction with Nands

import logic.BitArray;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

public class Nand {
    private static final int MAXDEPTH = 100;
    private static final int MAXAGE = 1;

    int bitsIndex;
    Nand a;
    Nand b;
    int complexity;
    int age;
    BitArray bits;

    Nand(int bitsIndex)
    {
        this.bitsIndex = bitsIndex;
        complexity = 1;
        age = 0;
    }

    private static int getBestOption(BitArray cares, BitArray target, BitArray[] inputs, int[] indices, Random random, int ignoreIndex, int maxScore) {
        int bestScore = maxScore+1;
        int bestOption = Integer.MIN_VALUE;

        if(ignoreIndex < 0)
            ignoreIndex = (-ignoreIndex) - 1;

        for(int i = indices.length-1; i > -1; i--)
        {
            //Randomizing the array, getting random items
            int rIndex = random.nextInt(i+1);
            if(rIndex != i)
            {
                int t = indices[rIndex];
                indices[rIndex] = indices[i];
                indices[i] = t;
            }

            int index = indices[i];

            if(ignoreIndex == index)
                continue;
            BitArray input = inputs[index];
            int[] score = BitArray.getDualScore(input, target, cares, bestScore);
            int minScore = Math.min(score[0], score[1]);

            if (minScore < bestScore) {
                bestScore = minScore;
                bestOption = score[1] < score[0] ? -(index + 1) : index;
                if (bestScore < 1)
                    break;
            }
        }

        return bestOption;
    }

    public static Nand expand(BitArray target, BitArray[] inputs)
    {
        BitArray[] invertedInputs = new BitArray[inputs.length];
        int[] indices = new int[inputs.length];
        for(int i = 0; i < invertedInputs.length; i++) {
            invertedInputs[i] = BitArray.invert(inputs[i]);
            indices[i] = i;
        }

        Random random = new Random();

        BitArray cares = BitArray.createEmptyArray(target, true);

        Instant start = Instant.now();
        BitArray invertedTarget = BitArray.invert(target);
        int bestOption = getBestOption(cares, target, inputs, indices, random, Integer.MIN_VALUE, target.getBitLength());
        Nand root = new Nand(bestOption);
        int score = BitArray.getScore(bestOption >= 0 ? inputs[bestOption] : invertedInputs[(-bestOption) - 1], target, cares, target.getBitLength());
        int loops = 0;
        int noChanges = 0;
        while(score > 0 || (score == 0 && noChanges < 100000))
        {
            int complexity = root.complexity;
            root.expand(cares, target, invertedTarget, indices, random, inputs, invertedInputs, 0);
            score = BitArray.getScore(root.bits, target, cares, Integer.MAX_VALUE);
            if(score < 1)
            {
                if(root.complexity < complexity)
                    noChanges = 0;
                else
                    noChanges++;
            }
            Duration d = Duration.between(start, Instant.now());
            System.out.println(d.toString().substring(2) + " - #" + (++loops) + " score: " + score + " complexity: " + root.complexity + " " + root.bits.getHash());
        }
        Instant finish = Instant.now();
        Duration d = Duration.between(start, finish);
        System.out.println("Done in " + d.toString());

        return root;
    }

    BitArray getBits(BitArray[] inputs, BitArray[] invertedInputs)
    {
        return bitsIndex >= 0 ? inputs[bitsIndex] : invertedInputs[(-bitsIndex) - 1];
    }

    void expand(BitArray cares, BitArray target, BitArray invertedTarget, int[] indices, Random random, BitArray[] inputs, BitArray[] invertedInputs, int depth) {
        if(depth > MAXDEPTH)
            return;
        age++;

        if(a == null) {
            bits = getBits(inputs, invertedInputs);
            int startScore = BitArray.getScore(bits, target, cares, 0);

            if(startScore > 0 || age > 30) {
                startScore = BitArray.getScore(bits, target, cares, target.getBitLength());
                //And
                int aIndex = (-bitsIndex) - 1;
                BitArray andBits = aIndex >= 0 ? inputs[aIndex] : invertedInputs[(-aIndex)-1];
                BitArray andCares = BitArray.and(cares, andBits); //Invert for getting the best b index
                int andBArrayIndex = getBestOption(andCares, invertedTarget, inputs, indices, random, bitsIndex, Math.min(andCares.getTrueCount(), startScore));

                if (andBArrayIndex != Integer.MIN_VALUE) {
                    BitArray andBArray = andBArrayIndex >= 0 ? inputs[andBArrayIndex] : invertedInputs[(-andBArrayIndex) - 1];
                    BitArray nandArray = BitArray.nand(andBits, andBArray);
                    int nandScore = BitArray.getScore(nandArray, target, cares, startScore);

                    if ((nandScore <= startScore || (startScore > 0 && age > MAXAGE))) {
                        a = new Nand(aIndex);
                        a.bits = andBits;

                        b = new Nand(andBArrayIndex);
                        b.bits = b.getBits(inputs, invertedInputs);

                        complexity = 3;
                        age = 0;

                        bits = nandArray;
                    }
                }
            }
            else
            {
                int newBitIndex = getBestOption(cares, target, inputs, indices, random, bitsIndex, 0);
                if(newBitIndex != Integer.MIN_VALUE) {
                    bitsIndex = newBitIndex;
                    bits = getBits(inputs, invertedInputs);
                    age = 0;//?
                }
            }
        }
        else {
            if (random.nextBoolean()) {
                Nand t = a;
                a = b;
                b = t;
            }

            BitArray caresA = BitArray.and(cares, b.bits);
            BitArray caresB = BitArray.and(cares, a.bits);

            BitArray caresToAdd = BitArray.xor(caresB, cares);
            caresA = BitArray.or(caresA, caresToAdd);
            a.expand(caresA, invertedTarget, target, indices, random, inputs, invertedInputs, depth + 1);

            caresB = BitArray.and(cares, a.bits);
            b.expand(caresB, invertedTarget, target, indices, random, inputs, invertedInputs, depth + 1);

            bits = BitArray.nand(a.bits, b.bits);
            complexity = a.complexity + b.complexity + 1;

            if (complexity < 16)
            {
                int secondScore = BitArray.getScore(bits, target, cares, 0);
                if(secondScore < 1) {
                    int newBitIndex = getBestOption(cares, target, inputs, indices, random, Integer.MIN_VALUE,  0);
                    if (newBitIndex != Integer.MIN_VALUE) {
                        bitsIndex = newBitIndex;
                        a = null;
                        b = null;
                        age = 0;
                        complexity = 1;
                        bits = getBits(inputs, invertedInputs);
                    }
                }
            }
        }
    }
}
