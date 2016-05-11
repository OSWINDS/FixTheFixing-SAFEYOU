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
        writeToTagcloudFile(getCountriesFrequencies(calculateFrequenciesSimple("geo","twitter")),"location_frequencies_twitter.txt");
        writeToTagcloudFile(getCountriesFrequencies(calculateFrequenciesSimple("location","youtube")),"location_frequencies_youtube.txt");
    }

    private HashMap<String,Integer> getCountriesFrequencies(HashMap<String,Integer> locations) {
        HashMap<String, Integer> countriesFrequencies = new HashMap<>();
        for(String location : locations.keySet()) {
            String city = "";
            if(location.contains(",")) {
                city = location.substring(0, location.indexOf(",")).replaceAll(" ", "_");
            }
            city = city.replaceAll(" ","_");
            String country = LocationDetector.getCountryOf(city);
            if(!country.isEmpty()) {
                countriesFrequencies.putIfAbsent(country, 0);
                countriesFrequencies.computeIfPresent(country, (k, v) -> v + 1);
            }
        }
        return countriesFrequencies;
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
                String key = s.replace(", ",";"); // If there are commas in the initial key String
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
