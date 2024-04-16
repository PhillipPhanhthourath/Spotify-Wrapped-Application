package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.net.Uri;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WrappedPartOneActivity extends AppCompatActivity {
    private GridLayout frontCard;
    private ImageView[] covers;
    private TextView backCard;
    private String mAccessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");

        // Set up frontCard and backCard
        covers = new ImageView[]{findViewById(R.id.cover1), findViewById(R.id.cover2), findViewById(R.id.cover3), findViewById(R.id.cover4),
                findViewById(R.id.cover5), findViewById(R.id.cover6), findViewById(R.id.cover7), findViewById(R.id.cover8),
                findViewById(R.id.cover9), findViewById(R.id.cover10), findViewById(R.id.cover11), findViewById(R.id.cover12),
                findViewById(R.id.cover13), findViewById(R.id.cover14), findViewById(R.id.cover15), findViewById(R.id.cover16)};
        frontCard = findViewById(R.id.grid_image_card);
        backCard = findViewById(R.id.text_card);
        backCard.setVisibility(View.INVISIBLE);
        populateCards();
        frontCard.setVisibility(View.VISIBLE);
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));

        // Set up rest of view on arrival to page
        TextView title = findViewById(R.id.title_text);
        TextView tapPrompt = findViewById(R.id.tap_prompt);
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonNext = findViewById(R.id.continue_button);
        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        Animation fadeInSlow = WrappedHelper.animation(this, "fade in");
        tapPrompt.setVisibility(View.VISIBLE);
        tapPrompt.startAnimation(fadeInSlow);
        buttonNext.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToPartOne();
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
        SpotifyApiHelper apiHelper = new SpotifyApiHelper(mAccessToken);
        apiHelper.playlistUtil((playlists) -> {
            String nameList = "";
            String[] imageUrls = new String[covers.length];
            // getting 16 (or fewer) random playlists
            System.out.println("made it into playlistUtil call");
            List<String> temp = new ArrayList<>(playlists.keySet());
            List<String> playlistNames = new ArrayList<>();
            // if someone has a lot of playlists
            if (temp.size() > covers.length * 10) {
                Random rand = new Random();
                Set<Integer> visited = new HashSet<>();
                while (visited.size() < covers.length) {
                    int index = rand.nextInt(playlistNames.size());
                    playlistNames.add(temp.get(index));
                    visited.add(index);
                }
            } else if (temp.size() > covers.length) {
                Random rand = new Random();
                int index = rand.nextInt(temp.size() - covers.length);
                playlistNames = temp.subList(index, index + covers.length);
            } else {
                playlistNames = temp;
            }
            /*for (String name: playlistNames) {
                System.out.print(name);
            }
            System.out.println();*/
            // creating the name list
            int index = 0;
            int songIndex = 0;
            while (index < imageUrls.length && songIndex < 5) {
                for (String name: playlistNames) {
                    List<PlaylistSongs.Song> topSongs = Objects.requireNonNull(playlists.get(name)).top5SongsFreq();
                    if (songIndex < topSongs.size()) {
                        PlaylistSongs.Song topSong = topSongs.get(songIndex);
                        imageUrls[index++] = topSong.getUrlToImage();
                    }
                    if (index == imageUrls.length) {
                        break;
                    }
                }
                songIndex++;
            }
            /*
            for (String name: playlistNames) {
                nameList += name + "\n";
                List<PlaylistSongs.Song> topSongs = Objects.requireNonNull(playlists.get(name)).top5SongsFreq();
                if (topSongs.size() > 0) {
                    PlaylistSongs.Song topSong = topSongs.get(0);
                    imageUrls[urlIndex++] = topSong.getUrlToImage();
                }
                if (urlIndex == imageUrls.length) {
                    break;
                }
            }*/
            List<String> finalPlaylistNames = playlistNames;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < covers.length; i++) {
                        Picasso.get().load(imageUrls[i]).into(covers[i]);
                    }
                    String nameList = "";
                    for (int i = 0; i < finalPlaylistNames.size() && i < 8; i++) {
                        nameList += finalPlaylistNames.get(i) + "\n";
                    }
                    backCard.setText(nameList);
                }
            });
        });
    }

    /**
     * Return to the main page
     */
    protected void returnToPartOne() {
        Intent intent = new Intent(WrappedPartOneActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Continue onwards in the Wrapped Summary
     */
    protected void continueToGame() {
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