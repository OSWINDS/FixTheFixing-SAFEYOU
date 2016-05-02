package combiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by gogopavl on 2/5/2016.
 *
 * Class used in order to count unique terms and their frequencies for all obtained youtube comments and tweets
 */
public class FrequencyCounter {

    private TreeMap<String, Integer> frequenciesList;//Index
    /**
    * Constructor
    */
    public FrequencyCounter(){
        frequenciesList = new TreeMap<>();
    }
    /**
     * Given any string, this function tokenizes it and adds each unique term to the
     * frequencies list (treemap). If it already exists, it only updates its value.
     */
    public void insert(String text){
        StringTokenizer tokenizer = new StringTokenizer(text, " .,;:!*^/");
        String currentToken;

        while(tokenizer.hasMoreTokens()){
            currentToken = tokenizer.nextToken().toLowerCase();
            if(!frequenciesList.containsKey(currentToken)){
                frequenciesList.put(currentToken,1);
            }
            else{
               frequenciesList.put(currentToken, frequenciesList.get(currentToken)+1);
            }
        } // End of tokenizer while loop
    }
    /**
     * Function that exports the treemap to a file with the following format:
     * "term frequency". For example: "example 18"
     */
    public void exportFrequencies() throws IOException {

        File dir = new File("frequenciesOutput");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            try{
                dir.mkdir();
            }
            catch(SecurityException se){
                //handle it
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("frequenciesOutput\\frequencies.txt")));
        String line;
        for(Map.Entry<String, Integer> entry : frequenciesList.entrySet()) {

            // Creating a string with key and value
            line = entry.getKey() + " " + entry.getValue();

            // Write line in file
            bw.write(line);
            bw.newLine();

        }
        // Close writer
        bw.close();


    }
    /**
     * Function that exports the treemap to a file with the following format:
     * "term frequency". For example: "example 18" in a descending order
     * (based on word frequency)
     */
    public void exportFrequenciesByValue() throws IOException {
        File dir = new File("frequenciesOutput");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            try{
                dir.mkdir();
            }
            catch(SecurityException se){
                //handle it
            }
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("frequenciesOutput\\frequenciesByValue.txt")));
        String line;
        for(Map.Entry<String, Integer> entry : sortByValues(frequenciesList).entrySet()) {

            // Creating a string with key and value
            line = entry.getKey() + " " + entry.getValue();

            // Write line in file
            bw.write(line);
            bw.newLine();

        }
        // Close writer
        bw.close();
    }
    /**
     * Sorts a given map by its values
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator =  new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return 1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }
}
