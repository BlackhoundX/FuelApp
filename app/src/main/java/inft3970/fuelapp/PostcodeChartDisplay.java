package inft3970.fuelapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


/**
 * Class: PostcodeChartDisplay
 * Author: Matt Couch
 * Purpose: This class displays the requested chart from Firebase Storage.
 * Creation Date: 30-Oct-17
 * Modification Date: 01-Nov-17
 */

public class PostcodeChartDisplay extends FragmentActivity {
    Context context;
    ImageView chartImage;
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();  //Gets the reference to the Firebase Storage location


    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It initialises some variables and objects.
     * Returns: None
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_display);
        Firebase.setAndroidContext(this);
        context = App.getContext();
        chartImage = (ImageView)findViewById(R.id.chartButton);
    }

    /**
     * Method: onStart
     * Purpose: Automatically is called when the class is started. It takes in the selection of
     * postcode and whether past or future records are requested, and displays the image from Firebase Storage.
     * Returns: None
     */
    @Override
    protected void onStart() {
        super.onStart();

        String postCode = getIntent().getStringExtra("PostCodeValue");  //Gets the requested postcode
        boolean pastValues = getIntent().getBooleanExtra("PastValues", true); //Gets the requested chart from past or future

        if(pastValues) {  //Display the chart for past values
            storageRef.child("Postcodes").child(postCode + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri.toString()).fit().into(chartImage); //Load the image
                    Toast.makeText(getApplicationContext(), "Tap the image to return", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Error: Failed to load image", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
        else{  //Display the chart for future values
            storageRef.child("Predictions").child("Predictions.JPG").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri.toString()).fit().into(chartImage); //Load the image
                    Toast.makeText(getApplicationContext(), "Tap the image to return", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Error: Failed to load image", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }

        //Set the image to close when clicked
        chartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
