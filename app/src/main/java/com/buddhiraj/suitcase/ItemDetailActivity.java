package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.view.View; // Add this import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ItemDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Find views in the layout
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView itemImageView = findViewById(R.id.itemImage);

        // Set ProgressBar visibility to VISIBLE
        progressBar.setVisibility(View.VISIBLE);

        // Retrieve the document name from the intent extras
        String documentName = getIntent().getStringExtra("documentName");

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Documents");

        // Retrieve additional details for the item based on the document name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String loggedInUserId = currentUser.getUid();
            databaseReference.orderByChild("userId").equalTo(loggedInUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot documentSnapshot : dataSnapshot.getChildren()) {
                        String name = documentSnapshot.child("name").getValue(String.class);

                        if (name != null && name.equals(documentName)) {
                            // Retrieve additional details
                            String description = documentSnapshot.child("description").getValue(String.class);
                            String price = documentSnapshot.child("price").getValue(String.class);
                            String storeName = documentSnapshot.child("storeName").getValue(String.class);
                            String imageUrl = documentSnapshot.child("imageUrl").getValue(String.class);

                            // Find other TextViews in the layout
                            TextView itemNameTextView = findViewById(R.id.itemName);
                            TextView itemDescriptionTextView = findViewById(R.id.itemDescription);
                            TextView itemPriceTextView = findViewById(R.id.itemPrice);
                            TextView storeNameTextView = findViewById(R.id.storeName);

                            // Set the retrieved details to the corresponding TextViews
                            itemNameTextView.setText(name);
                            itemDescriptionTextView.setText(description);
                            itemPriceTextView.setText(price);
                            storeNameTextView.setText(storeName);

                            // Load the item image using Picasso (you may need to adjust this)
                            Picasso.get().load(imageUrl).into(itemImageView, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    // Set ProgressBar visibility to GONE when image is loaded successfully
                                    progressBar.setVisibility(View.GONE);
                                    itemImageView.setVisibility(View.VISIBLE); // Show the image
                                }

                                @Override
                                public void onError(Exception e) {
                                    // Handle image loading error here if needed
                                }
                            });

                            break; // Exit loop after finding the matching document
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
    }
}
