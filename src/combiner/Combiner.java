package combiner;

import analytics.AnalyticsExtractor;
import analytics.FrequencyCounter;
import analytics.LocationDetector;
import org.json.JSONException;
import sentiment.Analysis;
import twitter.main.TwitterExporter;
import youtube.YoutubeExporter;

import java.io.IOException;

/**
 * Class to combine mongo, twitter and youtube packages
 * Created by Sofia on 4/5/2016.
 */
public class Combiner {

    private static FrequencyCounter fc = new FrequencyCounter();
    private static AnalyticsExtractor analyticsExtractor;

    /**
     * Main class to run the project
     * @param args Arguments needed for Twitter search and tweets and comments storage
     * @throws JSONException
     * @throws IOException
     */
    public static void main(String[] args) throws JSONException, IOException {


        if(args != null) {
/*            // Collect Youtube comments and insert them to DB
            YoutubeExporter.main(args);
            Preprocessor.preprocessComments(args[4].split("=")[1], fc);


            // Collect tweets and insert them to DB
            TwitterExporter.main(args);
            Preprocessor.preprocessTweets(args[4].split("=")[1], fc);

            fc.exportFrequencies(); //creates frequencies.txt - sorted alphabetically
            fc.exportFrequenciesByValue(); //creates frequenciesByValue.txt - sorted by frequencies (descending order)

            if(args.length >= 5) {
                analyticsExtractor = new AnalyticsExtractor(args[4].split("=")[1]);
                analyticsExtractor.analyze();
            }*/

            Analysis analysis = new Analysis(args[4].split("=")[1]);
            //analysis.analyze("twitter");
            //analysis.analyze("youtube");
        }

    }
}
