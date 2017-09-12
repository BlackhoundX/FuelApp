package inft3970.fuelapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by shane on 11/09/2017.
 */

public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context inputContext) {
        mContext = inputContext;
    }

}
