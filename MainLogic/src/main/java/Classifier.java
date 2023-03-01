import java.io.BufferedReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import weka.classifiers.Evaluation;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

public class Classifier {
    private static String[] features;
    private static String dpMin = "100";


    public static void writePrint (PrintWriter printWriter) {
        printWriter.println("@Relation hypernyms");
        printWriter.println("@Attribute isHypernym {true,false}");
        printWriter.println("@Attribute word1 string");
        printWriter.println("@Attribute word2 string");
    }
    public static void parsingOutput() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("wekainput.arff", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writePrint(writer);


        try (BufferedReader br = new BufferedReader(new FileReader(new File("results")))) {
            String row = br.readLine();
            String[] splitRow = row.split("\t");
            String[] commas = splitRow[1].split(",");
            for (int index = 1; index < commas.length; index++)
                writer.println("@ATTRIBUTE pattern" + index + " NUMERIC");
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        writer.println();
        writer.println("@Data");

        try (BufferedReader br = new BufferedReader(new FileReader(new File("results")))) {
            String row;
            while ((row = br.readLine()) != null) {
                String[] splitter = row.split("\t");
                String[] commas = splitter[1].split(",");
                String[] words = splitter[0].split(" ");
                String res = "{0" + " " + commas[0] + ", 1 \"" + words[0] + "\", 2 \"" + words[1] + "\",";

                for (int index = 1; index < commas.length; index++) {
                    if (!commas[index].equals("0"))
                        res += index + 2 + " " + commas[index] + ",";
                }
                writer.println(res.substring(0, res.length() - 1) + "}");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addInstanceCond (List<Instance> list, Instance instance) {
        if (list.size() < 10) {
            list.add(instance);
        }
    }

    public static void parsing(String str) {
        String[] splitted = str.split(",");
        for (int i = 3; i < splitted.length; i++) {
            String indexFromString = splitted[i].split(" ")[0];
            int index = Integer.parseInt(indexFromString) - 3;
            System.out.println(features[index]);
        }
        System.out.println();
    }

    public static void analyzingData () {
        try {
            DataSource source = new DataSource("wekainput.arff");
            Instances data = source.getDataSet();
            data.setClassIndex(0);
            weka.classifiers.Classifier classifier;
            classifier = new BayesNet();
            Remove rm = new Remove();
            rm.setAttributeIndices("2-3");

            // META
            FilteredClassifier fc = new FilteredClassifier();
            fc.setFilter(rm);
            fc.setClassifier(classifier);
            fc.buildClassifier(data);

            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(fc, data, 10, new Random(1));
            System.out.println(eval.toSummaryString());
            System.out.println(eval.toMatrixString());
            System.out.println(eval.toClassDetailsString());
            System.out.println("FMeasure: " + eval.fMeasure(0) + "\nPrecision: " + eval.precision(0) + "\nRecall: " + eval.recall(0));
            Instance instance, lastInstance = (Instance) data.lastInstance();
            boolean correctClassValue, predictedClassValue;
            List<Instance>
                    tp = new ArrayList<>(),
                    tn = new ArrayList<>(),
                    fp = new ArrayList<>(),
                    fn = new ArrayList<>();

            int i = 0;
            instance = (Instance) data.instance(i);
            while ((tp.size() < 10 || tn.size() < 10 || fp.size() < 10 || fn.size() < 10) && !instance.equals(lastInstance)) {
                correctClassValue = instance.classValue() == 0.0 ? true : false;
                instance.setClassMissing();
                predictedClassValue = fc.classifyInstance(instance) == 0.0 ? true : false;
                if (predictedClassValue == true) { // classified as true
                    if (correctClassValue == true) {
                        addInstanceCond(tp, instance);
                    } else {
                        addInstanceCond(fp, instance);
                    }
                } else {
                    if (correctClassValue == true) {
                        addInstanceCond(fn, instance);
                    } else {
                        addInstanceCond(tn, instance);
                    }
                }
                i++;
                instance = data.instance(i);
            }
            for (Instance s : tp) {
                parsing(s.toString());
            }
            for (Instance s : tn) {
                parsing(s.toString());
            }
            for (Instance s : fp) {
                parsing(s.toString());
            }
            for (Instance s : fn) {
                parsing(s.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] getFeatures() throws IOException {
        String fileName = "features" + dpMin + ".txt";
        AmazonS3 s3 = (AmazonS3) ((AmazonS3ClientBuilder) AmazonS3Client.builder().withRegion(Regions.US_EAST_1)).build();
        S3Object s3Object = s3.getObject(MainLogic.BUCKET_PATH, fileName);
        InputStream inputStream = s3Object.getObjectContent();
        OutputStream outputStream = Files.newOutputStream(Paths.get(fileName));
        IOUtils.copy(inputStream, outputStream);

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            return lines.toArray(new String[lines.size()]);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        features = getFeatures();
        parsingOutput();
        analyzingData();
    }
}
