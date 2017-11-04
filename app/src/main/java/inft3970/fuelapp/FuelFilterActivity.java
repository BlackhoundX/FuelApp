package inft3970.fuelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.TextViewCompat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
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
    TextView radiusText;
    TextView currentRadius;
    TextView currentType;

    FloatingActionButton saveButton;
    FloatingActionButton returnButton;
    AlertDialog brandDialog = null;
    private boolean allSelected = true;
    private int arrayLength;
    private int radiusSize;
    SeekBar radiusBar;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_filter);
        fuelTypeList = (Spinner)findViewById(R.id.fuel_type_spinner);
        brandListText = (TextView)findViewById(R.id.fuel_brand_names);
        radiusText = (TextView)findViewById(R.id.radiusValue);

        currentRadius = (TextView)findViewById(R.id.currentRadiusDisplay);
        currentType = (TextView)findViewById(R.id.CurrentFuelTypeDisplay);

        saveButton = (FloatingActionButton)findViewById(R.id.save_filter_button);
        returnButton = (FloatingActionButton)findViewById(R.id.returnButton);
        arrayLength = getResources().getStringArray(R.array.brands).length;
        radiusBar = (SeekBar)findViewById(R.id.radiusSeekBar);
        context = App.getContext();

        fuelTypeList.setOnItemSelectedListener(this);
        fuelNames = getResources().getStringArray(R.array.fuel_names);

        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, fuelNames);

        fuelTypeList.setAdapter(fuelTypeAdapter);

        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog errorDialog = createNoSavedDataDialog(null);
                if((selectedBrands != null) && selectedType != null) {
                    if(!selectedBrands.isEmpty()) {
                        String[] settingsData = new String[3];
                        settingsData[0] = FuelCodeNameCall.getTypeCode(selectedType);
                        settingsData[1] = getBrandNamesText(selectedBrands, true);
                        settingsData[2] = String.valueOf(radiusSize);
                        XmlSettings xmlSettings = new XmlSettings();
                        xmlSettings.writeXml(settingsData);
                        Toast.makeText(getApplicationContext(), "Settings saved!", Toast.LENGTH_LONG).show();
                        currentRadius.setText(radiusSize + "km");
                        currentType.setText(selectedType);
                    } else {
                        errorDialog.show();
                    }
                } else {
                    errorDialog.show();
                }
            }
        });

        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                radiusText.setText(progressChangedValue + "km");
                radiusSize = progressChangedValue;
            }
        });

        getSettings();

    }

    private void getSettings() {
        String fuelType = "";
        int savedRadius = 1;

        File settingsFile = new File(this.getFilesDir() + "/Settings.xml");
        ArrayList<String> settingsList = new ArrayList<>();
        if(settingsFile.exists()) {
            XmlSettings xmlSettings = new XmlSettings();
            settingsList = xmlSettings.readXml();
            fuelType = settingsList.get(0);
            savedRadius = Integer.parseInt(settingsList.get(2));
        } else {
            fuelType = "No Default Selected";
            savedRadius = 5;
        }
        currentRadius.setText(savedRadius + "km");
        currentType.setText(fuelType);
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
        boolean[] selectedItems = new boolean[arrayLength];
        selectedItems[0] = false;
        for(int i = 1; i < selectedItems.length; i++){
            selectedItems[i] = true;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(FuelFilterActivity.this);
        builder.setTitle("Select Brands");
        builder.setMultiChoiceItems(R.array.brands, selectedItems, null);
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
