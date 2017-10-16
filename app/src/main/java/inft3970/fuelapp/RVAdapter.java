package inft3970.fuelapp;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by shane on 15/09/2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.StationViewHolder> {

    private static final String TAG = RVAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> stationList;

    RVAdapter(ArrayList stationList) {
        this.stationList = stationList;
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    @Override
    public StationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fuel_card, viewGroup, false);
        StationViewHolder svh = new StationViewHolder(v);
        return svh;
    }

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

    @Override
    public void onBindViewHolder(StationViewHolder stationViewHolder, int i) {
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

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
