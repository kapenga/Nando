package tests;

import bri.Nand;
import logic.TruthTable;

public class Partition {

    private static final int PartitionSize = 15;
    private static final int Partitions = 5;

    public Partition() throws Exception {
        String[] inputTags = new String[PartitionSize];
        for(int i = 0; i < inputTags.length; i++)
            inputTags[i] = "i" + i;
        TruthTable table = new TruthTable(inputTags,new String[]{"IsParity"}, 1 << PartitionSize, "Partition");

        int partitionBits = PartitionSize / Partitions;
        int[] values = new int[Partitions];
        int mask = (1 << partitionBits) - 1;

        for(int i = 0; i < (1 << PartitionSize); i++)
        {
            boolean[] inputs = new boolean[PartitionSize];
            for(int j = 0; j < PartitionSize; j++) {
                if (((i >>> j) & 1) == 1) {
                    inputs[j] = true;
                }
            }
            for(int j = 0; j < Partitions; j++)
            {
                values[j] = (i >>> (j * partitionBits)) & mask;
            }

            table.addCase(inputs, new boolean[]{isPartition(values)});
        }



        //Contexter3 contexter = new Contexter3(table, 0);
//
//        for(int i = 0; i < 100000; i++)
//            contexter.tryThing3();

        //for(int i =0; i < 1000000; i++) {
        Nand.expand(table.getOutputColumns()[0],table.getInputColumns());
        //}
    }

    private boolean isPartition(int[] values)
    {
        int sum = values[0];
        for(int i = 1; i < values.length; i++)
            sum += values[i];
        int half = sum >>> 1;

        for(int i = 1; i < (1 << values.length); i++)
        {
            int s = 0;
            for(int j = 0; j < values.length; j++)
            {
                if(((i >> j) & 1) == 1)
                    s += values[j];
            }
            if(s == half)
                return true;
        }

        return false;
    }

}
