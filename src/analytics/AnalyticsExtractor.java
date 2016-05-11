package analytics;

import mongo.MongoConnector;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 * Class to produce the analytics
 * Created by sifantid on 5/5/2016.
 */
public class    AnalyticsExtractor {
    private static MongoConnector mc;

    /**
     * Class constructor
     * @param collectionName The name of the specific collection to be accessed
     */
    public AnalyticsExtractor(String collectionName) {
        mc = new MongoConnector("localhost", 27017, collectionName);
    }

    /**
     * Gets the hashtags frequencies in collected tweets
     */
    private void getHashtagFrequencies() {
        writeToTagcloudFile(calculateFrequencies("hashtags","twitter"),"hashtag_frequencies_twitter.txt");
    }

    /**
     * Gets the mentions frequencies in collected tweets
     */
    private void getTwitterMentionFrequencies() {
        writeToTagcloudFile(calculateFrequencies("mentions","twitter"),"mentions_frequencies_twitter.txt");
    }

    /**
     * Gets the location frequencies in collected tweets and Youtube comments
     */
    private void getLocationFrequencies() {
        writeToTagcloudFile(calculateFrequenciesSimple("geo","twitter"),"location_frequencies_twitter.txt");
        writeToTagcloudFile(calculateFrequenciesSimple("location","youtube"),"location_frequencies_youtube.txt");
    }

    /**
     * Gets the date frequencies in collected tweets
     */
    private void getDateFrequencies() {
        writeToTagcloudFile(calculateFrequenciesSimple("date","twitter"),"date_frequencies_twitter.txt");
    }

    /**
     * Gets the twitter users frequencies in collected tweets
     */
    private void getTwitterUsersFrequencies() {
        writeToTagcloudFile(calculateFrequenciesSimple("user_name","twitter"),"user_frequencies_twitter.txt");
    }

    /**
     * Gets the youtube users frequencies in collected Youtube comments
     */
    private void getYoutubeUsersFrequencies() {
        writeToTagcloudFile(calculateFrequenciesSimple("authorID","youtube"),"user_frequencies_youtube.txt");
    }

    /**
     * Calculates frequencies of field by hashing, without preprocessing
     * @param field The field, which frequency is counted
     * @param medium The social medium e.g. "twitter" or "youtube"
     * @return A map of distinct keys and frequencies values
     */
    private HashMap<String,Integer> calculateFrequenciesSimple(String field, String medium) {
        HashMap<String,Integer> frequencies = new HashMap<>();
        HashMap<ObjectId,JSONObject> tweets_comments;
        if(medium.equals("twitter")) {
            tweets_comments = mc.getTweets();
        } else {
            tweets_comments = mc.getComments();
        }

        for(JSONObject tweet_comment : tweets_comments.values()) {
            String key = null;
            try {
                key = tweet_comment.getString(field);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(key != null && !key.isEmpty()) {
                frequencies.putIfAbsent(key, 0);
                frequencies.computeIfPresent(key, (k, v) -> v + 1);
            }
        }
        return frequencies;
    }

    /**
     * Calculates the frequencies of given field for a specific collection of tweets/comments with preprocessing
     * @param field The field we want to calculate the frequency of
     * @param medium The social medium e.g. "twitter" or "youtube"
     * @return Pairs of field values and frequencies
     */
    private HashMap<String,Integer> calculateFrequencies(String field, String medium) {
        HashMap<String,Integer> frequencies = new HashMap<>();
        HashMap<ObjectId,JSONObject> tweets_comments;
        if(medium.equals("twitter")) {
            tweets_comments = mc.getTweets();
        } else {
            tweets_comments = mc.getComments();
        }

        for(JSONObject tweet_comment : tweets_comments.values()) {
            String[] fieldValues = new String[0];
            try {
                fieldValues = tweet_comment.getString(field).split(" ");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(!fieldValues[0].isEmpty()) {
                for (String fieldValue : fieldValues) {
                    frequencies.putIfAbsent(fieldValue, 0);
                    frequencies.computeIfPresent(fieldValue, (k, v) -> v + 1);
                }
            }
        }
        return frequencies;
    }

    /**
     * Writes file to be read by tag cloud creation tools
     * @param map The key-frequency pairs
     * @param filename The name of the exported file
     */
    private void writeToTagcloudFile(HashMap<String,Integer> map, String filename) {
        String path = "out\\" + filename;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), "utf-8"))) {
            for(String s : map.keySet()) {
                String key = s.replace(", ",";");
                writer.write(key + " , " + map.get(s));
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Combines all the analytics extractors
     */
    public void analyze() {
        getHashtagFrequencies();
        getTwitterMentionFrequencies();
        getLocationFrequencies();
        getDateFrequencies();
        getTwitterUsersFrequencies();
        getYoutubeUsersFrequencies();
    }
}
