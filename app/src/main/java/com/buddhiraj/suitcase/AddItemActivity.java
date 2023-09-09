package com.buddhiraj.suitcase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_form);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle back button click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        EditText itemDetailEditText = findViewById(R.id.itemDetailEditText);
        EditText itemPriceEditText = findViewById(R.id.itemPriceEditText);
        EditText storeNameEditText = findViewById(R.id.storeNameEditText);
        Button addItemButton = findViewById(R.id.addItemButton);
        imageView = findViewById(R.id.imageView);

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference itemsRef = databaseRef.child("items");

        // Find the Spinner in the add_item_form layout
        Spinner categorySpinner = findViewById(R.id.categorySpinner);

        // Define the array of category options
        String[] categoryOptions = {"Documents", "Payment", "Health", "Clothing", "Electronic", "Others"};

        // Create an ArrayAdapter using the defined categoryOptions array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryOptions);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the Spinner
        categorySpinner.setAdapter(adapter);

        // Choose Image Button click listener
        Button chooseImageButton = findViewById(R.id.chooseImageButton);
        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Add Item Button click listener
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if an image has been selected
                if (imageUri == null) {
                    Toast.makeText(AddItemActivity.this, "Please choose an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retrieve values from form fields
                String itemName = itemNameEditText.getText().toString();
                String itemDetail = itemDetailEditText.getText().toString();
                double itemPrice = Double.parseDouble(itemPriceEditText.getText().toString());
                String storeName = storeNameEditText.getText().toString();
                String selectedCategory = categorySpinner.getSelectedItem().toString();

                // Generate a unique key for the item
                String itemKey = itemsRef.push().getKey();

                // Upload the image to Firebase Storage
                uploadImageToFirebaseStorage(itemKey);

                // Create an Item object with the item's information (excluding the image URL)
                DocumentItem newItem = new DocumentItem(null, itemName, String.valueOf(itemPrice), itemDetail, storeName);

                // Push the item to the appropriate category node
                DatabaseReference categoryNodeRef = databaseRef.child(selectedCategory);
                categoryNodeRef.child(itemKey).setValue(newItem);

                // Optionally, show a success message or navigate back to the main activity
                Toast.makeText(AddItemActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Finish the AddItemActivity and return to the main activity
            }
        });
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
        // Upload the image to Firebase Storage and get the download URL
        // You need to implement this part based on your Firebase Storage setup
        // Here's a basic example of how it might be done:

        // FirebaseStorage storage = FirebaseStorage.getInstance();
        // StorageReference storageRef = storage.getReference().child("items/" + itemKey);
        // StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        // UploadTask uploadTask = storageRef.putFile(imageUri, metadata);

        // uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        //     @Override
        //     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        //         // Get the download URL for the uploaded image
        //         storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
        //             @Override
        //             public void onSuccess(Uri uri) {
        //                 // Save the image URL in the database or use it as needed
        //                 String imageURL = uri.toString();
        //                 // Update the item in the database with the imageURL
        //                 // Example: itemsRef.child(itemKey).child("imageURL").setValue(imageURL);
        //             }
        //         });
        //     }
        // });
    }
}
