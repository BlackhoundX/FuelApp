package inft3970.fuelapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.annotations.NotNull;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.PlacePhotoResult;
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
import java.util.StringTokenizer;

/**
 * Created by shane on 15/10/2017.
 */

public class StationActivity extends Activity {

    private static final String TAG = StationActivity.class.getSimpleName();
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);
    Context context = App.getContext();
    private GeoDataClient mGeoDataClient = Places.getGeoDataClient(context, null);
    private int transactionId = 0;
    private String authCode;
    ArrayList fuelPrices;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_station);
        HashMap stationInfo = (HashMap) getIntent().getSerializableExtra("stationData");
        String stationCode = getIntent().getStringExtra("stationCode");
        authCode = App.getAuthCode();
        Date time = new Date();
        String timeString = dateFormat.format(time);

        TextView stationName = (TextView)findViewById(R.id.station_name_text);
        TextView stationAddress = (TextView)findViewById(R.id.station_address_text);
        TextView stationOpenTimes = (TextView)findViewById(R.id.open_times_txt);
        ImageView stationBrand = (ImageView)findViewById(R.id.station_brand_img);
        ImageView stationPhoto = (ImageView)findViewById(R.id.station_photo_img);
        stationPhoto.setVisibility(View.GONE);



        stationName.setText((String)stationInfo.get("name"));
        stationAddress.setText((String)stationInfo.get("address"));

        PlaceIDCall placeIDCall = new PlaceIDCall();
        String placeID = placeIDCall.getPlaceID((String)stationInfo.get("address"), Double.parseDouble((String)stationInfo.get("latitude")), Double.parseDouble((String)stationInfo.get("longitude")));
        if (placeID != null) {
            getPhoto(placeID, stationPhoto);
        }

        PlaceDetailsCall placeDetailsCall = new PlaceDetailsCall();
        PlaceDetails placeDetails = placeDetailsCall.getPlaceDetails(placeID);

        if(placeDetails.getOpenTimes() != null) {
            StringBuilder openTimesText = new StringBuilder();
            openTimesText.append("Open Hours:");
            ArrayList openTimes = placeDetails.getOpenTimes();
            for (int itemCount = 0; itemCount < openTimes.size(); itemCount++) {
                openTimesText.append("\n");
                openTimesText.append(openTimes.get(itemCount));
            }
            stationOpenTimes.setText(openTimesText.toString());
        }

        try {
            String icon = IconStringCall.getIconString((String)stationInfo.get("brand"));
            AssetManager assetManager = App.getContext().getAssets();
            InputStream is = assetManager.open(icon);
            Drawable draw = Drawable.createFromStream(is, null);
            stationBrand.setImageDrawable(draw);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        FuelPriceByCodeCall fuelPriceCall = new FuelPriceByCodeCall();
        String[][] headers = new String[][]{{"apikey", getString(R.string.fuel_api_key)},{"transactionid", Integer.toString(transactionId++)},{"requesttimestamp", timeString},{"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer "+authCode}};
        fuelPrices = fuelPriceCall.getFuelPricesByCode(stationCode, headers);

        RecyclerView stationFuelRv = (RecyclerView)findViewById(R.id.station_fuel_rv);
        stationFuelRv.setHasFixedSize(false);

        LinearLayoutManager llmStation = new LinearLayoutManager(this);
        stationFuelRv.setLayoutManager(llmStation);

        FuelPriceAdapter adapter = new FuelPriceAdapter(fuelPrices);
        stationFuelRv.setAdapter(adapter);

        if(placeDetails.getReviews() != null) {
            RecyclerView reviewRv = (RecyclerView) findViewById(R.id.review_rv);
            reviewRv.setHasFixedSize(false);

            LinearLayoutManager llmReview = new LinearLayoutManager(this);
            reviewRv.setLayoutManager(llmReview);

            ReviewAdapter reviewAdapter = new ReviewAdapter(placeDetails.getReviews());
            reviewRv.setAdapter(reviewAdapter);
        }
    }

    private void getPhoto(String placeId, final ImageView img) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponseTask = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponseTask.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer placePhotoMetadataBuffer = photos.getPhotoMetadata();
                PlacePhotoMetadata placePhotoMetadata = null;
                if(placePhotoMetadataBuffer.getCount() != 0) {
                    placePhotoMetadata = placePhotoMetadataBuffer.get(0);
                }
                if(placePhotoMetadata != null) {
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
}
