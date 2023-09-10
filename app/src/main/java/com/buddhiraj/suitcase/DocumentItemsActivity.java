package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_items);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Documents");

        // Find the container layout by its ID
        containerLayout = findViewById(R.id.containerLayout);

        // Add a ValueEventListener to fetch and display all documents
        databaseReference.addValueEventListener(new ValueEventListener() {
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
        // Finish the current activity
        finish();

        // Start the same activity to refresh it
        Intent refreshIntent = new Intent(this, DocumentItemsActivity   .class);
        startActivity(refreshIntent);
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

    public void itemDetail(View view) {
        TextView nameTextView = (TextView) view.findViewById(R.id.textViewName);
        String documentName = nameTextView.getText().toString();
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("documentName", documentName);
        startActivity(intent);
    }
}
