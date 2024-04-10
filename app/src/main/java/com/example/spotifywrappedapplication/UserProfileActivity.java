package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userInfoTextView = findViewById(R.id.user_info_text_view);

        // Get the user information from the intent
        String userInfo = getIntent().getStringExtra("USER_INFO");
        userInfoTextView.setText(userInfo);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the 'user profile' as the selected item in the bottom navigation
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
}
