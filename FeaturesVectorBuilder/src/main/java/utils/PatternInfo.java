package utils;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PatternInfo implements Writable {
    private IntWritable patternIndex;
    private IntWritable occurrences;

    public PatternInfo() {
        this.patternIndex = new IntWritable();
        this.occurrences = new IntWritable();
    }

    public PatternInfo(IntWritable pattern, IntWritable total) {
        this.patternIndex = pattern;
        this.occurrences = total;
    }

    public void write(DataOutput dataOutput) throws IOException {
        occurrences.write(dataOutput);
        patternIndex.write(dataOutput);
    }

    public void readFields(DataInput dataInput) throws IOException {
        occurrences.readFields(dataInput);
        patternIndex.readFields(dataInput);
    }

    public IntWritable getPatternIndex() {
        return patternIndex;
    }

    public void setPatternIndex(IntWritable patternIndex) {
        this.patternIndex = patternIndex;
    }

    public IntWritable getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(IntWritable occurrences) {
        this.occurrences = occurrences;
    }
}
