package inft3970.fuelapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startSplash();
    }

    public void startSplash() {
        Animation fadeout = new AlphaAnimation(1.f, 1.f);
        fadeout.setDuration(2500);
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
