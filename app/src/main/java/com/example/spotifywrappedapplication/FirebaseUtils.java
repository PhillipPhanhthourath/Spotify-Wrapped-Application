package com.example.spotifywrappedapplication;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {

    private static FirebaseUtils instance;
    private FirebaseAuth firebaseAuth;

    private FirebaseUtils() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseUtils getInstance() {
        if (instance == null) {
            instance = new FirebaseUtils();
        }
        return instance;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public interface TokenFetchListener {
        void onTokenFetched(String token);
        void onError(String error);
    }

    public static void fetchAccessToken(FirebaseUser user, TokenFetchListener listener) {
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("accessToken");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String accessToken = dataSnapshot.getValue(String.class);
                        listener.onTokenFetched(accessToken);
                    } else {
                        listener.onError("Access token not found in the database.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onError("Failed to read access token: " + databaseError.getMessage());
                }
            });
        } else {
            listener.onError("No authenticated user found.");
        }
    }
}
