package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.checkerframework.checker.units.qual.K;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class WrappedPartTwoActivity extends AppCompatActivity {
    private String mAccessToken;
    private TextView frontCard;
    private LinearLayout backCard;
    private LinearLayout[] bars;
    private TextView[] genres;
    private TextView[] percents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAccessToken = this.getIntent().getStringExtra("ACCESS_TOKEN");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Set up frontCard and backCard
        frontCard = findViewById(R.id.word_cloud);
        backCard = findViewById(R.id.bar_graph);
        bars = new LinearLayout[]{findViewById(R.id.bar1), findViewById(R.id.bar2), findViewById(R.id.bar3), findViewById(R.id.bar4), findViewById(R.id.bar5)};
        genres = new TextView[]{findViewById(R.id.genre1), findViewById(R.id.genre2), findViewById(R.id.genre3), findViewById(R.id.genre4), findViewById(R.id.genre5)};
        // percents = new TextView[]{findViewById(R.id.percent1), findViewById(R.id.percent2), findViewById(R.id.percent3), findViewById(R.id.percent4), findViewById(R.id.percent5)};
        frontCard.setVisibility(View.VISIBLE);
        frontCard.startAnimation(WrappedHelper.animation(this, "fade in"));
        backCard.setVisibility(View.INVISIBLE);
        populateCards();

        // Set up rest of view on arrival to page
        TextView title = findViewById(R.id.title_text);
        title.setText("Now for your favorite genres.\nYou've been listening to a lot of...");
        Button buttonBack = findViewById(R.id.back_button);
        Button buttonNext = findViewById(R.id.continue_button);
        title.startAnimation(WrappedHelper.animation(this, "fade in"));
        Animation fadeInSlow = WrappedHelper.animation(this, "fade in");
        buttonNext.startAnimation(WrappedHelper.animation(this, "fade in"));
        buttonBack.startAnimation(WrappedHelper.animation(this, "fade in"));


        // Set the click listeners for the buttons & gesture detector
        buttonBack.setOnClickListener((v) -> {
            returnToPartOne();
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

    private void populateCards() {
        // building back bar graph
        SpotifyApiHelper helper = new SpotifyApiHelper(mAccessToken);
        /*(p) -> {
            System.out.println("getTracksFromAllPlaylists - attempting to get hrefs");
            JSONArray playlists = new JSONArray(p);
            List<String> trackHREFS = new LinkedList<>();
            for (int i = 0; i < playlists.length(); i++) {
                JSONObject playlist = playlists.getJSONObject(i);
                System.out.println("Playlist object: " + playlist);
                // JSONObject tracks = playlist.getJSONObject("tracks");
                // String href = tracks.getString("href");
                // trackHREFS.add(href);
            }
            for (String href: trackHREFS) {
                System.out.print(href);
            }
        }
        // get all tracks from all playlists and then get genres from tracks
        helper.getUserPlaylists(new CallBack() {

        });*/
        // building front card of recent genres
        helper.getUserSavedTracks((responseStr) -> {
            System.out.println("WrappedPartTwoActivity.java - made it into getUserSavedTracks");
            JSONObject response = new JSONObject(responseStr);
            JSONArray tracks = response.getJSONArray("items");
            Map<String, Integer> genreFreqs = new HashMap<>();
            // List<String> genresList = new ArrayList<>();
            StringBuilder genreStr = new StringBuilder();
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i).getJSONObject("track");
                JSONArray artists = track.getJSONArray("artists");
                helper.getArtistFromID((artistResponseStr) -> {
                    System.out.println("made it into getArtistFromID");
                    JSONObject artist = new JSONObject(artistResponseStr);
                    JSONArray genres = artist.getJSONArray("genres");
                    for (int j = 0; j < genres.length(); j++) {
                        System.out.print(genres.getString(j));
                        String genre = genres.getString(j);
                        genreStr.append(genre).append(" ");
                        int freq = genreFreqs.getOrDefault(genre, 0);
                        genreFreqs.put(genre, freq + 1);
                    }
                }, artists.getJSONObject(0).getString("id"));
            }
            /*for (String genre: genreFreqs.keySet()) {
                System.out.print(genre + " ");
            }*/
            List<String> genresSorted = StringIntPair.retrieveKeysSorted(genreFreqs);
            // System.out.println("genresSorted " + genresSorted.get(0));
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("genreStr: " + genreStr.toString());
                    frontCard.setText(genreStr.toString());
                    Random rand = new Random();
                    Integer[] colors = new Integer[]{
                            Color.parseColor("#FF1493"), // Neon Pink
                            Color.parseColor("#00BFFF"), // Neon Blue
                            Color.parseColor("#FF00FF"), // Neon Magenta
                            Color.parseColor("#FFA500"), // Neon Orange
                            Color.parseColor("#FF6347"), // Neon Tomato
                            Color.parseColor("#9932CC"), // Neon Purple
                            Color.parseColor("#FF4500"), // Neon Orange Red
                            Color.parseColor("#FFD700"), // Neon Gold
                            Color.parseColor("#7FFF00"), // Neon Chartreuse
                            Color.parseColor("#FFA07A"), // Light Salmon
                            Color.parseColor("#FF8C00"),  // Dark Orange
                            Color.parseColor("#9370DB"), // Medium Purple
                    };
                    for (int i = 0; i < genresSorted.size() && i < 5; i++) {
                        genres[i].setText(genresSorted.get(i));
                        genres[i].setBackgroundColor(colors[rand.nextInt(colors.length)]);
                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) bars[i].getLayoutParams();
                        params1.width = rand.nextInt(600) + 200; // Set width for bar 1
                        bars[i].setLayoutParams(params1);
                    }
                }
            });
        });
            /*System.out.println("GENRE FREQ TABLE: ");
            for (String key: genreFreq.keySet()) {
                System.out.print(key + " " + genreFreq.get(key) + "\t");
            }
            System.out.println();*/


            // TODO:sort genre frequency table keys by their value in descending order
            // List<Map.Entry<String, Integer>> entries = new LinkedList<>(genreFreq.entrySet());

            /*Stream<Map.Entry<String,Integer>> sorted =
                    genreFreq.entrySet().stream()
                            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

            Map.Entry<String, Integer>[] entries = (Map.Entry<String, Integer>[]) sorted.toArray();*/
        /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("WrappedPartTwoActivity.java - runOnUIThread");
                    Random rand = new Random();
                    Integer[] colors = new Integer[]{0x00000, 0xFF1493, 0xFFFF00, 0x00FF00, 0xB452CD, 0xFF00FF, 0xFF0000};
                    for (int i = 0; i < genresList.size() && i < 30; i++) {
                        String genre = genresList.get(i);
                        // create TextView for genre

                        // text style
                        TextView textView = new TextView(WrappedPartTwoActivity.this);
                        textView.setText(genre);
                        textView.setTextSize(rand.nextInt(50));
                        textView.setTextColor(colors[rand.nextInt(colors.length)]);

                        // text position
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int leftMargin = rand.nextInt(frontCard.getWidth());
                        int topMargin = rand.nextInt(frontCard.getHeight());
                        params.leftMargin = leftMargin;
                        params.topMargin = topMargin;

                        System.out.println(textView.toString());
                        frontCard.addView(textView, params);
                    }
                }
            });
        });
        */


    }

    /**
     * Return to the main page
     */
    public void returnToPartOne() {
        Intent intent = new Intent(WrappedPartTwoActivity.this, WrappedPartOneActivity.class);
        intent.putExtra("ACCESS_TOKEN", getIntent().getStringExtra("ACCESS_TOKEN"));
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