package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        ProgressBar progressBar = findViewById(R.id.progressBar);
        // Set ProgressBar visibility to VISIBLE
        progressBar.setVisibility(View.VISIBLE);

        // Get references to TextView and ImageView elements in the layout
        TextView itemNameTextView = findViewById(R.id.itemName);
        TextView itemDescriptionTextView = findViewById(R.id.itemDescription);
        TextView itemPriceTextView = findViewById(R.id.itemPrice);
        TextView storeNameTextView = findViewById(R.id.storeName);
        ImageView itemImageView = findViewById(R.id.itemImage);

        // Get data passed from DocumentItemsActivity
        Intent intent = getIntent();
        if (intent != null) {
            String itemName = intent.getStringExtra("itemName");
            String itemDescription = intent.getStringExtra("description");
            String itemPrice = intent.getStringExtra("itemPrice");
            String itemStoreName = intent.getStringExtra("itemStoreName");
            String imageUrl = intent.getStringExtra("imageUrl"); // Fetch the image URL

            // Create a SpannableString with "Rs" in green color
            SpannableString spannableString = new SpannableString("Rs " + itemPrice);
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#01D3D4")), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the SpannableString to the TextView
            itemPriceTextView.setText(spannableString);
            int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            spannableString.setSpan(new AbsoluteSizeSpan(fontSize), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set the retrieved data to the TextView elements
            itemNameTextView.setText(itemName);
            itemDescriptionTextView.setText(itemDescription);
            storeNameTextView.setText(itemStoreName);

            // Load the image using Picasso
            Picasso.get()
                    .load(imageUrl)
                    .into(itemImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully, hide the progress bar
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            // Handle any errors here, if needed
                            progressBar.setVisibility(View.GONE); // Hide the progress bar on error
                        }
                    });
        }
    }
}
