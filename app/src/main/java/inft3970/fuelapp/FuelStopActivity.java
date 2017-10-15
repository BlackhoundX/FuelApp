package inft3970.fuelapp;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import static inft3970.fuelapp.FuelMapActivity.googleID;


/**
 * Created by Matt on 13-Oct-17.
 */

public class FuelStopActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Spinner vehicleList;
    Context context;
    String selectedType;
    String userID = "";
    String[] vehicleNames = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_stop);
        context = App.getContext();


        userID = googleID;


        //Dummy values, loop here to read all.
        vehicleNames[0] = userID;
        vehicleNames[1] = "Two";

        //set visibility based on if no vehicles are available, only show button and addVehicle section

        vehicleList = (Spinner)findViewById(R.id.vehicleList);

        //Get the list of vehicles under the userID, and display in a dropdown menu
        vehicleList.setOnItemSelectedListener(this);
        ArrayAdapter<String> VehicleList = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, vehicleNames);
        vehicleList.setAdapter(VehicleList);

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
