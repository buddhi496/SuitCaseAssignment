package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item_form);

        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        EditText itemDetailEditText = findViewById(R.id.itemDetailEditText);
        EditText itemPriceEditText = findViewById(R.id.itemPriceEditText);
        EditText storeNameEditText = findViewById(R.id.storeNameEditText);
        Button addItemButton = findViewById(R.id.addItemButton);
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

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve values from form fields
                int imageResource = R.drawable.card; // Use the appropriate image resource
                String itemName = itemNameEditText.getText().toString();
                String itemDetail = itemDetailEditText.getText().toString();
                double itemPrice = Double.parseDouble(itemPriceEditText.getText().toString());
                String storeName = storeNameEditText.getText().toString(); // Get the store name
                String selectedCategory = categorySpinner.getSelectedItem().toString();

                // Create an Item object with the item's information
                DocumentItem newItem = new DocumentItem(imageResource, itemName, String.valueOf(itemPrice), itemDetail, storeName);

                // Generate a unique key for the item
                String itemKey = itemsRef.push().getKey();

                // Push the item to the appropriate category node
                DatabaseReference categoryNodeRef = databaseRef.child(selectedCategory);
                categoryNodeRef.child(itemKey).setValue(newItem);

                // Optionally, show a success message or navigate back to the main activity
                Toast.makeText(AddItemActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Finish the AddItemActivity and return to the main activity
            }
        });
    }
}
