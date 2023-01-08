import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.tartarus.snowball.ext.englishStemmer;
import utils.PairOfNouns;
import utils.PatternInfo;

import java.io.IOException;

/***
 * * The FeaturesVectorBuilder job is responsible for the following:
 *     1. Parse the data from the annotated set.
 *     2. Stem the pair.
 *     3. Aggregate the annotated set data & the PatternParser's output.
 *     4.
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

            BooleanWritable isHypernym = new BooleanWritable(Boolean.parseBoolean(arr[2]);
            PairOfNouns pairOfNouns =
                    new PairOfNouns(new Text(word1), new Text(word2), isHypernym, new IntWritable(-1));
            context.write(pairOfNouns, new PatternInfo(new IntWritable(-1), new IntWritable(-1)));
        }
    }


    /***
     * * Defines the partition policy of sending the key-value the Mapper created to the reducers.
     */
    public static class PartitionerClass extends Partitioner<PairOfNouns, PatternInfo> { // TODO: remove?
        public int getPartition(PairOfNouns key, PatternInfo value, int numPartitions) {
            return Math.abs(key.getWord1().toString().hashCode() + key.getWord2().toString().hashCode()) %
                    numPartitions;
        }
    }

    // TODO
    public static class ReducerClass extends Reducer<PairOfNouns, PatternInfo, Text, Text> {
        private String BUCKET_NAME;
        private PairOfNouns currPairOfNouns = null;
        int totalFeatures;

        @Override
        public void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            BUCKET_NAME = context.getConfiguration().get("BUCKET_NAME");
            S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(BUCKET_NAME)
                    .prefix("totals/")
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            for (ListIterator iterVals = objects.listIterator(); iterVals.hasNext(); ) {
                S3Object myValue = (S3Object) iterVals.next();
                String[] fileName = myValue.key().split("_");
                if (fileName.length > 2)
                    this.totalFeatures = Integer.parseInt(fileName[1]);
            }
        }

        public void reduce(PairOfNouns key, Iterable<PatternInfo> values, Context context) throws IOException, InterruptedException {
            if (key.getTotal().get() == -1) { // annotated pair
//                this.pairOfNouns =  new PairOfNouns(key.getWord1(), key.getWord2(), key.getIsHypernym(), new
//                IntWritable(-1));
                this.currPairOfNouns = key;
            }
            if (this.currPairOfNouns == null || !key.getWord1().toString().equals(this.currPairOfNouns.getWord1().toString()) || !key.getWord2().toString().equals(this.currPairOfNouns.getWord2().toString())) {
                System.err.println("NEED TO RETURN");
                return;
            }
            System.err.println("FILL VECTOR of " + key.getW1() + " " + key.getW2());
            Long[] featuresVector = new Long[this.totalFeatures];
            for (int i = 0; i < this.totalFeatures; i++) {
                featuresVector[i] = new Long(0);
            }
            for (TotalPatternPair pattern : values) {
                System.err.println("PATTERN " + pattern.getPattern().get());
                System.err.println("TOTAL COUNT " + pattern.getTotal_count().get());
                featuresVector[pattern.getPattern().get()] += pattern.getTotal_count().get();
            }
            Text val = new Text(StringUtils.join(featuresVector, ','));
            System.err.println("WRITE " + key.toString() + " CURPAIR: " + this.currPairOfNouns.toString());
            context.write(new Text(key.getW1().toString() + " " + key.getW2().toString()), new Text(this.currPairOfNouns.getIsHypernym().get() + "," + val));
        }
    }

    public static void main(String[] args) throws Exception {
    }


    }