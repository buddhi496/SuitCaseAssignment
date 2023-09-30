package com.buddhiraj.suitcase;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private ItemAdapter adapter;
    private Context context;
    private String category;

    public SwipeToDeleteCallback(Context context, ItemAdapter adapter, String category) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        this.category = category; // Initialize the category
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Not needed for swipe-to-delete, returning false
        return false;
    }

    // Inside onSwiped method
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Get the position of the swiped item
        final int position = viewHolder.getAdapterPosition();

        // Get the item name from your adapter
        String itemName = adapter.getItemName(position);

        // Show a confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // User confirmed deletion, remove the item from the dataset and the database
                    adapter.deleteItem(position);
                    removeFromDatabase(itemName); // Call the method to remove the item from the database
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> {
                    // Dialog canceled, notify the adapter to refresh the view
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    private void removeFromDatabase(String itemName) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Specify the category where your items are stored
        DatabaseReference categoryRef = databaseRef.child(category);

        // Query the category to find the item with the matching name
        Query query = categoryRef.orderByChild("name").equalTo(itemName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Iterate through the matching items (there may be multiple with the same name)
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        // Remove each matching item from the database
                        itemSnapshot.getRef().removeValue();
                    }

                    // Item(s) removed successfully from the database
                    Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
                } else {
                    // No matching item found in the database
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that occur during the query
                Toast.makeText(context, "Failed to remove item from database.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

