package com.example.spotifywrappedapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AccountSettingsActivity extends AppCompatActivity {
    private Button usernameBtn;
    private Button passwordBtn;
    private Button deleteBtn;
    private ImageView homeBtn;
    private ImageView card;
    private String mAccessToken;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");
        user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();
        // view on arrival to page
        TextView textView = findViewById(R.id.past_summaries_text);
        // set up card
        card = findViewById(R.id.past_summaries);
        List<String> pastSummaries = getPastSummaries();
        loadRandomSummary(pastSummaries);
        LinearLayout buttons = findViewById(R.id.buttons);
        usernameBtn = findViewById(R.id.change_username_btn);
        passwordBtn = findViewById(R.id.change_password_btn);
        deleteBtn = findViewById(R.id.delete_account_btn);
        homeBtn = findViewById(R.id.home_button);

        // animations
        textView.setVisibility(View.VISIBLE);
        textView.startAnimation(WrappedHelper.animation(this, "fly in"));
        card.setVisibility(View.VISIBLE);
        buttons.setVisibility(View.VISIBLE);
        homeBtn.setVisibility(View.VISIBLE);
        Animation fadeIn = WrappedHelper.animation(this, "fade in");
        card.startAnimation(fadeIn);
        buttons.startAnimation(fadeIn);
        homeBtn.startAnimation(fadeIn);

        // Set the click listeners for the buttons
        homeBtn.setOnClickListener((v) -> {
            returnToMain();
        });

        usernameBtn.setOnClickListener((v) -> {
            onChangeUsernameClicked();
        });

        passwordBtn.setOnClickListener((v) -> {
            onChangePasswordClicked();
        });

        deleteBtn.setOnClickListener((v) -> {
            onDeleteAccount();
        });

        card.setOnClickListener((v) -> {
            loadRandomSummary(pastSummaries);
        });
    }

    private void loadRandomSummary(List<String> pastSummaries) {
        if (pastSummaries.size() != 0) {
            Random rand = new Random();
            Picasso.get().load(pastSummaries.get(rand.nextInt(pastSummaries.size()))).into(card);
        }
    }

    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
        if (this.getIntent().getStringExtra("ACCESS_TOKEN") != null) {
            intent.putExtra("ACCESS_TOKEN", this.getIntent().getStringExtra("ACCESS_TOKEN"));
        }
        startActivity(intent);
    }

    /**
     * Deletes the user's account from Firebase
     */
    public void onDeleteAccount() {
        if (user != null) {
            // Get a reference to the Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference("users").child(user.getUid()).removeValue();

            // Delete the user node
            user.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Data deletion was successful!
                        Toast.makeText(AccountSettingsActivity.this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                        mAccessToken = null;
                        returnToMain();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to delete the data
                        Log.e("Firebase", "Failed to delete account", e);
                        Toast.makeText(AccountSettingsActivity.this, "Failed to delete user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // Handle the case where there is no authenticated user
            Toast.makeText(this, "No authenticated user found. Please log in first.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onChangeUsernameClicked() {
        DialogUtils.showChangeUserDialog(this, new DialogUtils.AuthDialogListener() {
                    @Override
                    public void onPositive(String email, String password, Context context) {
                        changeUsername(email);
                    }

                    @Override
                    public void onNegative(String email, String password, Context context) {
                        Toast.makeText(AccountSettingsActivity.this, "No negative button!!", Toast.LENGTH_SHORT).show();
                    }

                });
    }

    public void onChangePasswordClicked() {
        DialogUtils.showChangePasswordDialog(this, new DialogUtils.AuthDialogListener() {
            @Override
            public void onPositive(String email, String password, Context context) {
                changePassword(password);
            }

            @Override
            public void onNegative(String email, String password, Context context) {
                Toast.makeText(AccountSettingsActivity.this, "No negative button!!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * functionally changing username
     */
    public void changeUsername(String newUsername) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();
            user.verifyBeforeUpdateEmail(newUsername)

                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateProfile(profileUpdates);
                                Toast.makeText(AccountSettingsActivity.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * functionally changing password
     */
    public void changePassword(String newPassword) {
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountSettingsActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AccountSettingsActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public List<String> getPastSummaries() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        List<String> imageUrls = new LinkedList<>();
        userRef.child("images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
                System.out.println("DatabaseError: " + databaseError.getMessage());
            }
        });

        return imageUrls;
    }



}
