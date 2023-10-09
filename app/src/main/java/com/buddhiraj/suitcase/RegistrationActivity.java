package com.buddhiraj.suitcase;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {
    com.google.android.material.textfield.TextInputLayout userNameTextInputLayout, userEmailTextInputLayout, userPasswordTextInputLayout, conformPasswordTextInputLayout;
    Button signUpButton;
    ProgressBar progressbar;
    FirebaseAuth mAuth;
    CheckBox termsAndCondition;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        TextView signInNowTextView = findViewById(R.id.signInNow);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        signInNowTextView.setOnClickListener(view -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        mAuth = FirebaseAuth.getInstance();
        userNameTextInputLayout = findViewById(R.id.userName);
        userEmailTextInputLayout = findViewById(R.id.userEmail);
        userPasswordTextInputLayout = findViewById(R.id.userPassword);
        conformPasswordTextInputLayout = findViewById(R.id.conformPassword);
        signUpButton = findViewById(R.id.signUpButton);
        termsAndCondition = findViewById(R.id.termsAndCondition);
        progressbar = findViewById(R.id.progressbar);
        signUpButton.setOnClickListener(v -> registerNewUser());
        termsAndCondition.setOnCheckedChangeListener((buttonView, isChecked) -> signUpButton.setEnabled(isChecked));

        LinearLayout termsLayout = findViewById(R.id.terms);
        termsLayout.setOnClickListener(v -> {
            showTermsAndConditionsDialog();
        });
    }

    private void showTermsAndConditionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.terms_and_conditions);

        TextView closeButton = dialog.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private void registerNewUser() {
        progressbar.setVisibility(View.VISIBLE);
        String email, password;
        email = Objects.requireNonNull(userEmailTextInputLayout.getEditText()).getText().toString();
        password = Objects.requireNonNull(userPasswordTextInputLayout.getEditText()).getText().toString();

        String confirmPassword = Objects.requireNonNull(conformPasswordTextInputLayout.getEditText()).getText().toString();
        String name = Objects.requireNonNull(userNameTextInputLayout.getEditText()).getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter your name.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (!isEmailValid(email)) { //check for valid email
            Toast.makeText(getApplicationContext(),
                            "Invalid email format.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (!isPasswordValid(password)) { // check if password has at least 6 characters
            Toast.makeText(getApplicationContext(),
                            "Password must be at least 6 characters.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Validations for input email and password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter email!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!termsAndCondition.isChecked()) {
            Toast.makeText(getApplicationContext(),
                    "Please accept the terms and services.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                            "Please enter a password!!",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(),
                            "Passwords do not match.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        progressbar.setVisibility(View.GONE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                        String name1 = userNameTextInputLayout.getEditText().getText().toString();
                        User user = new User(name1, email);
                        databaseReference.child("users").child(userId).setValue(user);
                        Toast.makeText(getApplicationContext(),
                                        "Registration successful!",
                                        Toast.LENGTH_LONG)
                                .show();
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(
                                        getApplicationContext(),
                                        "Registration failed!!" + " Please try again later",
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
