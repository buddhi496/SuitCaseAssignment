package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (isOnboardingCompleted()) {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, OnBoardingActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },2000);

    }
    private boolean isOnboardingCompleted() {
        sharedPreferences = getSharedPreferences("onboarding", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isCompleted", false);
    }
}