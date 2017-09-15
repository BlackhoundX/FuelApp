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
import java.util.ArrayList;
import java.util.HashMap;

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
        ImageView stationIcon;



        StationViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.station_view);
            stationName = (TextView)itemView.findViewById(R.id.station_title_text);
            stationAddress = (TextView)itemView.findViewById(R.id.station_address_text);
            price = (TextView)itemView.findViewById(R.id.station_price_text);
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
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public String getIconString(String brand) {
        String iconFile;

        switch (brand) {
            case "7-Eleven":
                iconFile = "711icon.png";
                break;
            case "BP":
                iconFile = "bpIcon.png";
                break;
            case "Caltex":
                iconFile = "caltexIcon.png";
                break;
            case "Caltex Woolworths":
                iconFile = "woolworthsCaltex.png";
                break;
            case "Coles Express":
                iconFile = "colesexpress.png";
                break;
            case "Costco":
                iconFile = "costcoLogo.png";
                break;
            case "Enhance":
                iconFile = "defaultLogo.png";
                break;
            case "Independent":
                iconFile = "defaultLogo.png";
                break;
            case "Liberty":
                iconFile = "liberty.png";
                break;
            case "Lowes":
                iconFile = "lowes.png";
                break;
            case "Matilda":
                iconFile = "matilda.png";
                break;
            case "Metro Fuel":
                iconFile = "metro.png";
                break;
            case "Mobil":
                iconFile = "mobil.png";
                break;
            case "Prime Petroleum":
                iconFile = "defaultLogo.png";
                break;
            case "Puma Energy":
                iconFile = "puma.png";
                break;
            case "Shell":
                iconFile = "shell.png";
                break;
            case "Speedway":
                iconFile = "speedway.png";
                break;
            case "Tesla":
                iconFile = "tesla.png";
                break;
            case "United":
                iconFile = "united.png";
                break;
            case "Westside":
                iconFile = "westside.png";
                break;
            default:
                iconFile = "defaultLogo.png";
                break;
        }
        return iconFile;
    }
}
