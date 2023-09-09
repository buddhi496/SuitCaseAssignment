package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DocumentItemsActivity extends AppCompatActivity {

    private LinearLayout containerLayout; // LinearLayout to hold your items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_items);

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Documents");

        // Find the container layout by its ID
        containerLayout = findViewById(R.id.containerLayout);

        // Add a ValueEventListener to fetch and display all "name" fields
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                containerLayout.removeAllViews(); // Clear any previous views

                int itemNumber = 1; // To track the item number

                for (DataSnapshot documentSnapshot : dataSnapshot.getChildren()) {
                    String name = documentSnapshot.child("name").getValue(String.class);

                    if (name != null) {
                        // Inflate your existing item layout for each document
                        LinearLayout itemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_document_items, null);

                        // Find and update the TextView for the item number
                        TextView numberTextView = itemLayout.findViewById(R.id.textViewNumber);
                        numberTextView.setText(itemNumber + ".");

                        // Find and update the TextView for the document name
                        TextView nameTextView = itemLayout.findViewById(R.id.textViewName);
                        nameTextView.setText(name);

                        // Find and update the CheckBox
                        CheckBox checkBox = itemLayout.findViewById(R.id.checkbox);

                        // Add the item's LinearLayout to the container
                        containerLayout.addView(itemLayout);

                        itemNumber++; // Increment the item number
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
