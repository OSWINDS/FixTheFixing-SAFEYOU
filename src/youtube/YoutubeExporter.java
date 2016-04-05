package youtube;

import mongo.MongoConnector;

import javax.json.JsonObject;
import java.util.ArrayList;

public class YoutubeExporter {

    public static void main(String args[]){
        YTCommentsCollector ytCommentsCollector = new YTCommentsCollector();
        ArrayList<JsonObject> jsons = ytCommentsCollector.collectComments();
        MongoConnector mc = new MongoConnector("localhost", 27017, "djokovic");
        for(JsonObject json:jsons) {
            mc.addYoutubeComment(json.toString());
        }
        
    }
}