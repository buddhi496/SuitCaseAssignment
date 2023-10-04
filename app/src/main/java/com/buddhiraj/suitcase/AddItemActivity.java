package com.buddhiraj.suitcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;
    private DatabaseReference databaseRef;
    private ProgressBar progressBar;
    private ShakeViewModel shakeViewModel;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;

    private static final int SHAKE_THRESHOLD = 1000; // Adjust this threshold as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add your items");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Button addItemButton = findViewById(R.id.addItemButton);
        imageView = findViewById(R.id.imageView);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar
        shakeViewModel = new ViewModelProvider(this).get(ShakeViewModel.class);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Find the Spinner in the add_item_form layout
        Spinner categorySpinner = findViewById(R.id.categorySpinner);

        // Define the array of category options
        String[] categoryOptions = {"Clothing", "Books and Magazines", "Health", "Electronic", "Accessories", "Others"};

        // Create an ArrayAdapter using the defined categoryOptions array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the Spinner
        categorySpinner.setAdapter(adapter);

        // Choose Image Button click listener
        TextView chooseImageButton = findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(v -> openImageChooser());

        // Add Item Button click listener
        addItemButton.setOnClickListener(view -> {
            // Check if an image has been selected
            if (imageUri == null) {
                Toast.makeText(AddItemActivity.this, "Please choose an image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate a unique key for the item
            String itemKey = UUID.randomUUID().toString();
            progressBar.setVisibility(View.VISIBLE);

            // Upload the image to Firebase Storage and save the item to the database
            uploadImageToFirebaseStorage(itemKey);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register accelerometer sensor listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister accelerometer sensor listener
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > SHAKE_THRESHOLD) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float acceleration = x * x + y * y + z * z;

                // You can adjust this threshold as needed
                if (acceleration > 400) {
                    // Shake detected, clear fields
                    clearFields();
                    lastShakeTime = currentTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                // Load the selected image into the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage(String itemKey) {
        // Check if an image has been selected
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("item_images").child(itemKey);

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageURL = uri.toString();
                            // Save the image URL in the database along with other item details
                            saveItemToDatabase(itemKey, imageURL);
                        }).addOnFailureListener(e -> {
                            // Handle the error when getting the download URL
                            Toast.makeText(AddItemActivity.this, "Failed to get image URL.", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error when uploading the image
                        progressBar.setVisibility(View.GONE); // Hide the ProgressBar
                        Toast.makeText(AddItemActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveItemToDatabase(String itemKey, String imageURL) {

        String authUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        EditText itemDetailEditText = findViewById(R.id.itemDetailEditText);
        EditText itemPriceEditText = findViewById(R.id.itemPriceEditText);
        EditText storeNameEditText = findViewById(R.id.storeNameEditText);
        Spinner categorySpinner = findViewById(R.id.categorySpinner);

        // Retrieve values from form fields
        String itemName = itemNameEditText.getText().toString();
        String itemDetail = itemDetailEditText.getText().toString();

        // Check if itemPriceEditText is empty or not a valid double
        double itemPrice;
        if (itemPriceEditText.getText().toString().isEmpty()) {
            itemPrice = 0.0; // Default value for empty price
        } else {
            try {
                itemPrice = Double.parseDouble(itemPriceEditText.getText().toString());
            } catch (NumberFormatException e) {
                itemPrice = 0.0; // Default value for invalid price
            }
        }

        String storeName = storeNameEditText.getText().toString();
        String selectedCategory = categorySpinner.getSelectedItem().toString();

        // Create a DocumentItem object with the item's information
        Items newItem = new Items(imageURL, itemName, String.valueOf(itemPrice), itemDetail, storeName);
        newItem.setUserId(authUid);
        newItem.setItemKey(itemKey);

        // Push the item to the appropriate category node
        DatabaseReference categoryNodeRef = databaseRef.child(selectedCategory);
        categoryNodeRef.child(itemKey).setValue(newItem);
        progressBar.setVisibility(View.GONE);

        // Optionally, show a success message or navigate back to the main activity
        Toast.makeText(AddItemActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
        finish(); // Finish the AddItemActivity and return to the main activity
    }

    private void clearFields() {
        imageView.setImageResource(R.drawable.imageview); // Set the ImageView to display the default image

        // Show the default image
        imageView.setVisibility(View.VISIBLE);
        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        EditText itemDetailEditText = findViewById(R.id.itemDetailEditText);
        EditText itemPriceEditText = findViewById(R.id.itemPriceEditText);
        EditText storeNameEditText = findViewById(R.id.storeNameEditText);

        itemNameEditText.setText(""); // Clear the text fields
        itemDetailEditText.setText("");
        itemPriceEditText.setText("");
        storeNameEditText.setText("");
    }
}
