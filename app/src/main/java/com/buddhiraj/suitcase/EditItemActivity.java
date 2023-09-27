package com.buddhiraj.suitcase;
import android.os.Bundle;
import android.view.View;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditItemActivity extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize EditText fields and ImageView
        itemNameEditText = findViewById(R.id.editTextItemName);
        descriptionEditText = findViewById(R.id.editTextDescription);
        priceEditText = findViewById(R.id.editTextPrice);
        ImageView itemImageView = findViewById(R.id.imageViewItem); // Add ImageView for the image
        TextView storeNameTextView = findViewById(R.id.textViewStoreName); // Add TextView for the store name

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
        storeNameTextView.setText("Store Name: " + storeName);

        Button saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(v -> {
            // Handle saving the edited data to the database
            saveEditedData();
        });
    }


    private void saveEditedData() {
        // Get the edited values from the input fields
        String editedItemName = itemNameEditText.getText().toString().trim();
        String editedDescription = descriptionEditText.getText().toString().trim();
        String editedItemPrice = priceEditText.getText().toString().trim();

        // Validate the input fields (you can add more validation as needed)
        if (editedItemName.isEmpty() || editedDescription.isEmpty() || editedItemPrice.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the item key from the Intent extras
        String itemKey = getIntent().getStringExtra("itemKey");

        // Update specific fields of the item data in the database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Assuming you have a "Clothing" category in your database
        DatabaseReference clothingRef = databaseRef.child("Clothing").child(itemKey);

        // Create a Map to hold the updated item data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", editedItemName);
        updatedData.put("description", editedDescription);
        updatedData.put("price", editedItemPrice);

        // Update only the specified fields in the database
        clothingRef.updateChildren(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data updated successfully
                        Toast.makeText(EditItemActivity.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after successful update
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update data
                        Toast.makeText(EditItemActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}