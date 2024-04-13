package com.example.spotifywrappedapplication;
import androidx.lifecycle.ViewModelProvider;
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
    private SharedViewModel viewModel;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String CLIENT_ID = "895a2c54c32f4d9f98521f688d964af9";
    public static final String REDIRECT_URI = "spotifywrappedapplication://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

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
            handleSignIn();
        });

        codeBtn.setOnClickListener((v) -> {
            getCode();
        });

        generateSummaryBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });

    }

    /**
     * Performs the appropriate actions for log in user, and register user
     * Log in user: populate the Authenticator auth with user credentials
     * - will retrieve spotify access token saved in users account, check if valid, prompt spotify login if it isn't
     * Register user: populate the Authenticator auth with user credentials
     * - prompt spotify login
     */
    public void handleSignIn() {

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.logEvent("your_custom_event_name", null);

        //showAuthDialog(this);
        FirebaseAuth auth= FirebaseUtils.getInstance().getFirebaseAuth();
        DialogUtils.showSignInDialog(this, new DialogUtils.AuthDialogListener(){
            @Override
            public void onSignIn(String email, String password, Context context) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = auth.getCurrentUser();

                                FirebaseUtils.fetchAccessToken(user, new FirebaseUtils.TokenFetchListener() {
                                    @Override
                                    public void onTokenFetched(String token) {
                                        Log.d("Firebase", "Access token fetched: " + token);
                                        mAccessToken = token;
                                        // Proceed with using the token
                                        handleToken(token);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Log.e("Firebase", error);
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

            @Override
            public void onRegister(String email, String password, Context context) {
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
        });

    }

    /*
    Responsiblity: ensure that the token is valid, if it is not, then request the token again
     */
    private void handleToken(String token) {
        SpotifyApiHelper helper = new SpotifyApiHelper(token);
        helper.getUserProfile(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Request Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    System.out.println("Response from server: " + responseData);
                    if (responseData.contains("The access token expired")){
                        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
                        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
                    }
                    // Additional handling if the token expired etc.
                } else {
                    System.out.println("Response error: " + response.code());
                    final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
                    AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
                }
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
     *
     * Will save the spotify login info to the users database
     *
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
            FirebaseUser user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();
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
