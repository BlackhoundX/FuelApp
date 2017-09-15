package inft3970.fuelapp;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.Marker;

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
    private String[] authHeaders;
    private static String[][] headers;
    private static String body;
    private String fuelAPIKey;
    private int transactionId = 0;

    ArrayList<HashMap<String, String>> stationList;
    String[] stationType = new String[]{"7-Eleven","BP", "Budget","Caltex","Caltex Woolworths","Coles Express","Costco","Enhance","Independent","Liberty","Lowes","Matilda","Metro Fuel","Mobil","Prime Petroleum","Puma Energy","Shell","Speedway","Tesla","United","Westside"};
    private String brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.setContext(this);

        if(savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_fuel_map);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fuelAPIKey = getString(R.string.fuel_api_key);
        authHeaders = new String[]{"Authorization", getResources().getString(R.string.fuel_api_base64)};

        AuthCodeCall codeCall = new AuthCodeCall();
        authCode = codeCall.getAuthCode(authHeaders);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton listViewBtn = (FloatingActionButton)findViewById(R.id.list_view_button);
        listViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent = new Intent(getApplicationContext(), FuelListActivity.class);
                listIntent.putExtra("station_list", stationList);
                startActivity(listIntent);
            }
        });
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
                String stationBrand;
                String stationIcon;
                headers = new String[][]{{"apikey", fuelAPIKey}, {"transactionid", Integer.toString(transactionId++)}, {"requesttimestamp", timeString}, {"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer " + authCode}};
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
                StationByRadiusCall StationRadiusCall = new StationByRadiusCall();
                stationList = StationRadiusCall.getStationsByRadius(headers, body);
                Double latitude = 0.0;
                Double longitude = 0.0;
                for (int stationCount = 0; stationCount < stationList.size(); stationCount++) {
                    if (stationList.get(stationCount).get("latitude") != null) {
                        latitude = Double.parseDouble(stationList.get(stationCount).get("latitude"));
                    }
                    if (stationList.get(stationCount).get("longitude") != null) {
                        longitude = Double.parseDouble(stationList.get(stationCount).get("longitude"));
                    }
                    stationBrand = stationList.get(stationCount).get("brand");

                    //This if statement is required. It seems the data being read sometimes is incomplete, causing errors otherwise
                    if(stationBrand != null)
                    {
                        stationIcon = getIconString(stationBrand);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stationBrand).icon(BitmapDescriptorFactory.fromAsset(stationIcon)));
                    }
                }
            }
        } );
    }

    /*
    This method takes in the string of the petrol station brand name
    and returns a string containing the relevant image filename, contained
    in the Assets folder
     */
    public String getIconString(String brand) {
        String iconFile;

        switch (brand) {
            case "7-Eleven":
                iconFile = "711icon.png";
                break;
            case "BP":
                iconFile = "bpIcon.png";
                break;
            case "Caltex":
                iconFile = "caltexIcon.png";
                break;
            case "Caltex Woolworths":
                iconFile = "woolworthsCaltex.png";
                break;
            case "Coles Express":
                iconFile = "colesexpress.png";
                break;
            case "Costco":
                iconFile = "costcoLogo.png";
                break;
            case "Enhance":
                iconFile = "defaultLogo.png";
                break;
            case "Independent":
                iconFile = "defaultLogo.png";
                break;
            case "Liberty":
                iconFile = "liberty.png";
                break;
            case "Lowes":
                iconFile = "lowes.png";
                break;
            case "Matilda":
                iconFile = "matilda.png";
                break;
            case "Metro Fuel":
                iconFile = "metro.png";
                break;
            case "Mobil":
                iconFile = "mobil.png";
                break;
            case "Prime Petroleum":
                iconFile = "defaultLogo.png";
                break;
            case "Puma Energy":
                iconFile = "puma.png";
                break;
            case "Shell":
                iconFile = "shell.png";
                break;
            case "Speedway":
                iconFile = "speedway.png";
                break;
            case "Tesla":
                iconFile = "tesla.png";
                break;
            case "United":
                iconFile = "united.png";
                break;
            case "Westside":
                iconFile = "westside.png";
                break;
            default:
                iconFile = "defaultLogo.png";
                break;
        }
        return iconFile;
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

}


