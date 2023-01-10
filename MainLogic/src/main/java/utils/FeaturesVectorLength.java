package utils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.log4j.BasicConfigurator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FeaturesVectorLength {
    private static AmazonS3 s3;
    private static final String bucket = "assignment3gy";
    private static final String fileName = "FeaturesVectorLength.txt";

    public static FeaturesVectorLength getInstance() {
        return FeaturesVectorLength.singletonHolder.instance;
    }

    private static class singletonHolder {
        private static final FeaturesVectorLength instance = new FeaturesVectorLength();

        private singletonHolder() {
        }
    }

    private FeaturesVectorLength() {
        BasicConfigurator.configure();
        s3 = (AmazonS3) ((AmazonS3ClientBuilder) AmazonS3Client.builder().withRegion(Regions.US_EAST_1)).build();
    }

    public int getLength() throws Exception {
        S3Object s3Object = s3.getObject(bucket, fileName);
        InputStream objectContent = s3Object.getObjectContent();
        Scanner scanner = (new Scanner(objectContent)).useDelimiter("\\A");
        String text = scanner.hasNext() ? scanner.next() : "";
        String[] testArray = text.split("\\s+");
        if (testArray.length > 1) {
            throw new Exception("Expected to find only the value of N.");
        } else {
            return Integer.parseInt(testArray[0]);
        }
    }

    public void setLength(int N) {
        String intValue = Integer.toString(N);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(intValue.getBytes(StandardCharsets.UTF_8));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength((int) intValue.length());
        PutObjectRequest request = new PutObjectRequest(bucket, fileName, inputStream, metadata);
        s3.putObject(request);
    }
}