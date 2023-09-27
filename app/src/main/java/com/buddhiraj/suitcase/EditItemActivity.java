package com.buddhiraj.suitcase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

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

        // Load and display the image using Picasso or any other image loading library
        Picasso.get().load(imageUrl).into(itemImageView);

        // Display the store name
        storeNameTextView.setText("Store Name: " + storeName);

        // Set up the "Save" button click listener
        Button saveButton = findViewById(R.id.btnSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle saving the edited data to the database
                saveEditedData();
            }
        });
    }


    private void saveEditedData() {
        String editedItemName = itemNameEditText.getText().toString().trim();
        String editedDescription = descriptionEditText.getText().toString().trim();
        String editedItemPrice = priceEditText.getText().toString().trim();

        // TODO: Update the item's data in the Firebase Realtime Database

        Toast.makeText(this, "Item data updated successfully", Toast.LENGTH_SHORT).show();

        finish();
    }
}