package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView welcomeText;
    private DatabaseReference databaseReference;
    private SensorManager sensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private View refreshProgressBar;
    private View dimBackground;
    private boolean doubleBackToExitPressedOnce = false;
    final Handler backButtonHandler = new Handler();
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(sensorManager).registerListener(mSensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        profileImage = findViewById(R.id.profileImage);
        welcomeText = findViewById(R.id.welcomeText);

        refreshProgressBar = findViewById(R.id.refreshProgressBar);
        dimBackground = findViewById(R.id.dimBackground);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            retrieveUserNameFromDatabase(currentUser.getUid());
        }
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
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
        Intent refreshIntent = new Intent(this, MainActivity.class);
        startActivity(refreshIntent);

        // Hide the ProgressBar and dim background overlay after 5 seconds
        handler.postDelayed(() -> {
            refreshProgressBar.setVisibility(View.GONE);
            dimBackground.setVisibility(View.GONE);
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
        if (doubleBackToExitPressedOnce) {
            // If the flag is true (user pressed back button twice), exit the app
            finishAffinity();
        } else {
            // If the flag is false (first back button press), show a message
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            // Set a delay to reset the flag after a certain time (e.g., 2 seconds)
            backButtonHandler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000); // 2000 milliseconds (2 seconds)
        }
    }


    private void retrieveUserNameFromDatabase(String userId) { //fetchUserName
        DatabaseReference userRef = databaseReference.child("users").child(userId);
        refreshProgressBar.setVisibility(View.GONE);
        dimBackground.setVisibility(View.GONE);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("username").getValue(String.class);
                    if (userName != null) {
                        String welcomeMessage = "Hi,\n " + userName;
                        // Apply different color to the username part
                        SpannableString spannableString = new SpannableString(welcomeMessage);
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.buttonBackground));
                        int startIndex = welcomeMessage.indexOf(userName);
                        int endIndex = startIndex + userName.length();
                        spannableString.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        welcomeText.setText(spannableString);
                    } else {
                        welcomeText.setText("Welcome, User");
                    }
                } else {
                    welcomeText.setText("Welcome, User");
                }

                retrieveUserItemCount(userId);
            }

            private void retrieveUserItemCount(String userId) {
                DatabaseReference itemsRef = databaseReference.child("Documents"); // Update with your item node reference
                Query userItemsQuery = itemsRef.orderByChild("userId").equalTo(userId);

                userItemsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long itemCount = dataSnapshot.getChildrenCount();
                            String itemCountText = itemCount + " Items";

                            // Update the totalItem TextView
                            TextView totalItemTextView = findViewById(R.id.totalItem);
                            totalItemTextView.setText(itemCountText);
                        } else {
                            // If no documents found for the user, set the count to 0
                            TextView totalItemTextView = findViewById(R.id.totalItem);
                            totalItemTextView.setText("0 Items");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error if data retrieval is canceled
                        Log.e("Database", "Data retrieval canceled: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if data retrieval is cancelled
                welcomeText.setText("Welcome, User");
                Log.e("Database", "Data retrieval cancelled: " + databaseError.getMessage());
            }
        });
        FloatingActionButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this::PopupMenu);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_your_profile) {
                    openProfileActivity(); // Open "Your Profile" activity
                    return true;
                } else if (itemId == R.id.menu_log_out) {
                    logoutUser(); // Log out
                    return true;
                }
                return false;
            }
            private void openProfileActivity() {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        popupMenu.show();
    }

    private void PopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add_options, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_add_item) {
                // Handle adding an item
                Intent addItemIntent = new Intent(MainActivity.this, AddItemActivity.class);

                // Start the AddItemCategory activity
                startActivity(addItemIntent);

                return true;
            }
            return false;
        });
        popupMenu.show();
    }
    public void showDocuments(View view) {
        Intent intent = new Intent(MainActivity.this, DocumentItemsActivity.class);
        startActivity(intent);
    }
}

