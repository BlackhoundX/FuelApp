package inft3970.fuelapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Class: StationActivity
 * Author: Shane
 * Purpose: This class handles the gathering and display of individual data for a selected station
 * Creation Date: 15-Oct-17
 * Modification Date: 05-Nov-17
 */

public class StationActivity extends Activity {

    private static final String TAG = StationActivity.class.getSimpleName();
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);
    Context context = App.getContext();
    private GeoDataClient mGeoDataClient = Places.getGeoDataClient(context, null);
    private int transactionId = 0;
    private String authCode;
    ArrayList fuelPrices;

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It sets up the objects for display and
     * handles the gathering of some data.
     * Returns: None
     */
    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_station);
        final HashMap stationInfo = (HashMap) getIntent().getSerializableExtra("stationData");  //Get the list of station data
        String stationCode = getIntent().getStringExtra("stationCode"); //Get the station being looked at
        authCode = App.getAuthCode();
        Date time = new Date();
        String timeString = dateFormat.format(time);

        //Initialise the display objects
        final TextView stationName = (TextView) findViewById(R.id.station_name_text);
        TextView stationAddress = (TextView) findViewById(R.id.station_address_text);
        TextView stationOpenTimes = (TextView) findViewById(R.id.open_times_txt);
        ImageView stationBrand = (ImageView) findViewById(R.id.station_brand_img);
        ImageView stationPhoto = (ImageView) findViewById(R.id.station_photo_img);
        stationPhoto.setVisibility(View.GONE);
        Button callButton = (Button) findViewById(R.id.call_btn);
        Button websiteButton = (Button) findViewById(R.id.website_btn);
        Button directionsButton = (Button) findViewById(R.id.direction_btn);

        //Get the name and address of the station
        stationName.setText((String) stationInfo.get("name"));
        stationAddress.setText((String) stationInfo.get("address"));

        //Get the address and location details of the station
        PlaceIDCall placeIDCall = new PlaceIDCall();
        final String placeID = placeIDCall.getPlaceID((String) stationInfo.get("address"), Double.parseDouble((String) stationInfo.get("latitude")), Double.parseDouble((String) stationInfo.get("longitude")));
        if (placeID != null) {
            getPhoto(placeID, stationPhoto);
        }

        //Get the open times of the station, if applicable.
        PlaceDetailsCall placeDetailsCall = new PlaceDetailsCall();
        final PlaceDetails placeDetails = placeDetailsCall.getPlaceDetails(placeID);

        if (placeDetails.getOpenTimes() != null) {
            StringBuilder openTimesText = new StringBuilder();
            openTimesText.append("Open Hours:");
            ArrayList openTimes = placeDetails.getOpenTimes();
            for (int itemCount = 0; itemCount < openTimes.size(); itemCount++) {
                openTimesText.append("\n");
                openTimesText.append(openTimes.get(itemCount));
            }
            stationOpenTimes.setText(openTimesText.toString());
        }

        //Get the icon for the station
        try {
            String icon = IconStringCall.getIconString((String) stationInfo.get("brand"));
            AssetManager assetManager = App.getContext().getAssets();
            InputStream is = assetManager.open(icon);
            Drawable draw = Drawable.createFromStream(is, null);
            stationBrand.setImageDrawable(draw);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        //Get the prices for the station from NSW Fuel API
        FuelPriceByCodeCall fuelPriceCall = new FuelPriceByCodeCall();
        String[][] headers = new String[][]{{"apikey", getString(R.string.fuel_api_key)}, {"transactionid", Integer.toString(transactionId++)}, {"requesttimestamp", timeString}, {"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer " + authCode}};
        fuelPrices = fuelPriceCall.getFuelPricesByCode(stationCode, headers);

        //Set up a Recycler View to tile each type of fuel provided by the station
        RecyclerView stationFuelRv = (RecyclerView) findViewById(R.id.station_fuel_rv);
        stationFuelRv.setHasFixedSize(false);
        stationFuelRv.setNestedScrollingEnabled(false);
        LinearLayoutManager llmStation = new LinearLayoutManager(this);
        stationFuelRv.setLayoutManager(llmStation);
        FuelPriceAdapter adapter = new FuelPriceAdapter(fuelPrices);
        stationFuelRv.setAdapter(adapter);
        stationFuelRv.getLayoutParams().height = 375 * adapter.getItemCount();

        //Get the reviews for the station from Google Business, if any.
        if (placeDetails.getReviews() != null) {
            RecyclerView reviewRv = (RecyclerView) findViewById(R.id.review_rv);
            reviewRv.setHasFixedSize(false);

            LinearLayoutManager llmReview = new LinearLayoutManager(this);
            reviewRv.setLayoutManager(llmReview);

            ReviewAdapter reviewAdapter = new ReviewAdapter(placeDetails.getReviews());
            reviewRv.setAdapter(reviewAdapter);
        }

        //Initialise the Call button, allowing the user to call a station if the phone number is provided.
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeDetails.getPhoneNumber() != null) {
                    //Create alert dialog to check if the user wants to call
                    AlertDialog phoneDialog = createCallAlert(placeDetails.getPhoneNumber(), (String)stationName.getText());
                    phoneDialog.show();
                }
            }
        });

        //Initialise the Website button, allowing the user to view the station website if it is provided
        websiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeDetails.getWebsite() != null) {
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                    websiteIntent.setData(Uri.parse(placeDetails.getWebsite()));
                    startActivity(websiteIntent);
                }
            }
        });

        //Initialise the Directions button, which will open Google Maps to provide directions
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri directionUri = Uri.parse("google.navigation:q="+stationInfo.get("address"));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, directionUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It sets up the objects for display and
     * handles the gathering of some data.
     * Returns: None
     */
    private void getPhoto(String placeId, final ImageView img) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponseTask = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponseTask.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer placePhotoMetadataBuffer = photos.getPhotoMetadata();
                PlacePhotoMetadata placePhotoMetadata = null;
                if (placePhotoMetadataBuffer.getCount() != 0) {
                    placePhotoMetadata = placePhotoMetadataBuffer.get(0);
                }
                if (placePhotoMetadata != null) {
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(placePhotoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap photoBitmap = photo.getBitmap();
                            img.setVisibility(View.VISIBLE);
                            img.setImageBitmap(photoBitmap);
                        }
                    });
                }
            }
        });
    }

    /**
     * Method: createCallAlert
     * Purpose: This method creates the alert dialog box, checking that the user does want to call
     * the fuel station
     * Returns: The Alert Dialog, ready to be called.
     */
    public AlertDialog createCallAlert(final String phone, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to call " + name);
        builder.setPositiveButton("Call", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                phoneIntent.setData(Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(StationActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    startActivity(phoneIntent);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
