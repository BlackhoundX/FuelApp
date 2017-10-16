package inft3970.fuelapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static inft3970.fuelapp.FuelMapActivity.googleID;


/**
 * Created by Matt on 13-Oct-17.
 */

public class FuelStopActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Spinner vehicleList;
    Context context;
    String selectedType;
    String userID = "";
    EditText vehicleNameInput;
    List<String> vehicleNames = new ArrayList<>();
    Button addVehicleButton;

    Firebase firebaseReference;
    DataSnapshot dataSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stop);
        context = App.getContext();
        Firebase.setAndroidContext(this);

        userID = googleID;

        firebaseReference = new Firebase("https://inft3970-major-p-1503830364825.firebaseio.com/");

        firebaseReference.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        }));
    }

    @Override
    protected void onStart(){
        super.onStart();
        vehicleNames.clear();

        firebaseReference.child(googleID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstChild;
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    firstChild = postsnapshot.getKey();
                    vehicleNames.add(firstChild);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        addVehicleButton = (Button)findViewById(R.id.addVehicleButton);
        vehicleNameInput = (EditText)findViewById(R.id.vehicleNameInput);
        addVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog errorDialog = createNoSavedDataDialog(null);
                String inputName = vehicleNameInput.getText().toString();
                if (!inputName.equals("")) {
                    for (int i = 0; i < vehicleNames.size(); i++) {
                        if (inputName.equals(vehicleNames.get(i))) {
                            errorDialog.show();
                        } else {
                            firebaseReference.child(googleID).child(inputName).child("FuelStop").child("VisitID").setValue("0");
                            finish();
                        }
                    }
                } else {
                    errorDialog.show();
                }
            }
        });


        vehicleList = (Spinner)findViewById(R.id.vehicleList);
        //Get the list of vehicles under the userID, and display in a dropdown menu
        vehicleList.setOnItemSelectedListener(this);
        ArrayAdapter<String> VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
        vehicleList.setAdapter(VehicleList);
    }

    public AlertDialog createNoSavedDataDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Vehicle name is invalid.");
        builder.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        selectedType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        selectedType = null;
    }
}
