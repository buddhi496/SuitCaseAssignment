package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

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
    GoogleSignInClient googleSignInClient;

    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Items> documentItemList;
    private ItemAdapter adapter;
    private String currentUserID;

    private List<Items> booksItems;
    private List<Items> clothingItems;
    private List<Items> accessoriesItems;
    private List<Items> healthItems;
    private List<Items> otherItems;
    private List<Items> electronicsItems;
    private static final int PICK_IMAGE_REQUEST = 1;


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
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);



        documentItemList = new ArrayList<>();
        adapter = new ItemAdapter(documentItemList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            retrieveUserNameFromDatabase(currentUser.getUid());
            currentUserID = currentUser.getUid();
        }

        // Initialize separate lists for each category
        booksItems = new ArrayList<>();
        clothingItems = new ArrayList<>();
        accessoriesItems = new ArrayList<>();
        healthItems = new ArrayList<>();
        otherItems = new ArrayList<>();
        electronicsItems = new ArrayList<>();

        // Set up the database listener for each category
        setupDatabaseListener("Books and Magazines", booksItems);
        setupDatabaseListener("Clothing", clothingItems);
        setupDatabaseListener("Accessories", accessoriesItems);
        setupDatabaseListener("Electronic", electronicsItems);
        setupDatabaseListener("Health", healthItems);
        setupDatabaseListener("Others", otherItems);

        // Create an instance of the SwipeToDeleteCallback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, adapter);

        // Attach the SwipeToDeleteCallback to your RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2000); // Simulate a 2-second delay
        });
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

                setupCategoryCountListener(userId, "Health", findViewById(R.id.totalHealthItems));
                setupCategoryCountListener(userId, "Clothing", findViewById(R.id.totalItem));
                setupCategoryCountListener(userId, "Electronic", findViewById(R.id.totalelectronicItems));
                setupCategoryCountListener(userId, "Accessories", findViewById(R.id.totalClothItems));
                setupCategoryCountListener(userId, "Books and Magazines", findViewById(R.id.totalPaymentMethods));
                setupCategoryCountListener(userId, "Others", findViewById(R.id.totalotherItems));
            }

            private void setupCategoryCountListener(String userId, String category, TextView categoryTextView) {
                DatabaseReference categoryRef = databaseReference.child(category);
                Query userCategoryQuery = categoryRef.orderByChild("userId").equalTo(userId);

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long categoryCount = dataSnapshot.getChildrenCount();
                            String categoryCountText = categoryCount + " " + category + "s"; // Pluralize the category name
                            categoryTextView.setText(categoryCountText);
                        } else {
                            categoryTextView.setText("0 " + category + "s");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error if data retrieval is canceled
                        Log.e("Database", "Data retrieval canceled: " + databaseError.getMessage());
                    }
                };

                userCategoryQuery.addValueEventListener(valueEventListener);
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
    public void showClothing(View view) {
        Intent intent = new Intent(MainActivity.this, ClothItemsActivity.class);
        startActivity(intent);
    }

    public void showBooks(View view) {
        Intent intent = new Intent(MainActivity.this, BAMActivity.class);
        startActivity(intent);
    }

    public void showHealth(View view) {
        Intent intent = new Intent(MainActivity.this, HealthItemsActivity.class);
        startActivity(intent);
    }

    public void showAccessories(View view) {
        Intent intent = new Intent(MainActivity.this, AccessoriesActivity.class);
        startActivity(intent);
    }

    public void showElectronic(View view) {
        Intent intent = new Intent(MainActivity.this, ElectronicItemsActivity.class);
        startActivity(intent);
    }

    public void showOthers(View view) {
        Intent intent = new Intent(MainActivity.this, OthersItemsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        Items clickedItem = documentItemList.get(position);
        Intent intent = new Intent(this, ItemDetailActivity.class);

        intent.putExtra("description", clickedItem.getDescription());
        intent.putExtra("imageUrl", clickedItem.getImageUrl());
        intent.putExtra("itemName", clickedItem.getName());
        intent.putExtra("itemPrice", clickedItem.getPrice());
        intent.putExtra("itemStoreName", clickedItem.getStoreName());

        startActivity(intent);
    }

    private void setupDatabaseListener(String category, final List<Items> itemList) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference(category);
        Query query = itemsRef.orderByChild("userId").equalTo(currentUserID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Get data fields from the snapshot
                    String imageUrl = itemSnapshot.child("imageUrl").getValue(String.class);
                    String name = itemSnapshot.child("name").getValue(String.class);
                    String price = itemSnapshot.child("price").getValue(String.class);
                    String description = itemSnapshot.child("description").getValue(String.class);
                    String storeName = itemSnapshot.child("storeName").getValue(String.class);

                    boolean status = itemSnapshot.child("status").getValue(Boolean.class);

                    // Create an Items object and set the status
                    Items item = new Items(imageUrl, name, price, description, storeName);
                    item.setStatus(status);

                    itemList.add(item);
                }

                // Merge data from all categories into documentItemList
                documentItemList.clear();
                documentItemList.addAll(booksItems);
                documentItemList.addAll(clothingItems);
                documentItemList.addAll(accessoriesItems);
                documentItemList.addAll(healthItems);
                documentItemList.addAll(otherItems);
                documentItemList.addAll(electronicsItems);

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();

            // Inflate the activity_edit.xml layout for the dialog
            View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_edit, null);

            // Find the itemImageView within the inflated layout
            ImageView itemImageView = dialogView.findViewById(R.id.imageViewItem);

            // Load the selected image into the itemImageView using Picasso
            Picasso.get().load(selectedImageUri).into(itemImageView);
        }
    }

}
