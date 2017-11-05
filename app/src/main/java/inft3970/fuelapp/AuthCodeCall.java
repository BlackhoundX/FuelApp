package inft3970.fuelapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Class: AuthCodeCall
 * Author: Shane
 * Purpose: This class handles the gathering of the Authorisation code, for use with NSW Fuel API.
 * Creation Date: 11-Sep-17
 * Modification Date: 05-Nov-17
 */

public class AuthCodeCall {
    private static final String TAG = AuthCodeCall.class.getSimpleName();
    private ProgressDialog pDialog;
    private FuelMapActivity fuelMap = new FuelMapActivity();
    private Context context = App.getContext();
    private String authCode;
    private String returnCode;
    private static String[] authHeaders;

    /**
     * Method: getAuthCode
     * Purpose: This method creates an instance of the call for authorisation code, and returns the
     * code upon success.
     * Returns: String containing the authorisation code
     */
    public String getAuthCode(String[] authHeader, ProgressBar progressBar) {
        authHeaders = authHeader;
        try {
            returnCode = new getAuthOCode(progressBar).execute().get();
        } catch(ExecutionException | InterruptedException e) {
            Log.e(TAG, "Exception in AuthCodeCall: " + e.getMessage());
        }
        return returnCode;
    }

    /**
     * Class: getAuthOCode
     * Author: Shane
     * Purpose: This class handles the actual gathering of the Authorisation code, for use with NSW Fuel API.
     * Creation Date: 11-Sep-17
     * Modification Date: 05-Nov-17
     */
    private class getAuthOCode extends AsyncTask<Void, Void, String> {

        private ProgressBar progressBar;

        /**
         * Method: getAuthOCode
         * Purpose: This acts as the constructor for the class.
         * Returns: None.
         */
        public getAuthOCode(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        /**
         * Method: onPreExecute
         * Purpose: This method begins the process of handling the background requests.
         * Returns: None.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        /**
         * Method: doInBackground
         * Purpose: This method performs the request itself, gathering the authorisation code for use in the app.
         * Returns: A string containing the authorisation code.
         */
        @Override
        protected String doInBackground(Void... arg0) {
            HttpHandler httpHdlr = new HttpHandler();
            String urlAuthCode = "https://api.onegov.nsw.gov.au/oauth/client_credential/accesstoken?grant_type=client_credentials";
            String jsonStr = httpHdlr.getAuthServiceCall(urlAuthCode, "GET", authHeaders, null);
            Log.e(TAG, "response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject authList = new JSONObject(jsonStr);
                    authCode = authList.getString("access_token");
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
            return authCode;
        }

        /**
         * Method: onPostExecute
         * Purpose: This method ends the background request processing.
         * Returns: None.
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
        }
    }

}
