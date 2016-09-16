package sentiment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom class the calculates the polarity of a word
 */
class SenticNet {

    static final double error = -5.0;
    private final Map<String, Double> polarities = new HashMap<>();

    /**
     * Constuctor of the class; Initializes SenticNet
     * @param path The system path to SenticNet's RDF
     * @throws IOException
     */
    SenticNet(String path) throws IOException {
        Document doc2 = Jsoup.parse(new File(path), "utf-8");

        Elements elements = doc2.select("rdf|rdf").select("rdf|description");

        for (Element el: elements) {
            String text = el.select("text").text();
            Double polarity = Double.parseDouble(el.select("polarity").text());
            polarities.put(text, polarity);
        }
    }

    /**
     * Gets the polarity for the given word
     * @param word The word to find its polarity
     * @return The polarity of the given word
     */
    double getPolarity(String word) {
        return polarities.getOrDefault(word, error);
    }


}
