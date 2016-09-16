package sentiment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import rita.RiWordNet;

/**
 * Calculates all the representative words per basic emotion
 */
class SentimentAnalysis {

    /**
     * Eliminates duplicate values from an ArrayList of strings
     * @param l The input list
     * @return The list without duplicate values
     */
    private ArrayList<String> eliminateDuplicates(ArrayList<String> l) {
        Set<String> hs = new HashSet<>();
        hs.addAll(l); // By adding elements in a set duplicates are eliminated by definition
        l.clear();
        l.addAll(hs);

        return l;
    }

    /**
     * Reads secondary emotions from file and maps them to basic emotions
     * @return A HashMap that maps each basic emotion to a list of related secondary emotions
     */
    private HashMap<Emotions,ArrayList<String>> getSecondaryEmotions() {

        HashMap<Emotions,ArrayList<String>> secondaryEmotions = new HashMap<>();

        for(Emotions e : Emotions.values()) {
            secondaryEmotions.put(e,null);
        }

        try (BufferedReader br = new BufferedReader(new FileReader("data/secondary_emotions.txt"))) {
            String line;
            int lineNo = 0; // Each line represents a basic emotion
            while ((line = br.readLine()) != null) {
                String[] secondaryArray = line.split("\t"); // All secondary emotions in an array of strings
                ArrayList<String> secondaryList = new ArrayList<>(Arrays.asList(secondaryArray)); // to ArrayList
                secondaryEmotions.put(Emotions.values()[lineNo],secondaryList); // Add secondary emotions to map
                lineNo++; // Next basic emotion
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return secondaryEmotions;
    }

    /**
     * Reads emoticons from file (Encoding: UTF-8 no BOM) and maps them to basic emotions
     * @return A HashMap that maps each basic emotion to a list of related emoticons
     */
    private HashMap<Emotions,ArrayList<String>> getEmoticons() {
        HashMap<Emotions,ArrayList<String>> emoticons = new HashMap<>();

        // Initialization of the map
        for(Emotions e : Emotions.values()) {
            emoticons.put(e,new ArrayList<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader("data/emoticons.txt"))) {
            String line;
            while ((line = br.readLine()) != null) { // Each line represents an emoticon
                String[] lineString = line.split("\t");
                String emotion = lineString[0].toUpperCase(); // The basic emotion the emoticon belongs to
                String emoticon = lineString[3]; // The emoticon
                emoticons.get(Emotions.valueOf(emotion)).add(emoticon); // Adds emoticon to the correct bucket/list
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return emoticons;
    }

    /**
     * Finds synonyms of secondary emotions using WordNet through rita API
     * @param secondaryEmotions The secondary emotions for each basic emotion
     * @return A HashMap that maps each basic emotion to a list of related synonyms based on its secondary emotions
     */
    private HashMap<Emotions,ArrayList<String>> getSynonyms(HashMap<Emotions,ArrayList<String>> secondaryEmotions) {
        RiWordNet wordnet = new RiWordNet("C:\\Program Files (x86)\\WordNet\\2.1"); // The WordNet directory
        HashMap<Emotions,ArrayList<String>> synonyms = new HashMap<>();

        for(Emotions e : Emotions.values()) { // For each basic emotion
            synonyms.put(e,null); // Add the emotion to the map
            ArrayList<String> tempSynonyms = new ArrayList<>(); // Includes all synonyms for a basic emotion
            for(String s : secondaryEmotions.get(e)) { // For each secondary emotion
                String[] emotionSynonymsNouns = wordnet.getAllSynonyms(s,"n"); // Get all noun synonyms
                String[] emotionSynonymsVerbs = wordnet.getAllSynonyms(s,"v"); // Get all verb synonyms
                String[] emotionSynonymsAdjectives = wordnet.getAllSynonyms(s,"a"); // Get all adjective synonyms
                String[] emotionSynonymsAdverbs = wordnet.getAllSynonyms(s,"r"); // Get all adverb synonyms
                // Turn string arrays into collections
                List<String> auxList1 = Arrays.asList(emotionSynonymsNouns);
                List<String> auxList2 = Arrays.asList(emotionSynonymsAdjectives);
                List<String> auxList3 = Arrays.asList(emotionSynonymsVerbs);
                List<String> auxList4 = Arrays.asList(emotionSynonymsAdverbs);
                // Add all synonyms to a single list
                tempSynonyms.addAll(auxList1);
                tempSynonyms.addAll(auxList2);
                tempSynonyms.addAll(auxList3);
                tempSynonyms.addAll(auxList4);
                tempSynonyms.remove("love");
                if(e == Emotions.JOY) {
                    tempSynonyms.add("love");
                }
            }
            synonyms.put(e,tempSynonyms);
        }

        return synonyms;
    }

    /**
     * Maps each basic emotion to a list that includes the basic emotion
     * @return A HashMap that maps each basic emotion to a list that includes the basic emotion
     */
    private HashMap<Emotions,ArrayList<String>> getBasicEmoticons() {
        HashMap<Emotions,ArrayList<String>> basicEmotions = new HashMap<>();

        for(Emotions e : Emotions.values()) {
            ArrayList<String> aux = new ArrayList<>();
            aux.add(e.toString().toLowerCase());
            basicEmotions.put(e, aux);
            basicEmotions.put(e,aux);
        }
        return basicEmotions;
    }
    /**
     * Finds all representative words for each basic emotion
     * @return A HashMap that maps each basic emotion to a list of representative words/phrases
     */
    HashMap<Emotions,ArrayList<String>> getRepresentativeWords() {
        // Maps each basic emotion to a list of secondary emotions
        HashMap<Emotions,ArrayList<String>> secondaryEmotions = getSecondaryEmotions();
        //Maps each basic emotion to a list of related emoticons
        HashMap<Emotions,ArrayList<String>> emoticons = getEmoticons();
        // Maps each basic emotion to a list of synonyms of its secondary emotions
        HashMap<Emotions,ArrayList<String>> synonyms = getSynonyms(secondaryEmotions);
        // Maps each basic emotion to a list that includes the emotion string
        HashMap<Emotions, ArrayList<String>> basicEmotions = getBasicEmoticons();
        // Maps each basic emotion to a list of representative words

        HashMap<Emotions,ArrayList<String>> representativeWords = new HashMap<>();

        for(Emotions e : Emotions.values()) { // For each basic emotion
            // Join the above lists
            ArrayList<String> representativesTemp = new ArrayList<>(secondaryEmotions.get(e));
            representativesTemp.addAll(emoticons.get(e));
            representativesTemp.addAll(synonyms.get(e));
            representativesTemp.addAll(basicEmotions.get(e));
            representativesTemp = eliminateDuplicates(representativesTemp);
            //representativesTemp.sort(String.CASE_INSENSITIVE_ORDER);
            System.out.println("Emotion: " + e);
            for(String word:representativesTemp) {
                System.out.println("Word: " + word);
            }
            System.out.println("-------------------------------------------------------------------------------------------");
            representativeWords.put(e,representativesTemp);
        }

        return representativeWords;
    }
}
