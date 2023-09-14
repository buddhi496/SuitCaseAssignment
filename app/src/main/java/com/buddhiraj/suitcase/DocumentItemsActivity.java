package com.buddhiraj.suitcase;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class DocumentItemsActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private List<DocumentItem> documentItemList;
    private DocumentItemAdapter adapter;
    private String currentUserID; // Store the currently logged-in user's ID here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_items);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);

        documentItemList = new ArrayList<>();
        adapter = new DocumentItemAdapter(documentItemList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Get the currently logged-in user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference itemsRef = database.getReference("Documents");

        // Modify the query to fetch items associated with the current user
        Query query = itemsRef.orderByChild("userId").equalTo(currentUserID);

        // Retrieve data from Firebase based on the modified query
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the existing list
                documentItemList.clear();

                // Iterate through the dataSnapshot to fetch items
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Get data fields from the snapshot as before
                    String imageUrl = itemSnapshot.child("imageUrl").getValue(String.class);
                    String name = itemSnapshot.child("name").getValue(String.class);
                    String price = itemSnapshot.child("price").getValue(String.class);
                    String description = itemSnapshot.child("description").getValue(String.class);
                    String storeName = itemSnapshot.child("storeName").getValue(String.class);

                    // Create a DocumentItem object and add it to the list
                    DocumentItem item = new DocumentItem(imageUrl, name, price, description, storeName);
                    documentItemList.add(item);
                }

                // Notify the adapter of the data change
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors here, if needed
            }
        });

        // Attach swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(this, adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Handle the refresh action here
                // Typically, you would load new data from a data source or perform some task
                // For demonstration purposes, we'll simulate a delay using a Handler
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Update the UI or refresh your data
                        // For example, you can notify your RecyclerView adapter of the data change

                        // Stop the swipe refresh animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000); // Simulate a 2-second delay
            }
        });
    }
}
