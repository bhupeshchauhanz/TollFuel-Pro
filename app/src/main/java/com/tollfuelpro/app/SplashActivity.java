package com.tollfuelpro.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private Handler splashHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        splashHandler = new Handler(Looper.getMainLooper());
        splashHandler.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            SharedPreferences sharedPreferences = getSharedPreferences("TollFuelProPrefs", MODE_PRIVATE);
            boolean isFirstTimeSetup = sharedPreferences.getBoolean("isFirstTimeSetup", true);

            if (isFirstTimeSetup) {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
    }
}
