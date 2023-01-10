import com.google.common.base.Joiner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.tartarus.snowball.ext.englishStemmer;
import utils.FeaturesVectorLength;
import utils.PairOfNouns;
import utils.PatternInfo;

import java.io.IOException;

/***
 * * The FeaturesVectorBuilder job is responsible for the following:
 *     1. Parse the data from the annotated set.
 *     2. Stem the pair.
 *     3. Aggregate the annotated set data & the PatternParser's output.
 *     4. Create a features vector for each noun pair
 */
public class FeaturesVectorBuilder {

    /***
     * * Map every line of PatternsParser's output into <pairOfNouns(w1, w2, false, total), patternIndex>
     */
    public static class MapperClass extends Mapper<PairOfNouns, IntWritable, PairOfNouns, PatternInfo> {

        @Override
        public void map(PairOfNouns key, IntWritable value, Context context) throws IOException, InterruptedException {
            context.write(new PairOfNouns(key.getWord1(), key.getWord2(),
                            new BooleanWritable(false), new IntWritable(key.getTotal().get())),
                    new PatternInfo(value, key.getTotal()));
        }
    }

    /***
     * * Map every line of the annotated set into <pairOfNouns(w1, w2, isHypernym, -1), -1>
     */
    public static class MapperClassAnnotated extends Mapper<LongWritable, Text, PairOfNouns, PatternInfo> {
        private final englishStemmer englishStemmer = new englishStemmer();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] arr = value.toString().split("\t");

            englishStemmer.setCurrent(arr[0]);
            englishStemmer.stem();
            String word1 = englishStemmer.getCurrent();

            englishStemmer.setCurrent(arr[1]);
            englishStemmer.stem();
            String word2 = englishStemmer.getCurrent();

            BooleanWritable isHypernym = new BooleanWritable(Boolean.parseBoolean(arr[2]));
            PairOfNouns pairOfNouns =
                    new PairOfNouns(new Text(word1), new Text(word2), isHypernym, new IntWritable(-1));
            context.write(pairOfNouns, new PatternInfo(new IntWritable(-1), new IntWritable(-1)));
        }
    }


    /***
     * * Defines the partition policy of sending the key-value the Mapper created to the reducers. The annotated pair
     * * should arrive before PatternsParser's output
     */
    public static class PartitionerClass extends Partitioner<PairOfNouns, PatternInfo> { // TODO: remove?
        public int getPartition(PairOfNouns key, PatternInfo value, int numPartitions) {
            return Math.abs(key.getWord1().toString().hashCode() + key.getWord2().toString().hashCode()) %
                    numPartitions;
        }
    }

    // TODO
    public static class ReducerClass extends Reducer<PairOfNouns, PatternInfo, Text, Text> {
        private PairOfNouns currPairOfNouns;
        private int featuresVectorLength;

        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            this.currPairOfNouns = null;
            this.featuresVectorLength = context.getConfiguration().getInt("Length", 0);
        }

        public void reduce(PairOfNouns key, Iterable<PatternInfo> values,
                           Context context) throws IOException, InterruptedException {
            if (key.getTotal().get() == -1) { // annotated pair
                currPairOfNouns = key;
            } else if (this.currPairOfNouns != null && currPairOfNouns.getWord1().equals(key.getWord1()) &&
                    // PatternsParser's output
                    currPairOfNouns.getWord2().equals(key.getWord2())) {

                Long[] featuresVector = new Long[this.featuresVectorLength]; // initialize features vector
                for (int i = 0; i < this.featuresVectorLength; i++) {
                    featuresVector[i] = 0L;
                }

                for (PatternInfo patternInfo : values) { // fill features vector with pattern occurrences
                    if (patternInfo.getPatternIndex().get() < featuresVectorLength) {
                        featuresVector[patternInfo.getPatternIndex().get()] += patternInfo.getOccurrences().get();
                    }
                }

                context.write(new Text(String.format("%s %s",
                                currPairOfNouns.getWord1().toString(), currPairOfNouns.getWord2().toString())),
                        new Text(String.format("Hypernym: %s featuresVector: %s",
                                currPairOfNouns.getIsHypernym().get(), Joiner.on(",").join(featuresVector))));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        long featuresVectorLength = FeaturesVectorLength.getInstance().getLength();
        conf.setLong("Length", featuresVectorLength);
        Job job = Job.getInstance(conf, "FeaturesVectorBuilder");
        job.setJarByClass(FeaturesVectorBuilder.class);
        job.setNumReduceTasks(1);
        MultipleInputs.addInputPath(job, new Path(.BUCKET_PATH + "/Step1"), SequenceFileInputFormat.class,
                MapperClass.class);
        MultipleInputs.addInputPath(job, new Path(MainLogic.BUCKET_PATH + "/ANNOTATED_SET"), TextInputFormat.class, MapperClassAnnotated.class);
        job.setReducerClass(ReducerClass.class);
        job.setMapOutputKeyClass(PairOfNouns.class);
        job.setMapOutputValueClass(PatternInfo.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileOutputFormat.setOutputPath(job, new Path(MainLogic.BUCKET_PATH + "/Step2"));
        job.setOutputFormatClass(TextOutputFormat.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }


}