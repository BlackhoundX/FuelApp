package inft3970.fuelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static inft3970.fuelapp.FuelMapActivity.googleID;

/**
 * Created by quikb on 21-Oct-17.
 */

public class FuelAnalyticsActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Button fuelStopButton;
    Spinner vehicleListSpinner;
    Context context;
    Firebase firebaseReference;
    DataSnapshot dataSnapshot;
    ArrayAdapter<String> VehicleList;
    List<String> vehicleNames = new ArrayList<>();
    String selectedType;
    List<Integer> fuelStopIDList = new ArrayList<>();
    Map<Integer, List<String>> fuelStopMap = new HashMap<Integer, List<String>>();
    List<String> fuelStopDetails;

    private RecyclerView rvFuelStop;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stop);
        context = App.getContext();
        Firebase.setAndroidContext(this);
        
        setContentView(R.layout.activity_fuel_analytics);

        firebaseReference = new Firebase("https://inft3970-major-p-1503830364825.firebaseio.com/");

        vehicleListSpinner = (Spinner) findViewById(R.id.vehicleListSpinner);
        VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
        VehicleList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleListSpinner.setAdapter(VehicleList);
        vehicleListSpinner.setOnItemSelectedListener(this);

        //Beginning of recycler view part, it crashes on rvFuelStop.setHasFixedSize(false);
        rvFuelStop = (RecyclerView)findViewById(R.id.rvFuelStopCard);
        rvFuelStop.setHasFixedSize(false);

        layout = new LinearLayoutManager(this);
        rvFuelStop.setLayoutManager(layout);

        adapter = new RVFuelStopAdapter(fuelStopMap);
        rvFuelStop.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
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

        fuelStopButton = (Button)findViewById(R.id.fuelStopButton);
        fuelStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent filterIntent = new Intent(getApplicationContext(), FuelStopActivity.class);
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
                String date = "";
                String fuelType = "";
                String odometer = "";
                String litres = "";
                String brandName = "";
                String pricePerLitre = "";
                String location = "";
                String totalCost;

                fuelStopIDList.clear();
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    entryID = postsnapshot.getKey();
                    fuelStopIDList.add(Integer.parseInt(entryID));

                    if(!entryID.equals("0")) {
                        for (DataSnapshot childsnapshot : dataSnapshot.child(entryID).getChildren()) {
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
                            fuelStopDetails = new ArrayList<>();
                            fuelStopDetails.add(date);
                            fuelStopDetails.add(fuelType);
                            fuelStopDetails.add(odometer);
                            fuelStopDetails.add(litres);
                            fuelStopDetails.add(brandName);
                            fuelStopDetails.add(pricePerLitre);
                            fuelStopDetails.add(location);

                            fuelStopMap.put(Integer.parseInt(entryID), fuelStopDetails);
                        }
                        Toast.makeText(getApplicationContext(), date + fuelType + odometer + litres + brandName + pricePerLitre + location, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

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
