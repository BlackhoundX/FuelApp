package inft3970.fuelapp;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static inft3970.fuelapp.R.id.dateText;

/**
 * Created by quikb on 22-Oct-17.
 */

public class RVFuelStopAdapter extends RecyclerView.Adapter<RVFuelStopAdapter.FuelStopHolder> {

    private static final String TAG = RVFuelStopAdapter.class.getSimpleName();

    Map<Integer, List<String>> fuelStopList = new HashMap<Integer, List<String>>();
    //String[] fuelStopDetails = new String[];

    RVFuelStopAdapter(Map fuelStopList) {
        this.fuelStopList = fuelStopList;
    }

    @Override
    public int getItemCount() {
        return fuelStopList.size();
    }

    @Override
    public RVFuelStopAdapter.FuelStopHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fuel_stop_card, viewGroup, false);
        RVFuelStopAdapter.FuelStopHolder fsh = new RVFuelStopAdapter.FuelStopHolder(v);
        return fsh;
    }

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
            date = (TextView)itemView.findViewById(R.id.dateText);
            fuelType = (TextView)itemView.findViewById(R.id.fuelTypeText);
            odometer = (TextView)itemView.findViewById(R.id.odometerText);
            litres = (TextView)itemView.findViewById(R.id.litresText);
            brand = (TextView)itemView.findViewById(R.id.brandText);
            centsPerLitreText = (TextView)itemView.findViewById(R.id.centsPerLitre);
            location = (TextView)itemView.findViewById(R.id.locationText);
            totalCost = (TextView)itemView.findViewById(R.id.totalCostText);
        }
    }

    @Override
    public void onBindViewHolder(RVFuelStopAdapter.FuelStopHolder viewStopHolder, int i) {
        viewStopHolder.date.setText("test");
        viewStopHolder.fuelType.setText("test");
        viewStopHolder.odometer.setText("test");
        viewStopHolder.litres.setText("test");
        viewStopHolder.brand.setText("test");
        viewStopHolder.centsPerLitreText.setText("test");
        viewStopHolder.location.setText("test");
        viewStopHolder.totalCost.setText("test");

        //HERE IS WHERE WE FILL THE CARD



        /*if(stationList.get(i).get("name") != null) {
            stationViewHolder.stationName.setText(stationList.get(i).get("name"));
        }
        if(stationList.get(i).get("address") != null) {
            stationViewHolder.stationAddress.setText(stationList.get(i).get("address"));
        }
        if(stationList.get(i).get("price") != null) {
            stationViewHolder.price.setText(stationList.get(i).get("price"));
        }
        if(stationList.get(i).get("lastUpdated") != null) {
            stationViewHolder.stationLastUpdated.setText(getFormattedLastUpdated(stationList.get(i).get("lastUpdated")));
        }
        if(stationList.get(i).get("distance") != null) {
            String distanceText = stationList.get(i).get("distance") + " Km Away";
            stationViewHolder.stationKmFromPoint.setText(distanceText);
        }
        if(stationList.get(i).get("brand") != null) {
            try {
                String icon = getIconString(stationList.get(i).get("brand"));
                AssetManager mg = App.getContext().getAssets();
                InputStream ims = mg.open(icon);
                Drawable draw = Drawable.createFromStream(ims, null);
                stationViewHolder.stationIcon.setImageDrawable(draw);
                ims.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }*/
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
