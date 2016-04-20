package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import static com.mongodb.client.model.Filters.*;

import java.util.*;

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

        JSONObject jsonObj = new JSONObject(json);
        if(_db.getCollection(_coll_name_twitter).find(eq("tweet.user_id",jsonObj.getString("user_id"))).first() == null) {
            Document doc = new Document(_TWEET_PARSED_STRING_, null)
                    .append(_TWEET_JSON_, Document.parse(json));

            coll.insertOne(doc);
        } else {
            System.out.println("ALREADY IN!");
        }
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
     * Generalised function to insert parsed text to database
     * @param UUID The document's id
     * @param parsed The parsed text to be inserted
     * @param col The collection for the insertion
     * @param field The field to get
     * @return Insert parsed text into database
     */
    private boolean insertParsed(ObjectId UUID, String parsed, MongoCollection<Document> col, String field) {
        FindIterable<Document> iterable = col.find(new Document(_COLL_INDEX_, UUID));

        Document document = iterable.first();

        if (document != null) {
            /* Document exist*/
            if (document.replace(field, null, parsed)) {
                col.replaceOne(new Document(_COLL_INDEX_, UUID), document).getMatchedCount();
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
     * Searches in the data base for the Youtube comment with ID UUID and changes the parsed text field with
     * the given one
     * @param UUID The UUID of the document that we want to change
     * @param parsedComment the comment that we want to put in the field
     * @return True, if the text successfully changed, false otherwise
     */
    public boolean insertParsedComment(ObjectId UUID, String parsedComment) {
        /* Get db and document */
        MongoCollection<Document> comments = _db_youtube.getCollection(_coll_name_youtube);
        return insertParsed(UUID, parsedComment, comments, _COMMENT_PARSED_STRING_);
    }

    /**
     * Searches in the data base for the tweet with ID UUID and changes the parsed text field with
     * the given
     * @param UUID The UUID of the document that we want to change
     * @param parsedText the text that we want to put in the field
     * @return True, if the text successfully changed, false otherwise
     */
    public boolean insertParsedTweet(ObjectId UUID, String parsedText) {

        /* Get db and document */
        MongoCollection<Document> tweets = _db.getCollection(_coll_name_twitter);
        return insertParsed(UUID, parsedText, tweets, _TWEET_PARSED_STRING_);
    }

    /**
     * Gets all tweets from twitter database
     * @return Pairs of tweets' IDs and tweets' JSONs
     */
    public HashMap<ObjectId,JSONObject> getTweets() {
        MongoCollection<Document> tweetsCollection = _db.getCollection(_coll_name_twitter);
        return getDocuments(tweetsCollection,_TWEET_JSON_);
    }

    /**
     * Gets all youtube comments from youtube database
     * @return Pairs of comments' IDs and comments' JSONs
     */
    public HashMap<ObjectId,JSONObject> getComments() {
        MongoCollection<Document> commentsCollection = _db_youtube.getCollection(_coll_name_youtube);
        return getDocuments(commentsCollection,_COMMENT_JSON_);
    }

    /**
     * Gets all documents from a collection
     * @param col The name of the collection
     * @param field The field we want to get
     * @return Pairs of documents' IDs and JSONs
     */
    private HashMap<ObjectId,JSONObject> getDocuments(MongoCollection<Document> col, String field) {
        FindIterable<Document> iterable = col.find();

        HashMap<ObjectId,JSONObject> docs = new HashMap<>();

        for (Document doc: iterable) {
            ObjectId id = doc.getObjectId(_COLL_INDEX_);
            Document document = (Document) doc.get(field);
            JSONObject json = new JSONObject(document);
            docs.put(id,json);
        }
        return docs;
    }

    /**
     * Centralised management for error displaying
     * @param str the error message
     */
    private void errorMongo(String str) {
        System.err.println(str);
    }
}
