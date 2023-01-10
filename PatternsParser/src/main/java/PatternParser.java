import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import utils.FeaturesVectorLength;
import utils.PairOfNouns;

public class PatternParser {


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.set("dpMin", args[1]); // TODO: see reference in FeaturesVectorBuilder line 93 for extract this data
        Job job = Job.getInstance(conf, "PatternParser");
        job.setJarByClass(PatternParser.class);
        job.setNumReduceTasks(1);
        job.setReducerClass(ReducerClass.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(PairOfNouns.class);
        job.setOutputKeyClass(PairOfNouns.class);
        job.setOutputValueClass(IntWritable.class);
        MultipleInputs.addInputPath(job,new Path(MainLogic.BUCKET_PATH + "training_input1"), TextInputFormat.class, //
                // TODO: change the training file name?
                MapperClass.class);
        MultipleInputs.addInputPath(job,new Path(MainLogic.BUCKET_PATH + "training_input2"), TextInputFormat.class,
                MapperClass.class);                 // TODO: change the training file name?
        FileOutputFormat.setOutputPath(job, new Path(MainLogic.BUCKET_PATH + "/Step1"));
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        if (job.waitForCompletion(true)) {
            Counters counters = job.getCounters();
            Counter counter = counters.findCounter(PatternParser.ReducerClass.Counter.N); // TODO: change
            FeaturesVectorLength.getInstance().setLength((int) counter.getValue());
            System.exit(0);
        }
        System.exit(1);    }
}
