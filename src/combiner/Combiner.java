package combiner;

import org.json.JSONException;
import twitter.main.TwitterExporter;
import youtube.YoutubeExporter;

import java.io.IOException;

/**
 * Class to combine mongo, twitter and youtube packages
 * Created by Sofia on 4/5/2016.
 */
public class Combiner {
    /**
     * Main class to run the project
     * @param args Arguments neeeded for Twitter search
     */
    private static FrequencyCounter fc = new FrequencyCounter();

    public static void main(String[] args) throws JSONException, IOException {
        // Collect Youtube comments and insert them to DB
        YoutubeExporter.main(null);
        Preprocessor.preprocessComments(fc);

        // Collect tweets and insert them to DB
        TwitterExporter.main(args);
        Preprocessor.preprocessTweets(fc);

        fc.exportFrequencies();//creates frequencies.txt - sorted alphabetically
        fc.exportFrequenciesByValue();//creates frequenciesByValue.txt - sorted by frequencies (descending order)


    }
}
