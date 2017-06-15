package evaluation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import combiner.Preprocessor;
import javafx.util.Pair;
import sentiment.*;

import java.util.List;
import java.util.Scanner;

/**
 * Created by sifantid on 15/6/2017.
 */
public class Evaluator {

    private static HashMap<String,String> annotatedData;
    private static Analysis analyzer;

    /**
     * Keep only relevant data from annotated set (6 basic emotions and neutral sentiment)
     */
    static {
        // Prepare new annotated dataset
        annotatedData = new HashMap<>();
        // Read the file and keep only relevant lines (referring to 6 basic emotions or neutral)
        try {
            Scanner input = new Scanner(new FileReader("./resources/annotatedData.txt"));
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
                    annotatedData.put(tweet,emotion);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            analyzer = new Analysis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            System.out.println("Percentage: " + analyzeAnnotated());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Analyzes the annotated dataset
     * @return The percentage of correct sentiment labeling (using local algorithm) for an annotated dataset
     * @throws IOException
     */
    private static double analyzeAnnotated() throws IOException {
        int correct = 0; // The number of correct tweet sentiment
        String parsed;
        String maxSentiment;
        List<Pair<String, Double>> localSentiment;
        for(String tweet : annotatedData.keySet()) { // For each tweet check if emotion is right
            parsed = Preprocessor.preprocessTweet(tweet); // Preprocess tweet
            localSentiment = analyzer.sentiment(parsed); // Calculate sentiment
            // System.out.println("Tweet: " + tweet + "\n" + localSentiment);
            maxSentiment = calculateMaxSentiment(localSentiment); // Find main sentiment based on local algorithm
            // System.out.println("------------------------------> " + maxSentiment);
            // System.out.println("------------------------------> Expected Sentiment: " + annotatedData.get(tweet));
            if(maxSentiment.toLowerCase().equals(annotatedData.get(tweet))) { // Compare local sentiment to annotation
                correct++;
            }
        }
        System.out.println("CORRECT: " + correct);
        return calculatePercentage(correct, annotatedData.size()); // Get the final percentage of correct sentiment
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
