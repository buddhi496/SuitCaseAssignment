package com.buddhiraj.suitcase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
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
import java.util.Objects;

public class BAMActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Items> documentItemList;
    private ItemAdapter adapter;
    private String currentUserID;

    private List<Items> booksItems;
    private List<Items> clothingItems;
    private List<Items> accessoriesItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_items);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set the back button's click listener
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        documentItemList = new ArrayList<>();
        adapter = new ItemAdapter(documentItemList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Get the currently logged-in user's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }

        // Initialize separate lists for each category
        booksItems = new ArrayList<>();
        clothingItems = new ArrayList<>();
        accessoriesItems = new ArrayList<>();

        // Set up the database listener for each category
        setupDatabaseListener("Books and Magazines", booksItems);
        setupDatabaseListener("Clothing", clothingItems);
        setupDatabaseListener("Accessories", accessoriesItems);

        // Create an instance of the SwipeToDeleteCallback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, adapter);

        // Attach the SwipeToDeleteCallback to your RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2000);
        });
    }

    @Override
    public void onItemClick(int position) {
        Items clickedItem = documentItemList.get(position);
        Intent intent = new Intent(this, ItemDetailActivity.class);

        intent.putExtra("description", clickedItem.getDescription());
        intent.putExtra("imageUrl", clickedItem.getImageUrl());
        intent.putExtra("itemName", clickedItem.getName());
        intent.putExtra("itemPrice", clickedItem.getPrice());
        intent.putExtra("itemStoreName", clickedItem.getStoreName());

        startActivity(intent);
    }

    private void setupDatabaseListener(String category, final List<Items> itemList) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference(category);
        Query query = itemsRef.orderByChild("userId").equalTo(currentUserID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Get data fields from the snapshot
                    String imageUrl = itemSnapshot.child("imageUrl").getValue(String.class);
                    String name = itemSnapshot.child("name").getValue(String.class);
                    String price = itemSnapshot.child("price").getValue(String.class);
                    String description = itemSnapshot.child("description").getValue(String.class);
                    String storeName = itemSnapshot.child("storeName").getValue(String.class);

                    boolean status = itemSnapshot.child("status").getValue(Boolean.class);

                    // Create an Items object and set the status
                    Items item = new Items(imageUrl, name, price, description, storeName);
                    item.setStatus(status);

                    itemList.add(item);
                }

                // Merge data from all categories into documentItemList
                documentItemList.clear();
                documentItemList.addAll(booksItems);
                documentItemList.addAll(clothingItems);
                documentItemList.addAll(accessoriesItems);

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
