package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;


public class OnBoardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MaterialButton nextButton;
    private OnboardingPagerAdaptor adaptor;
    private int[] images = {R.drawable.onboard, R.drawable.board_second, R.drawable.board_third};
    private String[] texts = {"List Items", "Purchase Items", "Share Items"};
    private String[] messages = {" Discover the power of organizing your travel essentials with ease. List Items is your perfect travel companion.",
            "Keep track of everything you need for your next trip. List Items helps you create and manage your packing lists effortlessly.",
            "Share your lists with travel companions. Collaborate and plan together for a seamless journey."};
    private int currentPage = 0;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        viewPager = findViewById(R.id.viewPager);
        adaptor = new OnboardingPagerAdaptor(this, images, texts, messages);
        viewPager.setAdapter(adaptor);
        nextButton = findViewById(R.id.nextButtonOB);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("OnboardingActivity", "Current Page: " + currentPage);
                Log.d("OnboardingActivity", "adapter count: " + adaptor.getCount());
                if (currentPage < adaptor.getCount() - 1) {
                    currentPage++;
                    viewPager.setCurrentItem(currentPage);
                } else if (currentPage == adaptor.getCount() - 1) {
                    nextButton.setText("Let's Get Started");
                    navigateToLogin();
                }
            }
        });

        setupIndicator();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("onboarding", Context.MODE_PRIVATE);

    }

    private void setupIndicator() {
        LinearLayout indicatorLayout = findViewById(R.id.indicatorLayoutOB);
        final ImageView[] indicators = new ImageView[images.length];

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageResource(R.drawable.indicator_inactive);
            indicators[i].setVisibility(View.VISIBLE);


            // Set layout parameters for the ImageView
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    30, 30);
            layoutParams.setMargins(4, 0, 4, 0);
            indicators[i].setLayoutParams(layoutParams);


            // Add the ImageView to the indicatorLayout
            indicatorLayout.addView(indicators[i]);

            Log.d("OnboardingActivity", "Indicator added at position: " + i);
        }

        // Set the initial active indicator
        indicators[0].setImageResource(R.drawable.indicator_active);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("OnboardingActivity", "Page selected: " + position);

                currentPage = position;

                for (int i = 0; i < indicators.length; i++) {
                    indicators[i].setImageResource(i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
                    Log.d("OnboardingActivity", "Indicator " + i + " set to " + (i == position ? "active" : "inactive"));
                }
                // If it's the last page, update the button text
                if (position == adaptor.getCount() - 1) {
                    nextButton.setText("Let's Get Started");
                } else {
                    nextButton.setText("Next");
                }

            }


            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void navigateToLogin() {
        Intent intent = new Intent(OnBoardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        markOnboardingAsCompleted();
    }

    private void markOnboardingAsCompleted() {
        // Mark onboarding as completed using SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isCompleted", true);
        editor.apply();
    }
}