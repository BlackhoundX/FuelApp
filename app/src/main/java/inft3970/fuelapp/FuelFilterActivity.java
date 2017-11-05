package inft3970.fuelapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Class: FuelFilterActivity
 * Author: Shane
 * Purpose: This class allows the user to filter display of fuel data, based on brand, type and radius.
 * Creation Date: 7-Oct-17
 * Modification Date: 05-Nov-17
 */

public class FuelFilterActivity extends Activity implements AdapterView.OnItemSelectedListener {
    Spinner fuelTypeList;

    TextView brandListText;
    TextView radiusText;
    TextView currentRadius;
    TextView currentType;

    FloatingActionButton saveButton;
    FloatingActionButton returnButton;

    AlertDialog brandDialog = null;

    SeekBar radiusBar;

    private boolean allSelected = true;

    ArrayList<String> selectedBrands;
    String selectedType;
    String[] fuelNames;

    private int arrayLength;
    private int radiusSize;

    Context context;

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It instantiates the various display
     * fields and sets up the Listeners for the buttons and objects.
     * Returns: None
     */
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

        //Create the listener for the Return button
        returnButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Create the listener for the Save button
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

        //Create the listeners for the Radius bar
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            //Tracks the changes
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressChangedValue = i + 1; //Seek bars default to a minimum value of 0, so adding 1 sets the minimum radius to 1
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                radiusText.setText(progressChangedValue + "km"); //Display the new radius
                radiusSize = progressChangedValue;
            }
        });
        getSettings(); //Read the settings from the XML file on the device
    }

    /**
     * Method: getSettings
     * Purpose: Reads the settings from the XML file
     * Returns: None
     */
    private void getSettings() {
        String fuelType = "";
        int savedRadius = 1;

        File settingsFile = new File(this.getFilesDir() + "/Settings.xml");
        ArrayList<String> settingsList = new ArrayList<>();
        if(settingsFile.exists()) {
            XmlSettings xmlSettings = new XmlSettings();
            settingsList = xmlSettings.readXml();
            fuelType = settingsList.get(0); //Get the Fuel Type
            savedRadius = Integer.parseInt(settingsList.get(2)); //Get the radius size
        } else {
            fuelType = "No Default Selected";
            savedRadius = 5;
        }
        currentRadius.setText(savedRadius + "km");
        currentType.setText(fuelType);
    }

    /**
     * Method: onDialog
     * Purpose: Sets up and displays the list of Brands
     * Returns: None
     */
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
                    if(allSelected) { //Handles the selection of all brands
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

    /**
     * Method: createFuelBrandDialog
     * Purpose: Creates the dialog allowing the user to select the brand of fuel
     * Returns: The build dialog box, ready for display
     */
    public AlertDialog createFuelBrandDialog(Bundle savedInstanceState) {

        //Checks all the boxes at the start of the display
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

    /**
     * Method: createNoSavedDataDialog
     * Purpose: Creates the dialog warning the user that a field was left blank
     * Returns: The built dialog box, ready for display
     */
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

    /**
     * Method: getBrandNamesText
     * Purpose: Gets the brand names in full. Takes in an Array List of brand names, and a boolean
     * to determine if the set was for XML or not.
     * Returns: The string containing the brand name.
     */
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

    /**
     * Method: onItemSelected
     * Purpose: Listener for when an item is selected from the Spinner object.
     * Returns: None.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedType = null;
    }

}
