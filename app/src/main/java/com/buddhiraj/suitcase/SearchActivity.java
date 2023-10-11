package com.buddhiraj.suitcase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class SearchActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {
    private List<Items> documentItemList;
    private ItemAdapter adapter;
    private String currentUserID;
    private List<Items> booksItems;
    private List<Items> clothingItems;
    private List<Items> accessoriesItems;
    private String selectedSortingOption = "All Items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        documentItemList = new ArrayList<>();
        adapter = new ItemAdapter(documentItemList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }

        booksItems = new ArrayList<>();
        clothingItems = new ArrayList<>();
        accessoriesItems = new ArrayList<>();

        setupDatabaseListener("Books and Magazines", booksItems);
        setupDatabaseListener("Clothing", clothingItems);
        setupDatabaseListener("Accessories", accessoriesItems);

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        SearchView searchBar = findViewById(R.id.searchBar);
        searchBar.setIconified(false);
        searchBar.requestFocus();

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Hide RecyclerView when there's no text
                    recyclerView.setVisibility(View.GONE);

                    // Restart the activity
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.filterByName(newText);
                }
                return true;
            }

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
                    String imageUrl = itemSnapshot.child("imageUrl").getValue(String.class);
                    String name = itemSnapshot.child("name").getValue(String.class);
                    String price = itemSnapshot.child("price").getValue(String.class);
                    String description = itemSnapshot.child("description").getValue(String.class);
                    String storeName = itemSnapshot.child("storeName").getValue(String.class);

                    boolean status = itemSnapshot.child("status").getValue(Boolean.class);

                    Items item = new Items(imageUrl, name, price, description, storeName);
                    item.setStatus(status);

                    itemList.add(item);
                }

                documentItemList.clear();
                documentItemList.addAll(booksItems);
                documentItemList.addAll(clothingItems);
                documentItemList.addAll(accessoriesItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

