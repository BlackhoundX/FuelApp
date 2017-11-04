package inft3970.fuelapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Class: FuelStopActivity
 * Author: Matt Couch
 * Purpose: This class allows the user to manage their vehicles and fuel stops.
 * Creation Date: 13-Oct-17
 * Modification Date: 30-Oct-17
 */

public class FuelStopActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FuelMapActivity.class.getSimpleName();
    Context context;

    String selectedType;
    Double calculatedValue;
    String userID = "";

    List<String> vehicleNames = new ArrayList<>();      //List to store the vehicle names
    List<Integer> fuelStopIDList = new ArrayList<>();   //List to store the IDs for each fuel stop
    ArrayAdapter<String> VehicleList;

    Button analysisButton;
    Button deleteVehicleButton;
    Button addVehicleButton;
    Button clearFormButton;
    Button addFuelButton;
    FloatingActionButton returnButton;
    FloatingActionButton helpButton;

    SignInButton sign_in_button;

    EditText vehicleNameInput;
    EditText brandInput;
    EditText fuelTypeInput;
    EditText odometerInput;
    EditText litresInput;
    EditText pricePerLitreInput;
    EditText locationInput;

    Spinner vehicleListSpinner;

    TextView costCalculatedOutput;

    Firebase firebaseReference;

    public GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    static String googleID = "";

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It instantiates the editText fields,
     * sets up the Google Login builder, initiates the Firebase address, activates listeners to automatically calculate
     * the price field, sets up the clear form button, and also automatically attempts to sign the user in to Google.
     * Returns: None
     */
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
        returnButton = (FloatingActionButton)findViewById(R.id.returnButton);
        helpButton = (FloatingActionButton)findViewById(R.id.helpButton);

        userID = googleID;

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseReference = new Firebase("https://inft3970-major-p-1503830364825.firebaseio.com/");

        //Watcher for the Litres Input value
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
                    //Valid inputs detected. Calculate the value in dollars
                    calculatedValue = Double.parseDouble(litresInput.getText().toString()) * Double.parseDouble(pricePerLitreInput.getText().toString()) / 100.0;
                    calculatedValue = round(calculatedValue, 2);
                    costCalculatedOutput.setText("$" + calculatedValue.toString());
                }
            }
        });

        //Watcher for the Cents Per Litre input
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
                    //Valid inputs detected. Calculate the value in dollars
                    calculatedValue = Double.parseDouble(litresInput.getText().toString()) * Double.parseDouble(pricePerLitreInput.getText().toString()) / 100.0;
                    calculatedValue = round(calculatedValue, 2);
                    costCalculatedOutput.setText("$" + calculatedValue.toString());
                }
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog helpScreen = displayHelp();
                helpScreen.show();
            }
        });

        //Create and set a watcher on the Clear Form button. On a click it resets the form.
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

        //Creates the add fuel button but does not instantiate yet. It does this after a valid login is confirmed.
        addFuelButton = (Button) findViewById(R.id.addFuelButton);

        //Connect to Google and attempt to log in.
        mGoogleApiClient.connect();
        signIn();
    }

    /**
     * Method: onStart
     * Purpose: Automatically is called when the class is starting. It initialises a few more buttons, as well as
     * setting a listener for the Google Play sign in button, which will appear only if the user is not signed in
     * Returns: None
     */
    @Override
    protected void onStart(){
        super.onStart();

        addVehicleButton = (Button)findViewById(R.id.addVehicleButton);

        deleteVehicleButton = (Button)findViewById(R.id.deleteVehicleButton);

        analysisButton = (Button)findViewById(R.id.analysisButton);

        sign_in_button = (SignInButton)findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    /**
     * -------------------------------------------------------
     * This section of code handles the Google Sign in itself, and is taken from the guide found
     * at https://developers.google.com/identity/sign-in/android/start-integrating
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            googleID = acct.getId();
            checkLoggedIn(); //Method to check if the user is logged in. If so, then activates the form for use
            sign_in_button.setVisibility(View.GONE); //Hides the login button
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    /**
     * End Google login section of code
     * -------------------------------------------------------
     */


    /**
     * Method: checkLoggedIn
     * Purpose: This method checks if the user is logged in, returning a boolean to indicate this. I
     * If they are logged in then all the button listeners are activated, as well as the vehicle list spinner
     * Returns: Boolean value indicating if the user is logged in
     */
    private boolean checkLoggedIn(){
        boolean loggedIn;
        if(googleID.isEmpty()){
            loggedIn = false;
        }
        else{
            loggedIn = true;

            //This portion of code checks the logged in user's Google ID against the list in the database.
            //If it is a new user, the database is adjusted to add them.
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

            //This portion of code lists the vehicle names under the particular Google ID.
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

            //This portion of code activates the Add Vehicle Button, and also performs validation on the entered name
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
                                firebaseReference.child(googleID).child(inputName).child("0").child("DefaultValue").setValue("0");
                                Toast.makeText(getApplicationContext(), inputName + " added.", Toast.LENGTH_LONG).show();
                                vehicleNameInput.setText("");
                            }
                        }
                    } else {
                        errorDialog.show();
                    }
                }
            });

            //This portion of code activates the Add Fuel Stop button, and validates the inputs
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

            //This portion of code activates the Analysis button, which closes this activity and opens another when clicked
            analysisButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Intent filterIntent = new Intent(getApplicationContext(), FuelAnalyticsActivity.class);
                    finish();
                    startActivity(filterIntent);
                }
            });

            //This portion of code activates the Delete Vehicle button, and checks if the user is sure they want to delete it.
            deleteVehicleButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    AlertDialog confirmDeleteDialog = confirmDeleteVehicleDialog(null);
                    confirmDeleteDialog.show();
                }
            });

            //This portion of code creates and activates the Vehicle Name spinner
            vehicleListSpinner = (Spinner) findViewById(R.id.vehicleListSpinner);
            VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
            VehicleList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vehicleListSpinner.setAdapter(VehicleList);
            vehicleListSpinner.setOnItemSelectedListener(this);
        }

        return loggedIn;
    }

    /**
     * Method: prepareFuelStopList
     * Purpose: This method searches the database for instances of fuel stops, and saves them in a list
     * Returns: None
     */
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

    /**
     * Method: confirmAddFuelStop
     * Purpose: This method builds and displays the message checking the user wants to add the fuel stop.
     * If confirmed, it adds the new data to the database
     * Returns: None
     */
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

    public AlertDialog displayHelp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelStopActivity.this);
        builder.setTitle("Fuel Stop Help");
        builder.setMessage("To use this feature, create a vehicle. \nNext, add in the details of your fuel stop and press the Add FuelStop button.\n\nKeep adding fuel stops, and click the Analytics button to view a breakdown of your fuel usage.");
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();
    }

    /**
     * Method: addData
     * Purpose: This method is called by the confirmAddFuelStop method, and adds the new data to the database
     * Returns: None
     */
    public void addData (String brandName, String fuelType, String odometer, String litresPurchased, String pricePerLitre, String location, String totalCost){
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        int max = 0;
        int newIndex = 0;

        //For loop to find the highest Fuel Stop ID in the database.
        for (int i : fuelStopIDList){
            if(i > max){
                max = i;
            }
        }
        newIndex = max + 1;

        //Add each piece of data to the database
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Date").setValue(date);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("BrandName").setValue(brandName);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("FuelType").setValue(fuelType);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Location").setValue(location);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Odometer").setValue(odometer);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Litres").setValue(litresPurchased);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Price").setValue(pricePerLitre);
        firebaseReference.child(googleID).child(selectedType).child(Integer.toString(newIndex)).child("Cost").setValue(totalCost);

        //Activate the Clear Form button
        clearFormButton.callOnClick();
    }

    /**
     * Method: confirmDeleteVehicleDialog
     * Purpose: This method checks if the user wants to delete the selected vehicle. On confirmation
     * it removes the vehicle and all data saved within.
     * Returns: None
     */
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

    /**
     * Method: createNoSavedDataDialog
     * Purpose: This method alerts the user if there is an invalid vehicle name.
     * This usually occurs when the name is already in use
     * Returns: None
     */
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

    /**
     * Method: invalidFuelStopError
     * Purpose: This method alerts the user that invalid details have been entered when the fuel stop
     * form was completed.
     * Returns: None
     */
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

    /**
     * Method: round
     * Purpose: This method takes in a Double and an Integer, and rounds the double to the input number of places
     * Returns: The rounded Double value
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * Method: onItemSelected
     * Purpose: The listener for the vehicle name spinner, it is called automatically whenever the data is updated.
     * It allows the user to select a vehicle, and also updates the list of vehicle details
     * Returns: None
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedType = parent.getItemAtPosition(position).toString();
        prepareFuelStopList();
        Toast.makeText(getApplicationContext(), "You have selected " + selectedType, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}