package inft3970.fuelapp;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class: PlaceDetails
 * Author: Shane
 * Purpose: This class stores the details of each station as an individual object. It contains the
 * necessary Getters and Setters to allow the data to be readily accessed.
 * Creation Date: 01-Nov-17
 * Modification Date: 05-Nov-17
 */

public class PlaceDetails {
    private String phoneNumber;
    private Double rating;
    private ArrayList<String> openTimes;
    private ArrayList<HashMap<String, String>> reviews;
    private String website;

    /**
     * Method: setPhoneNumber
     * Purpose: Sets the phone number of the place. Takes a String as input.
     * Returns: None.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Method: setRating
     * Purpose: Sets the rating of the place. Takes a Double as input.
     * Returns: None.
     */
    public void setRating(Double rating) {
        this.rating = rating;
    }

    /**
     * Method: setOpenTimes
     * Purpose: Sets the open times of the place. Takes an array list of Strings as input.
     * Returns: None.
     */
    public void setOpenTimes(ArrayList<String> openTimes) {
        this.openTimes = openTimes;
    }

    /**
     * Method: setReviews
     * Purpose: Sets the reviews of the place. Takes an array list containing a hashmap, which contains
     * each review, as input.
     * Returns: None.
     */
    public void setReviews(ArrayList<HashMap<String, String>> reviews) {
        this.reviews = reviews;
    }

    /**
     * Method: setWebsite
     * Purpose: Sets the website of the place. Takes a String as input.
     * Returns: None.
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Method: getPhoneNumber
     * Purpose: Gets the website of the place.
     * Returns: Returns the String containing the phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Method: getRating
     * Purpose: Gets the rating of the place.
     * Returns: Returns the Double containing the rating.
     */
    public Double getRating() {
        return rating;
    }

    /**
     * Method: getOpenTimes
     * Purpose: Gets the open times for the place.
     * Returns: Returns the Array List containing the open times.
     */
    public ArrayList getOpenTimes() {
        return openTimes;
    }

    /**
     * Method: getReviews
     * Purpose: Gets the reviews of the place.
     * Returns: Returns the Array List containing the reviews.
     */
    public ArrayList getReviews() {
        return reviews;
    }

    /**
     * Method: getWebsite
     * Purpose: Gets the website of the place.
     * Returns: Returns the String containing the website.
     */
    public String getWebsite() {
        return  website;
    }
}
