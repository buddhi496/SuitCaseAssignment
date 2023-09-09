package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ItemDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Retrieve the document name from the intent extras
        String documentName = getIntent().getStringExtra("documentName");

        // Find the TextView with the ID 'categoryValue'
        TextView categoryValueTextView = findViewById(R.id.category);

        // Set the retrieved document name as the text of the TextView
        categoryValueTextView.setText(documentName);
    }
}
