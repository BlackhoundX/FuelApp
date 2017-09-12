package inft3970.fuelapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
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

    public ArrayList getStationsByRadius(String[][] stationHeaders, String body) {
        stationList = new ArrayList<>();
        headers = stationHeaders;
        StationByRadiusCall.body = body;
        try {
            returnStationList = new getFuelStationsRadius().execute().get();
        } catch(ExecutionException | InterruptedException e) {
            Log.e(TAG, "Exception in StationByRadiusCall " + e.getMessage());
        }
        return returnStationList;
    }

    private class getFuelStationsRadius extends AsyncTask<Void, Void, ArrayList> {

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            fuelMap.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,
                            "Placing Pins",
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

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
                            JSONObject item = stations.getJSONObject(countItem);

                            String brand = item.getString("brand");
                            int code = item.getInt("code");
                            String name = item.getString("name");
                            String address = item.getString("address");

                            JSONObject location = item.getJSONObject("location");
                            double latitude = location.getDouble("latitude");
                            double longitude = location.getDouble("longitude");
                            double distance = location.getDouble("distance");

                            HashMap<String, String> station = new HashMap<>();
                            station.put("brand", brand);
                            station.put("code", Integer.toString(code));
                            station.put("name", name);
                            station.put("address", address);
                            station.put("latitude", Double.toString(latitude));
                            station.put("longitude", Double.toString(longitude));
                            station.put("distance", Double.toString(distance));

                            stationList.add(station);
                        }

                        for (int countItem = 0; countItem < prices.length(); countItem++) {
                            JSONObject item = prices.getJSONObject(countItem);

                            Double price = item.getDouble("price");
                            String lastUpdated = item.getString("lastupdated");

                            HashMap<String, String> priceItem = new HashMap<>();
                            priceItem.put("price", Double.toString(price));
                            priceItem.put("lastUpdated", lastUpdated);

                            stationList.add(priceItem);
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
                    Log.e(TAG, "Couldn't get json from server.");
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

            }
        }

    }
