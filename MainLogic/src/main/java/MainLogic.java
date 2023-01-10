import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.BasicConfigurator;

import java.util.Random;

public class MainLogic {
    public static String BUCKET_PATH = "s3n://assignment3gy";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("dpMin argument is missing");
            return;
        }
        String dpMin = args[0];
        String randomId = RandomStringUtils.random(7, true, true);

        BasicConfigurator.configure();
        AmazonElasticMapReduce mapReduce = AmazonElasticMapReduceClient.builder().withRegion(Regions.US_EAST_1).build();

        HadoopJarStepConfig hadoopJarStep1 = new HadoopJarStepConfig()
                .withJar(BUCKET_PATH + "/PatternParser.jar")
                .withMainClass("PatternParser")
                .withArgs(randomId, dpMin);
        StepConfig stepConfig1 = new StepConfig()
                .withName("PatternParser")
                .withHadoopJarStep(hadoopJarStep1)
                .withActionOnFailure("TERMINATE_JOB_FLOW");

        HadoopJarStepConfig hadoopJarStep2 = new HadoopJarStepConfig()
                .withJar(BUCKET_PATH + "/FeaturesVectorBuilder.jar")
                .withMainClass("FeaturesVectorBuilder")
                .withArgs(new String[] {randomId});
        StepConfig stepConfig2 = new StepConfig()
                .withName("FeaturesVectorBuilder")
                .withHadoopJarStep(hadoopJarStep2)
                .withActionOnFailure("TERMINATE_JOB_FLOW");

        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(7)
                .withMasterInstanceType(InstanceType.M4Large.toString())
                .withSlaveInstanceType(InstanceType.M4Large.toString())
                .withHadoopVersion("2.10.1")
                .withEc2KeyName("Assignment3-Key-Pair")
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1a"));

        RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                .withName("HypernymDetection")
                .withReleaseLabel("emr-5.20.0")
                .withInstances(instances)
                .withSteps(stepConfig1, stepConfig2)
                .withLogUri(BUCKET_PATH + "/logs/"  + randomId)
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withServiceRole("EMR_DefaultRole");

        RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
        String jobFlowId = runJobFlowResult.getJobFlowId();
        System.out.println("Ran job flow with id: " + jobFlowId + "and random id: " + randomId);
    }
}
