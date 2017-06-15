package sentiment;

import mongo.MongoConnector;
import org.bson.types.ObjectId;
import javafx.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Map.Entry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom class the performs the pre-processing and sentiment analysis on tweets
 */
public class Analysis {

    private Emotions[] emotions;

    private HashMap<Emotions, ArrayList<String>> representativeWords;
    private SenticNet senticNetLib;
    private MongoConnector mongoConnector;
    private String collectionName;

    /**
     * Constructor of the class; Initializes all variables
     * @param collectionName The name of the collection at hand
     * @throws IOException
     */
    public Analysis(String collectionName) throws IOException {
        emotions = Emotions.values();

        String senticNetFilename = "./data/senticnet3.rdf.xml";
        senticNetLib = new SenticNet(senticNetFilename);

        this.collectionName = collectionName;

        mongoConnector =  new MongoConnector("localhost", 27017, collectionName);

        SentimentAnalysis sentiment = new SentimentAnalysis();

        representativeWords = sentiment.getRepresentativeWords();
    }

    /**
     * Constructor of the class; Initializes all variables
     * @throws IOException
     */
    public Analysis() throws IOException {
        emotions = Emotions.values();

        String senticNetFilename = "./data/senticnet3.rdf.xml";
        senticNetLib = new SenticNet(senticNetFilename);

        SentimentAnalysis sentiment = new SentimentAnalysis();

        representativeWords = sentiment.getRepresentativeWords();
    }

    /**
     * Sentiment analysis for the whole case
     * @throws JSONException In case a field cannot be found
     */
    private void analyzeCaseTotal(String dbType) throws JSONException {
        HashMap<ObjectId, JSONObject> objects;
        Month caseTotal;
        String key;
        if(dbType.equals("twitter")) {
            objects = mongoConnector.getFullTweets(); // Get all tweets
            caseTotal = new Month("twitter_sentiment", "total");
        } else {
            objects = mongoConnector.getFullComments(); // Get all tweets
            caseTotal = new Month("youtube_sentiment", "total");
        }
        for (JSONObject object : objects.values()) { // For each tweet
            // Increment feelings
            caseTotal.addFeelingCount(object.getJSONObject("emScores").getDouble("ANGER"),
                    object.getJSONObject("emScores").getDouble("DISGUST"),
                    object.getJSONObject("emScores").getDouble("FEAR"),
                    object.getJSONObject("emScores").getDouble("JOY"),
                    object.getJSONObject("emScores").getDouble("SADNESS"),
                    object.getJSONObject("emScores").getDouble("SURPRISE"));
        }

        caseTotal.finalizeFeelings();
        writeFeelingsToFile(caseTotal);
    }

    /**
     * Sentiment analysis of a case's tweets per month
     * @throws JSONException In case a field cannot be found
     */
    private void analyzeCase() throws JSONException {
        HashMap<ObjectId, JSONObject> tweets = mongoConnector.getFullTweets(); // Get all tweets
        HashMap<String, Month> months = new HashMap<>(); // To save emotions per month
        String month;
        String year;
        String key; // map key
        for (JSONObject tweet : tweets.values()) { // For each tweet
            month = getMonth(tweet.getJSONObject("tweet").getString("date")); // Find month
            year = getYear(tweet.getJSONObject("tweet").getString("date")); // Find year
            key = year + "_" + month;

            months.putIfAbsent(key, new Month(month,year)); // If it's the first tweet of the month, add month
            // Increment feelings
            months.get(key).addFeelingCount(tweet.getJSONObject("emScores").getDouble("ANGER"),
                    tweet.getJSONObject("emScores").getDouble("DISGUST"),
                    tweet.getJSONObject("emScores").getDouble("FEAR"),
                    tweet.getJSONObject("emScores").getDouble("JOY"),
                    tweet.getJSONObject("emScores").getDouble("SADNESS"),
                    tweet.getJSONObject("emScores").getDouble("SURPRISE"));
        }

        for (Month monthObject : months.values()) { // For each month
            monthObject.finalizeFeelings(); // Find the feelings for the whole month
            writeFeelingsToFile(monthObject);
        }
    }

    /**
     * Writes sentiment scores for a specific month to file
     * @param monthObject All the month related information to be written to file
     */
    private void writeFeelingsToFile(Month monthObject) {
        String path = "out\\" + collectionName + "\\" + monthObject.getYear() + "_" + monthObject.getMonth() + ".txt";
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "utf-8"))) {
            writer.write("ANGER" + " , " + monthObject.getAngerCount());
            writer.write(System.lineSeparator());
            writer.write("DISGUST" + " , " + monthObject.getDisgustCount());
            writer.write(System.lineSeparator());
            writer.write("FEAR" + " , " + monthObject.getFearCount());
            writer.write(System.lineSeparator());
            writer.write("JOY" + " , " + monthObject.getJoyCount());
            writer.write(System.lineSeparator());
            writer.write("SADNESS" + " , " + monthObject.getSadnessCount());
            writer.write(System.lineSeparator());
            writer.write("SURPRISE" + " , " + monthObject.getSurpriseCount());
            writer.write(System.lineSeparator());
            writer.write("Total tweets" + " : " + monthObject.getCount());
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the month out of a date
     * @param date The date string
     * @return The month string
     */
    private String getMonth(String date) {
        Matcher m = Pattern.compile("\\/(\\d{2})\\/").matcher(date);
        if(m.find()) {
            return m.group(1);
        }
        return "";
    }

    /**
     * Get the year out of a date
     * @param date The date string
     * @return The year string
     */
    private String getYear(String date) {
        Matcher m = Pattern.compile("(\\d{4})\\/").matcher(date);
        if(m.find()) {
            return m.group(1);
        }
        return "";
    }

    /**
     * Inserts the processed tweets and the emotions in the DB
     * @param dbType The type of the database; can be either twitter or youtube
     * @throws IOException
     */
    private void analyze(String dbType) throws IOException {
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
    public List<Pair<String, Double>> sentiment(String tweet) throws  IOException {
        tweet = tweet.concat(" ");
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

    /**
     * Calls all methods needed for sentiment analysis
     * @throws IOException In case the file cannot open
     * @throws JSONException In case the specified JSON field does not exist
     */
    public void SentimentAnalysis() throws IOException, JSONException {
        System.out.println("Calculating tweets' emotions...");
        analyze("twitter"); // Calculate emotion scores for tweets
        System.out.println("Calculating comments' emotions...");
        analyze("youtube"); // Calculate emotion scores for comments
        System.out.println("Calculating Twitter's emotions per month...");
        analyzeCase(); // Analyze case's tweets per month
        System.out.println("Calculating Twitter's emotions in total...");
        analyzeCaseTotal("twitter"); // Analyze case's tweets in total
        System.out.println("Calculating YouTube's emotions in total...");
        analyzeCaseTotal("youtube"); // Analyze case's comments in total
    }
}

