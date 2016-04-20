package combiner;

import twitter.main.TwitterExporter;
import youtube.YoutubeExporter;

/**
 * Class to combine mongo, twitter and youtube packages
 * Created by Sofia on 4/5/2016.
 */
public class Combiner {
    public static void main(String[] args) {
        // Collect Youtube comments and insert them to DB
        YoutubeExporter.main(null);
        Preprocessor.preprocessComments();

        // Collect tweets and insert them to DB
        TwitterExporter.main(args);
        Preprocessor.preprocessTweets();
    }
}
