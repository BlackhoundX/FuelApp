package inft3970.fuelapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by shane on 11/09/2017.
 */

public class StationByRadiusCall {
    private static final String TAG = StationByRadiusCall.class.getSimpleName();
    private FuelMapActivity fuelMap = new FuelMapActivity();
    private ArrayList<HashMap<String,String>> stationList;
    private Context context = App.getContext();
    private static String[][] headers;
    private static String body;
    private ArrayList returnStationList;

    public ArrayList getStationsByRadius(String[][] stationHeaders, String body, ProgressBar progressBar) {
        stationList = new ArrayList<>();
        headers = stationHeaders;
        StationByRadiusCall.body = body;
        try {
            returnStationList = new getFuelStationsRadius(progressBar).execute().get();
        } catch(ExecutionException | InterruptedException e) {
            Log.e(TAG, "Exception in StationByRadiusCall " + e.getMessage());
        }
        return returnStationList;
    }

    private class getFuelStationsRadius extends AsyncTask<Void, Void, ArrayList> {

        private final ProgressBar progressBar;

        public getFuelStationsRadius(final ProgressBar progressBar) {
            this.progressBar = progressBar;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList doInBackground(Void...arg0) {
                HttpHandler httpHdlr = new HttpHandler();
                String urlRadius = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/nearby";
                String jsonStr = httpHdlr.getServiceCall(urlRadius, "POST", headers, body);
                Log.e(TAG, "response from url: " + jsonStr);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStr);

                        JSONArray stations = jsonObject.getJSONArray("stations");
                        JSONArray prices = jsonObject.getJSONArray("prices");

                        for (int countItem = 0; countItem < stations.length(); countItem++) {
                            JSONObject itemStation = stations.getJSONObject(countItem);


                            String brand = itemStation.getString("brand");
                            int code = itemStation.getInt("code");
                            String name = itemStation.getString("name");
                            String address = itemStation.getString("address");

                            JSONObject location = itemStation.getJSONObject("location");
                            double latitude = location.getDouble("latitude");
                            double longitude = location.getDouble("longitude");
                            double distance = location.getDouble("distance");

                            Double price = 0.0;
                            String lastUpdated = null;
                            for(int countPrice = 0; countPrice < prices.length(); countPrice++) {
                                JSONObject itemPrices = prices.getJSONObject(countPrice);
                                if(itemPrices.getInt("stationcode") == itemStation.getInt("code")) {
                                    price = itemPrices.getDouble("price");
                                    lastUpdated = itemPrices.getString("lastupdated");
                                }
                            }



                            HashMap<String, String> station = new HashMap<>();
                            station.put("brand", brand);
                            station.put("code", Integer.toString(code));
                            station.put("name", name);
                            station.put("address", address);
                            station.put("latitude", Double.toString(latitude));
                            station.put("longitude", Double.toString(longitude));
                            station.put("distance", Double.toString(distance));
                            station.put("price", Double.toString(price));
                            station.put("lastUpdated", lastUpdated);

                            stationList.add(station);
                        }

                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        fuelMap.runOnUiThread(new Runnable() {
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
                    Log.e(TAG, "No Connection. Try Again Later");
                    fuelMap.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }

            return stationList;
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            }
        }

    }
