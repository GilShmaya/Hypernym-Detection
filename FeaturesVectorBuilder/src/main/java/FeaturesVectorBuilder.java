import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

public class FeaturesVectorBuilder{
    public static class MapperClassAnnotated extends Mapper<LongWritable, Text, PairOfNouns, TotalPatternPair> {

        @Override
        public void map(LongWritable key, Text value, Context context) {

}