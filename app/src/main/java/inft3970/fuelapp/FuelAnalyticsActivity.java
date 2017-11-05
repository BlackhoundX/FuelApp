package inft3970.fuelapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static inft3970.fuelapp.FuelStopActivity.googleID;
import static inft3970.fuelapp.FuelStopActivity.round;

/**
 * Class: FuelAnalyticsActivity
 * Author: Matt Couch
 * Purpose: This class handles and displays the analytics relating to the user's input data
 * Creation Date: 21-Oct-17
 * Modification Date: 30-Oct-17
 */

public class FuelAnalyticsActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Context context;
    Button fuelStopButton; //Create the button which returns to the Fuel Stop activity
    Firebase firebaseReference; //Initialise the reference to Firebase

    //Initialise the set of variables relating to the dropdown menu of vehicle names
    Spinner vehicleListSpinner;
    ArrayAdapter<String> VehicleList;
    List<String> vehicleNames = new ArrayList<>();
    String selectedType;

    //Create the list of fuel stop IDs. This is static so the RVFuelStopAdapter class can read it.
    public static List<Integer> fuelStopIDList = new ArrayList<>();

    /*
    * Create the object to store the data from the database, using a List of Lists
    * The top level of the structure uses unique identifiers going from 1 and up
    * The bottom level contains the data itself, ordered as follows:
    *
    * Position 0: Date
    * Position 1: Fuel Type
    * Position 2: Odometer Reading
    * Position 3: Litres Filled
    * Position 4: Brand Name
    * Position 5: Price Per Litre (stored as cents per litre)
    * Position 6: Location
    *
    */
    List<List<String>> fuelStops = new ArrayList<>();
    List<String> fuelStopDetails;

    //Initialise the Recycler View components
    private RecyclerView rvFuelStop;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layout;

    //Initialise the components of the Economy portion of display
    TextView kilometrePerLitre;
    TextView litresToHundred;
    TextView kilometrePerDollar;
    TextView dollarPerHundred;

    //Initialise the components of the Totals portion of display
    TextView totalDistance;
    TextView totalRefills;
    TextView totalLitresFilled;
    TextView totalSpent;

    //Initialise the components of the Average Fuel Stop portion of display
    TextView averageDistance;
    TextView averageLitresFilled;
    TextView averageSpend;
    TextView averageCentsPerLitre;

    FloatingActionButton returnButton;

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It initialises some variables and objects.
     * Returns: None
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = App.getContext();
        Firebase.setAndroidContext(this);
        
        setContentView(R.layout.activity_fuel_analytics);
        returnButton = (FloatingActionButton)findViewById(R.id.returnButton);

        //Root URL of the firebase database
        firebaseReference = new Firebase("https://inft3970-major-p-1503830364825.firebaseio.com/");

        //Creates and sets up the spinner which lists all the vehicles
        vehicleListSpinner = (Spinner) findViewById(R.id.vehicleListSpinner);
        VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
        VehicleList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleListSpinner.setAdapter(VehicleList);
        vehicleListSpinner.setOnItemSelectedListener(this);

        //Creates the button which will return the user to the Fuel Stop Activity
        fuelStopButton = (Button)findViewById(R.id.fuelStopButton);
        fuelStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelStopActivity.class);
                finish();
                startActivity(filterIntent);
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Initialise the components of the Economy portion of display
        kilometrePerLitre = (TextView)findViewById(R.id.kilometrePerLitre);
        litresToHundred = (TextView)findViewById(R.id.litresToHundred);
        kilometrePerDollar = (TextView)findViewById(R.id.kilometrePerDollar);
        dollarPerHundred = (TextView)findViewById(R.id.dollarPerHundred);

        //Initialise the components of the Totals portion of display
        totalDistance = (TextView)findViewById(R.id.totalDistance);
        totalRefills = (TextView)findViewById(R.id.totalRefills);
        totalLitresFilled = (TextView)findViewById(R.id.totalLitresFilled);
        totalSpent = (TextView)findViewById(R.id.totalSpent);

        //Initialise the components of the Average Fuel Stop portion of display
        averageDistance = (TextView)findViewById(R.id.averageDistance);
        averageLitresFilled = (TextView)findViewById(R.id.averageLitresFilled);
        averageSpend = (TextView)findViewById(R.id.averageSpend);
        averageCentsPerLitre = (TextView)findViewById(R.id.averageCentsPerLitre);
    }

    /**
     * Method: onStart
     * Purpose: Automatically is called when the class is started. It populates the vehicle name spinner.
     * Returns: None
     */
    @Override
    protected void onStart() {
        super.onStart();
        vehicleNames.clear(); //Resets the list to avoid repeat entries on each startup
        firebaseReference.child(googleID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstChild;
                vehicleNames.clear();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) { //Loops through each child
                    firstChild = postsnapshot.getKey(); //Gets the vehicle name
                    vehicleNames.add(firstChild); //Stores the vehicle name in the list
                    VehicleList.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Method: prepareFuelStopList
     * Purpose: Reads from the database to fill up the list of fuel stops.
     * It then creates the recycler and uses the newly added data to calculate and display the totals, averages, etc.
     * Returns: None
     */
    public void prepareFuelStopList(){
        fuelStopIDList.clear();
        firebaseReference.child(googleID).child(selectedType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String entryID;
                String date = "";
                String fuelType = "";
                String odometer = "";
                String litres = "";
                String brandName = "";
                String pricePerLitre = "";
                String location = "";

                fuelStopIDList.clear();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {

                    //Go through each entry in the database and compile list of fuel stop IDs
                    entryID = postsnapshot.getKey();
                    fuelStopIDList.add(Integer.parseInt(entryID));

                    if(!entryID.equals("0")) { //If the entry ID is 0 then skip it; this is a default value in Firebase
                        for (DataSnapshot childsnapshot : dataSnapshot.child(entryID).getChildren()) {
                            //This loops through each child in the database and sorts it based on the entry Key
                            if(childsnapshot.getKey().equals("Date")) {
                                date = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("FuelType")) {
                                fuelType = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("Odometer")) {
                                odometer = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("Litres")) {
                                litres = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("BrandName")) {
                                brandName = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("Price")) {
                                pricePerLitre = childsnapshot.getValue().toString();
                            }
                            if(childsnapshot.getKey().equals("Location")) {
                                location = childsnapshot.getValue().toString();
                            }

                            fuelStopDetails = new ArrayList<>(); //Create the new list to store the details

                            //Set the values to the inner list, in the order as described upon initialisation
                            fuelStopDetails.add(date);
                            fuelStopDetails.add(fuelType);
                            fuelStopDetails.add(odometer);
                            fuelStopDetails.add(litres);
                            fuelStopDetails.add(brandName);
                            fuelStopDetails.add(pricePerLitre);
                            fuelStopDetails.add(location);

                            fuelStops.add(Integer.parseInt(entryID)-1, fuelStopDetails); //Add the data to the overall object
                        }
                    }
                }
                if(fuelStopIDList.size() > 0) {
                    createRecycler(); //Create the recycler view which will display each fuel stop as a separate card
                    calculateValues(); //Calculate the values for Economy, Totals and Average. It then displays them.
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    /**
     * Method: createRecycler
     * Purpose: Creates and populates the recycler which displays the details of each fuel stop
     * Returns: None
     */
    public void createRecycler()
    {
        rvFuelStop = (RecyclerView)findViewById(R.id.rvFuelStopCard);
        rvFuelStop.setHasFixedSize(false);

        layout = new LinearLayoutManager(this);
        rvFuelStop.setLayoutManager(layout);
        adapter = new RVFuelStopAdapter(fuelStops);
        rvFuelStop.setAdapter(adapter);
    }

    /**
     * Method: calculateValues
     * Purpose: Reads values from the database to calculate relevant data for the user including
     * Totals, Economy and Averages
     * Returns: None
     */
    public void calculateValues() {

        int totalRefillCount = fuelStopIDList.size() - 1; //Sets the number of refills based on the number of stops

        double kmPerLitreAverage;
        double litresToHundredAverage;
        double kilometresPerDollarAverage;
        double dollarPerHundredAverage;

        double totalDistanceTravelled;
        double totalLitres = 0;

        double distanceBetweenStopAverage;
        double litresFilledPerStopAverage;
        double pricePerStopAverage;

        if (totalRefillCount == 0) {
            //No data can be filled as there are no stops, so alert the user and set the fields to display 0s.
            Toast.makeText(getApplicationContext(), "No fuel stop data!", Toast.LENGTH_LONG).show();

            kilometrePerLitre.setText("0 km/litre");
            litresToHundred.setText("0 litres/100km");
            kilometrePerDollar.setText("0 km/$1");
            dollarPerHundred.setText("$0/100km");

            totalDistance.setText("0 km driven");
            totalRefills.setText("0 refills");
            totalLitresFilled.setText("0 litres filled");
            totalSpent.setText("$0 total spent");

            averageDistance.setText("0 km driven");
            averageLitresFilled.setText("0 litres filled");
            averageSpend.setText("$0 spent");
            averageCentsPerLitre.setText("0c per litre");

        } else if (totalRefillCount == 1) {
            //There is only one fuel stop, which means some of the values cannot be calculated.
            //The remainder of the values are calculated and displayed.

            //Economy details
            kilometrePerLitre.setText("0 km/litre");
            litresToHundred.setText("0 litres/100km");
            dollarPerHundred.setText("$0/100km");
            totalDistance.setText("0 km driven");

            //Totals details
            totalDistance.setText("0 km driven"); //This value cannot be calculated from a single entry
            totalRefills.setText((totalRefillCount) + " refills");
            totalLitres = round(Double.parseDouble(fuelStops.get(0).get(3)), 2);
            totalLitresFilled.setText(Double.toString(totalLitres) + " litres filled");

            totalLitres = round(totalLitres, 2);
            totalLitresFilled.setText(Double.toString(totalLitres) + " litres filled");

            double calculateSingleStop = Double.parseDouble(fuelStops.get(0).get(5)) * Double.parseDouble(fuelStops.get(0).get(3)) / 100;
            calculateSingleStop = round(calculateSingleStop, 2);
            totalSpent.setText("$" + calculateSingleStop + " total spent");

            //Averages details
            averageDistance.setText("0 km driven"); //This value cannot be calculated from a single entry
            averageLitresFilled.setText(totalLitres + " litres filled");
            averageSpend.setText("$" + calculateSingleStop + " spent");

            double singlePrice = Double.parseDouble(fuelStops.get(0).get(5));
            singlePrice = round(singlePrice, 2);
            averageCentsPerLitre.setText(singlePrice + "c per litre");

        } else {
            //More than one entry exists, and all values can be safely calculated

            double costRunningTotal = 0;
            double tempCost = 0;
            double priceAverageCalculator = 0;

            //Loop through all entries, adding together running totals of Litres filled, cost, and the average fuel price
            for (int i = 0; i < totalRefillCount; i++) {
                totalLitres += Double.parseDouble(fuelStops.get(i).get(3));

                tempCost = Double.parseDouble(fuelStops.get(i).get(5)) * Double.parseDouble(fuelStops.get(i).get(3));
                costRunningTotal += tempCost;

                priceAverageCalculator += Double.parseDouble(fuelStops.get(i).get(5));
            }

            //Below here the values are calculated. They are not in order of appearance on the app
            //due to their reliance on each other for the various values

            //Calculates total distance travelled
            Double finalOdometer = Double.parseDouble(fuelStops.get(totalRefillCount).get(2));
            Double initialOdometer = Double.parseDouble(fuelStops.get(0).get(2));
            totalDistanceTravelled = finalOdometer - initialOdometer;
            totalDistance.setText(Double.toString(totalDistanceTravelled) + " km driven");

            //Calculates and sets the total number of refills
            totalRefills.setText((totalRefillCount) + " refills");

            //Sets the total litres filled based on the above loop
            totalLitres = round(totalLitres, 2);
            totalLitresFilled.setText(Double.toString(totalLitres) + " litres filled");

            //Calculates and sets the total spent
            costRunningTotal = costRunningTotal / 100;
            costRunningTotal = round(costRunningTotal, 2);
            totalSpent.setText("$" + Double.toString(costRunningTotal) + " total spent");

            //Calculate and set the km/L average
            kmPerLitreAverage = totalDistanceTravelled / totalLitres;
            kmPerLitreAverage = round(kmPerLitreAverage, 2);
            kilometrePerLitre.setText(kmPerLitreAverage + " km/litre");

            //Calculates and sets the litres to hundred average
            litresToHundredAverage = 100 / kmPerLitreAverage;
            litresToHundredAverage = round(litresToHundredAverage, 2);
            litresToHundred.setText(litresToHundredAverage + " litres/100km");

            //Calculates and sets the kilometre per dollar average
            kilometresPerDollarAverage = totalDistanceTravelled / costRunningTotal;
            kilometresPerDollarAverage = round(kilometresPerDollarAverage, 2);
            kilometrePerDollar.setText(kilometresPerDollarAverage + " km/$1");

            //Calculates and sets the dollar per hundred average
            dollarPerHundredAverage = 100 / kilometresPerDollarAverage;
            dollarPerHundredAverage = round(dollarPerHundredAverage, 2);
            dollarPerHundred.setText("$" + dollarPerHundredAverage + "/100km");

            //Calculates and sets the average distance between stops
            distanceBetweenStopAverage = totalDistanceTravelled / totalRefillCount;
            distanceBetweenStopAverage = round(distanceBetweenStopAverage, 2);
            averageDistance.setText(distanceBetweenStopAverage + " km driven");

            //Calculates and sets the average litres filled per stop
            litresFilledPerStopAverage = totalLitres / totalRefillCount;
            litresFilledPerStopAverage = round(litresFilledPerStopAverage, 2);
            averageLitresFilled.setText(litresFilledPerStopAverage + " litres filled");

            //Calculates and sets the average $ spent per stop
            pricePerStopAverage = costRunningTotal / totalRefillCount;
            pricePerStopAverage = round(pricePerStopAverage, 2);
            averageSpend.setText("$" + pricePerStopAverage + " spent");

            //Calculates and sets the average price per litre
            priceAverageCalculator = priceAverageCalculator / totalRefillCount;
            priceAverageCalculator = round(priceAverageCalculator, 2);
            averageCentsPerLitre.setText(priceAverageCalculator + "c per litre");
        }
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
