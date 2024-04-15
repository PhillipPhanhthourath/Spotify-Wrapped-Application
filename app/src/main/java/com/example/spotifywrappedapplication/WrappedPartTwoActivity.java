package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

public class WrappedPartTwoActivity extends AppCompatActivity {
    private View frontCard;
    private LinearLayout backCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Set up frontCard and backCard
        frontCard = findViewById(R.id.word_cloud);
        frontCard.setVisibility(View.VISIBLE);
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        backCard = findViewById(R.id.bar_graph);
        backCard.setVisibility(View.INVISIBLE);

        // LAYER 1: FETCH TOKEN
        FirebaseUser user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();
        FirebaseUtils.fetchAccessToken(user, new FirebaseUtils.TokenFetchListener() {
            @Override
            public void onTokenFetched(String token) {
                Log.d("Firebase", "Access token fetched: " + token);
                SpotifyApiHelper helper = new SpotifyApiHelper(token); // Proceed with using the token

                // LAYER 2: FETCH GENRES
            }

            @Override
            public void onError(String error) {
                Log.e("Firebase", error);
            }
        });

        // Set up rest of view on arrival to page
        TextView title = findViewById(R.id.title_text);
        title.setText("Now for your favorite genres.\nSurprising or no?");
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonNext = findViewById(R.id.continue_button);
        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        Animation fadeInSlow = WrappedHelper.animation(this, "fade in");
        buttonNext.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonNext.setOnClickListener((v) -> {
            continueToPartThree();
        });

        frontCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });

        backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });
    }

    /**
     * Return to the main page
     */
    public void returnToMain() {
        Intent intent = new Intent(WrappedPartTwoActivity.this, WrappedPartOneActivity.class);
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToPartThree() {
        Intent intent = new Intent(WrappedPartTwoActivity.this, WrappedPartThreeActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    public void logJsonForDebugging(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String formattedJson = jsonObject.toString(4); // Indentation of 4 spaces for better readability

            Log.d("JSONOutput", formattedJson); // Log the formatted JSON with a tag for easy filtering
        } catch (Exception e) {
            Log.e("JSONError", "Error parsing JSON", e);
        }
    }
}