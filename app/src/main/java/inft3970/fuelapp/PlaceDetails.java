package inft3970.fuelapp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shane on 1/11/2017.
 */

public class PlaceDetails {
    private String phoneNumber;
    private Double rating;
    private ArrayList<String> openTimes;
    private ArrayList<HashMap<String, String>> reviews;
    private String website;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRating(Double rating) { this.rating = rating; }

    public void setOpenTimes(ArrayList<String> openTimes) {
        this.openTimes = openTimes;
    }

    public void setReviews(ArrayList<HashMap<String, String>> reviews) {
        this.reviews = reviews;
    }

    public void setWebsite(String website) { this.website = website; }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Double getRating() { return rating; }

    public ArrayList getOpenTimes() {
        return openTimes;
    }

    public ArrayList getReviews() {
        return reviews;
    }

    public String getWebsite() { return  website; }
}
