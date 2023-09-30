package com.buddhiraj.suitcase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

    public class EditBAMActivity extends AppCompatActivity {

        private EditText itemNameEditText;
        private EditText descriptionEditText;
        private EditText priceEditText;
        private EditText editTextStoreName;
        private ImageView itemImageView;
        private Button chooseImageButton;
        private static final int PICK_IMAGE_REQUEST = 1;
        private Uri selectedImageUri;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit);

            // Initialize EditText fields and ImageView
            itemNameEditText = findViewById(R.id.editTextItemName);
            descriptionEditText = findViewById(R.id.editTextDescription);
            priceEditText = findViewById(R.id.editTextPrice);
            editTextStoreName = findViewById(R.id.editTextStoreName); // Add TextView for the store name
            // Initialize views
            itemImageView = findViewById(R.id.imageViewItem);
            chooseImageButton = findViewById(R.id.btnChooseImage);

            chooseImageButton.setOnClickListener(v -> {
                openImagePicker();
            });


            // Retrieve the data from the Intent extras
            String itemName = getIntent().getStringExtra("itemName");
            String description = getIntent().getStringExtra("description");
            String itemPrice = getIntent().getStringExtra("itemPrice");
            String imageUrl = getIntent().getStringExtra("imageUrl"); // Get image URL
            String storeName = getIntent().getStringExtra("storeName"); // Get store name

            // Prepopulate the input fields with the existing values
            itemNameEditText.setText(itemName);
            descriptionEditText.setText(description);
            priceEditText.setText(itemPrice);
            Picasso.get().load(imageUrl).into(itemImageView);
            editTextStoreName.setText(storeName);

            Button saveButton = findViewById(R.id.btnSave);
            saveButton.setOnClickListener(v -> {
                // Handle saving the edited data to the database
                saveEditedData();
            });
        }

        private void openImagePicker() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                selectedImageUri = imageUri;


                // Load the selected image into the ImageView
                Picasso.get().load(imageUri).into(itemImageView);
            }
        }

        private void saveEditedData() {
            // Get the edited values from the input fields
            String editedItemName = itemNameEditText.getText().toString().trim();
            String editedDescription = descriptionEditText.getText().toString().trim();
            String editedItemPrice = priceEditText.getText().toString().trim();
            String editedStoreName = editTextStoreName.getText().toString().trim();

            // Retrieve the item name from the Intent extras
            String itemName = getIntent().getStringExtra("itemName");

            // Update all fields of the item data in the database based on the item name
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

            // Assuming you have a "Clothing" category in your database
            DatabaseReference clothingRef = databaseRef.child("Books and Magazines");

            // Create a Map to hold the updated item data
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("description", editedDescription);
            updatedData.put("name", editedItemName);
            updatedData.put("price", editedItemPrice);

            if (!editedStoreName.isEmpty()) {
                updatedData.put("storeName", editedStoreName);
            }

            // Upload the selected image to Firebase Storage and get the download URL
            if (selectedImageUri != null) {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference imageRef = storageRef.child("item_images" + itemName + ".jpg");

                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Image uploaded successfully, get the download URL
                            imageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        // Update the image URL in the database
                                        updatedData.put("imageUrl", uri.toString());

                                        // Query the item by name and update its data
                                        Query query = clothingRef.orderByChild("name").equalTo(itemName);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // Get the item's unique key
                                                    String itemKey = dataSnapshot.getChildren().iterator().next().getKey();
                                                    // Update the item data using the retrieved key
                                                    clothingRef.child(itemKey).updateChildren(updatedData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                // Data updated successfully
                                                                Toast.makeText(EditBAMActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                                                finish(); // Close the activity after successful update
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                // Failed to update data
                                                                Toast.makeText(EditBAMActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                // Handle any errors here
                                                Toast.makeText(EditBAMActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle the error of getting download URL
                                        Toast.makeText(EditBAMActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error of uploading the image
                            Toast.makeText(EditBAMActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // If no image was selected, update the item data without the image URL
                Query query = clothingRef.orderByChild("name").equalTo(itemName);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get the item's unique key
                            String itemKey = dataSnapshot.getChildren().iterator().next().getKey();
                            // Update the item data using the retrieved key
                            clothingRef.child(itemKey).updateChildren(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Data updated successfully
                                        Toast.makeText(EditBAMActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                        finish(); // Close the activity after successful update
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to update data
                                        Toast.makeText(EditBAMActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle any errors here
                        Toast.makeText(EditBAMActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }