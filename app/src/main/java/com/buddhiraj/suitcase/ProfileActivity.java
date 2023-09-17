package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Your Profile");
        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());



        TextView changeProfileImageText = findViewById(R.id.changeProfileImage);
        TextView changePasswordText = findViewById(R.id.changePassword);
        TextView changeUsernameText = findViewById(R.id.changeUsername);

        changeProfileImageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Change Profile Image" click
                // Implement the logic to change the profile image here
                Toast.makeText(ProfileActivity.this, "Change Profile Image clicked", Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Change Password" click
                // Implement the logic to change the password here
                Toast.makeText(ProfileActivity.this, "Change Password clicked", Toast.LENGTH_SHORT).show();
            }
        });

        changeUsernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle "Change Username" click
                // Implement the logic to change the username here
                Toast.makeText(ProfileActivity.this, "Change Username clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
