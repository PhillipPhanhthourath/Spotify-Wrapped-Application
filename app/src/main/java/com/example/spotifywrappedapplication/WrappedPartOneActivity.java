package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedPartOneActivity extends AppCompatActivity {
    private TextView title, tapPrompt;
    private Button buttonNext;
    private Button buttonBack;
    private GridLayout frontCard;
    private ImageView[] covers;
    private TextView backCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Set up frontCard and backCard
        frontCard = findViewById(R.id.grid_image_card);
        covers = new ImageView[]{findViewById(R.id.cover1), findViewById(R.id.cover2), findViewById(R.id.cover3), findViewById(R.id.cover4),
                findViewById(R.id.cover5), findViewById(R.id.cover6), findViewById(R.id.cover7), findViewById(R.id.cover8),
                findViewById(R.id.cover9), findViewById(R.id.cover10), findViewById(R.id.cover11), findViewById(R.id.cover12),
                findViewById(R.id.cover13), findViewById(R.id.cover14), findViewById(R.id.cover15), findViewById(R.id.cover16)};
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        backCard = findViewById(R.id.text_card);
        backCard.setVisibility(View.INVISIBLE);
        // LAYER 1: FETCH TOKEN
        FirebaseUser user = FirebaseUtils.getInstance().getFirebaseAuth().getCurrentUser();
        FirebaseUtils.fetchAccessToken(user, new FirebaseUtils.TokenFetchListener() {
            @Override
            public void onTokenFetched(String token) {
                Log.d("Firebase", "Access token fetched: " + token);
                SpotifyApiHelper helper = new SpotifyApiHelper(token); // Proceed with using the token

                // LAYER 2: FETCH PLAYLISTS
                helper.getUserPlaylists(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("Spotify API", "Failed to fetch playlists", e);
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                // get playlist images to populate the frontCard
                                JSONArray playlists = jsonObject.getJSONArray("items");
                                ArrayList<String> urls = new ArrayList<>();
                                ArrayList<String> names = new ArrayList<>();
                                for (int i = 0; i < playlists.length() && urls.size() < covers.length; i++) {
                                    JSONObject playlist = playlists.getJSONObject(i);
                                    if (!playlist.get("images").toString().equals("null")) {
                                        JSONArray images = playlist.getJSONArray("images");
                                        urls.add(images.getJSONObject(0).getString("url"));
                                    } else {
                                        /*JSONArray tracks = playlist.getJSONArray("tracks");
                                        JSONObject firstTrack = tracks.getJSONObject(0);
                                        JSONObject album = firstTrack.getJSONObject("album");
                                        urls.add(album.getJSONArray("images").getJSONObject(0).getString("url"));
                                        */
                                        helper.getTracksFromAPlaylist(new Callback() {
                                            @Override
                                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                Log.e("Spotify API", "Failed to fetch playlist", e);
                                            }
                                            @Override
                                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                try {
                                                    assert response.body() != null;
                                                    JSONObject trackObject = new JSONObject(response.body().string());
                                                    JSONArray tracks = trackObject.getJSONArray("items");
                                                    if (tracks.length() > 0) {
                                                        JSONObject firstTrack = tracks.getJSONObject(0).getJSONObject("track");
                                                        JSONObject album = firstTrack.getJSONObject("album");
                                                        urls.add(album.getJSONArray("images").getJSONObject(0).getString("url"));
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }, playlist.getString("id"));

                                    }
                                    names.add(playlist.getString("name"));
                                }
                                Collections.shuffle(urls);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < covers.length && i < urls.size(); i++) {
                                            Picasso.get().load(urls.get(i)).into(covers[i]);
                                        }
                                        String backCardText = "";
                                        for (int i = 0; i < names.size() && i < 10; i++) {
                                            backCardText += names.get(i) + "\n";
                                        }
                                        backCard.setText(backCardText);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("Spotify API", "Response not successful or body is null");
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("Firebase", error);
            }
        });

        // Set up rest of view on arrival to page
        title = findViewById(R.id.title_text);
        tapPrompt = findViewById(R.id.tap_prompt);
        buttonBack = findViewById(R.id.back_button);
        buttonNext = findViewById(R.id.continue_button);
        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        Animation fadeInSlow = WrappedHelper.animation(this, "fade in");
        fadeInSlow.setDuration(5000);
        tapPrompt.startAnimation(fadeInSlow);
        frontCard.startAnimation(fadeInSlow);
        buttonNext.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonNext.setOnClickListener((v) -> {
            continueToPartTwo();
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
        Intent intent = new Intent(WrappedPartOneActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    public void continueToPartTwo() {
        Intent intent = new Intent(WrappedPartOneActivity.this, WrappedPartTwoActivity.class);
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