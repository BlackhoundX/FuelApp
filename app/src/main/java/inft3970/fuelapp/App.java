package inft3970.fuelapp;

import android.app.Application;
import android.content.Context;

/**
 * Class: App
 * Author: Shane
 * Purpose: This is the root class of the app. It sets up the context and gathers the Authorisation code
 * for NSW Fuel API.
 * Creation Date: 11-Sep-17
 * Modification Date: 05-Nov-17
 */

public class App extends Application {
    private static Context mContext;
    private static String authCode;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context inputContext) {
        mContext = inputContext;
    }

    public static void setAuthCode(String authCodeInput) {authCode = authCodeInput; }

    public static String getAuthCode() { return authCode; }
}
