package inft3970.fuelapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class: FuelListActivity
 * Author: Shane
 * Purpose: This class handles the Recycler View for displaying each instance of Fuel Station object,
 * and displays them in list view.
 * Creation Date: 14-Sep-17
 * Modification Date: 05-Nov-17
 */

public class FuelListActivity extends Activity {

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It sets up the list of stations,
     * sets up the Recycler, and initialises the Return button.
     * Returns: None
     */
    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_fuel_list);

        //Gets the list of stations
        ArrayList<HashMap<String, String>> stationList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("station_list");

        RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
        rv.setHasFixedSize(false);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(stationList);
        rv.setAdapter(adapter);

        FloatingActionButton mapViewBtn = (FloatingActionButton)findViewById(R.id.returnButton);
        mapViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
