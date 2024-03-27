package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class LoggedInActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        // Add any initialization or setup code for your logged-in view
    }
    public void logoutOnClick(View view) {
        // Launch the Spotify logout URL in a browser
        String logoutUrl = "https://accounts.spotify.com";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(logoutUrl));
        startActivity(intent);
        finish();
    }
}
