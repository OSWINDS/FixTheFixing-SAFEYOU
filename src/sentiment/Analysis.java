package sentiment;

import mongo.MongoConnector;
import org.bson.types.ObjectId;
import javafx.util.Pair;
import java.util.Map.Entry;

import java.io.IOException;
import java.util.*;

/**
 * Custom class the performs the pre-processing and sentiment analysis on tweets
 */
public class Analysis {

    private Emotions[] emotions;

    private HashMap<Emotions, ArrayList<String>> representativeWords;
    private SenticNet senticNetLib;
    private MongoConnector mongoConnector;

    /**
     * Constructor of the class; Initializes all variables
     * @throws IOException
     */
    public Analysis(String collectionName) throws IOException {
        emotions = Emotions.values();

        String senticNetFilename = "./data/senticnet3.rdf.xml";
        senticNetLib = new SenticNet(senticNetFilename);

        mongoConnector =  new MongoConnector("localhost", 27017, collectionName);

        SentimentAnalysis sentiment = new SentimentAnalysis();

        representativeWords = sentiment.getRepresentativeWords();
    }

    /**
     * Inserts the processed tweets and the emotions in the DB
     * @throws IOException
     */
    public void analyze(String dbType) throws IOException {
        if(dbType.equals("twitter")) { // Analyze tweets
            HashMap<ObjectId, String> tweetsToParse = mongoConnector.getParsedTweets();

            for(Entry<ObjectId,String> parsedTweet : tweetsToParse.entrySet()) {
                //Retrieve the tweet's info
                ObjectId UUID = parsedTweet.getKey();
                String tweet = parsedTweet.getValue();
                mongoConnector.insertEmotionsTwitter(UUID, sentiment(tweet));
            }
        } else { // Analyze YouTube comments
            HashMap<ObjectId, String> commentsToParse = mongoConnector.getParsedComments();

            for(Entry<ObjectId,String> parsedComment : commentsToParse.entrySet()) {
                //Retrieve the tweet's info
                ObjectId UUID = parsedComment.getKey();
                String comment = parsedComment.getValue();
                mongoConnector.insertEmotionsYoutube(UUID, sentiment(comment));
            }
        }
    }

    /**
     * Calculates the sentiment scores for each basic sentiment per tweet
     * @param tweet The tweet to be analyzed
     * @return List of pairs (Emotion,Score) for the given tweet
     * @throws IOException
     */
    private List<Pair<String, Double>> sentiment(String tweet) throws  IOException {
        //System.out.println("----------------------------------------------------");
        tweet = tweet.concat(" ");
        //System.out.println("Tweet: " + tweet);
        // If you want to apply stemming techniques to the tweet remove the comment characters
        /*String[] words = tweet.split(" ");
        String[] stemmedWords = new String[words.length];
        for(int i = 0; i < words.length; i++) {
            stemmedWords[i] = stemmer.stemm(words[i]) + " ";
        }
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < stemmedWords.length; i++) {
            strBuilder.append(stemmedWords[i]);
        }
        tweet = strBuilder.toString();

        System.out.println("Stemmed Tweet: " + tweet);*/
        List<Pair<String, Double>> scores = new ArrayList<>();

        int[] myCounter = new int[6];
        double[] sum = new double[6];
        int allCounter = 0;
        for (int i = 0; i < 6; ++i) {
            myCounter[i] = 0;
            sum[i] = 0;
        }

        //Finds the emotional words and their polarity
        for (int i = 0; i < 6; i++) { // For each emotion
            ArrayList<String> curRepresentativeWords = representativeWords.get(emotions[i]);
            /* Splitting the tweet based on whitespaces and then iterate over the words first wouldn't be correct,
            as WordNet and SenticNet include phrases as well, which would be missed otherwise.
              */
            for (String phrase : curRepresentativeWords) { // For each word of the representatives
                double polarity = senticNetLib.getPolarity(phrase);
                if (polarity != SenticNet.error) {
                    if (tweet.contains(" " + phrase + " ")) { // Adding whitespaces because we want an exact match
                        //System.out.println("Word: " + phrase + " Emotion: " + emotions[i] + " Polarity: " + polarity);
                        sum[i] += Math.abs(polarity);
                        myCounter[i]++;
                        allCounter++;
                    }
                }
            }
        }

        for (int i = 0; i < 6; ++i) { // For each emotion
            String emotionName = emotions[i].toString();
            Double emotionScore = 0.0;
            if (myCounter[i] > 0) { // If the tweet had any words for the specific emotion
                emotionScore = sum[i] / myCounter[i] / allCounter; // Calculate the emotion score
            }

            Pair<String, Double> pair = new Pair<>(emotionName, emotionScore);
            scores.add(i, pair);
        }

        return scores;
    }
}

