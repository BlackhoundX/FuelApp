package inft3970.fuelapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FuelMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = FuelMapActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private ProgressDialog pDialog;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LatLng DEFAULT_LOCATION = new LatLng(-32.8927673,151.7019888);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastKnownLocation;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);

    private final String KEY_CAMERA_POSITION = "camera_position";
    private final String KEY_LOCATION = "location";

    private boolean mLocationPermissionGranted;

    private static String authCode;
    private LatLng center;
    private static String[] authHeaders;
    private static String[][] headers;
    private static String body;
    private String fuelAPIKey;
    private int transactionId = 0;

    ArrayList<HashMap<String, String>> stationList;
    String[] stationType = new String[]{"7-Eleven","BP", "Budget","Caltex","Caltex Woolworths","Coles Express","Costco","Enhance","Independent","Liberty","Lowes","Matilda","Metro Fuel","Mobil","Prime Petroleum","Puma Energy","Shell","Speedway","Tesla","United","Westside"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_fuel_map);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fuelAPIKey = getString(R.string.fuel_api_key);
        authHeaders = new String[]{"Authorization", getResources().getString(R.string.fuel_api_base64)};

        new getAuthOCode().execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds NSW = new LatLngBounds(new LatLng(-34, 141), new LatLng(-28, 154));
        mMap.setLatLngBoundsForCameraTarget(NSW);
        mMap.setMinZoomPreference(5.5f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NSW.getCenter(), 5.5f));
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        mMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                center = mMap.getCameraPosition().target;
                mMap.clear();
                stationList = new ArrayList<HashMap<String, String>>();
                Date time = new Date();
                String timeString = dateFormat.format(time);
                headers = new String[][]{{"apikey", fuelAPIKey}, {"transactionid", Integer.toString(transactionId++)}, {"requesttimestamp", timeString}, {"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer "+authCode}};
                body = "{" +
                        "    \"fueltype\":\"P95\"," +
                        "    \"brand\":[" + getAllStations() + "]," +
                        "    \"namedlocation\":\"location\"," +
                        "    \"latitude\":\"" + Double.toString(center.latitude) + "\"," +
                        "    \"longitude\":\"" + Double.toString(center.longitude) + "\"," +
                        "    \"radius\":\"10\"," +
                        "    \"sortby\": \"price\"," +
                        "    \"sortascending\":\"true\"" +
                        "}";
                if(authCode != null) {
                    new getFuelStationsRadius().execute();
                }
            }
        });
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private String getAllStations() {
        String allStations = "";
        for(String station:stationType) {
            allStations += "\"" + station +"\"";
        }
        return allStations;
    }
    private class getFuelStationsRadius extends AsyncTask<Void, Void, Void> {

        @Override
        protected  void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FuelMapActivity.this);
            pDialog.setMessage("Displaying Pins...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void...arg0) {
            if(authCode != null) {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                } else {
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(pDialog.isShowing()) {
                pDialog.dismiss();
            }
            double latitude = 0;
            double longitude = 0;
            for(int stationCount = 1; stationCount < stationList.size(); stationCount++) {
                if((stationList.get(stationCount).get("latitude")) != null) {
                    latitude = Double.parseDouble(stationList.get(stationCount).get("latitude"));
                }
                if((stationList.get(stationCount).get("longitude") != null)) {
                    longitude = Double.parseDouble(stationList.get(stationCount).get("longitude"));
                }

                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stationList.get(stationCount).get(("name"))));

            }
        }
    }

    private class getAuthOCode extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(FuelMapActivity.this);
            pDialog.setMessage("Get Authorization Code...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Log.e(TAG, "AuthCode = " + authCode);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "AuthCode = " + authCode,
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
        }
    }
}


