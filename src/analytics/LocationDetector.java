package analytics;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by platico on 5/11/16.
 */
public class LocationDetector {

    private static final HttpClient defaultHttpClient = HttpClients.createDefault();
    private static final String API_KEY = "AIzaSyC3tKP3j78TJNG9CqxujOeoUrZrqB_ewfY";    //the api key

    /**
     * Method used in order to get country of a specific city using the Google Maps API
     *
     * @param city to be searched
     * @return  the name of the city if the city was found OR empty string if the city was NOT found
     */
    public static String getCountryOf(String city){

        String url = String.format("http://maps.googleapis.com/maps/api/geocode/json?address="+city+"&sensor=false"+"?key="+API_KEY);
        HttpGet httpGet = new HttpGet(url);
        String country = "";

        try {
            HttpEntity entity = defaultHttpClient.execute(httpGet).getEntity();
            String retSrc = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(retSrc); //Convert String to JSON Object
            country = parseJSONforCountry(jsonObject);   //passes json to be parsed
            if(country.equals("")){
                System.out.println("Could not find country for: "+city);
            }
            else{
                System.out.println("City: "+city);
                System.out.println("Country: "+country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return country;


    }

    /**
     * Method used in order to parse JSON file and get the country of the city
     *
     * @param jsonObject
     *
     * @return country name if country found OR empty string if country not found
     */
    private static String parseJSONforCountry(JSONObject jsonObject){

        try {

            JSONArray resultsArray = jsonObject.getJSONArray("results");
            if (resultsArray.length() > 0) {    //if it returned results for the specific city
                JSONObject firstResult = resultsArray.getJSONObject(0);     //gets the first results
                JSONArray addressComponentsArray = firstResult.getJSONArray("address_components");
                String country = "";
                for (int i = 0; i < addressComponentsArray.length(); i++) {
                    JSONObject countryObject = addressComponentsArray.getJSONObject(i);     //gets the object
                    JSONArray typeArray = countryObject.getJSONArray("types");
                    if (typeArray.get(0).equals("country")) {     //if this JSONObject is a country object
                        country = countryObject.getString("long_name");
                        break;   //breaks out of the loop
                    }

                }
                if (country.equals("")) {
                    return "";  //if no country found returns empty string
                } else {
                    return country;
                }
            } else {
                return "";  //if no country found returns empty string
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";  //if no country found returns empty string

    }



}
