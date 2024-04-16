package com.example.spotifywrappedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.checkerframework.checker.units.qual.K;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class WrappedPartTwoActivity extends AppCompatActivity {
    private String mAccessToken;
    private View frontCard;
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
        frontCard = findViewById(R.id.grid_image_card);
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
        title.setText("Now for your favorite genres.\nSurprising or no?");
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
        SpotifyApiHelper helper = new SpotifyApiHelper(mAccessToken);
        helper.getUserSavedTracks((responseStr) -> {
            System.out.println("made it into getUserSavedTracks");
            JSONObject response = new JSONObject(responseStr);
            JSONArray tracks = response.getJSONArray("items");
            System.out.println(responseStr);
            Map<String, Integer> genreFreq = new HashMap<>();
            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i).getJSONObject("track");
                System.out.println(track.toString());
                JSONArray artists = track.getJSONArray("artists");
                helper.getArtistFromID((artistResponseStr) -> {
                    System.out.println("made it into getArtistFromID");
                    System.out.println(artistResponseStr);
                    JSONObject artist = new JSONObject(artistResponseStr);
                    JSONArray genres = artist.getJSONArray("genres");
                    for (int j = 0; j < genres.length(); j++) {
                        String genre = genres.getString(j);
                        int freq = genreFreq.getOrDefault(genre, 0);
                        genreFreq.put(genre, freq + 1);
                    }
                }, artists.getJSONObject(0).getString("id"));
            }

            System.out.println("GENRE FREQ TABLE: ");
            for (String key: genreFreq.keySet()) {
                System.out.print(key + " " + genreFreq.get(key) + "\t");
            }
            System.out.println();
            // TODO:sort genre frequency table keys by their value in descending order
            List<Map.Entry<String, Integer>> entries = new LinkedList<>(genreFreq.entrySet());

            /*Stream<Map.Entry<String,Integer>> sorted =
                    genreFreq.entrySet().stream()
                            .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

            Map.Entry<String, Integer>[] entries = (Map.Entry<String, Integer>[]) sorted.toArray();*/
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Random rand = new Random();

                    for (int i = 0; i < 5 && i < entries.size(); i++) {
                        String name = entries.get(i).getKey();
                        int percent = (int) (((double) entries.get(i).getValue()) / tracks.length() * 100);
                        String percentText = percent + "%";
                        genres[i].setText(name);
                        // percents[i].setText(percentText);
                        // bars[i].setLayoutParams(new LinearLayout.LayoutParams(rand.nextInt(250), 60));
                    }
                }
            });
        });
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