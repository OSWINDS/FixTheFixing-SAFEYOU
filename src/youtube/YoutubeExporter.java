package youtube;

import combiner.Preprocessor;
import mongo.MongoConnector;

import javax.json.JsonObject;
import java.util.ArrayList;

public class YoutubeExporter {

    public static void main(String args[]){
        YTCommentsCollector ytCommentsCollector = new YTCommentsCollector();
        ArrayList<JsonObject> jsons = ytCommentsCollector.collectComments();
        for(JsonObject json : jsons){
            String comment = json.getString("comment");     //gets the comment field
            //String preprocessed = Preprocessor.preprocessComment(comment);
            //PREPROCESSED TEXT SHOULD BE SAVEN INSIDE MONGOCONNECTOR (SOFIA)
        }
        MongoConnector mc = new MongoConnector("localhost", 27017, "djokovic");
        for(JsonObject json:jsons) {
            mc.addYoutubeComment(json.toString());
        }
        
    }
}