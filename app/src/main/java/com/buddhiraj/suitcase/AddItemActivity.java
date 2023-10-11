package com.buddhiraj.suitcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity implements SensorEventListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private Uri imageUri;
    private ImageView imageView;
    private DatabaseReference databaseRef;
    private ProgressBar progressBar;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_THRESHOLD = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.buttonBackground));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Items");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.buttonBackground));
        }

        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Button addItemButton = findViewById(R.id.addItemButton);
        imageView = findViewById(R.id.imageView);
        databaseRef = FirebaseDatabase.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar); // Initialize ProgressBar
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

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

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView) parentView.getChildAt(0)).setTextColor(getResources().getColor(R.color.black)); // Set text color to black
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

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
                    // Shake detected, show a confirmation dialog
                    showClearConfirmationDialog();
                    lastShakeTime = currentTime;
                }
            }
        }
    }

    private void showClearConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Fields");
        builder.setMessage("Are you sure you want to clear all fields?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            clearFields(); // Clear the fields if the user confirms
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // Do nothing if the user cancels
        });
        builder.show();
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
        // Create an array of options for the image selection dialog
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddItemActivity.this);
        builder.setTitle("Choose an image");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                // Launch the camera to capture an image
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                // Launch the gallery to choose an image
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Handle gallery image selection
            imageUri = data.getData();
            try {
                // Load the selected image into the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            // Handle camera capture result
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
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
        String authUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

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

        // Set the category property in the newItem
        newItem.setCategory(selectedCategory);

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
