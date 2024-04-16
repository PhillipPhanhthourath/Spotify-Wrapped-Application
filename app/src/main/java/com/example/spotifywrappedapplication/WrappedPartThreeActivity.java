package com.example.spotifywrappedapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedPartThreeActivity extends AppCompatActivity {
    private LinearLayout frontCard;
    private ImageView[] frontImages;
    private TextView[] frontNames;
    private LinearLayout backCard;
    private ImageView[] backImages;
    private TextView[] backNames;
    private String mAccessToken;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");

        // Set up frontCard and backCard
        frontCard = findViewById(R.id.top_5_card_a);
        frontImages = new ImageView[]{findViewById(R.id.image1a), findViewById(R.id.image2a), findViewById(R.id.image3a), findViewById(R.id.image4a), findViewById(R.id.image5a)};
        frontNames = new TextView[]{findViewById(R.id.text1a), findViewById(R.id.text2a), findViewById(R.id.text3a), findViewById(R.id.text4a), findViewById(R.id.text5a)};
        frontCard.setVisibility(View.VISIBLE);
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        backCard = findViewById(R.id.top_5_card_b);
        backImages = new ImageView[]{findViewById(R.id.image1b), findViewById(R.id.image2b), findViewById(R.id.image3b), findViewById(R.id.image4b), findViewById(R.id.image5b)};
        backNames = new TextView[]{findViewById(R.id.text1b), findViewById(R.id.text2b), findViewById(R.id.text3b), findViewById(R.id.text4b), findViewById(R.id.text5b)};
        backCard.setVisibility(View.INVISIBLE);
        populateCards();

        // Set up rest of view on arrival to page
        TextView title = findViewById(R.id.title_text);
        title.setText("What you've been waiting for...\nYour top artists and songs!");
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonNext = findViewById(R.id.continue_button);
        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonNext.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToPartTwo();
        });

        buttonNext.setOnClickListener((v) -> {
            continueToGame();
        });

        frontCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });

        backCard.setOnClickListener((v) -> {
            WrappedHelper.flipCard(this, frontCard, backCard);
        });
    }

    protected void populateCards() {
        SpotifyApiHelper helper = new SpotifyApiHelper(mAccessToken);
        helper.getUserTopArtists((responseStr) -> {
            JSONObject response = new JSONObject(responseStr);
            JSONArray artists = response.getJSONArray("items");
            for (int i = 0; i < 5; i++) {
                JSONObject artist = artists.getJSONObject(i);
                int finalI = i;
                helper.getArtistFromID((a) -> {
                    JSONObject fullArtist = new JSONObject(a);
                    JSONArray images = fullArtist.getJSONArray("images");
                    JSONObject icon = images.getJSONObject(0);
                    String url = icon.getString("url");
                    String name = fullArtist.getString("name");

                    // populate card
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get().load(url).resize(10, 0).into(frontImages[finalI]);
                            frontNames[finalI].setText(name);
                            if (name.length() > 12) {
                                frontNames[finalI].setTextSize(15);
                            }
                        }
                    });
                }, artist.getString("id"));
            }
        }, "short_term");

        /*
        helper.getUserTopTracks((responseStr) -> {
            JSONObject response = new JSONObject(responseStr);
            JSONArray tracks = response.getJSONArray("items");
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject artist = tracks.getJSONObject(i);
                helper.getArtistFromID((a) -> {
                    JSONObject fullArtist = new JSONObject(a);
                    JSONArray images = fullArtist.getJSONArray("images");
                    JSONObject icon = images.getJSONObject(0);
                    String url = icon.getString("url");
                    String name = fullArtist.getString("name");

                    // populate card
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < frontImages.length; i++) {
                                Picasso.get().load(url).into(frontImages[i]);
                                frontNames[i].setText(name);
                            }
                        }
                    });
                }, artist.getString("id"));
            }
        }, "short_term");*/
    }

    /**
     * Return to the main page
     */
    protected void returnToPartTwo() {
        Intent intent = new Intent(WrappedPartThreeActivity.this, WrappedPartTwoActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    protected void continueToGame() {
        Intent intent = new Intent(WrappedPartThreeActivity.this, WrappedEndScreenActivity.class);
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