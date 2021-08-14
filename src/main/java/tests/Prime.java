package tests;

import bri.Nand;
import logic.TruthTable;

public class Prime {

    private static final int PrimeSize = 15;
    public Prime() throws Exception {
        String[] inputTags = new String[PrimeSize];
        for (int i = 0; i < inputTags.length; i++)
            inputTags[i] = "i" + i;
        TruthTable table = new TruthTable(inputTags, new String[]{"IsPrime"}, 1 << PrimeSize, "Primes");

        for (int i = 0; i < (1 << PrimeSize); i++) {
            boolean[] inputs = new boolean[PrimeSize];
            for (int j = 0; j < PrimeSize; j++)
                if (((i >> j) & 1) == 1)
                    inputs[j] = true;


            table.addCase(inputs, new boolean[]{isPrime(i)});
        }

        Nand.expand(table.getOutputColumns()[0], table.getInputColumns());
    }

    private static boolean isPrime(int p)
    {
        if(p == 0)
            return false;
        if(p < 4)
            return true;
        for(int j = 2; j*j <= p; j++)
        {
            if((p % j) == 0)
                return false;
        }

        return true;
    }
}
