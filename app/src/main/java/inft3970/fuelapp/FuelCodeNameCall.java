package inft3970.fuelapp;

import android.content.Context;

/**
 * Class: FuelCodeNameCall
 * Author: Shane
 * Purpose: This class converts between fuel names and codes, returning the relevant strings.
 * Creation Date: 30-Oct-17
 * Modification Date: 05-Nov-17
 */

public class FuelCodeNameCall {

    /**
     * Method: getTypeCode
     * Purpose: Converts a name of a fuel type into its reference code. Takes in a String containing
     * the fuel name.
     * Returns: A string containing the code of the fuel.
     */
    public static String getTypeCode(String fuelName) {
        Context context = App.getContext();
        String[] typeCodes = context.getResources().getStringArray(R.array.fuel_codes);
        String[] typeNames = context.getResources().getStringArray(R.array.fuel_names);
        String typeCode = null;
        for (int i = 0; i < typeCodes.length; i++) {
            if (typeNames[i].equals(fuelName)) {
                typeCode = typeCodes[i];
            }
        }
        return typeCode;
    }

    /**
     * Method: getTypeName
     * Purpose: Converts a code of a fuel type into its full name. Takes in a String containing
     * the fuel code.
     * Returns: A string containing the name of the fuel type.
     */
    public static String getTypeName(String fuelCode) {
        Context context = App.getContext();
        String[] typeCodes = context.getResources().getStringArray(R.array.fuel_codes);
        String[] typeNames = context.getResources().getStringArray(R.array.fuel_names);
        String typeCode = null;
        for (int i = 0; i < typeCodes.length; i++) {
            if (typeCodes[i].equals(fuelCode)) {
                typeCode = typeNames[i];
            }
        }
        return typeCode;
    }
}
