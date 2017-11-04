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
import com.google.android.gms.maps.model.Marker;
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

public class FuelMapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = FuelMapActivity.class.getSimpleName();
    public GoogleMap mMap;
    public CameraPosition mCameraPosition;
    public ProgressBar pBar;
    public TextView fuelNameText;
    public CardView fuelNameCard;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final LatLng DEFAULT_LOCATION = new LatLng(-32.8927673,151.7019888);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final String DEFAULT_FUEL_CODE = "E10";
    private Location mLastKnownLocation;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);

    private final String KEY_CAMERA_POSITION = "camera_position";
    private final String KEY_LOCATION = "location";

    //public GoogleApiClient mGoogleApiClient;
    //private static final int RC_SIGN_IN = 9001;

    private boolean mLocationPermissionGranted;

    private String authCode;
    private LatLng center;
    private String[] authHeaders;
    private String[][] headers;
    private String body;
    private String fuelAPIKey;
    private int transactionId = 0;
    private LatLngBounds NSW = new LatLngBounds(new LatLng(-34, 141), new LatLng(-28, 154));

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
        fuelNameText = (TextView)findViewById(R.id.fuel_name_text);
        fuelNameCard = (CardView)findViewById(R.id.fuel_name_card);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fuelAPIKey = getString(R.string.fuel_api_key);
        authHeaders = new String[]{"Authorization", getResources().getString(R.string.fuel_api_base64)};

        AuthCodeCall codeCall = new AuthCodeCall();
        authCode = codeCall.getAuthCode(authHeaders, pBar);
        App.setAuthCode(authCode);

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
                searchCardView.setVisibility(View.GONE);
                fuelNameCard.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(Status status) {

            }
        });

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

        final FloatingActionButton searchBtn = (FloatingActionButton)findViewById(R.id.search_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchCardView.getVisibility() == View.GONE) {
                    searchBtn.setImageResource(R.drawable.places_ic_clear);
                    fuelNameCard.setVisibility(View.GONE);
                    searchCardView.setVisibility(View.VISIBLE);
                } else {
                    searchBtn.setImageResource(R.drawable.places_ic_search);
                    fuelNameCard.setVisibility(View.VISIBLE);
                    searchCardView.setVisibility(View.GONE);
                }
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

        FloatingActionButton fuelChartButton = (FloatingActionButton)findViewById(R.id.viewChartButton);
        fuelChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelChartActivity.class);
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
                stationList = new ArrayList<>();
                Date time = new Date();
                String timeString = dateFormat.format(time);
                String stationBrand;
                String stationIcon;
                ArrayList<String> stationSettings;
                stationSettings = getSettings();
                fuelNameText.setText(FuelCodeNameCall.getTypeName(stationSettings.get(0)));
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
                String stationCode;
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
                    if(stationList.get(stationCount).get("brand") != null && stationList.get(stationCount).get("code") != null)
                    {
                        stationBrand = stationList.get(stationCount).get("brand");
                        stationCode = stationList.get(stationCount).get("code");
                        String cheapStationCode = getCheapestStation(stationList);
                        stationIcon = IconStringCall.getIconString(stationBrand);
                        Bitmap iconBitmap = null;
                        try {
                            InputStream str = getApplicationContext().getAssets().open(stationIcon);
                            Bitmap unmutBitmap = BitmapFactory.decodeStream(str);
                            Bitmap mutBitmap = unmutBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            iconBitmap = Bitmap.createBitmap(unmutBitmap.getWidth() + 65, (unmutBitmap.getHeight() + 50), Bitmap.Config.ARGB_8888);
                            Canvas priceText = new Canvas(iconBitmap);
                            if(cheapStationCode.equals(stationCode)) {
                                Paint textStyle = new Paint();
                                textStyle.setColor(Color.GREEN);
                                textStyle.setTextAlign(Paint.Align.CENTER);
                                textStyle.setTextSize(45f);
                                priceText.drawRect(0, 0, 160, 60, new Paint(Color.BLACK));
                                priceText.drawBitmap(mutBitmap, 25, 50, new Paint());
                                priceText.drawText(price, 75f, 45f, textStyle);
                            } else {
                                Paint textStyle = new Paint();
                                textStyle.setColor(Color.WHITE);
                                textStyle.setTextAlign(Paint.Align.CENTER);
                                textStyle.setTextSize(45f);
                                priceText.drawRect(0, 0, 160, 60, new Paint(Color.BLACK));
                                priceText.drawBitmap(mutBitmap, 25, 50, new Paint());
                                priceText.drawText(price, 75f, 45f, textStyle);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(stationCode).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
                    }
                }
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Intent stationIntent = new Intent(getApplicationContext(), StationActivity.class);
                        stationIntent.putExtra("stationData", getStationData(stationList, marker.getTitle()));
                        stationIntent.putExtra("stationCode", marker.getTitle());
                        startActivity(stationIntent);
                        return false;
                    }
                });
            }
        });
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

    private HashMap<String, String> getStationData(ArrayList<HashMap<String, String>> stationList, String code) {
        HashMap returnList = null;
        for (HashMap<String, String> station:stationList) {
            if(station.get("code").equals(code)) {
                returnList = new HashMap();
                returnList.put("brand", station.get("brand"));
                returnList.put("name", station.get("name"));
                returnList.put("address", station.get("address"));
                returnList.put("latitude", station.get("latitude"));
                returnList.put("longitude", station.get("longitude"));
            }
        }
        return returnList;
    }

    private String getCheapestStation(ArrayList<HashMap<String,String>> stationList) {
        String cheapestCode = stationList.get(0).get("code");
        Double cheapestPrice = Double.parseDouble(stationList.get(0).get("price"));
        for(int count = 0;count < stationList.size();count++) {
            if(cheapestPrice > Double.parseDouble(stationList.get(count).get("price"))) {
                cheapestPrice = Double.parseDouble(stationList.get(count).get("price"));
                cheapestCode = stationList.get(count).get("code");
            }
        }
        return cheapestCode;
    }
}


