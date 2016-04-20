package combiner;

import org.json.JSONException;
import twitter.main.TwitterExporter;
import youtube.YoutubeExporter;

/**
 * Class to combine mongo, twitter and youtube packages
 * Created by Sofia on 4/5/2016.
 */
public class Combiner {
    /**
     * Main class to run the project
     * @param args Arguments neeeded for Twitter search
     */
    public static void main(String[] args) throws JSONException {

        if(args != null) {
            // Collect Youtube comments and insert them to DB
            YoutubeExporter.main(args);
            Preprocessor.preprocessComments(args[4].split("=")[1]);

            // Collect tweets and insert them to DB
            TwitterExporter.main(args);
            Preprocessor.preprocessTweets(args[4].split("=")[1]);
        }
    }
}
