package youtube;

import combiner.Preprocessor;
import mongo.MongoConnector;

import javax.json.JsonObject;
import java.util.ArrayList;

public class YoutubeExporter {

    private static String databaseName;

    public static void main(String args[]){
        YTCommentsCollector ytCommentsCollector = new YTCommentsCollector();
        ArrayList<JsonObject> jsons = ytCommentsCollector.collectComments(args[4].split("=")[1]);
        for(JsonObject json : jsons){
            String comment = json.getString("comment");     //gets the comment field
            //String preprocessed = Preprocessor.preprocessComment(comment);
            //PREPROCESSED TEXT SHOULD BE SAVED INSIDE MONGOCONNECTOR (SOFIA)
        }

        for (String parameter : args) {
            String[] parameterSplit = parameter.split("=");

            if (parameterSplit[0].equals("theme")) {
                databaseName = parameterSplit[1];
            }
        }
        MongoConnector mc = new MongoConnector("localhost", 27017, databaseName);
        for(JsonObject json:jsons) {
            mc.addYoutubeComment(json.toString());
        }
        
    }
}