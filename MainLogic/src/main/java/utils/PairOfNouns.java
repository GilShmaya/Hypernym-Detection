package utils;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PairOfNouns implements WritableComparable<PairOfNouns> {
    private Text word1;
    private Text word2;
    private BooleanWritable isHypernym;
    private IntWritable total;

    public PairOfNouns() {
        this.word1 = new Text();
        this.word2 = new Text();
        this.isHypernym = new BooleanWritable();
        this.total = new IntWritable();
    }

    public PairOfNouns(Text word1, Text word2, IntWritable total) {
        this.word1 = word1;
        this.word2 = word2;
        this.isHypernym = new BooleanWritable(false);
        this.total = new IntWritable(total.get());
    }

    public int compareTo(PairOfNouns o) {
        int c1, c2;
        if ((c1 = compareText(word1, o.getWord1())) == 0) {
            if ((c2 = compareText(word2, o.getWord2())) == 0) {
                if (total.get() == -1)
                    return -1;
                else if (o.total.get() == -1)
                    return 1;
            }
            return c2;
        }
        return c1;
    }

    public int compareText(Text w1, Text w2) {
        return w1.toString().compareTo(w2.toString());
    }

    public void write(DataOutput dataOutput) throws IOException {
        word1.write(dataOutput);
        word2.write(dataOutput);
        isHypernym.write(dataOutput);
        total.write(dataOutput);
    }

    public void readFields(DataInput dataInput) throws IOException {
        word1.readFields(dataInput);
        word2.readFields(dataInput);
        isHypernym.readFields(dataInput);
        total.readFields(dataInput);

    }

    public Text getWord1() {
        return word1;
    }

    public void setWord1(Text word1) {
        this.word1 = word1;
    }

    public Text getWord2() {
        return word2;
    }

    public void setWord2(Text word2) {
        this.word2 = word2;
    }

    public BooleanWritable getIsHypernym() {
        return isHypernym;
    }

    public void setIsHypernym(BooleanWritable isHypernym) {
        this.isHypernym = isHypernym;
    }

    public IntWritable getTotal() {
        return total;
    }

    public void setTotal(IntWritable total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%b", word1, word2, total, isHypernym);
    }
}
