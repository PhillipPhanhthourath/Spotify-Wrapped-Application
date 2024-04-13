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

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedPartOneActivity extends AppCompatActivity {
    TextView buttonAnchor;
    TextView title;
    Button buttonContinue;
    Button buttonBack;
    GridLayout frontCard;
    ImageView[] covers;
    TextView backCard;
    private SpotifyApiHelper apiHelper;
    GestureDetector gestureDetector;

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

        // get picture info from api and set up frontCard
        frontCard = findViewById(R.id.front_card);
        covers = new ImageView[]{findViewById(R.id.cover1), findViewById(R.id.cover2), findViewById(R.id.cover3), findViewById(R.id.cover4),
                findViewById(R.id.cover5), findViewById(R.id.cover6), findViewById(R.id.cover7), findViewById(R.id.cover8),
                findViewById(R.id.cover9), findViewById(R.id.cover10), findViewById(R.id.cover11), findViewById(R.id.cover12),
                findViewById(R.id.cover13), findViewById(R.id.cover14), findViewById(R.id.cover15), findViewById(R.id.cover16)};
        frontCard.startAnimation(animation("fade in"));
        String mAccessToken = getIntent().getStringExtra("ACCESS_TOKEN");
        apiHelper = new SpotifyApiHelper(mAccessToken);
        // Get user playlists
        // apiHelper.getImagesFromAllPlaylists(new PlaylistCallbackHandler());



        // rest of view on arrival to page
        backCard = findViewById(R.id.back_card);
        backCard.setVisibility(View.INVISIBLE);
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

        gestureDetector = new GestureDetector(this, new GestureListener());
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
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