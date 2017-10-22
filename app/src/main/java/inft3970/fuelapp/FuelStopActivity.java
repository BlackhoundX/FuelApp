package inft3970.fuelapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isDigitsOnly;
import static inft3970.fuelapp.FuelMapActivity.googleID;

/**
 * Created by Matt on 13-Oct-17.
 */

public class FuelStopActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = FuelMapActivity.class.getSimpleName();
    Spinner vehicleListSpinner;
    Context context;
    String selectedType;
    Double calculatedValue;
    String userID = "";
    EditText vehicleNameInput;
    List<String> vehicleNames = new ArrayList<>();
    List<Integer> fuelStopIDList = new ArrayList<>();
    ArrayAdapter<String> VehicleList;

    Button analysisButton;
    Button deleteVehicleButton;
    Button addVehicleButton;
    Button clearFormButton;
    Button addFuelButton;
    EditText brandInput;
    EditText fuelTypeInput;
    EditText odometerInput;
    EditText litresInput;
    EditText pricePerLitreInput;
    EditText locationInput;
    TextView costCalculatedOutput;

    Firebase firebaseReference;
    DataSnapshot dataSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stop);
        context = App.getContext();
        Firebase.setAndroidContext(this);

        brandInput = (EditText)findViewById(R.id.brandInput);
        fuelTypeInput = (EditText)findViewById(R.id.fuelTypeInput);
        odometerInput = (EditText)findViewById(R.id.odometerInput);
        litresInput = (EditText)findViewById(R.id.litresInput);
        pricePerLitreInput = (EditText)findViewById(R.id.pricePerLitreInput);
        costCalculatedOutput = (TextView)findViewById(R.id.costCalculatedOutput);
        locationInput = (EditText)findViewById(R.id.locationInput);

        userID = googleID;

        firebaseReference = new Firebase("https://inft3970-major-p-1503830364825.firebaseio.com/");

        firebaseReference.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userID;
                Boolean match = false;
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    userID = postsnapshot.getKey();
                    if(userID.equals(googleID))
                    {
                        match = true;
                    }
                    else
                    {
                        match = false;
                    }
                }
                if(!match)
                {
                    firebaseReference.child(googleID).child("DefaultVehicle").child("0").child("DefaultValue").setValue("0");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        }));

        litresInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(pricePerLitreInput.getText().toString().isEmpty() || litresInput.getText().toString().isEmpty()){

                }
                else
                {
                    calculatedValue = Double.parseDouble(litresInput.getText().toString()) * Double.parseDouble(pricePerLitreInput.getText().toString()) / 100.0;
                    calculatedValue = round(calculatedValue, 2);
                    costCalculatedOutput.setText("$" + calculatedValue.toString());
                }
            }
        });

        pricePerLitreInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(pricePerLitreInput.getText().toString().isEmpty() || litresInput.getText().toString().isEmpty()){

                }
                else
                {
                    calculatedValue = Double.parseDouble(litresInput.getText().toString()) * Double.parseDouble(pricePerLitreInput.getText().toString()) / 100.0;
                    calculatedValue = round(calculatedValue, 2);
                    costCalculatedOutput.setText("$" + calculatedValue.toString());
                }
            }
        });

        clearFormButton = (Button)findViewById(R.id.clearFormButton);
        clearFormButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                vehicleNameInput.setText("");
                brandInput.setText("");
                fuelTypeInput.setText("");
                odometerInput.setText("");
                litresInput.setText(null);
                pricePerLitreInput.setText(null);
                locationInput.setText("");
                costCalculatedOutput.setText("");
                Toast.makeText(getApplicationContext(), "Form cleared.", Toast.LENGTH_LONG).show();
            }
        });

        addFuelButton = (Button) findViewById(R.id.addFuelButton);
        addFuelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                boolean validDetails = true;
                if(brandInput.getText().equals("")){
                    validDetails = false;
                }
                if(fuelTypeInput.getText().equals("")){
                    validDetails = false;
                }
                if(odometerInput.getText().toString().isEmpty()){
                    validDetails = false;
                }
                if(litresInput.getText().toString().isEmpty()){
                    validDetails = false;
                }
                if(pricePerLitreInput.getText().toString().isEmpty()){
                    validDetails = false;
                }
                if(locationInput.getText().equals("")){
                    validDetails = false;
                }
                if(validDetails){
                    Toast.makeText(getApplicationContext(), "Valid details!.", Toast.LENGTH_LONG).show();
                    AlertDialog confirmAddFuelStop = confirmAddFuelStop(null);
                    confirmAddFuelStop.show();
                }
                else{
                    AlertDialog invalidFuelStopError = invalidFuelStopError(null);
                    invalidFuelStopError.show();
                }
            }
        });

        vehicleListSpinner = (Spinner) findViewById(R.id.vehicleListSpinner);
        VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
        VehicleList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleListSpinner.setAdapter(VehicleList);
        vehicleListSpinner.setOnItemSelectedListener(this);
    }




    @Override
    protected void onStart(){
        super.onStart();

        vehicleNames.clear();
        firebaseReference.child(googleID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstChild;
                vehicleNames.clear();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    firstChild = postsnapshot.getKey();
                    vehicleNames.add(firstChild);
                    VehicleList.notifyDataSetChanged();
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
                            Toast.makeText(getApplicationContext(), inputName + " added.", Toast.LENGTH_LONG).show();
                            vehicleNameInput.setText("");
                        }
                    }
                } else {
                    errorDialog.show();
                }
            }
        });

        deleteVehicleButton = (Button)findViewById(R.id.deleteVehicleButton);
        deleteVehicleButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog confirmDeleteDialog = confirmDeleteVehicleDialog(null);
                confirmDeleteDialog.show();
            }
        });

        analysisButton = (Button)findViewById(R.id.analysisButton);
        analysisButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelAnalyticsActivity.class);
                startActivity(filterIntent);
            }
        });
    }

    public void prepareFuelStopList(){
        fuelStopIDList.clear();
        firebaseReference.child(googleID).child(selectedType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String entryID;

                fuelStopIDList.clear();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    entryID = postsnapshot.getKey();
                    fuelStopIDList.add(Integer.parseInt(entryID));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addData (String brandName, String fuelType, String odometer, String litresPurchased, String pricePerLitre, String location, String totalCost){
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        int max = 0;
        int newIndex = 0;

        for (int i : fuelStopIDList){
            if(i > max){
                max = i;
            }
        }

        newIndex = max + 1;

        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Date").setValue(date);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("BrandName").setValue(brandName);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("FuelType").setValue(fuelType);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Location").setValue(location);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Odometer").setValue(odometer);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Litres").setValue(litresPurchased);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Price").setValue(pricePerLitre);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Cost").setValue(totalCost);

        clearFormButton.callOnClick();
    }

    public AlertDialog confirmDeleteVehicleDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to delete " + selectedType + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseReference.child(googleID).child(selectedType).removeValue();
                Toast.makeText(getApplicationContext(), selectedType + " deleted.", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public AlertDialog createNoSavedDataDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Vehicle name is invalid.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public AlertDialog invalidFuelStopError(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Invalid details entered for fuel stop.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public AlertDialog confirmAddFuelStop(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to save this fuel stop?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addData(brandInput.getText().toString(), fuelTypeInput.getText().toString(), odometerInput.getText().toString(), litresInput.getText().toString(), pricePerLitreInput.getText().toString(), locationInput.getText().toString(), calculatedValue.toString());
                Toast.makeText(getApplicationContext(), "Entry added.", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedType = parent.getItemAtPosition(position).toString();
        prepareFuelStopList();
        Toast.makeText(getApplicationContext(), "You have selected " + selectedType, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //selectedType = null;
    }
}