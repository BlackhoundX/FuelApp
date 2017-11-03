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
 * Created by shane on 30/10/2017.
 */

public class PlaceIDCall {
    private final static String TAG = PlaceIDCall.class.getSimpleName();
    private StationActivity stationActivity = new StationActivity();
    private String placeID;
    private Context context = App.getContext();
    private static String placeAddress;
    private static String placeLatitude;
    private static String placeLongitude;

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

    private class getPlaceIdCall extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?keyword=" + placeAddress + "&location="+ placeLatitude +","+placeLongitude+"&radius=50&types=gas_station&key=" + context.getString(R.string.google_api_key);
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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

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
