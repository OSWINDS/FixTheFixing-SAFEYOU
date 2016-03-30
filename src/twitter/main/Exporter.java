package twitter.main;

/**
 * Created by gogopavl on 30/3/2016.
 */

import mongo.MongoConnector;
import twitter.manager.TweetManager;
import twitter.manager.TwitterCriteria;
import twitter.model.Tweet;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Exporter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    private static JsonBuilderFactory factory = Json.createBuilderFactory(null);
    private static MongoConnector mc = new MongoConnector("localhost",27017,"Djokovic");

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("You must pass some parameters. Use \"-h\" to help.");
            System.exit(0);
        }

        if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("\nTo use this jar, you can pass the folowing attributes:");
            System.out.println("   username: Username of a specific twitter account (without @)");
            System.out.println("      since: The lower bound date (yyyy-mm-aa)");
            System.out.println("      until: The upper bound date (yyyy-mm-aa)");
            System.out.println("querysearch: A query text to be matched");
            System.out.println("  maxtweets: The maximum number of tweets to retrieve");

            System.out.println("\nExamples:");
            System.out.println("# Example 1 - Get tweets by username [@example]");
            System.out.println("java -jar got.jar username=example maxtweets=1\n");

            System.out.println("# Example 2 - Get tweets by query search [asian handicap]");
            System.out.println("java -jar got.jar querysearch=\"asian handicap\" maxtweets=1\n");

            System.out.println("# Example 3 - Get tweets by username and bound dates [example, '2015-09-10', '2015-09-12']");
            System.out.println("java -jar got.jar username=example since=2015-09-10 until=2015-09-12 maxtweets=1");
        } else {
            TwitterCriteria criteria = TwitterCriteria.create();

            for (String parameter : args) {
                String[] parameterSplit = parameter.split("=");

                if (parameterSplit[0].equals("username")) {
                    criteria.setUsername(parameterSplit[1]);
                } else if (parameterSplit[0].equals("since")) {
                    criteria.setSince(parameterSplit[1]);
                } else if (parameterSplit[0].equals("until")) {
                    criteria.setUntil(parameterSplit[1]);
                } else if (parameterSplit[0].equals("querysearch")) {
                    //Operators: https://dev.twitter.com/rest/public/search
                    criteria.setQuerySearch(parameterSplit[1]);
                } else if (parameterSplit[0].equals("maxtweets")) {
                    criteria.setMaxTweets(Integer.valueOf(parameterSplit[1]));
                }
            }

            //try {
                //BufferedWriter bw = new BufferedWriter(new FileWriter("output_got.csv"));
                //bw.write("username;date;retweets;favorites;text;geo;mentions;hashtags;id;permalink");
                //bw.newLine();

                System.out.println("Obtaining tweets... \n");

                for (Tweet t : TweetManager.getTweets(criteria)) {
                    //bw.write(String.format("%s;%s;%d;%d;\"%s\";%s;%s;%s;\"%s\";%s", t.getUsername(), sdf.format(t.getDate()), t.getRetweets(), t.getFavorites(), t.getText(), t.getGeo(), t.getMentions(), t.getHashtags(), t.getId(), t.getPermalink()));
                    //bw.newLine();

                    JsonObject value = factory.createObjectBuilder()
                            .add("user_id", t.getId())
                            .add("user_name", t.getUsername())
                            .add("date", sdf.format(t.getDate()))
                            .add("text", t.getText())
                            .add("retweets", t.getRetweets())
                            .add("favorites", t.getFavorites())
                            .add("mentions", t.getMentions())
                            .add("hashtags", t.getHashtags())
                            .add("geo", t.getGeo())
                            .add("permalink",t.getPermalink())
                            .build();
                    //Add "value" to mongo
                    //System.out.println("JSON: "+value.toString());

                    mc.addTweet(value.toString());
                }

                //bw.close();

                System.out.println("Done!"); //Output file generated "output_got.csv"
            /*} catch (IOException e) {
                e.printStackTrace();
            }*/
        }

    }

}