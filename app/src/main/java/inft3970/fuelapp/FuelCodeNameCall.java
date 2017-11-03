package inft3970.fuelapp;

import android.content.Context;

/**
 * Created by shane on 30/10/2017.
 */

public class FuelCodeNameCall {

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
