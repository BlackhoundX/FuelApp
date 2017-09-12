package inft3970.fuelapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

/**
 * Created by shane on 11/09/2017.
 */

public class AuthCodeCall {
    private static final String TAG = AuthCodeCall.class.getSimpleName();
    private ProgressDialog pDialog;
    private FuelMapActivity fuelMap = new FuelMapActivity();
    private Context context = App.getContext();
    private String authCode;
    private String returnCode;
    private static String[] authHeaders;
    public String getAuthCode(String[] authHeader) {
        authHeaders = authHeader;
        try {
            returnCode = new getAuthOCode().execute().get();
        } catch(ExecutionException | InterruptedException e) {
            Log.e(TAG, "Exception in AuthCodeCall: " + e.getMessage());
        }
        return returnCode;
    }

    private class getAuthOCode extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Get Authorization Code...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

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

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Log.e(TAG, "AuthCode = " + authCode);
            fuelMap.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,
                            "AuthCode = " + authCode,
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }

    }

}
