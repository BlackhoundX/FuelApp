package inft3970.fuelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

/**
 * Class: SplashScreen
 * Author: Shane
 * Purpose: This class displays the introduction splash screen when the app is launched
 * Creation Date: 01-Nov-17
 * Modification Date: 05-Nov-17
 */

public class SplashScreen extends AppCompatActivity {

    /**
     * Method: onCreate
     * Purpose: Automatically is called when the class is created. It sets up the display and
     * calls the method to control the Splash Screen
     * Returns: None
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startSplash();
    }

    /**
     * Method: startSplash
     * Purpose: This method handles the display of the splash screen
     * Returns: None
     */
    public void startSplash() {
        Animation fadeout = new AlphaAnimation(1.f, 1.f);
        fadeout.setDuration(2500); //Time is set to 2.5 seconds
        final View gifImageView = findViewById(R.id.splash_screen);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                gifImageView.setBackgroundResource(R.drawable.splash_slower);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent mainIntent = new Intent(getApplicationContext(), FuelMapActivity.class);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gifImageView.startAnimation(fadeout);
    }
}
