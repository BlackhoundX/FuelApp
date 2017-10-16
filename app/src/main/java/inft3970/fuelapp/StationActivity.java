package inft3970.fuelapp;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by shane on 15/10/2017.
 */

public class StationActivity extends Activity {

    private static final String TAG = StationActivity.class.getSimpleName();
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.ENGLISH);
    private int transactionId = 0;
    private String authCode;
    ArrayList fuelPrices;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_station);
        HashMap stationInfo = (HashMap) getIntent().getSerializableExtra("stationData");
        String stationCode = getIntent().getStringExtra("stationCode");
        authCode = getIntent().getStringExtra("authCode");
        Date time = new Date();
        String timeString = dateFormat.format(time);

        TextView stationName = (TextView)findViewById(R.id.station_name_text);
        TextView stationAddress = (TextView)findViewById(R.id.station_address_text);
        ImageView stationBrand = (ImageView)findViewById(R.id.station_brand_img);

        stationName.setText((String)stationInfo.get("name"));
        stationAddress.setText((String)stationInfo.get("address"));
        try {
            String icon = IconStringCall.getIconString((String)stationInfo.get("brand"));
            AssetManager assetManager = App.getContext().getAssets();
            InputStream is = assetManager.open(icon);
            Drawable draw = Drawable.createFromStream(is, null);
            stationBrand.setImageDrawable(draw);
            is.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        FuelPriceByCodeCall fuelPriceCall = new FuelPriceByCodeCall();
        String[][] headers = new String[][]{{"apikey", getString(R.string.fuel_api_key)},{"transactionid", Integer.toString(transactionId++)},{"requesttimestamp", timeString},{"Content-Type", "application/json; charset=utf-8"}, {"Authorization", "Bearer "+authCode}};
        fuelPrices = fuelPriceCall.getFuelPricesByCode(stationCode, headers);

        ListView fuelList = (ListView)findViewById(R.id.fuel_list);
        FuelPriceAdapter adapter = new FuelPriceAdapter(App.getContext(), fuelPrices);
        fuelList.setAdapter(adapter);
    }
}
