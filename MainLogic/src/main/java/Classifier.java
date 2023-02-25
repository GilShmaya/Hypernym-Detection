import java.io.BufferedReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

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
    private static String dpMin = "100"; // todo : check


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
            String[] split = row.split("\t");
            String[] commas = split[1].split(",");
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
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitter = line.split("\t");
                String[] commas = splitter[1].split(",");
                String[] words = splitter[0].split(" ");

                String res = "{0" + " " + commas[0] + ", 1 \"" + words[0] + "\", 2 \"" + words[1] + "\",";
//                writer.print("{");

                for (int i = 1; i < commas.length; i++) {
                    if (!commas[i].equals("0"))
                        res += i + 2 + " " + commas[i] + ",";
                }
                writer.println(res.substring(0, res.length() - 1) + "}");
//                writer.println(splitter[1]);
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // Add the instance only if the list size if smaller then 10
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
//          classifier = new ZeroR();
//          classifier = new RandomForest();
            classifier = new BayesNet();
//          classifier = new J48();

//            ((RandomForest) classifier).setNumIterations(10);
            Remove rm = new Remove();
            rm.setAttributeIndices("2-3");  // remove words attribute

            // meta-classifier
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

            // Use the last classifier (from the 10th fold) to classify,
            // and fetch 10 pairs from each TP/TN/FP/FN instance.
            Instance instance, lastInstance = (Instance) data.lastInstance();
            boolean correctClassValue, predictedClassValue;
            List<Instance>
                    tp = new ArrayList<>(),
                    tn = new ArrayList<>(),
                    fp = new ArrayList<>(),
                    fn = new ArrayList<>();

            // Get the first instance
            int i = 0;
            instance = (Instance) data.instance(i);

            while ((tp.size() < 10 || tn.size() < 10 || fp.size() < 10 || fn.size() < 10) && !instance.equals(lastInstance)) {
                //correct class value.

                correctClassValue = instance.classValue() == 0.0 ? true : false;
                instance.setClassMissing();

                predictedClassValue = fc.classifyInstance(instance) == 0.0 ? true : false;


                if (predictedClassValue == true) { // classified as true
                    if (correctClassValue == true) {
                        // TP
                        addInstanceCond(tp, instance);
                    } else {
                        // FP
                        addInstanceCond(fp, instance);
                    }
                } else {                           // classified as false
                    if (correctClassValue == true) {
                        // FN
                        addInstanceCond(fn, instance);
                    } else {
                        // TN
                        addInstanceCond(tn, instance);
                    }
                }

                // Get the next instance
                i++;
                instance = data.instance(i);
            }

            // Print tp/tn/fp/fn lists.
            // System.out.println("~~~TP:~~~"); todo: delete when finish running
            for (Instance s : tp) {
               //  System.out.println("word1: " + s.stringValue(1) + " word2: " + s.stringValue(2)); todo: delete when finish running
                parsing(s.toString());
            }
            System.out.println("\n~~~TN:~~~");
            for (Instance s : tn) {
                // System.out.println("word1: " + s.stringValue(1) + " word2: " + s.stringValue(2)); todo: delete when finish running
                parsing(s.toString());
            }

            System.out.println("\n~~~FP:~~~");
            for (Instance s : fp) {
                // System.out.println("word1: " + s.stringValue(1) + " word2: " + s.stringValue(2)); todo: delete when finish running
                parsing(s.toString());
            }

            System.out.println("\n~~~FN:~~~");
            for (Instance s : fn) {
                // System.out.println("word1: " + s.stringValue(1) + " word2: " + s.stringValue(2)); todo: delete when finish running
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

//        s3Object.getObjectContent().transferTo(Files.newOutputStream(Paths.get(fileName)));
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
        // System.out.println("Num of features: " + features.length); todo: delete when finish running
        parsingOutput();
        // System.out.println("Analyzing..."); todo: delete when finish running
        analyzingData();
    }
}