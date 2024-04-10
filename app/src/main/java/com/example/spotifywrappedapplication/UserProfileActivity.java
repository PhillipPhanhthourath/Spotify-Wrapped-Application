package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class UserProfileActivity extends AppCompatActivity {

    private SpotifyApiHelper apiHelper;
    private TextView textView;
    Button buttonProfile;
    Button buttonPlaylists;
    Button buttonCurrentTrack;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        textView = findViewById(R.id.text_view_result);
        buttonProfile = findViewById(R.id.button_get_profile);
        buttonPlaylists = findViewById(R.id.button_get_playlists);
        buttonCurrentTrack = findViewById(R.id.button_current_track);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Retrieve spotify token
        String mAccessToken = getIntent().getStringExtra("ACCESS_TOKEN");
        apiHelper = new SpotifyApiHelper(mAccessToken);

        buttonProfile.setOnClickListener(v -> apiHelper.getUserProfile(new CallbackHandler()));
        buttonPlaylists.setOnClickListener(v -> apiHelper.getUserPlaylists(new CallbackHandler()));
        buttonCurrentTrack.setOnClickListener(v -> apiHelper.getCurrentlyPlayingTrack(new CallbackHandler()));

        bottomNavigationView.setSelectedItemId(R.id.navigation_user_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.navigation_game) {
                // Navigate to GameActivity
                intent = new Intent(UserProfileActivity.this, GameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.navigation_user_profile) {
                // We're already in UserProfileActivity, no need to do anything
                return true;
            }
            return false;
        });

    }

    private class CallbackHandler implements Callback {
        /*
        In this example, we simply populate the textview object with the text returned from the request
        Need to define different CallbackHandler's depending on what you want to do with the text returned from the request!
         */
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> textView.setText("Error: " + e.getMessage()));
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            runOnUiThread(() -> textView.setText(result));
        }
    }
}

