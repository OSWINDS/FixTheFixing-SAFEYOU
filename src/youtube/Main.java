package youtube;

import java.util.ArrayList;

public class Main {

    public static void main(String args[]){
        YTCommentsCollector ytCommentsCollector = new YTCommentsCollector();
        ArrayList<String> comments = ytCommentsCollector.collectComments();
        System.out.println("Total comments : "+comments.size());
    }
}