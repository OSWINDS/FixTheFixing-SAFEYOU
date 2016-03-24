package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Custom class that connects to MongoDB and inserts, edits and deletes documents
 */
public class MongoConnector {

    public static final String _TWEET_JSON_  = "tweet";
    public static final String _TWEET_TREND_ = "trend";
    public static final String _TWEET_PARSED_STRING_ = "parsedString";
    public static final String _TWEET_EMOTION_SCORES_ = "emScores";

    private MongoDatabase _db;
    /**
     * Connects to the given server:port to the database fixthefixing
     * @param host The host name of the server
     * @param port The port of the server
     */
    public MongoConnector(String host, int port) {
        MongoClient mongoClient = new MongoClient(host, port);
        _db = mongoClient.getDatabase("fixthefixing");
    }

    public void addTweet(String trend, String json) {
        MongoCollection<Document> coll = _db.getCollection("tweets");

        Document doc = new Document(_TWEET_TREND_, trend)
                .append(_TWEET_PARSED_STRING_, null)
                .append(_TWEET_JSON_, Document.parse(json));

        coll.insertOne(doc);
    }

    public static void main(String[] args) {
        MongoConnector mongoConnector = new MongoConnector("localhost", 27017);
    }
}
