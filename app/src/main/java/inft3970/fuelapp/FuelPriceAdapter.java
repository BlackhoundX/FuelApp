package inft3970.fuelapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shane on 16/10/2017.
 */

public class FuelPriceAdapter extends ArrayAdapter<HashMap<String, String>> {
    private static final String TAG = FuelPriceAdapter.class.getSimpleName();
    ArrayList<HashMap<String, String>> fuelPrices;

    public FuelPriceAdapter(Context context, ArrayList fuelPrices) {
        super(context,0,fuelPrices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap fuelPrice = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fuel_item, parent, false);
        }

        TextView fuelTypeText = (TextView)convertView.findViewById(R.id.fuel_code_txt);
        TextView fuelPriceText = (TextView)convertView.findViewById(R.id.fuel_price_txt);

        fuelTypeText.setText((String)fuelPrice.get("fuelType"));
        fuelPriceText.setText((String)fuelPrice.get("price"));

        return convertView;
    }
}
