package youtube;

import javax.json.JsonObject;
import java.util.ArrayList;

public class Main {

    public static void main(String args[]){
        YTCommentsCollector ytCommentsCollector = new YTCommentsCollector();
        ArrayList<JsonObject> jsons = ytCommentsCollector.collectComments();
        
    }
}