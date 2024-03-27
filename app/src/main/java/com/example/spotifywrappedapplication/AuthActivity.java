package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotifywrappedapplication.LoggedInActivity;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class AuthActivity extends AppCompatActivity {

    // Define your client ID and redirect URI
    private static final String CLIENT_ID = "895a2c54c32f4d9f98521f688d964af9";
    private static final String REDIRECT_URI = "spotifywrappedapplication://callback";

    // Request code for authentication
    private static final int AUTH_REQUEST_CODE = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Start the authentication process
        startAuthentication();
    }

    private void startAuthentication() {
        // Construct the authentication request
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        builder.setShowDialog(true); // Show dialog for log out option
        AuthorizationRequest request = builder.build();

        // Open a web browser for authentication
        AuthorizationClient.openLoginInBrowser(this, request);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    String accessToken = response.getAccessToken();
                    // Start LoggedInActivity
                    Intent loggedInIntent = new Intent(this, LoggedInActivity.class);
                    startActivity(loggedInIntent);
                    // Finish AuthActivity to prevent returning to it with back button
                    finish();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    String errorMessage = response.getError();
                    // Show an error message to the user
                    // Handle the error gracefully without redirecting to MainActivity
                    break;

                // Most likely auth flow was cancelled
                default:
                    break;
            }
        }
    }
}
