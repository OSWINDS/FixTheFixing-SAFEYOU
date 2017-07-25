package evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.habernal.confusionmatrix.ConfusionMatrix;
import combiner.Preprocessor;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sentiment.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sifantid on 15/6/2017.
 */
public class Evaluator {

    private static HashMap<String,String> actualSentiment; // The actual sentiment of the dataset based on annotation
    private static HashMap<String,String> predictedSentiment; // The predicted sentiment of the dataset based on local algorithm
    private static Analysis analyzer; // Preprocessing class
    private static ConfusionMatrix cm;
    private static String datasetPath;
    private static String datasetPathEmotions;
    private static String datasetPathText;

    static {
        // Initialize Confusion Matrix
        cm = new ConfusionMatrix();

        actualSentiment = new HashMap<>();
        predictedSentiment = new HashMap<>();

        // Initialize the preprocessor class
        try {
            analyzer = new Analysis();
        } catch (IOException e) {
            e.printStackTrace();
        }

        datasetPath = "./resources/annotatedData.txt";
        datasetPathEmotions = "./resources/affectivetext_test.emotions.gold";
        datasetPathText = "./resources/affectivetext_test.xml";
    }

    /**
     * Step-by-step main class
     * @param args
     */
    public static void main(String[] args) {
        try {
            //getActualSentiment();
            getActualSentimentSemEval();
            calculatePredictedSentiment();
            fillConfusionMatrix();
            System.out.println("Accuracy: " + cm.getAccuracy());
            System.out.println("-------------------------------------------------");
            System.out.println("Precision per sentiment:");
            System.out.println(cm.getPrecisionForLabels());
            System.out.println();
            System.out.println("Average Precision: " + cm.getAvgPrecision());
            System.out.println("-------------------------------------------------");
            System.out.println("Recall per sentiment:");
            System.out.println(cm.getRecallForLabels());
            System.out.println();
            System.out.println("Average Recall: " + cm.getAvgRecall());
            System.out.println("-------------------------------------------------");
            System.out.println(cm.printNiceResults());
            System.out.println("-------------------------------------------------");
            System.out.println("Annotated dataset size: " + actualSentiment.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets relevant actual sentiment classes from annotated dataset SemEval (namely 6 basic emotions and neutral)
     */
    private static void getActualSentimentSemEval() {
        // Don't forget neutral emotion
        try {
            // The emotions file
            Scanner inputEmotions = new Scanner(new FileReader(datasetPathEmotions));
            // The text file
            File file = new File(datasetPathText);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);


            String id;
            String mainEmotion;
            String text;
            String[] line;
            while(inputEmotions.hasNext()) {
                List<Pair<String,Double>> emotions = new ArrayList<>();
                line = inputEmotions.nextLine().split(" ");

                // Get id
                id = line[0];
                // The emotions for the given id
                emotions.add(new Pair("anger",Double.valueOf(line[1])));
                emotions.add(new Pair("disgust",Double.valueOf(line[2])));
                emotions.add(new Pair("fear",Double.valueOf(line[3])));
                emotions.add(new Pair("joy",Double.valueOf(line[4])));
                emotions.add(new Pair("sadness",Double.valueOf(line[5])));
                emotions.add(new Pair("surprise",Double.valueOf(line[6])));
                // Get main emotion for the given id
                mainEmotion = calculateMaxSentiment(emotions); // Get main emotions for specific id

                // For debugging purposes
                /*if(mainEmotion.equals("neutral")) {
                    for (Pair<String, Double> em : emotions) {
                        System.out.println("Emotion: " + em.getKey() + " Value: " + em.getValue());
                    }
                    System.out.println("Main Emotion: " + mainEmotion);
                    System.out.println("--------------------------------");
                }*/

                // Get the text for the given id
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("//instance[@id=" + id + "]");
                NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
                text = nl.item(0).getTextContent();

                actualSentiment.put(text,mainEmotion);

            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets relevant actual sentiment classes from annotated dataset (namely 6 basic emotions and neutral)
     */
    private static void getActualSentiment() {
        // Read the file and keep only relevant lines (referring to 6 basic emotions or neutral)
        try {
            Scanner input = new Scanner(new FileReader(datasetPath));
            String emotion;
            String tweet;
            String[] line;
            while (input.hasNext()) {
                line = input.nextLine().split("\\t"); // Each tweet of the annotated dataset
                emotion = line[line.length-1]; // Get last word - emotion
                tweet = line[line.length-2]; // Get the related tweet
                if(emotion.compareTo("joy") == 0 || emotion.compareTo("surprise") == 0 ||
                        emotion.compareTo("fear") == 0 || emotion.compareTo("anger") == 0 ||
                        emotion.compareTo("neutral") == 0 || emotion.compareTo("sadness") == 0 ||
                        emotion.compareTo("disgust") == 0 ) {
                    actualSentiment.put(tweet,emotion.toLowerCase());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates predicted sentiment of given dataset based on local algorithm
     * @throws IOException
     */
    private static void calculatePredictedSentiment() throws IOException {
        String parsed;
        String maxSentiment;
        List<Pair<String, Double>> localSentiment;
        for(String tweet : actualSentiment.keySet()) { // For each tweet calculate main predicted emotion
            parsed = Preprocessor.preprocessTweet(tweet); // Preprocess tweet
            localSentiment = analyzer.sentiment(parsed); // Calculate sentiment
            maxSentiment = calculateMaxSentiment(localSentiment); // Find main sentiment based on local algorithm

            predictedSentiment.put(tweet,maxSentiment.toLowerCase()); // Add the predicted sentiment to a map
        }
    }

    /**
     * Fills confusion matrix based on actual sentiment (line) and predicted sentiment (column)
     */
    private static void fillConfusionMatrix() {
       int correct = 0;
        for(String tweet : actualSentiment.keySet()) {
            String actualSentimentString = actualSentiment.get(tweet); // Actual Sentiment
            String predictedSentimentString = predictedSentiment.get(tweet); // Predicted Sentiment

            if(actualSentimentString.equals(predictedSentimentString)) {
                correct++;
            }
            cm.increaseValue(actualSentimentString,predictedSentimentString);
        }
    }


    /**
     * Calculates total accuracy (deprcated)
     * @return The percentage of correct sentiment labeling (using local algorithm) for an annotated dataset
     * @throws IOException
     */
    private static double calculateAccuracy() throws IOException {
        int correct = 0; // The number of correct tweet sentiment
        String parsed;
        String maxSentiment;
        List<Pair<String, Double>> localSentiment;
        for(String tweet : actualSentiment.keySet()) { // For each tweet check if emotion is right
            parsed = Preprocessor.preprocessTweet(tweet); // Preprocess tweet
            localSentiment = analyzer.sentiment(parsed); // Calculate sentiment
            // System.out.println("Tweet: " + tweet + "\n" + localSentiment);
            maxSentiment = calculateMaxSentiment(localSentiment); // Find main sentiment based on local algorithm
            // System.out.println("------------------------------> " + maxSentiment);
            // System.out.println("------------------------------> Expected Sentiment: " + annotatedData.get(tweet));
            if(maxSentiment.toLowerCase().equals(actualSentiment.get(tweet))) { // Compare local sentiment to annotation
                correct++;
            }
        }
        return calculatePercentage(correct, actualSentiment.size()); // Get the final percentage of correct sentiment
    }


    /**
     * Calculates the main tweet sentiment given a list of sentiment scores
     * @param localSentiment List of sentiment and score pairs
     * @return The main sentiment of th tweet
     */
    private static String calculateMaxSentiment(List<Pair<String,Double>> localSentiment) {
        double max = 0.0;
        String mainEmotion = "neutral";
        for(Pair<String,Double> emotion : localSentiment) {
            if(emotion.getValue() > max) {
                mainEmotion = emotion.getKey();
                max = emotion.getValue();
            }
        }
        return mainEmotion;
    }

    /**
     * Calculates a percentage
     * @param noItems The nominator
     * @param outOf The denominator
     * @return The percentage
     */
    private static double calculatePercentage(int noItems, int outOf) {
        if(outOf != 0) {
            return (double) noItems / outOf;
        } else {
            throw new IllegalArgumentException("Division by zero");
        }
    }
}
