package com.io.sis.development;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo, teamLogo;
    private TextView textDisplay;
    private static int splashTimeOut=3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo=(ImageView)findViewById(R.id.appLogo);
        textDisplay=(TextView)findViewById(R.id.textDisplay);
        teamLogo = (ImageView)findViewById(R.id.teamLogo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
                String participantPin = pref.getString("participantPin", "0");

                if(participantPin != "0"){
                    Toast.makeText(getApplicationContext(), "Saved pin: " + participantPin, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
                else{
                    Intent i = new Intent(SplashActivity.this,PinActivity.class);
                    startActivity(i);
                    finish();
                }


            }
        },splashTimeOut);

        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.io_sis_splash_animation);
        logo.startAnimation(myanim);
        textDisplay.startAnimation(myanim);
        teamLogo.startAnimation(myanim);
    }

    @Override
    public void onBackPressed() {
    // super.onBackPressed();
    // Not calling **super**, disables back button in current screen.
    }

}
