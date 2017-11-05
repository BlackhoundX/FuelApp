package inft3970.fuelapp;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import static inft3970.fuelapp.FuelStopActivity.round;


/**
 * Class: RVFuelStopAdapter
 * Author: Matt Couch
 * Purpose: This class handles the Recycler View for displaying each instance of the FuelStopCard object.
 * Creation Date: 22-Oct-17
 * Modification Date: 30-Oct-17
 */

public class RVFuelStopAdapter extends RecyclerView.Adapter<RVFuelStopAdapter.FuelStopHolder> {

    private static final String TAG = RVFuelStopAdapter.class.getSimpleName();
    Context context;

    List<List<String>> fuelStops;  //The list object containing the fuel stop data

    /**
     * Method: RVFuelStopAdapter
     * Purpose: Acts as the constructor of this class. Points the fuelStops List object to the list
     * in FuelAnalyticsActivity which has the data filled out
     * Returns: None
     */
    RVFuelStopAdapter(List fuelStops) {
        context = App.getContext();
        this.fuelStops = fuelStops;
    }

    /**
     * Method: getItemCount
     * Purpose: Retrieves the length of the list being used. This is called by the Recycler View itself
     * Returns: The size of the list as an integer
     */
    @Override
    public int getItemCount() {
        return FuelAnalyticsActivity.fuelStopIDList.size()-1;
    }

    /**
     * Method: onCreateViewHolder
     * Purpose: Creates the layout for the recycler to populate with data, and calls the method to create each Holder
     * Returns: The constructed holder
     */
    @Override
    public FuelStopHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fuel_stop_card, viewGroup, false);
        RVFuelStopAdapter.FuelStopHolder fsh = new RVFuelStopAdapter.FuelStopHolder(v);
        return fsh;
    }

    /**
     * Class: FuelStopHolder
     * Author: Matt Couch
     * Purpose: This class creates each individual instance of the data container for display, and initialises the fields
     * Creation Date: 22-Oct-17
     * Modification Date: 30-Oct-17
     */
    public static class FuelStopHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView date;
        TextView fuelType;
        TextView odometer;
        TextView litres;
        TextView brand;
        TextView centsPerLitreText;
        TextView location;
        TextView totalCost;

        FuelStopHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.fuelStopView);
            date = (TextView)itemView.findViewById(R.id.dateTextField);
            fuelType = (TextView)itemView.findViewById(R.id.fuelTypeTextField);
            odometer = (TextView)itemView.findViewById(R.id.odometerTextField);
            litres = (TextView)itemView.findViewById(R.id.litresTextField);
            brand = (TextView)itemView.findViewById(R.id.brandTextField);
            centsPerLitreText = (TextView)itemView.findViewById(R.id.centsPerLitreField);
            location = (TextView)itemView.findViewById(R.id.locationTextField);
            totalCost = (TextView)itemView.findViewById(R.id.totalCostTextField);
        }
    }

    /**
     * Method: onBindViewHolder
     * Purpose: Takes in the Holder object and populates it with data based on the integer input as
     * a reference to the particular series of data.
     * Returns: None
     */
    @Override
    public void onBindViewHolder(RVFuelStopAdapter.FuelStopHolder viewStopHolder, int i) {
        viewStopHolder.date.setText(fuelStops.get(i).get(0));
        viewStopHolder.fuelType.setText(fuelStops.get(i).get(1));
        viewStopHolder.odometer.setText(fuelStops.get(i).get(2) + "km");
        viewStopHolder.litres.setText(fuelStops.get(i).get(3) + " litres");
        viewStopHolder.brand.setText(fuelStops.get(i).get(4));
        viewStopHolder.centsPerLitreText.setText(fuelStops.get(i).get(5) + " cents/litre");
        viewStopHolder.location.setText(fuelStops.get(i).get(6));

        //Calculates the total cost of the fuel stop based on the other values for the stop
        double totalCost = Double.parseDouble(fuelStops.get(i).get(5)) * Double.parseDouble(fuelStops.get(i).get(3)) / 100.0;
        totalCost = round(totalCost, 2);
        viewStopHolder.totalCost.setText("$" + totalCost);
    }

    /**
     * Method: onAttachedToRecyclerView
     * Purpose: Automatically generated method which is called on by the Recycler as it starts observing this Adapter
     * Returns: None
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
