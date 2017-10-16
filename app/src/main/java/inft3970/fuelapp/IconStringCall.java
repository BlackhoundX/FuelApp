package inft3970.fuelapp;

/**
 * Created by shane on 16/10/2017.
 */

/*
        This method takes in the string of the petrol station brand name
        and returns a string containing the relevant image filename, contained
        in the Assets folder
        */

public class IconStringCall {
    public static String getIconString(String brand) {
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
