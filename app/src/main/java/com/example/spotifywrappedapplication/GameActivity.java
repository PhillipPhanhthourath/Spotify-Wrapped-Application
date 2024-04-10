package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the 'game' as the selected item in the bottom navigation
        bottomNavigationView.setSelectedItemId(R.id.navigation_game);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                if (item.getItemId() == R.id.navigation_user_profile) {
                    // Navigate to UserProfileActivity
                    intent = new Intent(GameActivity.this, UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.navigation_game) {
                    // We're already in GameActivity, no need to do anything
                    return true;
                }
                return false;
            }
        });

    }
}
