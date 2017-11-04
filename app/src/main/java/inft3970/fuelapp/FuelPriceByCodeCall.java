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
 * Created by shane on 15/10/2017.
 */

public class FuelPriceByCodeCall {
    private static final String TAG = FuelPriceByCodeCall.class.getSimpleName();
    private StationActivity stationActivity = new StationActivity();
    private ArrayList<HashMap<String, String>> fuelList;
    private Context context = App.getContext();
    private static String fuelCode;
    private static String[][] headers;
    private ArrayList returnFuelList;

    public ArrayList getFuelPricesByCode(String fuelCode, String[][] headers) {
        fuelList = new ArrayList<>();
        FuelPriceByCodeCall.fuelCode = fuelCode;
        FuelPriceByCodeCall.headers = headers;
        try {
            returnFuelList = new getFuelPricesCode().execute().get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Exception in FuelPriceByCodeCall " + e.getMessage());
        }
        return returnFuelList;
    }

    private class getFuelPricesCode extends AsyncTask<Void, Void, ArrayList> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList doInBackground(Void...arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String url = "https://api.onegov.nsw.gov.au/FuelPriceCheck/v1/fuel/prices/station/"+fuelCode;
            String jsonStr = httpHandler.getServiceCall(url, "GET", headers, null);
            Log.e(TAG, "response from url: " + jsonStr);
            if(jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);

                    JSONArray prices = jsonObject.getJSONArray("prices");

                    for(int countItem = 0;countItem < prices.length();countItem++) {
                        JSONObject itemPrice = prices.getJSONObject(countItem);

                        String fuelType = itemPrice.getString("fueltype");
                        Double fuelPrice = itemPrice.getDouble("price");
                        String lastUpdated = itemPrice.getString("lastupdated");

                        HashMap<String, String> priceList = new HashMap<>();
                        priceList.put("fuelType", fuelType);
                        priceList.put("price", Double.toString(fuelPrice));
                        priceList.put("lastUpdated", lastUpdated);

                        fuelList.add(priceList);
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
                                "Cannot Connect to Server. Please Try Again.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return fuelList;
        }

        @Override
        protected void onPostExecute(ArrayList result) {
            super.onPostExecute(result);
        }
    }
}
