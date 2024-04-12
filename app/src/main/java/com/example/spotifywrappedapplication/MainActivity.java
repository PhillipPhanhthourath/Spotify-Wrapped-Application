package com.example.spotifywrappedapplication;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String CLIENT_ID = "895a2c54c32f4d9f98521f688d964af9";
    public static final String REDIRECT_URI = "spotifywrappedapplication://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    // Initialize FirebaseAuth
    FirebaseAuth auth;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken, mAccessCode;
    private Call mCall;

    private TextView profileTextView;
    // private TextView tokenTextView, codeTextView, profileTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize the views
        tokenTextView = (TextView) findViewById(R.id.token_text_view);
        codeTextView = (TextView) findViewById(R.id.code_text_view);
        */
        profileTextView = (TextView) findViewById(R.id.response_text_view);

        // Initialize the buttons
        Button loginBtn = (Button) findViewById(R.id.spotify_login_btn);
        Button generateSummaryBtn = (Button) findViewById(R.id.generate_summary_btn);
        Button codeBtn = (Button) findViewById(R.id.code_btn);

        // Set the click listeners for the buttons

        loginBtn.setOnClickListener((v) -> {
            getToken();
        });

        codeBtn.setOnClickListener((v) -> {
            getCode();
        });

        generateSummaryBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });

    }

    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent("your_custom_event_name", null);

        showAuthDialog(this);


    }

    public void showAuthDialog(Context context) {
        // Create a linear layout for the dialog
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an EditText for the email
        final EditText emailInput = new EditText(context);
        emailInput.setHint("Email");
        layout.addView(emailInput);

        // Add an EditText for the password
        final EditText passwordInput = new EditText(context);
        passwordInput.setHint("Password");
        layout.addView(passwordInput);

        auth=FirebaseAuth.getInstance();

        // Create the AlertDialog
        new AlertDialog.Builder(context)
                //12312322
                .setTitle("Sign In / Register")
                .setView(layout)
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = emailInput.getText().toString();
                        String password = passwordInput.getText().toString();
                        signInUser(email, password, auth, context);
                    }
                })
                .setNegativeButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = emailInput.getText().toString();
                        String password = passwordInput.getText().toString();
                        registerUser(email, password, auth, context);
                    }
                })
                .show();
    }

    // home screen (when button pressed)-> dialog (once signed in)-> grab from database

    private void signInUser(String email, String password, FirebaseAuth auth, Context context) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Assuming this is inside a method and 'context' is available
                        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = auth.getCurrentUser();

// Variable to store the fetched access token
                        final String[] fetchedAccessToken = {null};

                        if (user != null) {
                            // Get a reference to the Firebase Realtime Database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref = database.getReference("users").child(user.getUid()).child("accessToken");

                            // Read the access token from the database
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Get the access token from the database and store it in the local variable
                                        fetchedAccessToken[0] = dataSnapshot.getValue(String.class);
                                        Log.d("Firebase", "Access token fetched: " + fetchedAccessToken[0]);
                                        mAccessToken=fetchedAccessToken[0];

                                        // Here you can now use fetchedAccessToken as needed

                                        // See whether access token is valid:


                                    } else {
                                        Log.d("Firebase", "Access token not found in the database.");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("Firebase", "Failed to read access token", databaseError.toException());
                                }
                            });
                        } else {
                            Log.e("Firebase", "No authenticated user found.");
                        }
                    } else {
                        Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // home screen (when button pressed)-> dialog (once registered)-> open external spotify (once HTTP response received)-> add to database

    private void registerUser(String email, String password, FirebaseAuth auth, Context context) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show();
                        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
                        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);

                    } else {
                        // Get and display the error message
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(context, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    @SuppressLint("RestrictedApi")
    public void getCode() {

        // Registration succeeded, get the user info
    }


    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     *
     * In our case - this block of code will run once a response is recieved from the spotify API
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            // setTextAsync("Token: " + mAccessToken, tokenTextView);
            // We retrieve the default database and place the access token in it for this user
            // Assuming auth is already initialized as FirebaseAuth instance
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                System.out.println("user exists");
                // Get a reference to the Firebase Realtime Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference userRef = database.getReference("users").child(user.getUid());

                // Store the access token in the database under this user's node
                userRef.child("accessToken").setValue(mAccessToken)
                        .addOnSuccessListener(aVoid -> {
                            // Data save was successful!
                            Log.d("Firebase", "AccessToken saved successfully.");
                        })
                        .addOnFailureListener(e -> {
                            // Failed to save the data
                            Log.e("Firebase", "Failed to save accessToken", e);
                        });
            } else {
                // Handle the case where there is no authenticated user
                Log.e("Firebase", "No authenticated user found.");
            }

        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            // setTextAsync("Code: " + mAccessCode, codeTextView);
        }
    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void onGetUserProfileClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to Login to Spotify first!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
        intent.putExtra("ACCESS_TOKEN", mAccessToken);
        startActivity(intent);
    }


    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-read-email" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }
}
