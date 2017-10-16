package inft3970.fuelapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FuelMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FuelMapActivity.class.getSimpleName();
    public GoogleMap mMap;
    public CameraPosition mCameraPosition;
    public ProgressBar pBar;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    private final LatLng DEFAULT_LOCATION = new LatLng(-32.8927673,151.7019888);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final String DEFAULT_FUEL_CODE = "E10";
    private Location mLastKnownLocation;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);

    private final String KEY_CAMERA_POSITION = "camera_position";
    private final String KEY_LOCATION = "location";

    public GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;

    private boolean mLocationPermissionGranted;

    private String authCode;
    private LatLng center;
    private String[] authHeaders;
    private String[][] headers;
    private String body;
    private String fuelAPIKey;
    private int transactionId = 0;
    private LatLngBounds NSW = new LatLngBounds(new LatLng(-34, 141), new LatLng(-28, 154));

    static String googleID = "";

    ArrayList<HashMap<String, String>> stationList;
    String[] stationType = new String[]{"7-Eleven","BP", "Budget","Caltex","Caltex Woolworths","Coles Express","Costco","Enhance","Independent","Liberty","Lowes","Matilda","Metro Fuel","Mobil","Prime Petroleum","Puma Energy","Shell","Speedway","Tesla","United","Westside"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.setContext(this);

        if(savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_fuel_map);
        pBar = (ProgressBar)findViewById(R.id.progressBar);
        final CardView searchCardView = (CardView)findViewById(R.id.search_card_view);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        fuelAPIKey = getString(R.string.fuel_api_key);
        authHeaders = new String[]{"Authorization", getResources().getString(R.string.fuel_api_base64)};

        AuthCodeCall codeCall = new AuthCodeCall();
        authCode = codeCall.getAuthCode(authHeaders, pBar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.search_fragment);
        autocompleteFragment.setBoundsBias(NSW);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng placeLatLng = place.getLatLng();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
                searchCardView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Status status) {

            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this/* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signIn();

        FloatingActionButton listViewBtn = (FloatingActionButton)findViewById(R.id.list_view_button);
        listViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent listIntent = new Intent(getApplicationContext(), FuelListActivity.class);
                listIntent.putExtra("station_list", stationList);
                startActivity(listIntent);
            }
        });

        FloatingActionButton filterBtn = (FloatingActionButton)findViewById(R.id.filter_button);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelFilterActivity.class);
                startActivity(filterIntent);
            }
        });

        FloatingActionButton searchBtn = (FloatingActionButton)findViewById(R.id.search_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCardView.setVisibility(View.VISIBLE);
            }
        });

        FloatingActionButton fuelStop = (FloatingActionButton)findViewById(R.id.fuel_tracking_button);
        fuelStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelStopActivity.class);
                startActivity(filterIntent);
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

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG, "User ID is: " + acct.getId());
            googleID = acct.getId();
        }
    }
    // [END handleSignInResult]

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    /**
     * Does stuff
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setLatLngBoundsForCameraTarget(NSW);
        mMap.setMinZoomPreference(6.5f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NSW.getCenter(), 6.5f));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        mMap.setOnCameraIdleListener(new OnCameraIdleListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onCameraIdle() {
                center = mMap.getCameraPosition().target;
                mMap.clear();
                stationList = new ArrayList<HashMap<String, String>>();
                Date time = new Date();
                String timeString = dateFormat.format(time);
                String stationBrand;
                String stationIcon;
                ArrayList<String> stationSettings = new ArrayList<String>();
                stationSettings = getSettings();
                headers = new String[][]{{"apikey", fuelAPIKey}, {"transactionid", Integer.toString(transactionId++)}, {"requesttimestamp", timeString}, {"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer " + authCode}};
                body = "{" +
                        "    \"fueltype\":\"" + stationSettings.get(0) + "\"," +
                        "    \"brand\":[" + stationSettings.get(1) + "]," +
                        "    \"namedlocation\":\"location\"," +
                        "    \"latitude\":\"" + Double.toString(center.latitude) + "\"," +
                        "    \"longitude\":\"" + Double.toString(center.longitude) + "\"," +
                        "    \"radius\":\"10\"," +
                        "    \"sortby\": \"price\"," +
                        "    \"sortascending\":\"true\"" +
                        "}";
                StationByRadiusCall StationRadiusCall = new StationByRadiusCall();
                stationList = StationRadiusCall.getStationsByRadius(headers, body, pBar);
                Double latitude = 0.0;
                Double longitude = 0.0;
                String price = "N/A";
                String fuelType = stationSettings.get(0);
                for (int stationCount = 0; stationCount < stationList.size(); stationCount++) {
                    if (stationList.get(stationCount).get("latitude") != null) {
                        latitude = Double.parseDouble(stationList.get(stationCount).get("latitude"));
                    }
                    if (stationList.get(stationCount).get("longitude") != null) {
                        longitude = Double.parseDouble(stationList.get(stationCount).get("longitude"));
                    }
                    if(stationList.get(stationCount).get("price") != null) {
                        price = stationList.get(stationCount).get("price");
                    }
                    //This if statement is required. It seems the data being read sometimes is incomplete, causing errors otherwise
                    if(stationList.get(stationCount).get("brand") != null)
                    {
                        stationBrand = stationList.get(stationCount).get("brand");
                        stationIcon = getIconString(stationBrand);
                        Bitmap iconBitmap = null;
                        try {
                            InputStream str = getApplicationContext().getAssets().open(stationIcon);
                            Bitmap unmutBitmap = BitmapFactory.decodeStream(str);
                            Bitmap mutBitmap = unmutBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            iconBitmap = Bitmap.createBitmap(unmutBitmap.getWidth() + 65, (unmutBitmap.getHeight() + 50), Bitmap.Config.ARGB_8888);
                            Canvas priceText = new Canvas(iconBitmap);
                            Paint textStyle = new Paint();
                            textStyle.setColor(Color.WHITE);
                            textStyle.setTextAlign(Paint.Align.CENTER);
                            textStyle.setTextSize(35f);
                            priceText.drawRect(0, 0, 160, 60, new Paint(Color.BLACK));
                            priceText.drawBitmap(mutBitmap, 25, 50, new Paint());
                            priceText.drawText(fuelType + ":" + price, 75f, 45f, textStyle);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(fuelType + ":" + price).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
                    }
                }
            }
        });
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

    public void getDeviceLocation() {
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

    public void getLocationPermission() {
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

    public void updateLocationUI() {
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

    private ArrayList<String> getSettings() {
        File settingsFile = new File(this.getFilesDir() + "/Settings.xml");
        ArrayList<String> settingsList = new ArrayList<>();
        if(settingsFile.exists()) {
            XmlSettings xmlSettings = new XmlSettings();
            settingsList = xmlSettings.readXml();
            String[] fuelBrands = settingsList.get(1).split(", ");
            String brandString = "";
            for(String brand:fuelBrands) {
                brandString += "\"" + brand + "\",";
            }
            brandString = brandString.substring(0, brandString.lastIndexOf(","));
            settingsList.remove(1);
            settingsList.add(1, brandString);
        } else {
            settingsList.add(DEFAULT_FUEL_CODE);
            settingsList.add(getAllStations());
        }
        return settingsList;
    }
}


