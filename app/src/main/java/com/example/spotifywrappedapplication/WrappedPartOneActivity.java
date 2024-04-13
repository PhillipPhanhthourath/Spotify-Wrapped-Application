package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedPartOneActivity extends AppCompatActivity {
    private TextView buttonAnchor;
    private TextView title;
    private Button buttonContinue;
    private Button buttonBack;
    private GridLayout frontCard;
    private ImageView[] covers;
    private TextView backCard;
    private SpotifyApiHelper apiHelper;

    private Animation animation(String anim) {
        anim = anim.toLowerCase().trim();
        if (anim.equals("fly in")) {
            return AnimationUtils.loadAnimation(this, R.anim.fly_in_animation);
        } else if (anim.equals("fly out")) {
            return AnimationUtils.loadAnimation(this, R.anim.fly_out_animation);
        } else if (anim.equals("fade in")) {
            return AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        } else if (anim.equals("fade out")) {
            return AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);
        } else if (anim.equals("flip in")) {
            return AnimationUtils.loadAnimation(this, R.anim.flip_in_animation);
        } else if (anim.equals("flip out")) {
            return AnimationUtils.loadAnimation(this, R.anim.flip_out_animation);
        }

        throw new IllegalArgumentException("animation not available.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrapped_part_one);

        // Set up frontCard and backCard
        frontCard = findViewById(R.id.front_card);
        covers = new ImageView[]{findViewById(R.id.cover1), findViewById(R.id.cover2), findViewById(R.id.cover3), findViewById(R.id.cover4),
                findViewById(R.id.cover5), findViewById(R.id.cover6), findViewById(R.id.cover7), findViewById(R.id.cover8),
                findViewById(R.id.cover9), findViewById(R.id.cover10), findViewById(R.id.cover11), findViewById(R.id.cover12),
                findViewById(R.id.cover13), findViewById(R.id.cover14), findViewById(R.id.cover15), findViewById(R.id.cover16)};
        frontCard.startAnimation(animation("fade in"));
        backCard = findViewById(R.id.back_card);
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
                                    System.out.println(playlist.get("images"));
                                    if (!playlist.get("images").toString().equals("null")) {
                                        JSONArray images = playlist.getJSONArray("images");
                                        urls.add(images.getJSONObject(0).getString("url"));
                                    }
                                    names.add(playlist.getString("name"));
                                }
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
        buttonAnchor = findViewById(R.id.text_anchor);
        title = findViewById(R.id.part_one_text);
        buttonBack = findViewById(R.id.back_button);
        buttonContinue = findViewById(R.id.continue_button);
        buttonContinue.startAnimation(animation("fade in"));
        buttonBack.startAnimation(animation("fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToMain();
        });

        buttonContinue.setOnClickListener((v) -> {
            continueToGame();
        });

        frontCard.setOnClickListener((v) -> {
            flipCard();
        });

        backCard.setOnClickListener((v) -> {
            flipCard();
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
    public void continueToGame() {
        Intent intent = new Intent(WrappedPartOneActivity.this, GameNavActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
        startActivity(intent);
    }

    /**
     * flips the card
     */
    public void flipCard() {
        if (backCard.getVisibility() == View.INVISIBLE) {
            frontCard.startAnimation(animation("fade out"));
            frontCard.setVisibility(View.INVISIBLE);
            backCard.setVisibility(View.VISIBLE);
            backCard.startAnimation(animation("fade in"));
        } else {
            backCard.startAnimation(animation("fade out"));
            backCard.setVisibility(View.INVISIBLE);
            frontCard.setVisibility(View.VISIBLE);
            frontCard.startAnimation(animation("fade in"));
        }
    }

    /**
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            // Check if the tap event occurred in the center of the screen
            if (e.getX() >= (centerX - 150) && e.getX() <= (centerX + 150)
                    && e.getY() >= (centerY - 150) && e.getY() <= (centerY + 150)) {
                // perform animation
                frontCard.startAnimation(animation("fade out"));
                frontCard.setVisibility(View.INVISIBLE);
                backCard.setVisibility(View.VISIBLE);
                backCard.startAnimation(animation("fade in"));
                // get info from API
                // apiHelper.getTops(new PlaylistCallbackHandler());
                // api
                // backCard.setText();
                backCard.setTextColor(Color.BLACK);
                return true;
            }
            Toast.makeText(WrappedPartOneActivity.this, "Tap in the center to see more!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
     **/

    private class PlaylistCallbackHandler implements Callback {
        private List<String> artistList = new ArrayList<>();

        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> Toast.makeText(WrappedPartOneActivity.this, "Failed to Retrieve Artist List from API!", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray playlists = jsonObject.getJSONArray("items");
                    String[] images = new String[covers.length];
                    int imageIndex = 0;
                    for (int i = 0; i < playlists.length() && imageIndex < images.length; i++) {
                        JSONObject currPlaylist = playlists.getJSONObject(i);
                        JSONArray imageObjects = currPlaylist.getJSONArray("images");
                        for (int j = 0; j < imageObjects.length() && imageIndex < images.length; j++) {
                            JSONObject imageObject = imageObjects.getJSONObject(i);
                            String url = imageObject.getString("url");
                            images[imageIndex++] = url;
                        }
                    }
                    runOnUiThread(() -> {
                        buildFrontCard(images);
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                runOnUiThread(() -> Toast.makeText(WrappedPartOneActivity.this, "Failed to Retrieve Artist List because of invalid response!", Toast.LENGTH_SHORT).show());
            }
        }
    }

    public void buildFrontCard(String[] urls) {
        if (urls.length != covers.length) {
            throw new IllegalArgumentException("length mismatch between number of urls and number of covers");
        }
        for (int i = 0; i < covers.length; i++) {
            covers[i].setImageURI(Uri.parse(urls[i]));
        }
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