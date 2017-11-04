package inft3970.fuelapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by shane on 1/11/2017.
 */

public class PlaceDetailsCall {
    private final static String TAG = PlaceDetailsCall.class.getSimpleName();
    private StationActivity stationActivity = new StationActivity();
    private Context context = App.getContext();
    private PlaceDetails placeDetails = new PlaceDetails();
    private static String placeID;

    public PlaceDetails getPlaceDetails(String placeID) {
        PlaceDetailsCall.placeID = placeID;
        try {
            placeDetails = new getPlaceDetailsCall().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "getPlaceDetails: " + e.getMessage());
        }
        return placeDetails;
    }

    private class getPlaceDetailsCall extends AsyncTask<Void, Void, PlaceDetails> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected PlaceDetails doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+ placeID + "&key=" + context.getString(R.string.google_api_key);
            String jsonStr = httpHandler.getServiceCall(url, "GET", null, null);
            Log.e(TAG, "response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject result = jsonObject.getJSONObject("result");

                    if(result.has("formatted_phone_number")) {
                        String phoneNumber = result.getString("formatted_phone_number");
                        placeDetails.setPhoneNumber(phoneNumber);
                    }

                    if(result.has("rating")) {
                        Double rating = result.getDouble("rating");
                        placeDetails.setRating(rating);
                    }

                    if(result.has("opening_hours")) {
                        ArrayList<String> openTimes = new ArrayList<>();
                        JSONObject openingHours = result.getJSONObject("opening_hours");
                        JSONArray openTimesArray = openingHours.getJSONArray("weekday_text");
                        for (int countItem = 0; countItem < openTimesArray.length(); countItem++) {
                            String openTimeText = openTimesArray.getString(countItem);
                            openTimes.add(openTimeText);
                        }
                        placeDetails.setOpenTimes(openTimes);
                    }

                    if(result.has("reviews")) {
                        ArrayList<HashMap<String, String>> reviewsArray = new ArrayList<>();
                        JSONArray reviews = result.getJSONArray("reviews");
                        for (int countItem = 0; countItem < reviews.length(); countItem++) {
                            JSONObject review = reviews.getJSONObject(countItem);
                            String authorName = review.getString("author_name");
                            String photoURL = review.getString("profile_photo_url");
                            Double reviewRating = review.getDouble("rating");
                            String time = review.getString("relative_time_description");
                            String reviewText = review.getString("text");

                            HashMap<String, String> reviewHash = new HashMap<>();
                            reviewHash.put("authorName", authorName);
                            reviewHash.put("photoUrl", photoURL);
                            reviewHash.put("rating", Double.toString(reviewRating));
                            reviewHash.put("time", time);
                            reviewHash.put("reviewText", reviewText);

                            reviewsArray.add(reviewHash);
                        }
                        placeDetails.setReviews(reviewsArray);
                    }

                    if(result.has("website")) {
                        String website = result.getString("website");
                        placeDetails.setWebsite(website);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    stationActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                stationActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,
                                "Cannot get Station Details.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            return placeDetails;
        }

        @Override
        protected void onPostExecute(PlaceDetails result) {
            super.onPostExecute(result);
        }
    }
}
