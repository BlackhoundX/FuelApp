package inft3970.fuelapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Class: PlaceIDCall
 * Author: Shane
 * Purpose: This class handles the gathering of the Place ID for a particular station
 * Creation Date: 30-Oct-17
 * Modification Date: 05-Nov-17
 */

public class PlaceIDCall {
    private final static String TAG = PlaceIDCall.class.getSimpleName();
    private StationActivity stationActivity = new StationActivity();
    private String placeID;
    private Context context = App.getContext();
    private static String placeAddress;
    private static String placeLatitude;
    private static String placeLongitude;

    /**
     * Method: getPlaceID
     * Purpose: This method gets the place ID of the fuel station. It takes in the address, latitude and longitude.
     * Returns: The ID of the place described, as a String.
     */
    public String getPlaceID(String placeAddress, Double placeLatitude, Double placeLongitude) {
        PlaceIDCall.placeAddress = placeAddress;
        PlaceIDCall.placeLatitude = Double.toString(placeLatitude);
        PlaceIDCall.placeLongitude = Double.toString(placeLongitude);
        try {
            placeID = new getPlaceIdCall().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "getPlaceID: " + e.getMessage());
        }
        return placeID;
    }

    /**
     * Class: getPlaceIDCall
     * Author: Shane
     * Purpose: This class functions in the background to gather the location ID
     * Creation Date: 30-Oct-17
     * Modification Date: 05-Nov-17
     */
    private class getPlaceIdCall extends AsyncTask<Void, Void, String> {

        /**
         * Method: onPreExecute
         * Purpose: This method begins the process of handling the background requests.
         * Returns: None.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Method: doInBackground
         * Purpose: This method performs the request itself, gathering the place data and
         * saving it into a String for use in the app.
         * Returns: A String containing the place ID.
         */
        @Override
        protected String doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?keyword=" + placeAddress.replaceAll(" ", "%20") + "&location="+ placeLatitude +","+placeLongitude+"&radius=50&types=gas_station&key=" + context.getString(R.string.google_api_key);
            String jsonStr = httpHandler.getServiceCall(url, "GET", null, null);
            Log.e(TAG, "response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONArray results = jsonObject.getJSONArray("results");

                    for (int countItem = 0; countItem < results.length(); countItem++) {
                        JSONObject itemResult = results.getJSONObject(countItem);
                        placeID = itemResult.getString("place_id");
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
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return placeID;
        }

        /**
         * Method: onPostExecute
         * Purpose: This method ends the background request processing.
         * Returns: None.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    /**
     * Method: fuelNameFix
     * Purpose: This method corrects an error with the NSW Fuel API, in which fuel station brands
     * using Metro Fuel are actually called Metro Petroleum. It takes in the fuel brand as a string.
     * Returns: The corrected string.
     */
    private String fuelNameFix(String fuelBrand) {
        String fuelName = null;
        if(fuelBrand.equals("Metro Fuel")) {
            fuelName = "Metro Petroleum";
        } else {
            fuelName = fuelBrand;
        }
        return fuelName;
    }
}
