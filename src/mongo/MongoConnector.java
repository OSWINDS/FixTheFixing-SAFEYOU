package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * Custom class that connects to MongoDB and inserts, edits and deletes documents
 * Created by sifantid on 10/3/2016.
 */
public class MongoConnector {

    // Fields for twitter DB
    private static final String _TWEET_JSON_  = "tweet";
    private static final String _TWEET_PARSED_STRING_ = "parsedString";
    private static final String _TWEET_EMOTION_SCORES_ = "emScores";
    // Fields for youtube DB
    private static final String _COMMENT_JSON_  = "comment";
    private static final String _COMMENT_PARSED_STRING_ = "parsedString";
    private static final String _COMMENT_EMOTION_SCORES_ = "emScores";

    private static final String _COLL_INDEX_ = "_id";

    private MongoDatabase _db;
    private MongoDatabase _db_youtube;
    private String _coll_name_twitter;
    private String _coll_name_youtube;

    /**
     * Connects to the given server:port to the database fixthefixing
     * @param host The host name of the server
     * @param port The port of the server
     * @param _coll_name The name of the collection
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
     * @throws JSONException
     */
    public void addTweet(String json) throws JSONException {
        MongoCollection<Document> coll = _db.getCollection(_coll_name_twitter);

        JSONObject jsonObj = new JSONObject(json);
        if(_db.getCollection(_coll_name_twitter).find(eq("tweet.id",jsonObj.getString("id"))).first() == null) {
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
     * Gets all parsed tweets from twitter database
     * @return Pairs of tweets' IDs and tweets' parsed text
     */
    public HashMap<ObjectId, String> getParsedTweets() {
        MongoCollection<Document> parsedTweetsCollection = _db.getCollection(_coll_name_twitter);
        return getParsed(parsedTweetsCollection, _TWEET_PARSED_STRING_);
    }

    public boolean insertEmotionsTwitter(ObjectId UUID, List<Pair<String, Double>> emotions) {
        MongoCollection<Document> tweets = _db.getCollection(_coll_name_twitter);
        return insertEmotions(UUID, emotions, tweets);
    }

    public boolean insertEmotionsYoutube(ObjectId UUID, List<Pair<String, Double>> emotions) {
        MongoCollection<Document> comments = _db_youtube.getCollection(_coll_name_youtube);
        return insertEmotions(UUID, emotions, comments);
    }

    /**
     * Takes a tweet id and inserts emotions with their values
     * @param UUID The Id of the tweet that we want to insert the emotions to.
     *           Pair (Document's Unique number, the trend name of the tweet)
     * @param emotions an array list of pair(string, double) where the string is the name of the emotion and
     *                 the double is the value of that emotion
     * @return if the document
     */
    private boolean insertEmotions(ObjectId UUID, List<Pair<String/* Emotion Name */, Double/* score */>> emotions, MongoCollection<Document> col) {
        FindIterable<Document> iterable = col.find(new Document(_COLL_INDEX_, UUID));

        Document document = iterable.first();

        if (document != null) {
            Document scores = new Document();

            for (Pair<String, Double> pair : emotions) {
                scores.append(pair.getKey(), pair.getValue());
            }

            document.append(_TWEET_EMOTION_SCORES_, scores);

            col.replaceOne(new Document(_COLL_INDEX_, UUID), document);
            return true;
        }

        errorMongo("Document " + UUID + " not found!");
        return false;
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
     * Gets all youtube comments' parsed text
     * @return Pairs of comments' IDs and comments' parsed texts
     */
    public HashMap<ObjectId,String> getParsedComments() {
        MongoCollection<Document> parsedCollection = _db_youtube.getCollection(_coll_name_youtube);
        return getParsed(parsedCollection,_COMMENT_PARSED_STRING_);
    }

    /**
     * Gets all documents' parsed strings from a collection
     * @param col The name of the collection
     * @param field The field we want to get
     * @return Pairs of documents' IDs and parsed texts
     */
    private HashMap<ObjectId, String> getParsed(MongoCollection<Document> col, String field) {
        FindIterable<Document> iterable = col.find();

        HashMap<ObjectId,String> docs = new HashMap<>();

        for (Document doc: iterable) {
            ObjectId id = doc.getObjectId(_COLL_INDEX_);
            String parsed = (String) doc.get(field);
            docs.put(id,parsed);
        }
        return docs;
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
