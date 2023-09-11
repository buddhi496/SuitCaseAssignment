package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DocumentItemsActivity extends AppCompatActivity {
    private LinearLayout containerLayout; // LinearLayout to hold your items
    private SensorManager sensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private View refreshProgressBar;
    private View dimBackground;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_items);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        refreshProgressBar = findViewById(R.id.refreshProgressBar);
        dimBackground = findViewById(R.id.dimBackground);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Documents");

        // Find the container layout by its ID
        containerLayout = findViewById(R.id.containerLayout);

        if (currentUser != null) {
            String loggedInUserId = currentUser.getUid();

            // Add a ValueEventListener to fetch and display documents for the logged-in user
            databaseReference.orderByChild("userId").equalTo(loggedInUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    containerLayout.removeAllViews(); // Clear any previous views

                    int itemNumber = 1; // To track the item number

                    for (DataSnapshot documentSnapshot : dataSnapshot.getChildren()) {
                        String name = documentSnapshot.child("name").getValue(String.class);

                        if (name != null) {
                            // Inflate your existing item layout for each document
                            LinearLayout itemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_document_items, null);

                            // Find and update the TextView for the item number
                            TextView numberTextView = itemLayout.findViewById(R.id.textViewNumber);
                            numberTextView.setText(itemNumber + ".");

                            // Find and update the TextView for the document name
                            TextView nameTextView = itemLayout.findViewById(R.id.textViewName);
                            nameTextView.setText(name);

                            // Find and update the CheckBox
                            CheckBox checkBox = itemLayout.findViewById(R.id.checkbox);

                            // Add the item's LinearLayout to the container
                            containerLayout.addView(itemLayout);

                            itemNumber++; // Increment the item number
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors here
                    // You can log or display an error message
                }
            });
        }
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 12) {
                refreshActivity();
            }
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void refreshActivity() {
        // Show the dim background overlay
        dimBackground.setVisibility(View.VISIBLE);

        // Show the ProgressBar
        refreshProgressBar.setVisibility(View.VISIBLE);

        // Finish the current activity
        finish();

        // Start the same activity to refresh it (you can add a delay if needed)
        Intent refreshIntent = new Intent(this, DocumentItemsActivity.class);
        startActivity(refreshIntent);

        // Hide the ProgressBar and dim background overlay after 5 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshProgressBar.setVisibility(View.GONE);
                dimBackground.setVisibility(View.GONE);
            }
        }, 5000); // 5000 milliseconds (5 seconds)
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        sensorManager.unregisterListener(mSensorListener);
        super.onPause();

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void itemDetail(View view) {
        refreshProgressBar.setVisibility(View.GONE);
        dimBackground.setVisibility(View.GONE);
        TextView nameTextView = view.findViewById(R.id.textViewName);
        String documentName = nameTextView.getText().toString();
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("documentName", documentName);
        startActivity(intent);
    }
}