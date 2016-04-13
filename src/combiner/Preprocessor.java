package combiner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Antigoni Founta on 13/4/2016.
 */

public class Preprocessor {

    private static ArrayList<String> stopwordsList = new ArrayList<>(Arrays.asList("a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "cs", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldnt", "course", "currently", "definitely", "described", "despite", "did", "didnt", "different", "do", "does", "doesnt", "doing", "dont", "done", "down", "downwards", "during", "each", "edu", "eg", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "happens", "hardly", "has", "hasnt", "have", "havent", "having", "he", "hes", "hello", "help", "hence", "her", "here", "heres", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "im", "ive", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isnt", "it", "itd", "itll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that", "thats", "thats", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "theres", "thereafter", "thereby", "therefore", "therein", "theres", "thereupon", "these", "they", "theyd", "theyll", "theyre", "theyve", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasnt", "way", "we", "wed", "well", "were", "weve", "welcome", "well", "went", "were", "werent", "what", "whats", "whatever", "when", "whence", "whenever", "where", "wheres", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldnt", "yes", "yet", "you", "youd", "youll", "youre", "youve", "your", "yours", "yourself", "yourselves", "zero"));

    //Processor of tweets
    public static String preprocessTweet(String input){
        String[] temp = tokenizer(input);
        String output = "";
        for(String s : temp){
            if(isWhitelisted(s)){
                if(output!=null){
                    output = output + " ";
                }
                output = output + s;
            }
        }

        output = finalizeText(output);
        return output;
    }

    //Processor of youtube comments
    public static void preprocessComment(){

    }

    /**
     * Method checking if a string is whitelisted
     *
     * A string is called whitelisted if it's not a stopword or a mentrion or a number or a url
     * @param str
     * @return
     */
    private static boolean isWhitelisted(String str){
        if (isMention(str) == false && isStopWord(str) == false && isNumeric(str) == false && isURL(str) == false){
            return true;
        }
        else{
            return false;
        }
    }

    private static Boolean isMention(String str){
        if(str.charAt(0) == '@' )
            return true;
        else
            return false;
    }


    private static boolean isURL(String str){
        try{
            URL url = new URL(str);
            return true;
        }
        catch (MalformedURLException e){
            return false;
        }
    }

    private static boolean isNumeric(String str){
        if (str.matches(".*\\d.*")) //Old: str.matches("[-+]?\\d*\\.?\\d+")
            return true;
        else
            return false;
    }

    private static boolean isStopWord(String str){
        if(stopwordsList.contains(str))
            return true;
        else
            return false;
    }
    private static String[] tokenizer(String input){
        return input.split(" ");
    }

    private static String toLowerCase(String input){
        return input.toLowerCase();
    }

    private static String removePunctuation(String input){
        input = input.replaceAll("[.,:;()\\[\\]{}?_\\-!\'*\"@#$%^&+=|~`><]", " ");
        input = input.replaceAll("\\s+"," ");
        input = input.trim();

        return input;
    }

    private static String removeSingleCharacter(String input){
        input = input.replaceAll("\\s[a-z]\\s", " ");
        input = input.replaceAll("\\s+"," ");
        input = input.trim();

        return input;
    }

    //Removes useless characters like punctuation and single characters and transforms to lowercase
    private static String finalizeText(String input){
        input = toLowerCase(input);
        input = removePunctuation(input);
        input = removeSingleCharacter(input);

        return input;
    }

}
