package tests;



import bri.Nand;
import logic.TruthTable;

import java.util.Random;

public class RandomTest {

    private static final int size = 14;

    public RandomTest() throws Exception {
        String[] inputTags = new String[size];
        for(int i = 0; i < inputTags.length; i++)
            inputTags[i] = "i" + i;
        TruthTable table = new TruthTable(inputTags,new String[]{"Random"}, 1 << size, "Random");
        Random random = new Random();

        for(int i = 0; i < (1 << size); i++)
        {
            boolean[] inputs = new boolean[size];
            for(int j = 0; j < size; j++) {
                if (((i >>> j) & 1) == 1) {
                    inputs[j] = true;
                }
            }

            table.addCase(inputs, new boolean[]{ random.nextBoolean() });
        }



        //Contexter3 contexter = new Contexter3(table, 0);
//
//        for(int i = 0; i < 100000; i++)
//            contexter.tryThing3();

        //for(int i =0; i < 1000000; i++) {
        Nand.expand(table.getOutputColumns()[0],table.getInputColumns());
        //}
    }

}
