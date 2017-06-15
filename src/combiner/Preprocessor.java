package combiner;

import analytics.FrequencyCounter;
import mongo.MongoConnector;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Class to process text (tweets and comments)
 * Created by Antigoni Founta on 13/4/2016.
 */

public class Preprocessor {

    private static ArrayList<String> stopwordsList;
    private static MongoConnector mc;
    private static ArrayList<String> substancesList;
    private static HashSet<String> substancesSet;

    static {
        stopwordsList = new ArrayList<>(Arrays.asList("a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"));
        substancesSet = new HashSet<>();
        BufferedReader in;
        try {
            String filename = "./resources/substancesFinal.txt";
            in = new BufferedReader(new FileReader(filename));
            String line;
            while((line = in.readLine()) != null)
            {
                substancesSet.add(line.toLowerCase());
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        substancesList = new ArrayList<>(substancesSet);
    }

    /**
     * Processes all the comments' text
     * @param collectionName The collection to be processed
     * @param fc A FrequencyCounter object for counting frequencies
     * @throws JSONException
     */
    public static void preprocessComments(String collectionName, FrequencyCounter fc) throws JSONException {
        mc = new MongoConnector("localhost", 27017, collectionName);
        HashMap<ObjectId,JSONObject> comments = mc.getComments();
        String parsedComment;
        for(ObjectId id : comments.keySet()) {
            parsedComment = comments.get(id).getString("comment"); // Get comment's text to process
            parsedComment = preprocessComment(parsedComment);
            mc.insertParsedComment(id,parsedComment);
            fc.insert(parsedComment); //insert to treemap
        }
    }

    /**
     * Processes all the tweets' text
     * @param collectionName The collection to be processed
     * @param fc A FrequencyCounter object for counting frequencies
     * @throws JSONException
     */
    public static void preprocessTweets(String collectionName, FrequencyCounter fc, String substancesExist) throws JSONException {
        mc = new MongoConnector("localhost", 27017, collectionName);
        HashMap<ObjectId,JSONObject> tweets = mc.getTweets();
        String parsedTweet;
        String tweet;
        for(ObjectId id : tweets.keySet()) {
            tweet= tweets.get(id).getString("text"); // Get tweet's text to process
            parsedTweet = preprocessTweet(tweet); // Process it
            mc.insertParsedTweet(id,parsedTweet); // Insert it in the DB
            fc.insert(parsedTweet); // Insert to treemaps
            if(substancesExist.compareTo("y") == 0) {
                String substancesString = getSubstances(parsedTweet, tweet);
                // System.out.println("Substances: " + substancesString + " in tweet: " + parsedTweet);
                mc.insertSubstancesTwitter(id,substancesString);
            }
        }
    }

    /**
     * Checks whether the parsed tweet contains any substances names
     * @param parsedTweet The parsed tweet to be searched
     * @param tweet The tweet to be searched
     * @return A string that contains all the substances mentioned in the tweet
     */
    private static String getSubstances(String parsedTweet, String tweet) {
        /*Optional<String> substances;
        substances = substancesList.parallelStream().filter(tweet::contains).findAny();
        if(substances.isPresent()) {
            System.out.println("SUBSTANCES: " + substances);
            return substances.get();
        } else {
            return "";
        }*/
        HashSet<String> subs = new HashSet<>();
        for(String sub:substancesList) {
            if(parsedTweet.contains(sub.toLowerCase()) || tweet.contains(sub)) {
                subs.add(sub);
            }
        }
        return subs.toString();
    }


    /**
     * Processes a tweet's text
     * @param input The tweet's text before the processing
     * @return The parsed tweet text
     */
    public static String preprocessTweet(String input){
        input = prepareText(input);
        String[] tokens = tokenizer(input);

        StringBuilder builder = new StringBuilder("");
        for(String s : tokens){
            if(isWhitelisted(s)==true && !isMention(s, '@')){      //if the word is whitelisted and its not a 'Google Plus' mention
                builder.append(" ").append(s);
            }
        }

        return builder.toString();
    }

    /**
     * Processes a comments's text
     * @param input The comment's text before the processing
     * @return The parsed comment text
     */
    private static String preprocessComment(String input){
        input = prepareText(input);
        String[] tokens = tokenizer(input);     //tokenizes the input string
        StringBuilder builder = new StringBuilder("");
        for(String s : tokens){
            if(isWhitelisted(s) && !isMention(s, '+')){      //if the word is whitelisted and its not a 'Google Plus' mention
                builder.append(" ").append(s);
            }
        }
        return builder.toString();
    }

    /**
     * Method checking if a string is white-listed
     * A string is called white-listed if it's not a stop-word or a number or a URL
     * @param str The string to be checked
     * @return True if the string is white-listed, false otherwise
     */
    private static boolean isWhitelisted(String str){
        return !isStopWord(str) && !isNumeric(str);
    }

    /**
     * Checks whether a string contains mentions or not
     * @param str The string to be checked
     * @param mentionChar The mention identifying character
     * @return True, if the string contains a mention, or false otherwise
     */
    private static Boolean isMention(String str,char mentionChar) {
        return str.length() == 0 || str.charAt(0) == mentionChar;
    }

    /**
     * Checks whether a string contains URLs or not
     * @param str The string to be checked
     * @return True, if the string contains a URL, or false otherwise
     */
    private static boolean isURL(String str){
        try {
            URL url = new URL(str);
            return true;
        } catch (MalformedURLException e){
            return false;
        }
    }

    /**
     * Checks whether a string is a URL or not
     * @param str The string to be checked
     * @return output, the rest of the string without the URL
     */
    private static String urlRemoval(String str){
        String[] tmp = tokenizer(str);
        StringBuilder output = new StringBuilder();

        for(String token:tmp)
            if(!isURL(token)){output.append(token + " ");}

        return output.toString();
    }

    /**
     * Check whether a string contains numeric characters or not
     * @param str The string to be checked
     * @return True, if the string contains numeric characters, or false otherwise
     */
    private static boolean isNumeric(String str){
        return str.matches(".*\\d.*");
    }

    /**
     * Check whether a string contains stop-words or not
     * @param str The string to be checked
     * @return True, if the string contains stop-words, or false otherwise
     */
    private static boolean isStopWord(String str){
        return stopwordsList.contains(str);
    }

    /**
     * Separates string into words based on whitespaces
     * @param input The string to be tokenized
     * @return An array of the strings' words
     */
    private static String[] tokenizer(String input){
        return input.split(" ");
    }

    /**
     * Converts all characters to lower case
     * @param input The string to be turned into lower case
     * @return The string converted to lower case
     */
    private static String toLowerCase(String input){
        return input.toLowerCase();
    }

    /**
     * Removes all punctuation
     * @param input The string to be stripped from punctuation
     * @return The strig without punctuation
     */
    private static String removePunctuation(String input){
        input = input.replaceAll("_","");
        return input.replaceAll("\\W", " ");
    }

    /**
     * Removes all single characters from a string
     * @param input The string to be edited
     * @return The string without punctuation
     */
    private static String removeSingleCharacter(String input){
        return input.replaceAll("\\b[a-z]\\b", " ");
    }

    /**
     * Removes useless characters like punctuation and single characters and transforms to lowercase
     * @param input The string to be checked
     * @return The string finalized
     */
    private static String prepareText(String input){
        input = toLowerCase(input);
        input = urlRemoval(input);
        input = removePunctuation(input);
        input = removeSingleCharacter(input);
        input = input.replaceAll("\\s+"," ");
        input = input.trim();

        return input;
    }

}
