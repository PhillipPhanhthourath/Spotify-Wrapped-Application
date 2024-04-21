package com.example.spotifywrappedapplication;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private SharedViewModel viewModel;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static final String CLIENT_ID = "895a2c54c32f4d9f98521f688d964af9";
    public static final String REDIRECT_URI = "spotifywrappedapplication://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private String mAccessToken;
    private String mAccessCode;
    private FirebaseAuth mAuth;
    private Call mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize text
        TextView title = findViewById(R.id.spotify_wrapped_text_view);
        System.out.println(title.toString());
        TextView pastSummaries = findViewById(R.id.past_summaries);
        System.out.println(pastSummaries.toString());
        pastSummaries.setVisibility(View.VISIBLE);
        pastSummaries.startAnimation(WrappedHelper.animation(this, "fly in bottom"));
        // initialize access token if coming from a previous page
        if (this.getIntent().getStringExtra("ACCESS_TOKEN") != null) {
            mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");
        }

        // Initialize the buttons
        Button loginBtn = (Button) findViewById(R.id.spotify_login_btn);
        Button generateSummaryBtn = (Button) findViewById(R.id.generate_summary_btn);
        Button logoutBtn = (Button) findViewById(R.id.log_out_btn);
        Button accountBtn = (Button) findViewById(R.id.account_btn);
        logoutBtn.setVisibility(View.VISIBLE);
        logoutBtn.startAnimation(WrappedHelper.animation(this, "fly in bottom"));
        accountBtn.setVisibility(View.VISIBLE);
        accountBtn.startAnimation(WrappedHelper.animation(this, "fly in bottom"));

        // Set the click listeners for the buttons

        loginBtn.setOnClickListener((v) -> {
            handleSignIn();
        });

        generateSummaryBtn.setOnClickListener((v) -> {
            onGetUserProfileClicked();
        });

        logoutBtn.setOnClickListener((v) -> {
            onLogOut();
        });

        accountBtn.setOnClickListener((v) -> {
            onAccountClicked();
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

        mAuth = FirebaseUtils.getInstance().getFirebaseAuth();
        DialogUtils.showSignInDialog(this, new DialogUtils.AuthDialogListener() {
            @Override
            public void onPositive(String email, String password, Context context) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = mAuth.getCurrentUser();

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
                                        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
                                        AuthorizationClient.openLoginActivity(MainActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
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
            public void onNegative(String email, String password, Context context) {
                mAuth.createUserWithEmailAndPassword(email, password)
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
    Responsibility: ensure that the token is valid, if it is not, then request the token again
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
                    if (responseData.contains("The access token expired")) {
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
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     * <p>
     * In our case - this block of code will run once a response is received from the spotify API
     * <p>
     * Will save the spotify login info to the users database
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
        Intent intent = new Intent(MainActivity.this, WrappedWelcomeScreenActivity.class);
        intent.putExtra("ACCESS_TOKEN", mAccessToken);
        startActivity(intent);
    }

    /**
     * Logging a user out of their account
     */
    public void onLogOut() {
        if (mAccessToken == null || mAuth == null) {
            Toast.makeText(this, "You need to Login to Spotify first!", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signOut();
        mAccessToken = null;
        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    /**
     * Takes the user to their account settings
     */
    private void onAccountClicked() {
        if (mAccessToken == null || mAuth == null) {
            Toast.makeText(this, "You need to Login to Spotify first!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
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
                .setScopes(new String[]{"user-read-email", "user-top-read", "user-library-read"}) // <--- Change the scope of your requested token here
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

    public void testUtils(SpotifyApiHelper helper){

        System.out.println("MainActivity.java - testUtils");
        /*helper.playlistUtil(playlists -> {
            List<String> playlistnames = new ArrayList<>(playlists.keySet());
            for (String playlist : playlistnames){
                System.out.println("the name of the playlist:");
                System.out.println(playlist);
                System.out.println("the songs in the playlist:");
                System.out.println(playlists.get(playlist).getSongs());
                System.out.println("the top 5 artists in the playlist");
                List<PlaylistSongs.Artist> artists = playlists.get(playlist).top5Artists();
                PlaylistSongs.Artist artist = artists.get(0);
                String artistID = artist.getId();
                helper.getArtistFromID((a) -> {
                    // implement "execute" method
                    JsonNameFinder.printFormattedJson(a);
                }, artistID);
                System.out.println(playlists.get(playlist).top5Artists());
                System.out.println("the top 5 most frequently listened to songs");
                System.out.println(playlists.get(playlist).top5SongsFreq());
            }

        });*/

        helper.getTracksFromAllPlaylists(System.out::println);


    }
}