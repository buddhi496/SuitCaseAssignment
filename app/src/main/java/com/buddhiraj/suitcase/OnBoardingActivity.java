package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

public class OnBoardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(v -> {
            startActivity(new Intent(OnBoardingActivity.this, OnBoardingActivity2.class));
            overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
        });
    }
}
