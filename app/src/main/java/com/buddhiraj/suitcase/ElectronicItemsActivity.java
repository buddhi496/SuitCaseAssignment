package com.buddhiraj.suitcase;

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

public class ElectronicItemsActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Items> documentItemList;
    private ItemAdapter adapter;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Your Electronic Items");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

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

        // Set up the database listener
        setupDatabaseListener();

        // Create an instance of the SwipeToDeleteCallback
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this, adapter);

        // Attach the SwipeToDeleteCallback to your RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 2000); // Simulate a 2-second delay
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

    private void setupDatabaseListener() {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Electronic");
        Query query = itemsRef.orderByChild("userId").equalTo(currentUserID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                documentItemList.clear();
                List<Items> itemsWithStatusTrue = new ArrayList<>();
                List<Items> itemsWithStatusFalse = new ArrayList<>();

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

                    if (status) {
                        itemsWithStatusTrue.add(item);
                    } else {
                        itemsWithStatusFalse.add(item);
                    }
                }

                documentItemList.addAll(itemsWithStatusFalse);
                documentItemList.addAll(itemsWithStatusTrue);

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
