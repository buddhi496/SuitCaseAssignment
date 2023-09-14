package com.buddhiraj.suitcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    EditText emailTextView, passwordTextView;
    LinearLayout googleSignin;
    TextView forgetPassword;
    Button loginButton;
    ProgressBar divider;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in or not
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView registerNowTextView = findViewById(R.id.registerNow);

        // Set a click listener for the "Register Now" TextView
        registerNowTextView.setOnClickListener(view -> {
            // Start the RegistrationActivity when the TextView is clicked
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
        // taking FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // initialising all views through id defined above
        emailTextView = findViewById(R.id.email);
        passwordTextView = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        divider = findViewById(R.id.divider);
        forgetPassword = findViewById(R.id.forgetPassword);
        googleSignin = findViewById(R.id.googleSignin);


        // Set on Click Listener on Registration button
        loginButton.setOnClickListener(v -> loginUserAccount());
        forgetPassword.setOnClickListener(v -> showForgotPasswordDialog());
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("414408632509-mmmpdkqiurlvkcpovbc6rhafjnfi4kmh.apps.googleusercontent.com").requestEmail().build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        googleSignin.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(LoginActivity.this, task -> {
                if (task.isSuccessful()) {
                    // Sign-out successful, now start the Google Sign-In intent
                    Intent intent = googleSignInClient.getSignInIntent();
                    startActivityForResult(intent, 100);
                } else {
                    Toast.makeText(getApplicationContext(), "Google signin failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //check condition
        if(firebaseUser!= null){
            //when user is alread signed in redirect to mainactivity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

    }
        private void loginUserAccount()
        {
            // Take the value of two edit texts in Strings
            String email, password;
            email = emailTextView.getText().toString();
            password = passwordTextView.getText().toString();

            // validations for input email and password
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(),
                                "Please enter email!!",
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(),
                                "Please enter password!!",
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            // signin existing user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                            task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                                    "Login successful!!",
                                                    Toast.LENGTH_LONG)
                                            .show();


                                    // if sign-in is successful
                                    // intent to home activity
                                    Intent intent
                                            = new Intent(LoginActivity.this,
                                            MainActivity.class);
                                    startActivity(intent);
                                } else {

                                    // sign-in failed
                                    Toast.makeText(getApplicationContext(),
                                                    "Email or Password doesn't match!!",
                                                    Toast.LENGTH_LONG)
                                            .show();

                                }
                            });
        }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        View view = LayoutInflater.from(this).inflate(R.layout.forget_password, null);
        builder.setView(view);

        TextInputEditText emailEditText = view.findViewById(R.id.forgotPasswordReset);
        Button resetButton = view.findViewById(R.id.resetPassword);

        AlertDialog dialog = builder.create();

        resetButton.setOnClickListener(v -> {
            String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Enter your email");
                return;
            }

            divider.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        divider.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            // check condition
            if (signInAccountTask.isSuccessful()) {
                // When google sign in is successful, display a success message
                String successMessage = "Google sign in successful!";
                Toast.makeText(getApplicationContext(), successMessage, Toast.LENGTH_SHORT).show();

                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null, initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, task -> {
                            // Check condition
                            if (task.isSuccessful()) {
                                // When task is successful, redirect to profile activity
                                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                // Handle authentication failure here
                                String errorMessage = "Authentication Failed: " + Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}