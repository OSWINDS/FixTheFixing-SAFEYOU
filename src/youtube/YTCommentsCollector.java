package youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for collecting comments from youtube using the YouTube API
 *
 * @author Kostas Platis
 *
 */
public class YTCommentsCollector {

    private static YouTube youtube;
    private static JsonBuilderFactory factory = Json.createBuilderFactory(null);
    private static final HttpClient defaultHttpClient = HttpClients.createDefault();
    private static final String API_KEY = "AIzaSyC3tKP3j78TJNG9CqxujOeoUrZrqB_ewfY";    //the api key



    public ArrayList<JsonObject> collectComments(String filenameArgument) {

        System.out.println("Started collecting comments...");

        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
        ArrayList<String> videoIDs = fetchVideoIds(filenameArgument);   //fetching the videoIDs
        ArrayList<JsonObject> jsons = new ArrayList<>();

        try {
            //getting the credentials from Auth Class
            Credential credential = Auth.authorize(scopes, "commentthreads");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName("fix_the_fixing").build();

            for (String videoID : videoIDs) {     //for every video ID
                System.out.println("Collecting comments for videoID: "+videoID);
                //gets the comment thread list response
                CommentThreadListResponse commentThreadListResponse = youtube.commentThreads().list("snippet").setVideoId(videoID).setTextFormat("plainText").setMaxResults(100L).execute();
                List<CommentThread> videoComments = commentThreadListResponse.getItems();   //gets the comments from the list

                for (CommentThread videoComment : videoComments) {    //for every comment

                    CommentSnippet snippet = videoComment.getSnippet().getTopLevelComment().getSnippet();
                    String comment = snippet.getTextDisplay();
                    String authorID = snippet.getAuthorChannelId().toString();

                    String userURL = snippet.getAuthorGoogleplusProfileUrl();
                    if(userURL!=null){      //if the user still exists

                        String[] splitted = userURL.split("/");
                        String userID = splitted[splitted.length-1];    //gets the user ID

                        String url = String.format("https://www.googleapis.com/plus/v1/people/"+userID+"?key="+API_KEY);    //creates the user api URL

                        HttpGet httpGet = new HttpGet(url);
                        HttpEntity entity = defaultHttpClient.execute(httpGet).getEntity();

                        String[] userDetails = getDetailsFromJSON(entity);      //gets the user details

                        JsonObject jsonObject = createJSONfor(comment,authorID,userDetails[0],userDetails[1],userDetails[2]);
                        jsons.add(jsonObject);

                    }



                    /*      Comment's replies part

                    // Will use this thread as parent to new reply.
                    String parentId = videoComment.getId();
                    System.out.println("parentId = "+parentId);

                    // Call the YouTube Data API's comments.list method to retrieve
                    // existing comment
                    // replies.
                    CommentListResponse commentsListResponse = youtube.comments().list("snippet").setParentId(parentId).setTextFormat("plainText").execute();
                    List<Comment> commentReplies = commentsListResponse.getItems();

                    if (commentReplies.isEmpty()) {
                        System.out.println("This comment does not have replies...");
                    } else {
                        // Print information from the API response.
                        System.out.println("\n================== Returned Comment Replies ==================\n");
                        for (Comment commentReply : commentReplies) {
                            CommentSnippet commentSnippet = commentReply.getSnippet();
                            System.out.println("  - Author: " + commentSnippet.getAuthorDisplayName());
                            System.out.println("  - Comment: " + commentSnippet.getTextDisplay());
                            allComments.add(commentSnippet.getTextDisplay());
                            System.out.println("\n-------------------------------------------------------------\n");
                        }
                    }
                    */
                }
                System.out.println("Finished collecting comments...");

        }
        } catch (GoogleJsonResponseException e){
            e.getDetails();
        } catch(IOException e){
            e.printStackTrace();
        }

        return jsons;   //returns the jsons
    }

    /**
     * Method responsible for getting the user details from JSON
     *
     * @param entity
     * @return
     */
    private String[] getDetailsFromJSON(HttpEntity entity){
        String[] details = new String[3];
        String retSrc = null;

        if(entity!=null) {  //null => user does not exist

            try {
                retSrc = EntityUtils.toString(entity);

                JSONObject jsonObject = new JSONObject(retSrc); //Convert String to JSON Object
                if (jsonObject.has("gender")) {     //if it has a gender attribute
                    String gender = jsonObject.getString("gender");
                    details[0] = gender;
                } else {
                    details[0] = "-";
                }
                String place = "-";
                if (jsonObject.has("placesLived")) {//if it has a 'placesLived' attribute
                    JSONArray places = jsonObject.getJSONArray("placesLived");
                    for (int i = 0; i < places.length(); i++) {     //parse in order to find the primary location
                        JSONObject temp = (JSONObject) places.get(i);
                        if (temp.has("primary")) {      //if its the primary location, we save it
                            place = temp.getString("value");     //gets the place
                            break;
                        }
                    }
                    details[1] = place;
                } else {
                    details[1] = "-";
                }
                if (jsonObject.has("birthday")) {
                    String birthday = jsonObject.getString("birthday");
                    details[2] = "-";
                } else {
                    details[2] = "-";
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else{
            details[0] = "-";
            details[1] = "-";
            details[2] = "-";
        }
        return details;
    }

        /**
     * Method responsible for fetching video ids from the videoIDs.txt file
     *
     * @return a list with the
     */
    private ArrayList<String> fetchVideoIds(String filenameArgument){

        String filename = "./resources/"+filenameArgument+".txt";

        ArrayList<String> videoIDs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {    //for every line of the file
            String line;
            while ((line = br.readLine()) != null) {
                videoIDs.add(line);     //adds the videoID in the list
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return videoIDs;

    }

    private JsonObject createJSONfor(String comment,String authorID,String gender,String location,String birthday){

        JsonObject jsonObject = factory.createObjectBuilder()
                .add("comment", comment)
                .add("authorID",authorID)
                .add("gender",gender)
                .add("location",location)
                .add("birthday",birthday)
                .build();

        return jsonObject;
    }

}
