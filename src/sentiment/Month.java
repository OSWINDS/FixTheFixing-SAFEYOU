package sentiment;


/**
 * Class to represent all the month related information for sentiment analysis
 * Created by sifantid on 19/9/2016.
 */
public class Month {
    private String month;
    private String year;

    private Double angerCount;
    private Double disgustCount;
    private Double fearCount;
    private Double joyCount;
    private Double sadnessCount;
    private Double surpriseCount;

    private int count;

    public Month(String month, String year) {
        this.month = month;
        this.year = year;
        this.angerCount = 0.0;
        this.disgustCount = 0.0;
        this.fearCount = 0.0;
        this.joyCount = 0.0;
        this.sadnessCount = 0.0;
        this.surpriseCount = 0.0;
        this.count = 0;
    }

    void addFeelingCount(Double angerCount, Double disgustCount, Double fearCount, Double joyCount,
                         Double sadnessCount, Double surpriseCount) {
        this.angerCount = this.angerCount + angerCount;
        this.disgustCount = this.disgustCount + disgustCount;
        this.fearCount = this.fearCount + fearCount;
        this.joyCount = this.joyCount + joyCount;
        this.sadnessCount = this.sadnessCount + sadnessCount;
        this.surpriseCount = this.surpriseCount + surpriseCount;
        incrementCount();
    }

    void finalizeFeelings() {
        angerCount /= count;
        disgustCount /= count;
        fearCount /= count;
        joyCount /= count;
        sadnessCount /= count;
        surpriseCount /= count;
    }

    private void incrementCount() {
        count += 1;
    }

    public String getMonth() {

        return month;
    }

    String getYear() {
        return year;
    }

    Double getAngerCount() {
        return angerCount;
    }

    Double getDisgustCount() {
        return disgustCount;
    }

    Double getFearCount() {
        return fearCount;
    }

    Double getJoyCount() {
        return joyCount;
    }

    Double getSadnessCount() {
        return sadnessCount;
    }

    Double getSurpriseCount() {
        return surpriseCount;
    }

    int getCount() {
        return count;
    }
}
