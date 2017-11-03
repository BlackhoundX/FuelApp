package inft3970.fuelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by shane on 7/10/2017.
 */

public class FuelFilterActivity extends Activity implements AdapterView.OnItemSelectedListener {
    Spinner fuelTypeList;
    String[] fuelNames;
    ArrayList<String> selectedBrands;
    String selectedType;
    TextView brandListText;
    FloatingActionButton saveButton;
    AlertDialog brandDialog = null;
    private boolean allSelected = true;
    private int arrayLength;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_filter);
        fuelTypeList = (Spinner)findViewById(R.id.fuel_type_spinner);
        brandListText = (TextView)findViewById(R.id.fuel_brand_names);
        saveButton = (FloatingActionButton)findViewById(R.id.save_filter_button);
        arrayLength = getResources().getStringArray(R.array.brands).length;
        context = App.getContext();

        fuelTypeList.setOnItemSelectedListener(this);
        fuelNames = getResources().getStringArray(R.array.fuel_names);

        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, fuelNames);

        fuelTypeList.setAdapter(fuelTypeAdapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog errorDialog = createNoSavedDataDialog(null);
                if((selectedBrands != null) && selectedType != null) {
                    if(!selectedBrands.isEmpty()) {
                        String[] settingsData = new String[2];
                        settingsData[0] = FuelCodeNameCall.getTypeCode(selectedType);
                        settingsData[1] = getBrandNamesText(selectedBrands, true);
                        XmlSettings xmlSettings = new XmlSettings();
                        xmlSettings.writeXml(settingsData);
                        finish();
                    } else {
                        errorDialog.show();
                    }
                } else {
                    errorDialog.show();
                }
            }
        });
    }

    public void onDialog(View v) {
        brandDialog = createFuelBrandDialog(null);
        brandDialog.show();
        selectedBrands = new ArrayList<>();
        final ListView brandList = brandDialog.getListView();
        brandList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isChecked = brandList.isItemChecked(position);
                if(position == 0) {
                    if(allSelected) {
                        for(int i = 0;i < arrayLength; i++) {
                            if((isChecked && !brandList.isItemChecked(i)) || (!isChecked && brandList.isItemChecked(i))) {
                                if(!brandList.isItemChecked(i)) {
                                    selectedBrands.add(getResources().getStringArray(R.array.brands)[i]);
                                } else if(brandList.isItemChecked(i)) {
                                    selectedBrands.remove(i);
                                }
                                brandList.performItemClick(brandList, i, 0);
                            }
                        }
                    }
                } else {
                    if(!isChecked && brandList.isItemChecked(0)) {
                        allSelected = false;
                        selectedBrands.remove(getResources().getStringArray(R.array.brands)[position]);
                        brandList.performItemClick(brandList, 0, 0);
                        allSelected = true;
                    } else if(isChecked && !brandList.isItemChecked(0)) {
                        selectedBrands.add(getResources().getStringArray(R.array.brands)[position]);
                    } else if(!isChecked && !brandList.isItemChecked(0)) {
                        selectedBrands.remove(getResources().getStringArray(R.array.brands)[position]);
                    }
                }
            }
        });
    }


    public AlertDialog createFuelBrandDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelFilterActivity.this);
        builder.setTitle("Select Brands");
        builder.setMultiChoiceItems(R.array.brands, null, null);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!selectedBrands.isEmpty()) {
                    brandListText.setText(getBrandNamesText(selectedBrands, false));
                } else {
                    selectedBrands = null;
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedBrands = null;
            }
        });
        return builder.create();
    }

    public AlertDialog createNoSavedDataDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelFilterActivity.this);
        builder.setTitle("Warning");
        builder.setMessage("Some input boxes has been left blank.");
        builder.setPositiveButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder.create();
    }

    public String getBrandNamesText(ArrayList<String> brandNames, boolean setForXml) {
        String brandText = "";
        if(brandNames.size() == 21 && !setForXml) {
            brandText = "All";
        } else {
            for (String brand:brandNames) {
                brandText += brand + ", ";
            }
            brandText = brandText.substring(0, brandText.lastIndexOf(","));
            if(brandText.length() > 15 && !setForXml) {
                brandText = brandText.substring(0, 15) + "...";
            }
        }
        return brandText;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedType = null;
    }

}
