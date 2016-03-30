package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Custom class that connects to MongoDB and inserts, edits and deletes documents
 */
public class MongoConnector {

    // Fields for twitter DB
    public static final String _TWEET_JSON_  = "tweet";
    public static final String _TWEET_PARSED_STRING_ = "parsedString";
    public static final String _TWEET_EMOTION_SCORES_ = "emScores";
    // Fields for youtube DB
    public static final String _COMMENT_JSON_  = "comment";
    public static final String _COMMENT_PARSED_STRING_ = "parsedString";
    public static final String _COMMENT_EMOTION_SCORES_ = "emScores";

    private static final String _COLL_INDEX_ = "_id";

    private MongoDatabase _db;
    private MongoDatabase _db_youtube;
    private String _coll_name_twitter;
    private String _coll_name_youtube;

    /**
     * Connects to the given server:port to the database fixthefixing
     * @param host The host name of the server
     * @param port The port of the server
     */
    public MongoConnector(String host, int port, String _coll_name) {
        MongoClient mongoClient = new MongoClient(host, port);
        _db = mongoClient.getDatabase("twitter");
        _db_youtube = mongoClient.getDatabase("youtube");
        _coll_name_twitter = _coll_name + "_tweets";
        _coll_name_youtube = _coll_name + "_youtube";
    }

    /**
     * Inserts tweet to tweets collection
     * @param json The tweet's JSON
     */
    public void addTweet(String json) {
        MongoCollection<Document> coll = _db.getCollection(_coll_name_twitter);

        Document doc = new Document(_TWEET_PARSED_STRING_, null)
                .append(_TWEET_JSON_, Document.parse(json));

        coll.insertOne(doc);
    }

    /**
     * Inserts Youtube comments to comments collection
     * @param json The comment's JSON
     */
    public void addYoutubeComment(String json) {
        MongoCollection<Document> coll = _db_youtube.getCollection(_coll_name_youtube);

        Document doc = new Document(_COMMENT_PARSED_STRING_, null)
                .append(_COMMENT_JSON_, Document.parse(json));

        coll.insertOne(doc);
    }

    /**
     * Searches in the data base for the tweet with ID UUID and changes the parsed text field with
     * the given
     * @param UUID The UUID of the document that we want to change
     * @param parsedText the text that we want to put in the field
     * @return if the text successfully changed
     */
    public boolean insertParsedTweet(ObjectId UUID, String parsedText) {

        /* Get db and document */
        MongoCollection<Document> tweets = _db.getCollection(_coll_name_twitter);
        FindIterable<Document> iterable = tweets.find(new Document(_COLL_INDEX_, UUID));

        Document document = iterable.first();

        if (document != null) {
            /* Document exist*/
            if (document.replace(_TWEET_PARSED_STRING_, null, parsedText)) {
                tweets.replaceOne(new Document(_COLL_INDEX_, UUID), document).getMatchedCount();
                //printMongo("Replaced OK -> " + UUID);
                return true;

            } else {
                errorMongo("Replace error -> " + UUID + ", text already parsed!");
                return false;
            }
        }

        errorMongo("Document " + UUID + " doesn't exist here!");
        return false;
    }

    /**
     * Centralised management for error displaying
     * @param str the error message
     */
    private void errorMongo(String str) {
        System.err.println(str);
    }

    /**
     * Main function for
     * @param args
     */
    public static void main(String[] args) {
        MongoConnector mongoConnector = new MongoConnector("localhost", 27017, "djokovic");
        // Test insertion
        mongoConnector.addTweet("{\"phonetype\":\"N95\",\"cat\":\"WP\"}");
    }
}
