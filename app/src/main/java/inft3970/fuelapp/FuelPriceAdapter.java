package inft3970.fuelapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by shane on 16/10/2017.
 */

public class FuelPriceAdapter extends RecyclerView.Adapter<FuelPriceAdapter.FuelPriceHolder> {
    private static final String TAG = FuelPriceAdapter.class.getSimpleName();
    Context context = App.getContext();
    ArrayList<HashMap<String, String>> priceList;

    FuelPriceAdapter(ArrayList priceList) {this.priceList = priceList;}

    @Override
    public int getItemCount() {return  priceList.size();}

    @Override
    public FuelPriceHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fuel_item, viewGroup, false);
        FuelPriceHolder fpv = new FuelPriceHolder(v);
        return fpv;
    }

    public static class FuelPriceHolder extends RecyclerView.ViewHolder {
        TextView fuelName;
        TextView price;
        TextView lastUpdated;
        CardView fuelLayout;

        FuelPriceHolder(View itemView) {
            super(itemView);
            fuelName = (TextView)itemView.findViewById(R.id.fuel_code_txt);
            price = (TextView)itemView.findViewById(R.id.fuel_price_txt);
            lastUpdated = (TextView)itemView.findViewById(R.id.last_updated_txt);
            fuelLayout = (CardView)itemView.findViewById(R.id.fuel_item);
        }
    }

    @Override
    public void onBindViewHolder(FuelPriceHolder fuelPriceHolder, int i) {
        if(priceList.get(i).get("fuelType") != null) {
            fuelPriceHolder.fuelName.setText(FuelCodeNameCall.getTypeName(priceList.get(i).get("fuelType")));
        }
        if(priceList.get(i).get("price") != null) {
            fuelPriceHolder.price.setText(priceList.get(i).get("price"));
        }
        if(priceList.get(i).get("lastUpdated") != null) {
            fuelPriceHolder.lastUpdated.setText(getFormattedLastUpdated(priceList.get(i).get("lastUpdated")));
        }
        if((i & 1) == 0) {
            fuelPriceHolder.fuelLayout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            fuelPriceHolder.fuelLayout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private String getFormattedLastUpdated(String lastUpdated) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
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
