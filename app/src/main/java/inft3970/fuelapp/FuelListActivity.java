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
 * Created by shane on 14/09/2017.
 */

public class FuelListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_fuel_list);

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
