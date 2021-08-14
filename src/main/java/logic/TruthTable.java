package logic;

import java.io.*;
import java.util.HashSet;

public class TruthTable {

    private final BitArray[] inputColumns;
    private final BitArray[] outputColumns;
    private final String tag;
    private final int caseCount;

    private int caseInsertIndex;
    private HashSet<BitArray> uniqueCases;

    public TruthTable(String[] inputColumnTags, String[] outputColumnTags, int caseCount, String tag) {
        inputColumns = new BitArray[inputColumnTags.length];
        for (int i = 0; i < inputColumnTags.length; i++)
            inputColumns[i] = BitArray.create(caseCount, inputColumnTags[i]);
        outputColumns = new BitArray[outputColumnTags.length];
        for (int i = 0; i < outputColumnTags.length; i++)
            outputColumns[i] = BitArray.create(caseCount, outputColumnTags[i]);
        this.tag = tag;
        this.caseCount = caseCount;

        uniqueCases = new HashSet<>();
        caseInsertIndex = 0;
    }

    public void addCase(boolean[] inputs, boolean[] outputs) throws Exception {
        if(inputs.length != inputColumns.length || outputs.length != outputColumns.length)
            throw new Exception("Inputs and/or outputs are not equal to the given inputs and/or outputs.");

        if(caseInsertIndex >= caseCount)
            throw new Exception("Trying to add a case to an already full TruthTable.");

        BitArray currentCase = BitArray.create(inputs.length, "input_" + caseInsertIndex);
        for(int i = 0; i < inputColumns.length; i++)
        {
            if(inputs[i]) {
                inputColumns[i].setBit(caseInsertIndex);
                currentCase.setBit(i);
            }
        }

        for(int i = 0; i < outputColumns.length; i++)
            if(outputs[i])
                outputColumns[i].setBit(caseInsertIndex);

        if(uniqueCases.contains(currentCase))
            throw new Exception("Cases are not unique!");
        uniqueCases.add(currentCase);

        caseInsertIndex++;
        //The job of this object is done.
        if(caseInsertIndex == caseCount)
            uniqueCases = null;
    }

    public void write(String filename) throws IOException {
        FileOutputStream fw = new FileOutputStream(new File(filename));

        DataOutputStream outputStream = new DataOutputStream(fw);
        write(outputStream);

        fw.close();
    }

    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(tag);
        outputStream.writeInt(caseCount);

        outputStream.writeInt(inputColumns.length);
        for(int i = 0; i < inputColumns.length; i++)
            inputColumns[i].write(outputStream);

        outputStream.writeInt(outputColumns.length);
        for(int i = 0; i < outputColumns.length; i++)
            outputColumns[i].write(outputStream);
    }

    public static TruthTable tryRead(String filename) throws IOException {
        File file = new File(filename);
        if(file.exists())
        {
            FileInputStream fs = new FileInputStream(file);
            DataInputStream inputStream = new DataInputStream(fs);
            TruthTable result = new TruthTable(inputStream);
            fs.close();
            return result;
        }
        return null;
    }

    public TruthTable(DataInputStream inputStream) throws IOException {
        tag = inputStream.readUTF();

        caseCount = inputStream.readInt();

        int inputColumnCount = inputStream.readInt();
        inputColumns = new BitArray[inputColumnCount];
        for(int i = 0; i < inputColumnCount; i++)
            inputColumns[i] = BitArray.read(inputStream);

        int outputColumnsCount = inputStream.readInt();
        outputColumns = new BitArray[outputColumnsCount];
        for(int i = 0; i < outputColumnsCount; i++)
            outputColumns[i] = BitArray.read(inputStream);
    }

    public BitArray[] getInputColumns()
    {
        return inputColumns;
    }

    public BitArray[] getOutputColumns()
    {
        return outputColumns;
    }

}
