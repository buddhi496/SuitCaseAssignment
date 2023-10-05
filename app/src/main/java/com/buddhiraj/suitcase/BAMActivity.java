package com.buddhiraj.suitcase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Import SearchView
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

public class BAMActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Items> documentItemList;
    private ItemAdapter adapter;
    private String currentUserID;
    private List<Items> booksItems;
    private List<Items> clothingItems;
    private List<Items> accessoriesItems;
    private String selectedSortingOption = "All Items"; // Default option

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        documentItemList = new ArrayList<>();
        adapter = new ItemAdapter(documentItemList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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

        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2000);
        });

        SearchView searchBar = findViewById(R.id.searchBar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterByName(newText);
                return true;
            }
        });

        Spinner sortSpinner = findViewById(R.id.sortSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sorting_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedSortingOption = parentView.getItemAtPosition(position).toString();
                updateRecyclerView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
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

                updateRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateRecyclerView() {
        if ("Unpurchased Items".equals(selectedSortingOption)) {
            filterItemsByStatus(false);
        } else if ("Purchased Items".equals(selectedSortingOption)) {
            filterItemsByStatus(true);
        } else if ("All Items".equals(selectedSortingOption)) {
            documentItemList.clear();
            documentItemList.addAll(booksItems);
            documentItemList.addAll(clothingItems);
            documentItemList.addAll(accessoriesItems);
        }

        adapter.notifyDataSetChanged();
    }

    private void filterItemsByStatus(boolean statusToDisplay) {
        documentItemList.clear();

        for (Items item : booksItems) {
            if (item.isStatus() == statusToDisplay) {
                documentItemList.add(item);
            }
        }
        for (Items item : clothingItems) {
            if (item.isStatus() == statusToDisplay) {
                documentItemList.add(item);
            }
        }
        for (Items item : accessoriesItems) {
            if (item.isStatus() == statusToDisplay) {
                documentItemList.add(item);
            }
        }
    }
}

