package inft3970.fuelapp;

import android.content.Context;
import android.content.Intent;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Class: RVAdapter
 * Author: Shane
 * Purpose: This class handles the Recycler View for displaying each instance of the Fuel Station object.
 * Creation Date: 15-Sep-17
 * Modification Date: 05-Nov-17
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.StationViewHolder> {

    private static final String TAG = RVAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> stationList;
    Context context = App.getContext();

    /**
     * Method: RVAdapter
     * Purpose: Acts as the constructor of this class. Points the Station List object to the list
     * created in FuelListActivity which is filled out.
     * Returns: None
     */
    RVAdapter(ArrayList stationList) {
        this.stationList = stationList;
    }

    /**
     * Method: getItemCount
     * Purpose: Retrieves the length of the list being used. This is called by the Recycler View itself
     * Returns: The size of the list as an integer
     */
    @Override
    public int getItemCount() {
        return stationList.size();
    }

    /**
     * Method: onCreateViewHolder
     * Purpose: Creates the layout for the recycler to populate with data, and calls the method to create each Holder
     * Returns: The constructed holder
     */
    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fuel_card, viewGroup, false);
        StationViewHolder svh = new StationViewHolder(v);
        return svh;
    }

    /**
     * Class: StationViewHolder
     * Author: Shane
     * Purpose: This class creates each individual instance of the data container for display, and initialises the fields
     * Creation Date: 15-Sep-17
     * Modification Date: 05-Nov-17
     */
    public static class StationViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView stationName;
        TextView stationAddress;
        TextView price;
        TextView stationLastUpdated;
        TextView stationKmFromPoint;
        ImageView stationIcon;

        StationViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.station_view);
            stationName = (TextView)itemView.findViewById(R.id.station_title_text);
            stationAddress = (TextView)itemView.findViewById(R.id.station_address_text);
            price = (TextView)itemView.findViewById(R.id.station_price_text);
            stationLastUpdated = (TextView)itemView.findViewById(R.id.station_last_updated_text);
            stationKmFromPoint = (TextView)itemView.findViewById(R.id.station_km_from_point_text);
            stationIcon = (ImageView)itemView.findViewById(R.id.station_icon_img);
        }
    }

    /**
     * Method: onBindViewHolder
     * Purpose: Takes in the Holder object and populates it with data based on the integer input as
     * a reference to the particular series of data.
     * Returns: None
     */
    @Override
    public void onBindViewHolder(final StationViewHolder stationViewHolder, final int i) {
        stationViewHolder.cv.setClickable(true);
        stationViewHolder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> stationData = new HashMap<>();
                stationData.put("brand", stationList.get(i).get("brand"));
                stationData.put("name", stationList.get(i).get("name"));
                stationData.put("address", stationList.get(i).get("address"));
                stationData.put("latitude", stationList.get(i).get("latitude"));
                stationData.put("longitude", stationList.get(i).get("longitude"));
                Intent stationIntent = new Intent(context, StationActivity.class);
                stationIntent.putExtra("stationData", stationData);
                stationIntent.putExtra("stationCode", stationList.get(i).get("code"));
                context.startActivity(stationIntent);
            }
        });

        if(stationList.get(i).get("name") != null) {
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
                String icon = IconStringCall.getIconString(stationList.get(i).get("brand"));
                AssetManager mg = App.getContext().getAssets();
                InputStream ims = mg.open(icon);
                Drawable draw = Drawable.createFromStream(ims, null);
                stationViewHolder.stationIcon.setImageDrawable(draw);
                ims.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
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

    /**
     * Method: getFormattedLastUpdated
     * Purpose: Gets the date of last updated fuel price for a station, and formats it.
     * Returns: A string with the formatted time of last update
     */
    private String getFormattedLastUpdated(String lastUpdated) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date lastUpdatedDate = null;
        Date currentDate = null;
        String currentDateString = format.format(new Date());
        String lastUpdatedString = "Last Updated: ";
        try {
            lastUpdatedDate = format.parse(lastUpdated);
            currentDate = format.parse(currentDateString);
        } catch(ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        long timeDiff = Math.abs(currentDate.getTime() - lastUpdatedDate.getTime());
        long diffDays = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long diffHours = TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(diffDays);
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(diffHours);
        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff) - TimeUnit.MINUTES.toSeconds(diffMinutes);

        if(diffDays > 1) {
            lastUpdatedString += diffDays + " Days Ago";
        } else if(diffDays == 1) {
            lastUpdatedString += "1 Day Ago";
        } else if(diffHours > 1) {
            lastUpdatedString += diffHours + " Hours Ago";
        } else if(diffHours == 1) {
            lastUpdatedString += "1 Hour Ago";
        } else if(diffMinutes > 1) {
            lastUpdatedString += diffMinutes + " Minutes Ago";
        } else if(diffMinutes == 1) {
            lastUpdatedString += "1 Minute Ago";
        } else if(diffSeconds > 1) {
            lastUpdatedString += diffSeconds + " Seconds Ago";
        } else if(diffSeconds == 1) {
            lastUpdatedString += "1 Second Ago";
        }
    return lastUpdatedString;
    }
}
